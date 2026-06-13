package cnm.prs.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.DossierDto;
import cnm.prs.dto.MarcheDto;
import cnm.prs.dto.PpmDto;
import cnm.prs.dto.SaisieDossierRequest;
import cnm.prs.dto.SaisieMarcheLigne;
import cnm.prs.dto.SaisiePpmRequest;
import cnm.prs.entity.Dossier;
import cnm.prs.enums.StatutDossier;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.mapper.DossierMapper;
import cnm.prs.repository.DossierRepository;
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
    private final PpmService ppmService;
    private final MarcheService marcheService;
    private final DossierIntegriteService dossierIntegrite;

    public SaisieService(DossierRepository dossierRepository, PpmService ppmService,
            MarcheService marcheService, DossierIntegriteService dossierIntegrite) {
        this.dossierRepository = dossierRepository;
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
                MarcheDto m = new MarcheDto();
                m.setIdDetail(ligne.idDetail());
                m.setIdDossier(req.idDossier());
                m.setIdPpm(req.idPpm());
                m.setDesignationMarche(ligne.designationMarche());
                m.setNumCompte(ligne.numCompte());
                m.setMontEstim(ligne.montEstim());
                m.setFinancement(ligne.financement());
                m.setStatut(ligne.statut());
                m.setIdSituation(ligne.idSituation());
                m.setIdNature(ligne.idNature());
                marcheService.create(m);
            }
        }
        return DossierMapper.toDto(dossierRepository.findById(req.idDossier()).orElseThrow());
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
