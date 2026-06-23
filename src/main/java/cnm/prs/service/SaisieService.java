package cnm.prs.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.DossierDto;
import cnm.prs.dto.EditionPpmRequest;
import cnm.prs.dto.MarcheDto;
import cnm.prs.dto.MarchePrevisionDto;
import cnm.prs.dto.PpmDto;
import cnm.prs.dto.ProcessusMarche;
import cnm.prs.dto.SaisieDossierRequest;
import cnm.prs.dto.SaisieMarcheLigne;
import cnm.prs.dto.SaisiePpmRequest;
import cnm.prs.entity.Dossier;
import cnm.prs.entity.Marche;
import cnm.prs.entity.Ppm;
import cnm.prs.enums.StatutDossier;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ChampsInvalidesException;
import cnm.prs.exception.ErrorResponse;
import cnm.prs.mapper.DossierMapper;
import cnm.prs.mapper.PpmMapper;
import cnm.prs.repository.CapmRepository;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.EntiteContractRepository;
import cnm.prs.repository.MarchePrevisionRepository;
import cnm.prs.repository.MarcheRepository;
import cnm.prs.repository.PpmRepository;
import cnm.prs.repository.PrmpRepository;
import cnm.prs.security.CurrentUser;

/**
 * Façade de saisie (§3.1, Module 02) : « saisir un PPM/DAO/MAOO » EST créer le dossier à soumettre.
 *
 * <p>En un seul appel transactionnel, crée le {@code t_dossier} (statut <strong>BROUILLON</strong>,
 * propriété de la PRMP courante) et son contenu. Le PPM et les lignes de marché passent par
 * {@link PpmService}/{@link MarcheService}, donc par les garde-fous d'intégrité partagés
 * ({@link DossierIntegriteService}) : aucun chemin ne crée d'incohérence.</p>
 */
@Service
@Transactional
public class SaisieService {

    private static final String TYPE_PPM = "PPM";

    private final DossierRepository dossierRepository;
    private final PpmRepository ppmRepository;
    private final MarcheRepository marcheRepository;
    private final PpmService ppmService;
    private final MarcheService marcheService;
    private final DossierIntegriteService dossierIntegrite;
    private final EntiteContractRepository entiteContractRepository;
    private final PrmpRepository prmpRepository;
    private final ReferenceService referenceService;
    private final MarchePrevisionRepository marchePrevisionRepository;
    private final MarchePrevisionService marchePrevisionService;
    private final CapmRepository capmRepository;

    public SaisieService(DossierRepository dossierRepository, PpmRepository ppmRepository,
            MarcheRepository marcheRepository, PpmService ppmService,
            MarcheService marcheService, DossierIntegriteService dossierIntegrite,
            EntiteContractRepository entiteContractRepository, PrmpRepository prmpRepository,
            ReferenceService referenceService, MarchePrevisionRepository marchePrevisionRepository,
            MarchePrevisionService marchePrevisionService, CapmRepository capmRepository) {
        this.dossierRepository = dossierRepository;
        this.ppmRepository = ppmRepository;
        this.marcheRepository = marcheRepository;
        this.ppmService = ppmService;
        this.marcheService = marcheService;
        this.dossierIntegrite = dossierIntegrite;
        this.entiteContractRepository = entiteContractRepository;
        this.prmpRepository = prmpRepository;
        this.referenceService = referenceService;
        this.marchePrevisionRepository = marchePrevisionRepository;
        this.marchePrevisionService = marchePrevisionService;
        this.capmRepository = capmRepository;
    }

