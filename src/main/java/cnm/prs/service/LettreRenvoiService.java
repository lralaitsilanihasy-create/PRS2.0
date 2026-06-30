package cnm.prs.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

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
import cnm.prs.entity.EntiteContract;
import cnm.prs.repository.EntiteContractRepository;
import cnm.prs.repository.LocaliteRepository;
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
    private final EntiteContractRepository entiteContractRepository;
    private final LocaliteRepository localiteRepository;

    @Value("${storage.lettre-renvoi.path:${java.io.tmpdir}/prs-fsx/LR}")
    private String cheminStockageLr;

    public LettreRenvoiService(LettreRenvoiRepository repository, ExamenRepository examenRepository,
            DossierRepository dossierRepository, PpmRepository ppmRepository, PrmpRepository prmpRepository,
            ControleurDirectory controleurDirectory, ControleurRepository controleurRepository,
            NotificationService notificationService, LettreRenvoiLueRepository lueRepository,
            EntiteContractRepository entiteContractRepository, LocaliteRepository localiteRepository) {
        this.localiteRepository = localiteRepository;
        this.entiteContractRepository = entiteContractRepository;
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
        boolean prmpProprietaire = estPrmpProprietaireSignee(entity);
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

    /** Vrai si l'appelant est la PRMP propriétaire du dossier d'une lettre {@code SIGNE}. */
    private boolean estPrmpProprietaireSignee(LettreRenvoi entity) {
        String ref = CurrentUser.ref().filter(s -> !s.isBlank()).orElse(null);
        return CurrentUser.profil().filter(p -> p == ProfilUtilisateur.PRMP).isPresent()
                && ref != null
                && StatutLettreRenvoi.SIGNE.name().equals(entity.getStatut())
                && ppmRepository.existsByIdDossierAndIdPrmp(entity.getIdDossier(), ref);
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
     * Signature par le CC ou le Président : SOUMIS → SIGNE ; {@code imSignataire} = JWT.
     * <strong>Règle de localité (⚠️ ajoutée)</strong> : localité <strong>centrale</strong> ({@code ANT}) →
     * CC ou Président ; localité <strong>régionale</strong> (autre) → <strong>CC uniquement</strong>
     * (Président → 403). La localité est celle du dossier ({@code idLocalite}), avec repli sur la localité
     * de réception si absente. À la signature, le <strong>PDF</strong> de la lettre est généré (modèle
     * centrale/régionale) et stocké. Notifie la PRMP et les Assistants contrôleurs de la localité.
     */
    public LettreRenvoiDto signer(Integer id) {
        LettreRenvoi lettre = exigerExistante(id);
        if (!StatutLettreRenvoi.SOUMIS.name().equals(lettre.getStatut())) {
            throw new BusinessRuleException("Signature impossible : statut « " + lettre.getStatut() + " » (attendu SOUMIS).");
        }
        Dossier dossier = lettre.getIdDossier() == null ? null
                : dossierRepository.findById(lettre.getIdDossier()).orElse(null);
        String localite = dossier == null ? null : dossier.getIdLocalite();
        if (localite == null || localite.isBlank()) {
            localite = repository.findLocaliteByLettre(id).orElse(null);   // repli : localité de réception
        }
        boolean centrale = "ANT".equals(localite);
        if (!centrale && CurrentUser.profil().orElse(null) != ProfilUtilisateur.CHEF_COMMISSION) {
            throw new AccessDeniedException(
                    "Seul le Chef de Commission peut signer une lettre de renvoi pour une localité régionale.");
        }
        String im = CurrentUser.ref().filter(s -> !s.isBlank())
                .orElseThrow(() -> new AccessDeniedException("Signataire non identifié."));
        String localiteLibelle = localite == null ? "" : localiteRepository.findById(localite)
                .map(l -> l.getLibelleLocalite() == null ? "" : l.getLibelleLocalite()).orElse("");
        lettre.setImSignataire(im);
        lettre.setStatut(StatutLettreRenvoi.SIGNE.name());
        byte[] pdf = genererPdf(lettre, dossier, nomComplet(im), centrale, localiteLibelle);
        lettre.setCheminDocument(stockerSurFsx(lettre, pdf));   // PDF écrit sur le FSX (répertoire LR/)
        LettreRenvoi saved = repository.save(lettre);
        notifierSignature(saved);
        return peuplerNomSignataire(LettreRenvoiMapper.toDto(saved));
    }

    /** Écrit le PDF dans le répertoire FSX LR/ sous {@code {refLettre nettoyée}.pdf} ; renvoie le chemin. */
    private String stockerSurFsx(LettreRenvoi lettre, byte[] pdf) {
        String base = lettre.getRefLettre() != null && !lettre.getRefLettre().isBlank()
                ? lettre.getRefLettre() : ("lettre-" + lettre.getIdLettre());
        String nomFichier = base.replace('/', '_').replace('\\', '_') + ".pdf";
        try {
            Path dir = Path.of(cheminStockageLr);
            Files.createDirectories(dir);
            Path fichier = dir.resolve(nomFichier);
            Files.write(fichier, pdf);
            return fichier.toString();
        } catch (IOException e) {
            throw new BusinessRuleException("Stockage du document de la lettre impossible : " + e.getMessage());
        }
    }

    /**
     * Document PDF de la lettre signée (téléchargement). Accès : périmètre de localité habituel ou PRMP
     * propriétaire (lettre {@code SIGNE}). Lit le fichier sur le FSX ({@code CHEMIN_DOCUMENT}), avec repli
     * sur le contenu en base ({@code DOCUMENT_PDF}). 404 si la lettre n'a pas de document.
     */
    @Transactional(readOnly = true)
    public byte[] telechargerDocument(Integer id) {
        LettreRenvoi lettre = exigerExistante(id);
        if (!estPrmpProprietaireSignee(lettre)) {
            Visibilite.controler(loc -> repository.existsDansLocalite(id, loc));
        }
        if (lettre.getCheminDocument() != null && !lettre.getCheminDocument().isBlank()) {
            try {
                return Files.readAllBytes(Path.of(lettre.getCheminDocument()));
            } catch (IOException e) {
                throw new ResourceNotFoundException("Document introuvable sur le FSX pour la lettre : " + id);
            }
        }
        if (lettre.getDocumentPdf() != null && lettre.getDocumentPdf().length > 0) {
            return lettre.getDocumentPdf();   // repli compatibilité (lettres signées avant le stockage FSX)
        }
        throw new ResourceNotFoundException("Aucun document pour la lettre : " + id);
    }

    /**
     * Génère le PDF de la lettre de renvoi en reproduisant la mise en page du modèle officiel
     * (en-tête républicain, devise, ministère, CNM, type de commission selon la localité, objet/réf,
     * corps, signataire réel). Variante <strong>centrale</strong> (ANT) ou <strong>régionale</strong>.
     * Génération programmatique OpenPDF (les modèles {@code .docx} et une chaîne docx→PDF ne sont pas
     * disponibles dans l'environnement). Tolère les champs absents.
     */
    private byte[] genererPdf(LettreRenvoi lettre, Dossier dossier, String nomSignataire, boolean centrale,
            String localiteLibelle) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
        String dateLettre = lettre.getDateLettre() == null ? "" : lettre.getDateLettre().format(fmt);
        String dateExamen = lettre.getDateExamen() == null ? "" : lettre.getDateExamen().format(fmt);
        String reference = dossier == null || dossier.getRefeDossier() == null ? "" : dossier.getRefeDossier();
        String entite = dossier == null || dossier.getIdEntiteContract() == null ? ""
                : entiteContractRepository.findById(dossier.getIdEntiteContract())
                        .map(EntiteContract::getLibelleEntite).orElse("");
        String corps = lettre.getCorpsLettre() == null ? "" : lettre.getCorpsLettre();
        String nom = nomSignataire == null ? "" : nomSignataire;
        String loc = localiteLibelle == null ? "" : localiteLibelle.toUpperCase(Locale.FRENCH);
        // Type de commission : centrale (ANT) ; régionale → suffixe localité du dossier (placeholder <LOCALITE DOSSIER>).
        String typeCommission = centrale ? "COMMISSION CENTRALE DES MARCHES"
                : ("COMMISSION REGIONALE DES MARCHES " + loc).trim();
        String labelSignataire = centrale ? "Le Président ou le Chef de Commission,"
                : "Le Chef de la Commission Régionale des Marchés";

        Font enteteGras = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
        Font devise = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC);
        Font gras = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setCompressionLevel(0);   // contenu non compressé (vérification des placeholders)
            document.open();
            // En-tête républicain (centré, gras + devise en italique).
            document.add(centre(new Paragraph("REPOBLIKAN'I MADAGASIKARA", enteteGras)));
            document.add(centre(new Paragraph("Fitiavana - Tanindrazana - Fandrosoana", devise)));
            document.add(centre(new Paragraph("MINISTERE DE L'ECONOMIE ET DES FINANCES", gras)));
            document.add(centre(new Paragraph("COMMISSION NATIONALE DES MARCHES (CNM)", gras)));
            document.add(centre(new Paragraph(typeCommission, gras)));
            document.add(new Paragraph(" "));
            document.add(droite(new Paragraph("Antananarivo, le " + dateLettre, normal)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "Madame/Monsieur la Personne Responsable des Marchés Publics de " + entite, normal));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Objet : lettre de renvoi", gras));
            document.add(new Paragraph("Réf : " + reference, normal));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Faisant suite à la séance d'instruction du " + dateExamen
                    + " relative au dossier susvisé, la Commission a l'honneur de vous retourner ledit dossier "
                    + "pour les motifs ci-après :", normal));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(corps, normal));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(droite(new Paragraph(labelSignataire, normal)));
            document.add(droite(new Paragraph(nom, gras)));
            document.close();
            return baos.toByteArray();
        } catch (RuntimeException e) {
            if (document.isOpen()) {
                document.close();
            }
            throw new BusinessRuleException("Génération du document de la lettre impossible : " + e.getMessage());
        }
    }

    private static Paragraph centre(Paragraph p) {
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    private static Paragraph droite(Paragraph p) {
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    /** « Prénoms Nom » d'un contrôleur (signataire effectif), ou l'IM si introuvable. */
    private String nomComplet(String im) {
        if (im == null) {
            return "";
        }
        return controleurRepository.findById(im).map(c -> {
            String n = ((c.getPrenomsCont() == null ? "" : c.getPrenomsCont()) + " "
                    + (c.getNomCont() == null ? "" : c.getNomCont())).trim();
            return n.isBlank() ? im : n;
        }).orElse(im);
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
