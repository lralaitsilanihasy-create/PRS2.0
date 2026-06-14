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
import cnm.prs.dto.PpmDto;
import cnm.prs.dto.SaisieDossierRequest;
import cnm.prs.dto.SaisieMarcheLigne;
import cnm.prs.dto.SaisiePpmRequest;
import cnm.prs.entity.Dossier;
import cnm.prs.entity.Marche;
import cnm.prs.entity.Ppm;
import cnm.prs.enums.StatutDossier;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.mapper.DossierMapper;
import cnm.prs.mapper.PpmMapper;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.MarcheRepository;
import cnm.prs.repository.PpmRepository;
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

    public SaisieService(DossierRepository dossierRepository, PpmRepository ppmRepository,
            MarcheRepository marcheRepository, PpmService ppmService,
            MarcheService marcheService, DossierIntegriteService dossierIntegrite) {
        this.dossierRepository = dossierRepository;
        this.ppmRepository = ppmRepository;
        this.marcheRepository = marcheRepository;
        this.ppmService = ppmService;
        this.marcheService = marcheService;
        this.dossierIntegrite = dossierIntegrite;
    }

    /** Saisie d'un PPM = dossier (BROUILLON) + PPM + lignes de marché (mode auto), en une transaction. */
    public DossierDto saisirPpm(SaisiePpmRequest req) {
        String idPrmp = prmpCourante();
        // Localité dérivée de l'entité contractante choisie (parmi les entités de la PRMP).
        String localite = dossierIntegrite.localiteDeLEntiteDeLaPrmp(req.idEntiteContract(), idPrmp);
        creerDossier(req.idDossier(), TYPE_PPM, localite, idPrmp, req.idEntiteContract());

        PpmDto ppm = new PpmDto();
        ppm.setIdPpm(req.idPpm());
        ppm.setIdDossier(req.idDossier());
        ppm.setIdPrmp(idPrmp);
        ppm.setExercice(req.exercice());
        ppm.setSignataire(req.signataire());
        ppm.setDateSignature(req.dateSignature());
        ppm.setReference(req.reference());
        ppm.setIdLocalite(localite);
        ppmService.create(ppm);

        if (req.marches() != null) {
            for (SaisieMarcheLigne ligne : req.marches()) {
                marcheService.create(toMarcheDto(ligne, req.idDossier(), req.idPpm()));
            }
        }
        return DossierMapper.toDto(dossierRepository.findById(req.idDossier()).orElseThrow());
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
        return m;
    }

    /** Saisie d'un dossier sans contenu (DAO/MAOO) = un {@code t_dossier} (type + localité), BROUILLON. */
    public DossierDto saisirDossier(SaisieDossierRequest req) {
        if (TYPE_PPM.equalsIgnoreCase(req.idTypeDossier())) {
            throw new BusinessRuleException("Pour un PPM, utilisez POST /api/saisies/ppm.");
        }
        String idPrmp = prmpCourante();
        String localite = dossierIntegrite.localiteDeLEntiteDeLaPrmp(req.idEntiteContract(), idPrmp);
        Dossier d = creerDossier(req.idDossier(), req.idTypeDossier(), localite, idPrmp, req.idEntiteContract());
        return DossierMapper.toDto(d);
    }

    private Dossier creerDossier(Integer idDossier, String type, String idLocalite, String idPrmp,
            Integer idEntiteContract) {
        Dossier d = new Dossier();
        d.setIdDossier(idDossier);
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
}
