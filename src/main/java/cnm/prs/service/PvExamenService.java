package cnm.prs.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.PvActionRequest;
import cnm.prs.dto.PvExamenDto;
import cnm.prs.entity.Controleur;
import cnm.prs.entity.Prmp;
import cnm.prs.entity.PvExamen;
import cnm.prs.entity.PvNavette;
import cnm.prs.enums.RoleSignataire;
import cnm.prs.enums.SensNavette;
import cnm.prs.enums.StatutPv;
import cnm.prs.enums.TypeNotification;
import cnm.prs.enums.TypeObjet;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.PvExamenMapper;
import cnm.prs.repository.PrmpRepository;
import cnm.prs.repository.PvExamenRepository;
import cnm.prs.repository.PvNavetteRepository;
import cnm.prs.security.Visibilite;

/**
 * Logique métier pour {@link PvExamen}.
 *
 * <p>Outre le CRUD, ce service porte le <strong>cycle de vie du projet de PV</strong>
 * (circuit de contrôle §2, §3.2, §3.5). Le statut ({@code STATUT_PV}) et les dates de
 * workflow ne sont modifiables que via les transitions dédiées
 * ({@link #soumettre}, {@link #retourner}, {@link #accepter}, {@link #signer}) —
 * jamais par le {@code PUT} générique.</p>
 */
@Service
@Transactional
public class PvExamenService {

    private final PvExamenRepository repository;
    private final PvNavetteRepository navetteRepository;
    private final PrmpRepository prmpRepository;
    private final NotificationService notificationService;
    private final ControleurDirectory controleurDirectory;

    public PvExamenService(PvExamenRepository repository, PvNavetteRepository navetteRepository,
            PrmpRepository prmpRepository, NotificationService notificationService,
            ControleurDirectory controleurDirectory) {
        this.repository = repository;
        this.navetteRepository = navetteRepository;
        this.prmpRepository = prmpRepository;
        this.notificationService = notificationService;
        this.controleurDirectory = controleurDirectory;
    }

