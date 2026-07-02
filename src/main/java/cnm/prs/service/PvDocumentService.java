package cnm.prs.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.entity.Controleur;
import cnm.prs.entity.Dossier;
import cnm.prs.entity.EntiteContract;
import cnm.prs.entity.Examen;
import cnm.prs.entity.ExamenDetail;
import cnm.prs.entity.Localite;
import cnm.prs.entity.Marche;
import cnm.prs.entity.ObservationControle;
import cnm.prs.entity.Ppm;
import cnm.prs.entity.PvExamen;
import cnm.prs.entity.Reception;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.repository.ControleurRepository;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.EntiteContractRepository;
import cnm.prs.repository.ExamenDetailRepository;
import cnm.prs.repository.ExamenRepository;
import cnm.prs.repository.LocaliteRepository;
import cnm.prs.repository.MarcheRepository;
import cnm.prs.repository.ModePassationRepository;
import cnm.prs.repository.ObservationControleRepository;
import cnm.prs.repository.PpmRepository;
import cnm.prs.repository.ReceptionRepository;

/**
 * Génère et stocke le PDF du Projet de PV <strong>uniquement quand il est éligible</strong> :
 * avis favorable sous réserve ({@code FAVR}), dossier de localité centrale ({@code ANT} — seul modèle
 * disponible) et <strong>toutes</strong> les lignes de marché du PPM en appel d'offres ouvert. Hors de
 * ces conditions, aucun document n'est produit (le PV est créé normalement, sans {@code cheminDocument}).
 */
@Component
@Transactional(readOnly = true)
public class PvDocumentService {

    private static final String AVIS_FAVORABLE_RESERVE = "FAVR";
    private static final String LOCALITE_CENTRALE = "ANT";

    private final PvDocumentGenerator generator;
    private final ExamenRepository examenRepository;
    private final DossierRepository dossierRepository;
    private final PpmRepository ppmRepository;
    private final MarcheRepository marcheRepository;
    private final ModePassationRepository modePassationRepository;
    private final ReceptionRepository receptionRepository;
    private final EntiteContractRepository entiteContractRepository;
    private final LocaliteRepository localiteRepository;
    private final ControleurRepository controleurRepository;
    private final ExamenDetailRepository examenDetailRepository;
    private final ObservationControleRepository observationControleRepository;

    @Value("${storage.pv-examen.path:${java.io.tmpdir}/prs-fsx/PV}")
    private String cheminStockagePv;

    public PvDocumentService(PvDocumentGenerator generator, ExamenRepository examenRepository,
            DossierRepository dossierRepository, PpmRepository ppmRepository, MarcheRepository marcheRepository,
            ModePassationRepository modePassationRepository, ReceptionRepository receptionRepository,
            EntiteContractRepository entiteContractRepository, LocaliteRepository localiteRepository,
            ControleurRepository controleurRepository, ExamenDetailRepository examenDetailRepository,
            ObservationControleRepository observationControleRepository) {
        this.generator = generator;
        this.examenRepository = examenRepository;
        this.dossierRepository = dossierRepository;
        this.ppmRepository = ppmRepository;
        this.marcheRepository = marcheRepository;
        this.modePassationRepository = modePassationRepository;
        this.receptionRepository = receptionRepository;
        this.entiteContractRepository = entiteContractRepository;
        this.localiteRepository = localiteRepository;
        this.controleurRepository = controleurRepository;
        this.examenDetailRepository = examenDetailRepository;
        this.observationControleRepository = observationControleRepository;
    }

    /**
     * Génère et stocke le PDF du Projet de PV si le PV est éligible ; renvoie le chemin du fichier, ou
     * {@link Optional#empty()} si non éligible (avis ≠ FAVR, localité non centrale, ou une ligne de
     * marché hors appel d'offres ouvert).
     */
    public Optional<String> genererSiEligible(PvExamen pv) {
        if (!estEligible(pv)) {
            return Optional.empty();
        }
        Integer idExamen = pv.getIdExamen();
        Integer idDossier = examenRepository.findIdDossierByExamen(idExamen).orElse(null);
        Dossier dossier = dossierRepository.findById(idDossier).orElse(null);
        String localite = examenRepository.findLocaliteByExamen(idExamen).orElse(null);
        Ppm ppm = ppmRepository.findByIdDossier(idDossier).stream().findFirst().orElse(null);
        PvDocumentContexte ctx = construireContexte(pv, dossier, ppm, idExamen, localite);
        byte[] pdf = generator.genererPdf(ctx);
        return Optional.of(stockerSurFsx(pv, pdf));
    }

    /**
     * Prédicat <strong>pur</strong> d'éligibilité à la génération du PDF (sans effet de bord) : avis
     * {@code FAVR} + dossier de localité <strong>centrale ANT</strong> + <strong>toutes</strong> les lignes
     * de marché du PPM en <strong>appel d'offres ouvert</strong>. Sert au flag {@code documentDisponible}.
     */
    @Transactional(readOnly = true)
    public boolean estEligible(PvExamen pv) {
        if (pv == null || !AVIS_FAVORABLE_RESERVE.equals(pv.getIdAvis())) {
            return false;
        }
        Integer idExamen = pv.getIdExamen();
        Integer idDossier = examenRepository.findIdDossierByExamen(idExamen).orElse(null);
        if (idDossier == null || dossierRepository.findById(idDossier).isEmpty()) {
            return false;
        }
        // Localité du circuit (réception), comme les lettres de renvoi ; seul le modèle central (ANT) existe.
        if (!LOCALITE_CENTRALE.equals(examenRepository.findLocaliteByExamen(idExamen).orElse(null))) {
            return false;   // pas de variante régionale inventée
        }
        Ppm ppm = ppmRepository.findByIdDossier(idDossier).stream().findFirst().orElse(null);
        if (ppm == null) {
            return false;
        }
        List<Marche> marches = marcheRepository.findByIdPpm(ppm.getIdPpm());
        return !marches.isEmpty() && marches.stream().allMatch(this::estAppelOffresOuvert);
    }

