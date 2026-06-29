package cnm.prs.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.LettreRenvoiDto;
import cnm.prs.entity.Controleur;
import cnm.prs.entity.Dossier;
import cnm.prs.entity.Examen;
import cnm.prs.entity.LettreRenvoi;
import cnm.prs.entity.LettreRenvoiLue;
import cnm.prs.entity.Ppm;
import cnm.prs.entity.Prmp;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.enums.StatutLettreRenvoi;
import cnm.prs.enums.TypeNotification;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.LettreRenvoiMapper;
import cnm.prs.repository.ControleurRepository;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.ExamenRepository;
import cnm.prs.repository.LettreRenvoiLueRepository;
import cnm.prs.repository.LettreRenvoiRepository;
import cnm.prs.repository.PpmRepository;
import cnm.prs.repository.PrmpRepository;
import cnm.prs.security.CurrentUser;
import cnm.prs.security.Visibilite;

/**
 * Logique métier pour {@link LettreRenvoi} : action séparée pendant l'examen (un examen → N lettres).
 * Circuit {@code BROUILLON → SOUMIS → SIGNE} ; signature par le CC ou le Président uniquement.
 * À la signature : notification de la PRMP du dossier et des Assistants contrôleurs de la localité.
 */
@Service
@Transactional
public class LettreRenvoiService {

    private final LettreRenvoiRepository repository;
    private final ExamenRepository examenRepository;
    private final DossierRepository dossierRepository;
    private final PpmRepository ppmRepository;
    private final PrmpRepository prmpRepository;
    private final ControleurDirectory controleurDirectory;
    private final ControleurRepository controleurRepository;
    private final NotificationService notificationService;
    private final LettreRenvoiLueRepository lueRepository;

    public LettreRenvoiService(LettreRenvoiRepository repository, ExamenRepository examenRepository,
            DossierRepository dossierRepository, PpmRepository ppmRepository, PrmpRepository prmpRepository,
            ControleurDirectory controleurDirectory, ControleurRepository controleurRepository,
            NotificationService notificationService, LettreRenvoiLueRepository lueRepository) {
        this.repository = repository;
        this.examenRepository = examenRepository;
        this.dossierRepository = dossierRepository;
        this.ppmRepository = ppmRepository;
        this.prmpRepository = prmpRepository;
        this.controleurDirectory = controleurDirectory;
        this.controleurRepository = controleurRepository;
        this.notificationService = notificationService;
        this.lueRepository = lueRepository;
    }

    /**
     * Liste filtrée selon le profil : MEMBRE → ses lettres (par ses examens) ; CC → lettres SOUMIS de
     * sa localité ; ASSISTANT_CONTROLEUR → lettres SIGNE de sa localité ; Président/Admin → toutes.
     */
    @Transactional(readOnly = true)
    public List<LettreRenvoiDto> findAll() {
        ProfilUtilisateur profil = CurrentUser.profil().orElse(null);
        String loc = CurrentUser.localite().orElse(null);
        List<LettreRenvoi> lettres;
        if (Visibilite.voitTout()) {
            lettres = repository.findAll();                                  // Président / Administrateur
        } else if (profil == ProfilUtilisateur.MEMBRE) {
            lettres = repository.findByMembre(CurrentUser.ref().orElse(null));
        } else if (profil == ProfilUtilisateur.CHEF_COMMISSION) {
            lettres = repository.findByStatutEtLocalite(StatutLettreRenvoi.SOUMIS.name(), loc);
        } else if (profil == ProfilUtilisateur.ASSISTANT_CONTROLEUR) {
            lettres = repository.findByStatutEtLocalite(StatutLettreRenvoi.SIGNE.name(), loc);
        } else {
            lettres = List.of();
        }
        return lettres.stream().map(LettreRenvoiMapper::toDto).map(this::peuplerNomSignataire).map(this::peuplerLue).toList();
    }

    /** Lettres signées concernant les dossiers de la PRMP connectée (lecture seule). */
    @Transactional(readOnly = true)
    public List<LettreRenvoiDto> mesLettres() {
        String idPrmp = CurrentUser.ref().filter(s -> !s.isBlank()).orElse(null);
        if (idPrmp == null) {
            return List.of();
        }
        return repository.findSigneesPourPrmp(idPrmp).stream()
                .map(LettreRenvoiMapper::toDto).map(this::peuplerNomSignataire).map(this::peuplerLue).toList();
    }