    @Transactional(readOnly = true)
    public List<PvExamenDto> findAll() {
        return Visibilite.filtrer(repository::findAll, repository::findVisiblesParLocalite)
                .stream().map(PvExamenMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PvExamenDto findById(Integer id) {
        PvExamen entity = load(id);
        Visibilite.controler(loc -> repository.existsDansLocalite(id, loc));
        return PvExamenMapper.toDto(entity);
    }

    /**
     * Création d'un projet de PV. Un nouveau projet démarre toujours en
     * {@link StatutPv#BROUILLON} (§3.5), sans navette ni signature — quel que soit le
     * statut transmis : impossible de créer un PV directement accepté ou signé.
     */
    public PvExamenDto create(PvExamenDto dto) {
        PvExamen entity = PvExamenMapper.toEntity(dto);
        entity.setStatutPv(StatutPv.BROUILLON.name());
        entity.setNbNavettes(0);
        entity.setDateSoumissionInitiale(null);
        entity.setDateAcceptation(null);
        entity.setDateSignatureMembre(null);
        entity.setDateSignaturePresident(null);
        entity.setDateSignatureCc(null);
        entity.setDatePv(null);
        return PvExamenMapper.toDto(repository.save(entity));
    }

    /**
     * Mise à jour du contenu éditable du projet (avis, synthèse, signataires désignés,
     * référence). Ne touche <strong>pas</strong> au statut, au nombre de navettes ni aux
     * dates de workflow : ces champs sont pilotés exclusivement par les transitions.
     *
     * <p>Le projet n'est modifiable que tant qu'il n'a pas été soumis, c'est-à-dire aux
     * statuts {@link StatutPv#BROUILLON} ou {@link StatutPv#EN_RECTIFICATION} (§3.5).</p>
     */
    public PvExamenDto update(Integer id, PvExamenDto dto) {
        PvExamen existing = load(id);
        requireStatut(existing, StatutPv.BROUILLON, StatutPv.EN_RECTIFICATION);
        existing.setIdExamen(dto.getIdExamen());
        existing.setIdAvis(dto.getIdAvis());
        existing.setImCtrlPresident(dto.getImCtrlPresident());
        existing.setImCtrlCc(dto.getImCtrlCc());
        existing.setImCtrlMembre(dto.getImCtrlMembre());
        existing.setSyntheseObservations(dto.getSyntheseObservations());
        existing.setReferencePv(dto.getReferencePv());
        return PvExamenMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("PvExamen introuvable : " + id);
        }
        repository.deleteById(id);
    }

    // ----------------------------------------------------------------------
    // Transitions du circuit de contrôle (workflow)
    // ----------------------------------------------------------------------

    /**
     * Soumission du projet par le Membre (§3.5) : BROUILLON | EN_RECTIFICATION → PROJET_SOUMIS.
     * Insère une navette SENS = SOUMISSION et incrémente NUM_NAVETTE.
     */
    public PvExamenDto soumettre(Integer id, PvActionRequest req) {
        PvExamen pv = load(id);
        requireStatut(pv, StatutPv.BROUILLON, StatutPv.EN_RECTIFICATION);

        if (pv.getDateSoumissionInitiale() == null) {
            pv.setDateSoumissionInitiale(LocalDate.now());
        }
        pv.setStatutPv(StatutPv.PROJET_SOUMIS.name());
        ajouterNavette(pv, SensNavette.SOUMISSION, req.imActeur(), req.commentaire());
        PvExamen saved = repository.save(pv);
        // [Auto] Le CC et le Président de la localité sont notifiés qu'un projet de PV attend validation.
        notifierPvAValider(saved);
        return PvExamenMapper.toDto(saved);
    }

    /** [Auto] Notifie le CC et le Président de la localité du dossier ({@code PV_A_VALIDER}). */
    private void notifierPvAValider(PvExamen pv) {
        String localite = repository.findLocaliteByPv(pv.getIdPv()).orElse(null);
        Integer idDossier = repository.findIdDossierByPv(pv.getIdPv()).orElse(null);
        String reference = pv.getReferencePv() != null ? pv.getReferencePv() : ("n° " + pv.getIdPv());
        String titre = "Projet de PV à valider";
        String corps = "Le projet de PV " + reference + " a été soumis et attend votre validation.";

        List<Controleur> destinataires = new ArrayList<>(controleurDirectory.presidents());
        if (localite != null) {
            destinataires.addAll(controleurDirectory.chefsCommission(localite));
        }
        for (Controleur c : destinataires) {
            notificationService.emettreControleur(TypeNotification.PV_A_VALIDER, c.getImControleur(),
                    c.getEmailCont(), pv.getIdPv(), TypeObjet.PV, idDossier, titre, corps);
        }
    }

    /**
     * Retour du projet pour correction par le Président / CC (§3.2) :
     * PROJET_SOUMIS → EN_RECTIFICATION. Commentaire de rectification obligatoire.
     * Insère une navette SENS = RETOUR_RECTIF.
     */
    public PvExamenDto retourner(Integer id, PvActionRequest req) {
        PvExamen pv = load(id);
        requireStatut(pv, StatutPv.PROJET_SOUMIS);
        if (req.commentaire() == null || req.commentaire().isBlank()) {
            throw new BusinessRuleException("Le commentaire de rectification est obligatoire (§3.2).");
        }
        pv.setStatutPv(StatutPv.EN_RECTIFICATION.name());
        ajouterNavette(pv, SensNavette.RETOUR_RECTIF, req.imActeur(), req.commentaire());
        return PvExamenMapper.toDto(repository.save(pv));
    }

    /**
     * Acceptation du projet par le Président / CC (§3.2) :
     * PROJET_SOUMIS → PROJET_ACCEPTE (le PV devient signable).
     * Insère une navette SENS = ACCEPTATION.
     */
    public PvExamenDto accepter(Integer id, PvActionRequest req) {
        PvExamen pv = load(id);
        requireStatut(pv, StatutPv.PROJET_SOUMIS);

        pv.setStatutPv(StatutPv.PROJET_ACCEPTE.name());
        pv.setDateAcceptation(LocalDate.now());
        ajouterNavette(pv, SensNavette.ACCEPTATION, req.imActeur(), req.commentaire());
        return PvExamenMapper.toDto(repository.save(pv));
    }

    /**
     * Co-signature du PV accepté (§3.2, §3.3, §3.5). N'est possible qu'au statut
     * PROJET_ACCEPTE. Renseigne la date de signature du rôle indiqué, puis bascule en
     * SIGNE dès que le Membre <em>et</em> (le Président <em>ou</em> le CC) ont signé.
     */
    public PvExamenDto signer(Integer id, PvActionRequest req) {
        PvExamen pv = load(id);
        requireStatut(pv, StatutPv.PROJET_ACCEPTE);

        RoleSignataire role = parseRole(req.role());
        LocalDate today = LocalDate.now();
        switch (role) {
            case MEMBRE -> pv.setDateSignatureMembre(today);
            case PRESIDENT -> pv.setDateSignaturePresident(today);
            case CC -> pv.setDateSignatureCc(today);
        }

        boolean membreSigne = pv.getDateSignatureMembre() != null;
        boolean coSigne = pv.getDateSignaturePresident() != null || pv.getDateSignatureCc() != null;
        if (membreSigne && coSigne) {
            pv.setStatutPv(StatutPv.SIGNE.name());
            pv.setDatePv(today);
            PvExamenDto dto = PvExamenMapper.toDto(repository.save(pv));
            notifierPvSigne(pv);
            return dto;
        }
        return PvExamenMapper.toDto(repository.save(pv));
    }

    /**
     * Notifie la PRMP du dossier dès que le PV atteint le statut SIGNE (§3.1). La PRMP ne
     * reçoit que PV_SIGNE, pas les statuts internes de la navette.
     */
    private void notifierPvSigne(PvExamen pv) {
        String titre = "PV signé";
        String reference = pv.getReferencePv() != null ? pv.getReferencePv() : ("n° " + pv.getIdPv());
        String corps = "Le PV " + reference + " a été signé.";
        for (String idPrmp : repository.findIdPrmpByPv(pv.getIdPv())) {
            String email = prmpRepository.findById(idPrmp).map(Prmp::getEmailPrmp).orElse(null);
            notificationService.emettre(null, TypeNotification.PV_SIGNE, null, email, titre, corps);
        }
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    private PvExamen load(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PvExamen introuvable : " + id));
    }

    /** Vérifie que le PV est dans l'un des statuts attendus, sinon HTTP 409. */
    private void requireStatut(PvExamen pv, StatutPv... attendus) {
        String courant = pv.getStatutPv();
        for (StatutPv s : attendus) {
            if (s.name().equals(courant)) {
                return;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < attendus.length; i++) {
            if (i > 0) {
                sb.append(" ou ");
            }
            sb.append(attendus[i].name());
        }
        throw new BusinessRuleException(
                "Action impossible : le PV est au statut « " + courant + " », attendu « " + sb + " ».");
    }

    private RoleSignataire parseRole(String role) {
        if (role == null || role.isBlank()) {
            throw new BusinessRuleException("Le rôle du signataire (MEMBRE / PRESIDENT / CC) est obligatoire.");
        }
        try {
            return RoleSignataire.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Rôle de signataire invalide : « " + role + " ».");
        }
    }

    /** Insère un mouvement de navette (PK assignée + NUM_NAVETTE incrémenté) et met à jour NB_NAVETTES. */
    private void ajouterNavette(PvExamen pv, SensNavette sens, String imActeur, String commentaire) {
        int numNavette = navetteRepository.findMaxNumNavetteByPv(pv.getIdPv()) + 1;

        PvNavette navette = new PvNavette();
        navette.setIdNavette(navetteRepository.findMaxIdNavette() + 1);
        navette.setIdPv(pv.getIdPv());
        navette.setNumNavette(numNavette);
        navette.setSens(sens.name());
        navette.setImActeur(imActeur);
        navette.setDateAction(LocalDateTime.now());
        navette.setCommentaire(commentaire);
        navetteRepository.save(navette);

        pv.setNbNavettes(numNavette);
    }
}