    /**
     * Vrai si un PDF officiel est <strong>réellement disponible</strong> pour ce PV : fichier déjà stocké
     * ({@code CHEMIN_DOCUMENT} non nul) <strong>ou</strong> PV {@link #estEligible(PvExamen) éligible} (donc
     * régénérable à la demande). Reste juste après une (re)génération.
     */
    @Transactional(readOnly = true)
    public boolean documentDisponible(PvExamen pv) {
        if (pv != null && pv.getCheminDocument() != null && !pv.getCheminDocument().isBlank()) {
            return true;
        }
        return estEligible(pv);
    }

    private boolean estAppelOffresOuvert(Marche m) {
        if (m.getIdMode() == null) {
            return false;
        }
        return modePassationRepository.findById(m.getIdMode())
                .map(mode -> estLibelleAoo(mode.getLibelle())).orElse(false);
    }

    /** Tolérant : accepte le code court « AOO » ou un libellé contenant « appel … ouvert ». */
    private boolean estLibelleAoo(String libelle) {
        if (libelle == null) {
            return false;
        }
        String n = libelle.toUpperCase(java.util.Locale.FRENCH);
        return n.equals("AOO") || (n.contains("APPEL") && n.contains("OUVERT"));
    }

    private PvDocumentContexte construireContexte(PvExamen pv, Dossier dossier, Ppm ppm, Integer idExamen,
            String idLocalite) {
        LocalDate dateExamen = examenRepository.findById(idExamen).map(Examen::getDateExamen).orElse(null);
        String refPv = pv.getRefePv() != null ? pv.getRefePv() : pv.getReferencePv();
        LocalDate dateReception = receptionRepository.findByIdDossier(dossier.getIdDossier()).stream()
                .map(Reception::getDateReception).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).map(LocalDateTime::toLocalDate).orElse(null);
        String entite = dossier.getIdEntiteContract() == null ? "" : entiteContractRepository
                .findById(dossier.getIdEntiteContract()).map(EntiteContract::getLibelleEntite).orElse("");
        String localite = localiteRepository.findById(idLocalite)
                .map(Localite::getLibelleLocalite).orElse(idLocalite);
        return new PvDocumentContexte(dateExamen, refPv, dateReception, entite, ppm.getExercice(), localite,
                nomControleur(pv.getImCtrlPresident()), nomControleur(pv.getImCtrlCc()),
                nomControleur(pv.getImCtrlMembre()), nomControleur(pv.getIdSecretaireSeance()),
                construireObservations(idExamen));
    }

    /** « Prénoms Nom » d'un contrôleur, ou {@code null} si matricule absent (→ ligne « présents » retirée). */
    private String nomControleur(String im) {
        if (im == null || im.isBlank()) {
            return null;
        }
        return controleurRepository.findById(im)
                .map(c -> ((c.getPrenomsCont() == null ? "" : c.getPrenomsCont()) + " "
                        + (c.getNomCont() == null ? "" : c.getNomCont())).trim())
                .filter(s -> !s.isBlank()).orElse(im);
    }

    /** Toutes les observations des points de contrôle non conformes de l'examen, à plat (ordre stable). */
    private List<PvDocumentContexte.Observation> construireObservations(Integer idExamen) {
        List<PvDocumentContexte.Observation> out = new ArrayList<>();
        for (ExamenDetail ed : examenDetailRepository.findByIdExamen(idExamen)) {
            if (Boolean.FALSE.equals(ed.getConforme())) {
                String point = ed.getPtControle() == null ? null : ed.getPtControle().getLibelPointCtrl();
                for (ObservationControle o : observationControleRepository
                        .findByIdDetailOrderByOrdreAsc(ed.getIdDetailExamen())) {
                    out.add(new PvDocumentContexte.Observation(point, o.getAuLieuDe(), o.getLire()));
                }
            }
        }
        return out;
    }

    /** Écrit le PDF dans le répertoire FSX PV/ sous {@code {refePv nettoyée}.pdf} ; renvoie le chemin. */
    private String stockerSurFsx(PvExamen pv, byte[] pdf) {
        String base = pv.getRefePv() != null && !pv.getRefePv().isBlank()
                ? pv.getRefePv() : ("pv-" + pv.getIdPv());
        String nomFichier = base.replace('/', '_').replace('\\', '_') + ".pdf";
        try {
            Path dir = Path.of(cheminStockagePv);
            Files.createDirectories(dir);
            Path fichier = dir.resolve(nomFichier);
            Files.write(fichier, pdf);
            return fichier.toString();
        } catch (IOException e) {
            throw new BusinessRuleException("Stockage du document du PV impossible : " + e.getMessage());
        }
    }
}