    /**
     * Détail d'une lettre. Accès : périmètre de localité habituel <strong>ou</strong> PRMP propriétaire du
     * dossier pour une lettre {@code SIGNE} (sinon la PRMP serait hors périmètre → 403). À cette occasion,
     * la lettre est marquée « lue » pour la PRMP (trace {@code t_lettre_renvoi_lue}, idempotente, silencieuse).
     */
    public LettreRenvoiDto findById(Integer id) {
        LettreRenvoi entity = exigerExistante(id);
        String ref = CurrentUser.ref().filter(s -> !s.isBlank()).orElse(null);
        boolean prmpProprietaire = CurrentUser.profil().filter(p -> p == ProfilUtilisateur.PRMP).isPresent()
                && ref != null
                && StatutLettreRenvoi.SIGNE.name().equals(entity.getStatut())
                && ppmRepository.existsByIdDossierAndIdPrmp(entity.getIdDossier(), ref);
        if (!prmpProprietaire) {
            // Périmètre de localité habituel (la PRMP non propriétaire reste hors périmètre → 403).
            Visibilite.controler(loc -> repository.existsDansLocalite(id, loc));
        } else if (!lueRepository.existsByIdLettreAndIdPrmp(id, ref)) {
            // Marquage « lu » à la consultation par la PRMP propriétaire (silencieux, anti-doublon).
            lueRepository.save(new LettreRenvoiLue(null, id, ref, LocalDateTime.now()));
        }
        LettreRenvoiDto dto = peuplerNomSignataire(LettreRenvoiMapper.toDto(entity));
        dto.setLue(ref != null && lueRepository.existsByIdLettreAndIdPrmp(id, ref));
        return dto;
    }

    /** Renseigne le flag {@code lue} pour la PRMP courante (trace {@code t_lettre_renvoi_lue}). */
    private LettreRenvoiDto peuplerLue(LettreRenvoiDto dto) {
        String ref = CurrentUser.ref().filter(s -> !s.isBlank()).orElse(null);
        if (dto != null && dto.getIdLettre() != null) {
            dto.setLue(ref != null && lueRepository.existsByIdLettreAndIdPrmp(dto.getIdLettre(), ref));
        }
        return dto;
    }

    /** Renseigne {@code nomSignataire} (« prénoms nom ») depuis {@code tr_controleur} si la lettre est signée. */
    private LettreRenvoiDto peuplerNomSignataire(LettreRenvoiDto dto) {
        if (dto != null && dto.getImSignataire() != null) {
            controleurRepository.findById(dto.getImSignataire()).ifPresent(c -> {
                String n = ((c.getPrenomsCont() == null ? "" : c.getPrenomsCont()) + " "
                        + (c.getNomCont() == null ? "" : c.getNomCont())).trim();
                dto.setNomSignataire(n.isBlank() ? null : n);
            });
        }
        return dto;
    }

    /**
     * Création d'une lettre de renvoi pendant l'examen (Membre), statut BROUILLON. {@code idDossier},
     * {@code dateExamen} et {@code refLettre} (compteur {@code <seq>/LR/<code_localite>/<année>}) sont
     * dérivés de l'examen. Examen inexistant ou hors périmètre → 403.
     */
    public LettreRenvoiDto create(LettreRenvoiDto dto) {
        Integer idExamen = dto.getIdExamen();
        Visibilite.controler(loc -> examenRepository.existsDansLocalite(idExamen, loc));
        Examen examen = examenRepository.findById(idExamen)
                .orElseThrow(() -> new AccessDeniedException("Examen inexistant ou hors de votre périmètre."));
        Integer idDossier = examenRepository.findIdDossierByExamen(idExamen).orElse(null);

        LettreRenvoi lettre = new LettreRenvoi();
        lettre.setIdExamen(idExamen);
        lettre.setIdDossier(idDossier);
        lettre.setObjetLettre(dto.getObjetLettre());
        lettre.setCorpsLettre(dto.getCorpsLettre());
        lettre.setRefLettre(genererRefLettre(idExamen));
        lettre.setDateExamen(examen.getDateExamen());
        lettre.setDateLettre(LocalDate.now());
        lettre.setStatut(StatutLettreRenvoi.BROUILLON.name());
        return LettreRenvoiMapper.toDto(repository.save(lettre));
    }

    /**
     * Référence de la lettre dérivée de {@code refeDossier} (même séquence/format que le dossier et le PV) :
     * insère {@code /LR/} avant l'année ({@code .../YYYY} → {@code .../LR/YYYY}, ex. {@code 00007/PPM/CRM-ANT/LR/2026}).
     * {@code null} si refeDossier absent ou non structuré (comportement identique à {@code refePv}).
     */
    private String genererRefLettre(Integer idExamen) {
        String refe = examenRepository.findRefeDossierByExamen(idExamen)
                .filter(s -> s != null && s.matches(".*/\\d{4}$")).orElse(null);
        return refe == null ? null : refe.replaceFirst("/(\\d{4})$", "/LR/$1");
    }

    /** Édition du brouillon (objet + corps) par le Membre. */
    public LettreRenvoiDto update(Integer id, LettreRenvoiDto dto) {
        LettreRenvoi lettre = exigerExistante(id);
        if (!StatutLettreRenvoi.BROUILLON.name().equals(lettre.getStatut())) {
            throw new BusinessRuleException("Lettre non éditable : statut « " + lettre.getStatut() + " » (attendu BROUILLON).");
        }
        lettre.setObjetLettre(dto.getObjetLettre());
        lettre.setCorpsLettre(dto.getCorpsLettre());
        return LettreRenvoiMapper.toDto(repository.save(lettre));
    }

