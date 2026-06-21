package cnm.prs.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.MarcheDto;
import cnm.prs.entity.Dossier;
import cnm.prs.entity.Marche;
import cnm.prs.entity.Ppm;
import cnm.prs.entity.Prmp;
import cnm.prs.entity.ReglePassation;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.enums.TypeNotification;
import cnm.prs.exception.BadRequestException;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.MarcheMapper;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.MarchePrevisionRepository;
import cnm.prs.repository.ModePassationRepository;
import cnm.prs.repository.MarcheRepository;
import cnm.prs.repository.PpmRepository;
import cnm.prs.repository.PrmpRepository;
import cnm.prs.security.CurrentUser;
import cnm.prs.security.Visibilite;

/**
 * Logique métier pour {@link Marche}.
 *
 * <p>Le <strong>mode de passation</strong> n'est jamais fourni par le client : il est
 * <strong>déterminé automatiquement</strong> (§3.1, Module 02) à partir de quatre critères —
 * situation, nature, montant estimé et localité — via {@code t_regle_passation}/{@code t_seuil}.
 * La localité provient de la <strong>PRMP</strong> du PPM du marché
 * ({@code marché → PPM → PRMP.ID_LOCALITE}).</p>
 *
 * <ul>
 *   <li>À la <strong>création</strong> : le mode calculé est toujours imposé (option A).</li>
 *   <li>À la <strong>mise à jour</strong> : recalcul uniquement si la nature, le montant, la
 *       situation ou le PPM change.</li>
 *   <li>Si <strong>aucune règle</strong> ne correspond : le marché est créé avec {@code idMode = null}
 *       et une alerte (notification {@code MODE_NON_DETERMINE} + log) est émise (option B).</li>
 *   <li>Si la <strong>localité de la PRMP est absente</strong> : la création/mise à jour est refusée
 *       (HTTP 400) avec un message clair.</li>
 * </ul>
 */
@Service
@Transactional
public class MarcheService {

    private static final Logger log = LoggerFactory.getLogger(MarcheService.class);

    private final MarcheRepository repository;
    private final PpmRepository ppmRepository;
    private final PrmpRepository prmpRepository;
    private final DossierRepository dossierRepository;
    private final ReglePassationService reglePassationService;
    private final NotificationService notificationService;
    private final DossierIntegriteService dossierIntegrite;
    private final MarchePrevisionRepository marchePrevisionRepository;
    private final ModePassationRepository modePassationRepository;
    private final AuditLogService auditLogService;

    public MarcheService(MarcheRepository repository, PpmRepository ppmRepository,
            PrmpRepository prmpRepository, DossierRepository dossierRepository,
            ReglePassationService reglePassationService,
            NotificationService notificationService, DossierIntegriteService dossierIntegrite,
            MarchePrevisionRepository marchePrevisionRepository,
            ModePassationRepository modePassationRepository,
            AuditLogService auditLogService) {
        this.repository = repository;
        this.ppmRepository = ppmRepository;
        this.prmpRepository = prmpRepository;
        this.dossierRepository = dossierRepository;
        this.reglePassationService = reglePassationService;
        this.notificationService = notificationService;
        this.dossierIntegrite = dossierIntegrite;
        this.marchePrevisionRepository = marchePrevisionRepository;
        this.modePassationRepository = modePassationRepository;
        this.auditLogService = auditLogService;
    }

