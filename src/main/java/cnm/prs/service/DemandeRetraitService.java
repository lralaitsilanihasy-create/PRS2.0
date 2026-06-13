package cnm.prs.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.DemandeRetraitDto;
import cnm.prs.entity.DemandeRetrait;
import cnm.prs.entity.Prmp;
import cnm.prs.enums.StatutDossier;
import cnm.prs.enums.StatutRetrait;
import cnm.prs.enums.TypeNotification;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.DemandeRetraitMapper;
import cnm.prs.repository.DemandeRetraitRepository;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.PrmpRepository;
import cnm.prs.security.CurrentUser;
import cnm.prs.security.Visibilite;

/**
 * Logique métier pour {@link DemandeRetrait}.
 */
@Service
@Transactional
public class DemandeRetraitService {

    private final DemandeRetraitRepository repository;
    private final DossierRepository dossierRepository;
    private final PrmpRepository prmpRepository;
    private final NotificationService notificationService;

    public DemandeRetraitService(DemandeRetraitRepository repository, DossierRepository dossierRepository,
            PrmpRepository prmpRepository, NotificationService notificationService) {
        this.repository = repository;
        this.dossierRepository = dossierRepository;
        this.prmpRepository = prmpRepository;
        this.notificationService = notificationService;
    }

    /**
     * Liste filtrée (§1, §3.1) : Président/Admin → tout ; PRMP → ses propres demandes ;
     * autres contrôleurs → demandes de leur localité.
     */
    @Transactional(readOnly = true)
    public List<DemandeRetraitDto> findAll() {
        if (Visibilite.estPrmp()) {
            String idPrmp = CurrentUser.ref().orElse(null);
            if (idPrmp == null || idPrmp.isBlank()) {
                return List.of();
            }
            return repository.findByIdPrmp(idPrmp).stream().map(DemandeRetraitMapper::toDto).toList();
        }
        return Visibilite.filtrer(repository::findAll, repository::findVisiblesParLocalite)
                .stream().map(DemandeRetraitMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public DemandeRetraitDto findById(Integer id) {
        DemandeRetrait entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DemandeRetrait introuvable : " + id));
        if (Visibilite.estPrmp()) {
            String idPrmp = CurrentUser.ref().orElse(null);
            if (idPrmp == null || !idPrmp.equals(entity.getIdPrmp())) {
                throw new AccessDeniedException("Demande hors de votre périmètre de visibilité (§3.1).");
            }
        } else {
            Visibilite.controler(loc -> repository.existsDansLocalite(id, loc));
        }
        return DemandeRetraitMapper.toDto(entity);
    }

    /**
     * Création d'une demande de retrait par la PRMP. Une nouvelle demande est toujours
     * {@link StatutRetrait#EN_ATTENTE}, sans décision (§3.1). Le motif est obligatoire
     * (déjà imposé par {@code @NotBlank} sur le DTO, MOTIF_RETRAIT NOT NULL).
     */
    public DemandeRetraitDto create(DemandeRetraitDto dto) {
        DemandeRetrait entity = DemandeRetraitMapper.toEntity(dto);
        entity.setStatut(StatutRetrait.EN_ATTENTE.name());
        entity.setImCtrlCc(null);
        entity.setDateDecision(null);
        entity.setObsDecision(null);
        return DemandeRetraitMapper.toDto(repository.save(entity));
    }

    /**
     * Mise à jour / décision sur une demande de retrait. Le statut doit être une valeur
     * valide ({@link StatutRetrait}). Lorsque le CC statue (APPROUVE / REJETE), l'observation
     * et le matricule du CC décideur sont obligatoires (§3.3) ; la date de décision est
     * horodatée si absente.
     *
     * <p>NB : la propagation {@code APPROUVE → t_dossier.STATUT = RETIRE} est un comportement
     * automatique traité au Lot 3.</p>
     */
    public DemandeRetraitDto update(Integer id, DemandeRetraitDto dto) {
        DemandeRetrait existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DemandeRetrait introuvable : " + id));
        StatutRetrait ancienStatut = parseStatut(existing.getStatut());
        StatutRetrait nouveauStatut = parseStatut(dto.getStatut());

        existing.setIdDossier(dto.getIdDossier());
        existing.setIdPrmp(dto.getIdPrmp());
        existing.setMotifRetrait(dto.getMotifRetrait());
        existing.setDateDemande(dto.getDateDemande());
        existing.setStatut(nouveauStatut.name());

        if (nouveauStatut.estDecision()) {
            if (dto.getObsDecision() == null || dto.getObsDecision().isBlank()) {
                throw new BusinessRuleException("L'observation de décision est obligatoire pour statuer (§3.3).");
            }
            if (dto.getImCtrlCc() == null || dto.getImCtrlCc().isBlank()) {
                throw new BusinessRuleException("Le matricule du CC décideur (imCtrlCc) est obligatoire pour statuer (§3.3).");
            }
            existing.setImCtrlCc(dto.getImCtrlCc());
            existing.setObsDecision(dto.getObsDecision());
            existing.setDateDecision(dto.getDateDecision() != null ? dto.getDateDecision() : LocalDateTime.now());
        } else {
            existing.setImCtrlCc(dto.getImCtrlCc());
            existing.setObsDecision(dto.getObsDecision());
            existing.setDateDecision(dto.getDateDecision());
        }

        DemandeRetrait saved = repository.save(existing);

        // Comportements [Auto] (§3.3) déclenchés au passage EN_ATTENTE → décision.
        boolean transitionVersDecision = nouveauStatut.estDecision() && ancienStatut == StatutRetrait.EN_ATTENTE;
        if (transitionVersDecision) {
            appliquerDecisionRetrait(saved, nouveauStatut);
        }
        return DemandeRetraitMapper.toDto(saved);
    }

    /**
     * Effets automatiques d'une décision de retrait (§3.3) :
     * <ul>
     *   <li>si APPROUVE : le dossier passe au statut {@code RETIRE} ;</li>
     *   <li>dans tous les cas : notification {@code RETRAIT_APPROUVE} / {@code RETRAIT_REJETE}
     *       envoyée à la PRMP (par e-mail, depuis {@code t_prmp}).</li>
     * </ul>
     */
    private void appliquerDecisionRetrait(DemandeRetrait demande, StatutRetrait decision) {
        if (decision == StatutRetrait.APPROUVE && demande.getIdDossier() != null) {
            dossierRepository.findById(demande.getIdDossier()).ifPresent(dossier -> {
                dossier.setStatut(StatutDossier.RETIRE.name());
                dossierRepository.save(dossier);
            });
        }

        String emailPrmp = prmpRepository.findById(demande.getIdPrmp())
                .map(Prmp::getEmailPrmp)
                .orElse(null);
        TypeNotification type = decision == StatutRetrait.APPROUVE
                ? TypeNotification.RETRAIT_APPROUVE
                : TypeNotification.RETRAIT_REJETE;
        String titre = decision == StatutRetrait.APPROUVE
                ? "Demande de retrait approuvée"
                : "Demande de retrait rejetée";
        notificationService.emettre(demande.getIdDossier(), type, null, emailPrmp, titre, demande.getObsDecision());
    }

    private StatutRetrait parseStatut(String statut) {
        if (statut == null || statut.isBlank()) {
            throw new BusinessRuleException("Le statut de la demande est obligatoire (EN_ATTENTE / APPROUVE / REJETE).");
        }
        try {
            return StatutRetrait.valueOf(statut.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Statut de demande de retrait invalide : « " + statut + " ».");
        }
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("DemandeRetrait introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