    /** Soumission par le Membre propriétaire (attributaire de l'examen) : BROUILLON → SOUMIS. */
    public LettreRenvoiDto soumettre(Integer id) {
        LettreRenvoi lettre = exigerExistante(id);
        exigerProprietaire(lettre);
        if (!StatutLettreRenvoi.BROUILLON.name().equals(lettre.getStatut())) {
            throw new BusinessRuleException("Soumission impossible : statut « " + lettre.getStatut() + " » (attendu BROUILLON).");
        }
        lettre.setStatut(StatutLettreRenvoi.SOUMIS.name());
        return LettreRenvoiMapper.toDto(repository.save(lettre));
    }

    /**
     * Signature par le CC ou le Président (rôle contrôlé au contrôleur) : SOUMIS → SIGNE ;
     * {@code imSignataire} = JWT. Notifie la PRMP du dossier ({@code LETTRE_RENVOI_RECUE}) et les
     * Assistants contrôleurs de la localité ({@code LETTRE_RENVOI_COPIE}).
     */
    public LettreRenvoiDto signer(Integer id) {
        LettreRenvoi lettre = exigerExistante(id);
        if (!StatutLettreRenvoi.SOUMIS.name().equals(lettre.getStatut())) {
            throw new BusinessRuleException("Signature impossible : statut « " + lettre.getStatut() + " » (attendu SOUMIS).");
        }
        lettre.setImSignataire(CurrentUser.ref().filter(s -> !s.isBlank())
                .orElseThrow(() -> new AccessDeniedException("Signataire non identifié.")));
        lettre.setStatut(StatutLettreRenvoi.SIGNE.name());
        LettreRenvoi saved = repository.save(lettre);
        notifierSignature(saved);
        return LettreRenvoiMapper.toDto(saved);
    }

    public void delete(Integer id) {
        exigerExistante(id);
        repository.deleteById(id);
    }

    /** Notifie la PRMP du dossier (lettre reçue) et les Assistants contrôleurs de la localité (copie). */
    private void notifierSignature(LettreRenvoi lettre) {
        Dossier dossier = lettre.getIdDossier() == null ? null
                : dossierRepository.findById(lettre.getIdDossier()).orElse(null);
        String ref = lettre.getRefLettre() != null ? lettre.getRefLettre() : ("n° " + lettre.getIdLettre());
        String refDossier = dossier == null || dossier.getRefeDossier() == null
                ? (lettre.getIdDossier() == null ? "?" : "n° " + lettre.getIdDossier()) : dossier.getRefeDossier();
        // PRMP du dossier (via PPM).
        if (lettre.getIdDossier() != null) {
            String titre = "Lettre de renvoi reçue";
            String corps = "La lettre de renvoi " + ref + " concernant le dossier " + refDossier + " a été signée.";
            for (Ppm ppm : ppmRepository.findByIdDossier(lettre.getIdDossier())) {
                if (ppm.getIdPrmp() == null) {
                    continue;
                }
                String email = prmpRepository.findById(ppm.getIdPrmp()).map(Prmp::getEmailPrmp).orElse(null);
                notificationService.emettre(lettre.getIdDossier(), TypeNotification.LETTRE_RENVOI_RECUE,
                        null, email, titre, corps);
            }
        }
        // Assistants contrôleurs de la localité de circuit (réception de l'examen) (copie).
        String localite = examenRepository.findLocaliteByExamen(lettre.getIdExamen()).orElse(null);
        if (localite != null) {
            String titre = "Copie de lettre de renvoi signée";
            String corps = "Lettre de renvoi signée " + ref + " (dossier " + refDossier + ").";
            for (Controleur a : controleurDirectory.assistantsControleurs(localite)) {
                notificationService.emettre(lettre.getIdDossier(), TypeNotification.LETTRE_RENVOI_COPIE,
                        a.getImControleur(), a.getEmailCont(), titre, corps);
            }
        }
    }

    private LettreRenvoi exigerExistante(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lettre de renvoi introuvable : " + id));
    }

    /** Propriété (§2.4) : seul le Membre attributaire de l'examen (Examen.imCtrlMembre) peut soumettre. */
    private void exigerProprietaire(LettreRenvoi lettre) {
        String attributaire = examenRepository.findById(lettre.getIdExamen())
                .map(Examen::getImCtrlMembre).orElse(null);
        String moi = CurrentUser.ref().orElse(null);
        if (attributaire == null || !attributaire.equals(moi)) {
            throw new AccessDeniedException("Lettre réservée au Membre attributaire de l'examen.");
        }
    }
}