    /**
     * Liste des marchés <strong>scopée au périmètre de l'appelant</strong> (§1, §3.1) — jamais la
     * table entière : Président/Administrateur voient tout ; la PRMP ne voit que <strong>les
     * siens</strong> (marchés de ses PPM) ; les contrôleurs ne voient que ceux de <strong>leur
     * localité</strong> (dossier non brouillon) ; tout autre profil (ou sans localité) → liste vide.
     */
    @Transactional(readOnly = true)
    public List<MarcheDto> findAll() {
        if (Visibilite.voitTout()) {
            return repository.findAll().stream().map(MarcheMapper::toDto).toList();
        }
        if (Visibilite.estPrmp()) {
            String idPrmp = CurrentUser.ref().filter(s -> !s.isBlank()).orElse(null);
            return idPrmp == null ? List.of()
                    : repository.findVisiblesPourPrmp(idPrmp).stream().map(MarcheMapper::toDto).toList();
        }
        return Visibilite.localite()
                .map(loc -> repository.findVisiblesParLocalite(loc).stream().map(MarcheMapper::toDto).toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public MarcheDto findById(Integer id) {
        Marche entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marche introuvable : " + id));
        controlerVisibilite(entity);
        return MarcheMapper.toDto(entity);
    }

    /** Vérifie que le marché est dans le périmètre de l'appelant (§1, §3.1) — sinon 403. */
    private void controlerVisibilite(Marche marche) {
        if (Visibilite.voitTout()) {
            return;
        }
        if (CurrentUser.profil().orElse(null) == ProfilUtilisateur.PRMP) {
            String idPrmp = CurrentUser.ref().filter(s -> !s.isBlank()).orElse(null);
            if (idPrmp != null && repository.existsVisiblePourPrmp(marche.getIdDetail(), idPrmp)) {
                return;
            }
            throw new AccessDeniedException("Marché hors de votre périmètre (§3.1).");
        }
        boolean ok = Visibilite.localite()
                .map(loc -> repository.existsVisibleParLocalite(marche.getIdDetail(), loc)).orElse(false);
        if (!ok) {
            throw new AccessDeniedException("Marché hors de votre périmètre de visibilité (§1).");
        }
    }

    public MarcheDto create(MarcheDto dto) {
        // Une ligne de marché s'ajoute uniquement à un dossier PPM, en brouillon, propriété de la PRMP courante.
        dossierIntegrite.exigerBrouillonModifiable(dto.getIdDossier());
        dossierIntegrite.exigerTypePpm(dto.getIdDossier());
        Marche entity = MarcheMapper.toEntity(dto);
        entity.setIdDetail(repository.nextIdMarche().intValue());   // ⚠️ PK serveur (séquence) ; id client ignoré
        // ⚠️ Règle ajoutée — mode CHOISI par la PRMP, validé contre l'ensemble autorisé (sinon recommandé).
        validerOuAppliquerMode(entity);
        return MarcheMapper.toDto(repository.save(entity));
    }

    public MarcheDto update(Integer id, MarcheDto dto) {
        Marche existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marche introuvable : " + id));
        dossierIntegrite.exigerBrouillonModifiable(existing.getIdDossier());

        existing.setIdDossier(dto.getIdDossier());
        existing.setIdPpm(dto.getIdPpm());
        existing.setDesignationMarche(dto.getDesignationMarche());
        existing.setNumCompte(dto.getNumCompte());
        existing.setMontEstim(dto.getMontEstim());
        existing.setAncienMontEstim(dto.getAncienMontEstim());
        existing.setNouvMontEstim(dto.getNouvMontEstim());
        existing.setFinancement(dto.getFinancement());
        existing.setStatut(dto.getStatut());
        existing.setIdSituation(dto.getIdSituation());
        existing.setIdNature(dto.getIdNature());
        existing.setIdMode(dto.getIdMode());   // mode choisi (validé ci-dessous ; recommandé si null)
        // ⚠️ Règle ajoutée — valide le mode choisi contre l'ensemble autorisé, ou applique le recommandé.
        validerOuAppliquerMode(existing);
        return MarcheMapper.toDto(repository.save(existing));
    }

    /**
     * ⚠️ Règle ajoutée — édition restreinte (rectification) d'une ligne de marché dont le dossier est
     * {@code EN_ATTENTE_DECISION_PRMP}. La PRMP propriétaire corrige le contenu sans repasser par le
     * brouillon ; le <strong>statut du dossier reste inchangé</strong>. Identité figée (idDossier, idPpm) ;
     * mode revalidé. Tracé : MODIFICATION_RECTIFICATION / t_marche.
     */
    public MarcheDto modifierEnAttenteRectification(Integer id, MarcheDto dto) {
        Marche existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marche introuvable : " + id));
        dossierIntegrite.exigerEnAttenteDecisionPrmpModifiable(existing.getIdDossier());
        // Identité figée : idDossier, idPpm conservés ; seul le contenu est modifié.
        existing.setDesignationMarche(dto.getDesignationMarche());
        existing.setNumCompte(dto.getNumCompte());
        existing.setMontEstim(dto.getMontEstim());
        existing.setAncienMontEstim(dto.getAncienMontEstim());
        existing.setNouvMontEstim(dto.getNouvMontEstim());
        existing.setFinancement(dto.getFinancement());
        existing.setStatut(dto.getStatut());
        existing.setIdSituation(dto.getIdSituation());
        existing.setIdNature(dto.getIdNature());
        existing.setIdMode(dto.getIdMode());
        validerOuAppliquerMode(existing);   // mode revalidé contre l'ensemble autorisé (recommandé si null)
        Marche saved = repository.save(existing);
        auditLogService.enregistrer(CurrentUser.ref().orElse(null), "t_marche",
                String.valueOf(id), "MODIFICATION_RECTIFICATION", null);
        return MarcheMapper.toDto(saved);
    }

    public void delete(Integer id) {
        Marche existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marche introuvable : " + id));
        // Une ligne ne se retire que d'un dossier en brouillon, propriété de la PRMP courante.
        dossierIntegrite.exigerBrouillonModifiable(existing.getIdDossier());
        // Cascade applicative : supprimer d'abord les dates prévisionnelles de CE marché (sous-lignes intrinsèques).
        marchePrevisionRepository.deleteByIdDetail(id);
        repository.deleteById(id);
    }

    /**
     * ⚠️ Règle ajoutée — la PRMP <strong>choisit</strong> le mode parmi l'ensemble autorisé ; le serveur
     * <strong>valide</strong> :
     * <ul>
     *   <li>mode fourni &amp; dans l'ensemble autorisé → conservé ;</li>
     *   <li>mode fourni, ensemble non vide &amp; mode absent → <strong>409</strong> ;</li>
     *   <li>mode fourni &amp; aucune règle → accepté s'il existe dans {@code tr_mode} (saisie manuelle), sinon 409 ;</li>
     *   <li>aucun mode → mode recommandé (1ʳᵉ règle) ; si aucune règle → {@code null} + alerte MODE_NON_DETERMINE.</li>
     * </ul>
     *
     * @throws BadRequestException   si la localité du dossier ne peut être résolue (refus, §point 3)
     * @throws BusinessRuleException si le mode choisi n'est pas autorisé / inconnu
     */
    private void validerOuAppliquerMode(Marche marche) {
        String localite = resoudreLocaliteDossier(marche);
        List<Integer> autorises = reglePassationService
                .determinerRegles(marche.getIdSituation(), marche.getMontEstim(), marche.getIdNature(), localite)
                .stream().map(ReglePassation::getIdMode).distinct().toList();
        Integer choisi = marche.getIdMode();

        if (choisi != null) {
            if (autorises.contains(choisi)) {
                return;                                       // choix valide
            }
            if (autorises.isEmpty()) {                        // aucune règle → saisie manuelle (D-d)
                if (!modePassationRepository.existsById(choisi)) {
                    throw new BusinessRuleException("Mode de passation inconnu : " + choisi + ".");
                }
                return;
            }
            throw new BusinessRuleException(
                    "Mode de passation non autorisé pour ces paramètres ; choisir parmi " + autorises + ".");
        }
        // Aucun mode choisi → recommandé, ou null + alerte si aucune règle.
        if (!autorises.isEmpty()) {
            marche.setIdMode(autorises.get(0));
        } else {
            marche.setIdMode(null);
            alerterModeNonDetermine(marche, localite);
        }
    }

    /** Résout la localité du marché = celle de son <strong>dossier</strong> (dérivée de l'entité). */
    private String resoudreLocaliteDossier(Marche marche) {
        Dossier dossier = dossierRepository.findById(marche.getIdDossier())
                .orElseThrow(() -> new BadRequestException(
                        "Dossier introuvable (id " + marche.getIdDossier() + ") — mode de passation indéterminable."));
        String localite = dossier.getIdLocalite();
        if (localite == null || localite.isBlank()) {
            throw new BadRequestException(
                    "Le dossier " + marche.getIdDossier() + " n'a pas de localité — mode de passation non déterminable.");
        }
        return localite;
    }

    /** Émet l'alerte (log + notification à la PRMP) quand aucune règle ne donne de mode. */
    private void alerterModeNonDetermine(Marche marche, String localite) {
        log.warn("Mode de passation non déterminé pour le marché {} (PPM {}, nature {}, situation {}, "
                + "montant {}, localité {}) : aucune règle correspondante.",
                marche.getIdDetail(), marche.getIdPpm(), marche.getIdNature(),
                marche.getIdSituation(), marche.getMontEstim(), localite);

        String email = ppmRepository.findById(marche.getIdPpm())
                .map(Ppm::getIdPrmp).flatMap(prmpRepository::findById)
                .map(Prmp::getEmailPrmp).orElse(null);
        notificationService.emettre(marche.getIdDossier(), TypeNotification.MODE_NON_DETERMINE,
                null, email,
                "Mode de passation non déterminé",
                "Aucune règle de passation ne correspond au marché « " + marche.getDesignationMarche()
                        + " » (nature " + marche.getIdNature() + ", situation " + marche.getIdSituation()
                        + ", montant " + marche.getMontEstim() + ", localité " + localite
                        + "). Le mode doit être renseigné manuellement.");
    }

}
