package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.ReceptionDto;
import cnm.prs.entity.Controleur;
import cnm.prs.entity.Reception;
import cnm.prs.enums.StatutDossier;
import cnm.prs.enums.TypeNotification;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.ReceptionMapper;
import cnm.prs.repository.ControleurRepository;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.ReceptionRepository;
import cnm.prs.security.Visibilite;

/**
 * Logique métier pour {@link Reception}.
 */
@Service
@Transactional
public class ReceptionService {

    private final ReceptionRepository repository;
    private final DossierRepository dossierRepository;
    private final ControleurRepository controleurRepository;
    private final ControleurDirectory controleurDirectory;
    private final NotificationService notificationService;

    public ReceptionService(ReceptionRepository repository, DossierRepository dossierRepository,
            ControleurRepository controleurRepository, ControleurDirectory controleurDirectory,
            NotificationService notificationService) {
        this.repository = repository;
        this.dossierRepository = dossierRepository;
        this.controleurRepository = controleurRepository;
        this.controleurDirectory = controleurDirectory;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<ReceptionDto> findAll() {
        return Visibilite.filtrer(repository::findAll, repository::findVisiblesParLocalite)
                .stream().map(ReceptionMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ReceptionDto findById(Integer id) {
        Reception entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reception introuvable : " + id));
        Visibilite.controler(loc -> repository.existsDansLocalite(id, loc));
        return ReceptionMapper.toDto(entity);
    }

    public ReceptionDto create(ReceptionDto dto) {
        exigerLocaliteDossier(dto.getIdDossier());
        validatePassage(dto);
        Reception entity = ReceptionMapper.toEntity(dto);
        Reception saved = repository.save(entity);
        declencherPretDispatch(saved);
        return ReceptionMapper.toDto(saved);
    }

    public ReceptionDto update(Integer id, ReceptionDto dto) {
        exigerLocaliteDossier(dto.getIdDossier());
        validatePassage(dto);
        Reception existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reception introuvable : " + id));
        existing.setIdDossier(dto.getIdDossier());
        existing.setNumPassage(dto.getNumPassage());
        existing.setTypePassage(dto.getTypePassage());
        existing.setImCtrlRecept(dto.getImCtrlRecept());
        existing.setDateReception(dto.getDateReception());
        existing.setObservation(dto.getObservation());
        existing.setComplet(dto.getComplet());
        existing.setIdReceptionPrec(dto.getIdReceptionPrec());
        Reception saved = repository.save(existing);
        declencherPretDispatch(saved);
        return ReceptionMapper.toDto(saved);
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Reception introuvable : " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Comportement {@code [Auto]} (§2.2) : dès qu'une réception est marquée
     * {@code COMPLET = true}, le dossier passe au statut {@code PRET_DISPATCH}.
     *
     * <p>Lors de la <em>transition</em> vers PRET_DISPATCH, une notification est adressée au
     * Président (toutes localités) et au Chef de commission de la localité du dossier
     * (déduite du contrôleur réceptionnaire).</p>
     */
    private void declencherPretDispatch(Reception reception) {
        if (!Boolean.TRUE.equals(reception.getComplet()) || reception.getIdDossier() == null) {
            return;
        }
        dossierRepository.findById(reception.getIdDossier()).ifPresent(dossier -> {
            String statut = dossier.getStatut();
            // Ne pas réactiver un dossier déjà retiré ou clôturé.
            if (StatutDossier.RETIRE.name().equals(statut) || StatutDossier.CLOTURE.name().equals(statut)) {
                return;
            }
            boolean dejaPret = StatutDossier.PRET_DISPATCH.name().equals(statut);
            dossier.setStatut(StatutDossier.PRET_DISPATCH.name());
            dossierRepository.save(dossier);
            if (!dejaPret) {
                notifierPretDispatch(reception, dossier.getIdDossier());
            }
        });
    }

    /** Notifie le Président et le CC de la localité du passage d'un dossier en PRET_DISPATCH (§2.2, §3.4). */
    private void notifierPretDispatch(Reception reception, Integer idDossier) {
        String titre = "Dossier prêt à dispatcher";
        String corps = "Le dossier " + idDossier + " est complet et prêt à être dispatché.";

        for (Controleur president : controleurDirectory.presidents()) {
            notificationService.emettre(idDossier, TypeNotification.PRET_DISPATCH,
                    president.getImControleur(), president.getEmailCont(), titre, corps);
        }
        String localite = reception.getImCtrlRecept() == null ? null
                : controleurRepository.findById(reception.getImCtrlRecept())
                        .map(Controleur::getIdLocalite).orElse(null);
        if (localite != null) {
            for (Controleur cc : controleurDirectory.chefsCommission(localite)) {
                notificationService.emettre(idDossier, TypeNotification.PRET_DISPATCH,
                        cc.getImControleur(), cc.getEmailCont(), titre, corps);
            }
        }
    }

    /**
     * Contrainte de localité (§3.3) : un contrôleur n'agit que sur des dossiers de sa
     * localité (sauf Président/Admin). La localité du dossier est déduite de ses réceptions
     * existantes ; si le dossier n'a pas encore de localité, aucune contrainte (la première
     * réception l'établit).
     */
    private void exigerLocaliteDossier(Integer idDossier) {
        String localite = idDossier == null ? null
                : repository.findLocalitesByDossier(idDossier).stream().findFirst().orElse(null);
        Visibilite.exigerLocalite(localite);
    }

    /** Valeur de TYPE_PASSAGE pour la réception initiale (§3.4). */
    private static final String TYPE_PASSAGE_INITIAL = "INITIAL";

    /**
     * Cohérence NUM_PASSAGE / TYPE_PASSAGE (§3.4) : la réception initiale porte
     * {@code NUM_PASSAGE = 1} et {@code TYPE_PASSAGE = INITIAL}, et inversement le type
     * INITIAL n'est autorisé qu'au premier passage. NUM_PASSAGE doit être &gt;= 1.
     */
    private void validatePassage(ReceptionDto dto) {
        Integer num = dto.getNumPassage();
        String type = dto.getTypePassage();

        if (num != null && num < 1) {
            throw new BusinessRuleException("NUM_PASSAGE doit être supérieur ou égal à 1.");
        }
        boolean estInitial = TYPE_PASSAGE_INITIAL.equalsIgnoreCase(type);
        if (num != null && num == 1 && !estInitial) {
            throw new BusinessRuleException(
                    "Au premier passage (NUM_PASSAGE = 1), TYPE_PASSAGE doit être INITIAL (§3.4).");
        }
        if (estInitial && num != null && num != 1) {
            throw new BusinessRuleException(
                    "TYPE_PASSAGE = INITIAL n'est autorisé qu'au premier passage (NUM_PASSAGE = 1) (§3.4).");
        }
    }
}
