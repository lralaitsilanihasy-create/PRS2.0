package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.ExamenDto;
import cnm.prs.entity.Examen;
import cnm.prs.enums.StatutDossier;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.ExamenMapper;
import cnm.prs.repository.DispatchRepository;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.ExamenRepository;
import cnm.prs.security.Visibilite;

/**
 * Logique métier pour {@link Examen}.
 */
@Service
@Transactional
public class ExamenService {

    private final ExamenRepository repository;
    private final DispatchRepository dispatchRepository;
    private final DossierRepository dossierRepository;

    public ExamenService(ExamenRepository repository, DispatchRepository dispatchRepository,
            DossierRepository dossierRepository) {
        this.repository = repository;
        this.dispatchRepository = dispatchRepository;
        this.dossierRepository = dossierRepository;
    }

    @Transactional(readOnly = true)
    public List<ExamenDto> findAll() {
        return Visibilite.filtrer(repository::findAll, repository::findVisiblesParLocalite)
                .stream().map(ExamenMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ExamenDto findById(Integer id) {
        Examen entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examen introuvable : " + id));
        Visibilite.controler(loc -> repository.existsDansLocalite(id, loc));
        return ExamenMapper.toDto(entity);
    }

    public ExamenDto create(ExamenDto dto) {
        Visibilite.exigerLocalite(dispatchRepository.findLocaliteById(dto.getIdDispatch()));
        exigerDossierDispatche(dto.getIdDispatch());
        Examen entity = ExamenMapper.toEntity(dto);
        return ExamenMapper.toDto(repository.save(entity));
    }

    /**
     * Précondition du circuit (§2.3 → §2.4) : on n'examine qu'un dossier <strong>déjà dispatché</strong>
     * (statut {@link StatutDossier#DISPATCHE}). Le dispatch précède l'examen et fait passer le dossier
     * de PRET_DISPATCH à DISPATCHE ; un dossier non dispatché (ou clôturé/retiré) est refusé.
     */
    private void exigerDossierDispatche(Integer idDispatch) {
        String statut = idDispatch == null ? null
                : dossierRepository.findStatutByDispatch(idDispatch).orElse(null);
        if (!StatutDossier.DISPATCHE.name().equals(statut)) {
            throw new BusinessRuleException(
                    "Examen impossible : le dossier doit avoir été dispatché (statut DISPATCHE) (§2.4), "
                            + "statut actuel « " + statut + " ».");
        }
    }

    public ExamenDto update(Integer id, ExamenDto dto) {
        Visibilite.exigerLocalite(dispatchRepository.findLocaliteById(dto.getIdDispatch()));
        Examen existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examen introuvable : " + id));
        existing.setIdDispatch(dto.getIdDispatch());
        existing.setImCtrlMembre(dto.getImCtrlMembre());
        existing.setDateExamen(dto.getDateExamen());
        return ExamenMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Examen introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
