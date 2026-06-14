package cnm.prs.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.entity.Dossier;
import cnm.prs.entity.EntiteContract;
import cnm.prs.enums.StatutDossier;
import cnm.prs.exception.BadRequestException;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.EntiteContractRepository;
import cnm.prs.repository.MarcheRepository;
import cnm.prs.repository.PpmRepository;
import cnm.prs.repository.PrmpEntiteRepository;
import cnm.prs.security.CurrentUser;

/**
 * Garde-fous d'intégrité d'un dossier de saisie (§3.1) — appliqués dans un service <strong>partagé</strong>,
 * donc valables sur <em>toutes</em> les voies (façade de saisie ET endpoints granulaires) :
 *
 * <ul>
 *   <li><strong>Propriété</strong> : seule la PRMP propriétaire ({@code t_dossier.ID_PRMP}) édite/soumet
 *       son dossier ;</li>
 *   <li><strong>Éditabilité</strong> : on ne modifie que les dossiers au statut {@code BROUILLON} ;</li>
 *   <li><strong>Cohérence type↔contenu</strong> : un dossier {@code PPM} doit porter un {@code t_ppm} ;
 *       un {@code DAO}/{@code MAOO} ne doit pas en porter.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class DossierIntegriteService {

    private static final String TYPE_PPM = "PPM";

    private final DossierRepository dossierRepository;
    private final PpmRepository ppmRepository;
    private final MarcheRepository marcheRepository;
    private final EntiteContractRepository entiteContractRepository;
    private final PrmpEntiteRepository prmpEntiteRepository;

    public DossierIntegriteService(DossierRepository dossierRepository, PpmRepository ppmRepository,
            MarcheRepository marcheRepository, EntiteContractRepository entiteContractRepository,
            PrmpEntiteRepository prmpEntiteRepository) {
        this.dossierRepository = dossierRepository;
        this.ppmRepository = ppmRepository;
        this.marcheRepository = marcheRepository;
        this.entiteContractRepository = entiteContractRepository;
        this.prmpEntiteRepository = prmpEntiteRepository;
    }

    /**
     * Localité d'un dossier = celle de l'<strong>entité contractante</strong> choisie (§1). Vérifie
     * que l'entité existe, qu'elle fait partie des entités <strong>actives</strong> de la PRMP courante
     * (sinon 403) et qu'elle porte une localité (sinon 400). Source unique de la localité d'un dossier.
     */
    public String localiteDeLEntiteDeLaPrmp(Integer idEntiteContract, String idPrmp) {
        EntiteContract entite = entiteContractRepository.findById(idEntiteContract)
                .orElseThrow(() -> new BadRequestException(
                        "Entité contractante introuvable : " + idEntiteContract + "."));
        if (idPrmp == null
                || !prmpEntiteRepository.existsByIdPrmpAndIdEntiteContractAndActifTrue(idPrmp, idEntiteContract)) {
            throw new AccessDeniedException(
                    "Cette entité ne fait pas partie de vos entités contractantes (§3.1).");
        }
        String localite = entite.getIdLocalite();
        if (localite == null || localite.isBlank()) {
            throw new BadRequestException(
                    "L'entité « " + entite.getLibelleEntite() + " » n'a pas de localité : saisie impossible (§1).");
        }
        return localite;
    }

    /**
     * Charge un dossier modifiable : il existe, appartient à la PRMP courante et est au statut
     * {@code BROUILLON}. Utilisé avant toute édition (en-tête PPM, lignes de marché).
     *
     * @throws ResourceNotFoundException si le dossier n'existe pas
     * @throws AccessDeniedException     si le dossier ne lui appartient pas (→ 403)
     * @throws BusinessRuleException     si le dossier n'est pas un brouillon (→ 409)
     */
    public Dossier exigerBrouillonModifiable(Integer idDossier) {
        Dossier dossier = charger(idDossier);
        exigerProprietaire(dossier);
        if (!StatutDossier.BROUILLON.name().equals(dossier.getStatut())) {
            throw new BusinessRuleException(
                    "Le dossier n'est pas un brouillon (statut « " + dossier.getStatut() + " ») : édition impossible.");
        }
        return dossier;
    }

    /** Exige que le dossier appartienne à la PRMP courante (si une PRMP propriétaire est connue). */
    public void exigerProprietaire(Dossier dossier) {
        String courant = CurrentUser.ref().orElse(null);
        if (dossier.getIdPrmp() != null && !dossier.getIdPrmp().equals(courant)) {
            throw new AccessDeniedException("Ce dossier ne fait pas partie de vos dossiers (§3.1).");
        }
    }

    /** Un PPM (et ses lignes de marché) ne peut être rattaché qu'à un dossier de type {@code PPM}. */
    public void exigerTypePpm(Integer idDossier) {
        Dossier dossier = charger(idDossier);
        if (!TYPE_PPM.equals(dossier.getIdTypeDossier())) {
            throw new BusinessRuleException(
                    "PPM/marché interdit : le dossier " + idDossier + " n'est pas de type PPM (type « "
                            + dossier.getIdTypeDossier() + " »).");
        }
    }

    /**
     * Cohérence type↔contenu vérifiée à la soumission :
     * <ul>
     *   <li>un dossier {@code PPM} doit porter un {@code t_ppm} ;</li>
     *   <li>un dossier {@code PPM} doit comporter <strong>au moins une ligne de marché</strong>
     *       (⚠️ règle ajoutée, non littérale dans {@code regles-gestion.md} — un PPM est un plan de
     *       passation de marchés ; un PPM vide n'a rien à contrôler) ;</li>
     *   <li>un {@code DAO}/{@code MAOO} ne doit pas porter de {@code t_ppm} (et n'est pas concerné par
     *       la règle des marchés).</li>
     * </ul>
     */
    public void validerCoherenceAvantSoumission(Dossier dossier) {
        boolean aPpm = ppmRepository.existsByIdDossier(dossier.getIdDossier());
        boolean estPpm = TYPE_PPM.equals(dossier.getIdTypeDossier());
        if (estPpm && !aPpm) {
            throw new BusinessRuleException(
                    "Dossier de type PPM sans PPM rattaché : soumission impossible (§3.1).");
        }
        if (!estPpm && aPpm) {
            throw new BusinessRuleException(
                    "Dossier de type « " + dossier.getIdTypeDossier() + " » ne doit pas porter de PPM.");
        }
        // ⚠️ Règle ajoutée : un PPM doit comporter au moins un marché avant soumission.
        if (estPpm && !marcheRepository.existsByIdDossier(dossier.getIdDossier())) {
            throw new BusinessRuleException(
                    "Un PPM doit comporter au moins un marché avant soumission.");
        }
    }

    private Dossier charger(Integer idDossier) {
        return dossierRepository.findById(idDossier)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + idDossier));
    }
}