    /** Saisie d'un PPM = dossier (BROUILLON) + PPM + lignes de marché (mode auto), en une transaction. */
    public DossierDto saisirPpm(SaisiePpmRequest req) {
        String idPrmp = prmpCourante();
        // Localité dérivée de l'entité contractante choisie (parmi les entités de la PRMP).
        String localite = dossierIntegrite.localiteDeLEntiteDeLaPrmp(req.idEntiteContract(), idPrmp);
        Dossier dossier = creerDossier(TYPE_PPM, localite, idPrmp, req.idEntiteContract());  // PK séquence
        Integer idDossier = dossier.getIdDossier();

        PpmDto ppm = new PpmDto();
        ppm.setIdDossier(idDossier);
        ppm.setIdPrmp(idPrmp);
        ppm.setExercice(req.exercice());
        // ⚠️ Règle ajoutée — signataire auto (profil PRMP) + référence auto (acronyme entité), non saisis.
        ppm.setSignataire(signataireDeLaPrmp(idPrmp));
        ppm.setDateSignature(req.dateSignature());
        ppm.setReference(referenceService.genererPpm(libelleEntite(req.idEntiteContract()), req.exercice()));
        ppm.setIdLocalite(localite);
        Integer idPpm = ppmService.create(ppm).getIdPpm();          // PK séquence (retournée)

        if (req.marches() != null) {
            int prevSeq = marchePrevisionRepository.findMaxId();   // PK prévision allouée serveur (max+1)
            for (int i = 0; i < req.marches().size(); i++) {
                SaisieMarcheLigne ligne = req.marches().get(i);
                exigerAuMoinsUnProcessus(ligne, i);   // « au moins un processus » (NotEmpty, à la création)
                for (int j = 0; j < ligne.processus().size(); j++) {
                    exigerCapmConnu(ligne.processus().get(j), i, j);   // idCapm existant (avant création marché)
                }
                Integer idDetail = marcheService.create(toMarcheDto(ligne, idDossier, idPpm)).getIdDetail();
                for (ProcessusMarche p : ligne.processus()) {
                    marchePrevisionService.create(new MarchePrevisionDto(
                            ++prevSeq, idDetail, p.idCapm(), p.dateDebut(), p.dateFin(), null));
                }
            }
        }
        return DossierMapper.toDto(dossierRepository.findById(idDossier).orElseThrow());
    }

    /**
     * ⚠️ Règle ajoutée — chaque marché doit comporter au moins un processus à la création (400 sinon).
     * Validé ici (et non par {@code @NotEmpty} sur le DTO) pour ne pas exiger de processus à l'édition,
     * qui partage {@link SaisieMarcheLigne}.
     */
    private void exigerAuMoinsUnProcessus(SaisieMarcheLigne ligne, int i) {
        if (ligne.processus() == null || ligne.processus().isEmpty()) {
            throw new ChampsInvalidesException(List.of(new ErrorResponse.FieldError(
                    "marches[" + i + "].processus", "Au moins un processus est obligatoire.")));
        }
    }

    /** ⚠️ Règle ajoutée — l'{@code idCapm} d'un processus doit exister dans {@code t_capm} (400 sinon). */
    private void exigerCapmConnu(ProcessusMarche p, int i, int j) {
        if (p.idCapm() != null && !capmRepository.existsById(p.idCapm())) {
            throw new ChampsInvalidesException(List.of(new ErrorResponse.FieldError(
                    "marches[" + i + "].processus[" + j + "].idCapm",
                    "Processus inconnu : " + p.idCapm() + ".")));
        }
    }

    /**
     * Édition d'un brouillon PPM (§3.1 M02) : met à jour l'en-tête du PPM et <strong>réconcilie</strong>
     * ses lignes de marché avec la liste fournie (ajout / mise à jour par {@code idDetail} / retrait des
     * absentes), en une transaction. Garde-fous (propriété, statut BROUILLON, type PPM) + mode recalculé.
     */
    public DossierDto editerPpm(Integer idDossier, EditionPpmRequest req) {
        dossierIntegrite.exigerBrouillonModifiable(idDossier);
        dossierIntegrite.exigerTypePpm(idDossier);
        Ppm ppm = ppmRepository.findByIdDossier(idDossier).stream().findFirst()
                .orElseThrow(() -> new BusinessRuleException("Aucun PPM rattaché au dossier " + idDossier + "."));

        // 1) En-tête PPM (on repart de l'existant pour conserver idPrmp/idLocalite/idDossier).
        PpmDto entete = PpmMapper.toDto(ppm);
        entete.setExercice(req.exercice());
        entete.setSignataire(req.signataire());
        entete.setDateSignature(req.dateSignature());
        entete.setReference(req.reference());
        ppmService.update(ppm.getIdPpm(), entete);

        // 2) Réconciliation des lignes par idDetail.
        Set<Integer> existants = new HashSet<>();
        for (Marche m : marcheRepository.findByIdDossier(idDossier)) {
            existants.add(m.getIdDetail());
        }
        Set<Integer> demandes = new HashSet<>();
        if (req.marches() != null) {
            for (SaisieMarcheLigne ligne : req.marches()) {
                demandes.add(ligne.idDetail());
                MarcheDto m = toMarcheDto(ligne, idDossier, ppm.getIdPpm());
                if (existants.contains(ligne.idDetail())) {
                    marcheService.update(ligne.idDetail(), m);
                } else {
                    marcheService.create(m);
                }
            }
        }
        // 3) Retrait des lignes absentes de la demande.
        for (Integer id : existants) {
            if (!demandes.contains(id)) {
                marcheService.delete(id);
            }
        }
        return DossierMapper.toDto(dossierRepository.findById(idDossier).orElseThrow());
    }

