package cnm.prs.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.MarcheDto;
import cnm.prs.entity.Marche;
import cnm.prs.entity.Ppm;
import cnm.prs.entity.Prmp;
import cnm.prs.entity.ReglePassation;
import cnm.prs.enums.TypeNotification;
import cnm.prs.exception.BadRequestException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.MarcheMapper;
import cnm.prs.repository.MarcheRepository;
import cnm.prs.repository.PpmRepository;
import cnm.prs.repository.PrmpRepository;

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
    private final ReglePassationService reglePassationService;
    private final NotificationService notificationService;

    public MarcheService(MarcheRepository repository, PpmRepository ppmRepository,
            PrmpRepository prmpRepository, ReglePassationService reglePassationService,
            NotificationService notificationService) {
        this.repository = repository;
        this.ppmRepository = ppmRepository;
        this.prmpRepository = prmpRepository;
        this.reglePassationService = reglePassationService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<MarcheDto> findAll() {
        return repository.findAll().stream().map(MarcheMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public MarcheDto findById(Integer id) {
        Marche entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marche introuvable : " + id));
        return MarcheMapper.toDto(entity);
    }

    public MarcheDto create(MarcheDto dto) {
        Marche entity = MarcheMapper.toEntity(dto);
        // Le mode est toujours imposé par le calcul automatique (le client ne le choisit pas).
        appliquerModeAutomatique(entity);
        return MarcheMapper.toDto(repository.save(entity));
    }

    public MarcheDto update(Integer id, MarcheDto dto) {
        Marche existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marche introuvable : " + id));

        boolean recalcul = !Objects.equals(existing.getIdNature(), dto.getIdNature())
                || !Objects.equals(existing.getIdSituation(), dto.getIdSituation())
                || !Objects.equals(existing.getIdPpm(), dto.getIdPpm())
                || montantChange(existing.getMontEstim(), dto.getMontEstim());

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
        // idMode n'est pas pris du client : conservé tel quel, ou recalculé si un critère a changé.
        if (recalcul) {
            appliquerModeAutomatique(existing);
        }
        return MarcheMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Marche introuvable : " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Détermine et applique le mode de passation du marché à partir des quatre critères
     * (situation, nature, montant, localité). La localité provient de la PRMP du PPM.
     *
     * @throws BadRequestException si la localité de la PRMP ne peut être résolue (refus, §point 3)
     */
    private void appliquerModeAutomatique(Marche marche) {
        String localite = resoudreLocalitePrmp(marche);
        Optional<Integer> mode = reglePassationService
                .determinerRegle(marche.getIdSituation(), marche.getMontEstim(),
                        marche.getIdNature(), localite)
                .map(ReglePassation::getIdMode);

        if (mode.isPresent()) {
            marche.setIdMode(mode.get());
        } else {
            // Aucune règle correspondante : mode null + alerte (option B).
            marche.setIdMode(null);
            alerterModeNonDetermine(marche, localite);
        }
    }

    /** Résout la localité via {@code marché → PPM → PRMP.ID_LOCALITE} ; refuse si introuvable. */
    private String resoudreLocalitePrmp(Marche marche) {
        Ppm ppm = ppmRepository.findById(marche.getIdPpm())
                .orElseThrow(() -> new BadRequestException(
                        "PPM introuvable (id " + marche.getIdPpm() + ") — mode de passation indéterminable."));
        if (ppm.getIdPrmp() == null) {
            throw new BadRequestException(
                    "Le PPM " + ppm.getIdPpm() + " n'a pas de PRMP — localité indéterminable, mode non déterminable.");
        }
        Prmp prmp = prmpRepository.findById(ppm.getIdPrmp())
                .orElseThrow(() -> new BadRequestException(
                        "PRMP " + ppm.getIdPrmp() + " introuvable — localité indéterminable, mode non déterminable."));
        String localite = prmp.getIdLocalite();
        if (localite == null || localite.isBlank()) {
            throw new BadRequestException(
                    "La PRMP " + prmp.getIdPrmp() + " n'a pas de localité — mode de passation non déterminable.");
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

    /** Comparaison de montants tolérante à l'échelle (BigDecimal) et aux nulls. */
    private static boolean montantChange(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return a != b;
        }
        return a.compareTo(b) != 0;
    }
}