    private static MarcheDto toMarcheDto(SaisieMarcheLigne ligne, Integer idDossier, Integer idPpm) {
        MarcheDto m = new MarcheDto();
        m.setIdDetail(ligne.idDetail());
        m.setIdDossier(idDossier);
        m.setIdPpm(idPpm);
        m.setDesignationMarche(ligne.designationMarche());
        m.setNumCompte(ligne.numCompte());
        m.setMontEstim(ligne.montEstim());
        m.setFinancement(ligne.financement());
        m.setStatut(ligne.statut());
        m.setIdSituation(ligne.idSituation());
        m.setIdNature(ligne.idNature());
        m.setIdMode(ligne.idMode());   // mode choisi (validé en service)
        return m;
    }

    /** Saisie d'un dossier sans contenu (DAO/MAOO) = un {@code t_dossier} (type + localité), BROUILLON. */
    public DossierDto saisirDossier(SaisieDossierRequest req) {
        if (TYPE_PPM.equalsIgnoreCase(req.idTypeDossier())) {
            throw new BusinessRuleException("Pour un PPM, utilisez POST /api/saisies/ppm.");
        }
        String idPrmp = prmpCourante();
        String localite = dossierIntegrite.localiteDeLEntiteDeLaPrmp(req.idEntiteContract(), idPrmp);
        Dossier d = creerDossier(req.idTypeDossier(), localite, idPrmp, req.idEntiteContract());
        return DossierMapper.toDto(d);
    }

    private Dossier creerDossier(String type, String idLocalite, String idPrmp,
            Integer idEntiteContract) {
        Dossier d = new Dossier();
        d.setIdDossier(dossierRepository.nextIdDossier().intValue());   // PK serveur (séquence)
        d.setIdTypeDossier(type);
        d.setIdLocalite(idLocalite);
        d.setIdPrmp(idPrmp);
        d.setIdEntiteContract(idEntiteContract);
        d.setStatut(StatutDossier.BROUILLON.name());
        return dossierRepository.save(d);
    }

    private String prmpCourante() {
        return CurrentUser.ref().filter(s -> !s.isBlank())
                .orElseThrow(() -> new AccessDeniedException("Utilisateur PRMP non identifié."));
    }

    /** Libellé de l'entité contractante (source de l'acronyme de la référence PPM). */
    private String libelleEntite(Integer idEntiteContract) {
        return entiteContractRepository.findById(idEntiteContract).map(e -> e.getLibelleEntite()).orElse(null);
    }

    /** Signataire = « prénoms nom » de la PRMP (équivalent de t_prmp.signataire, absent) ; repli sur idPrmp. */
    private String signataireDeLaPrmp(String idPrmp) {
        return prmpRepository.findById(idPrmp).map(p -> {
            String n = ((p.getPrenomsPrmp() == null ? "" : p.getPrenomsPrmp()) + " "
                    + (p.getNomPrmp() == null ? "" : p.getNomPrmp())).trim();
            return n.isBlank() ? idPrmp : n;
        }).orElse(idPrmp);
    }
}
