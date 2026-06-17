package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.ExamenDetailDto;
import cnm.prs.entity.ExamenDetail;
import cnm.prs.enums.StatutDossier;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.ExamenDetailMapper;
import cnm.prs.repository.ExamenDetailRepository;
import cnm.prs.repository.ExamenRepository;

/**
 * Logique métier pour {@link ExamenDetail}.
 *
 * <p>Verrou d'édition (§2.6) : un point de contrôle n'est modifiable que tant que le dossier de
 * l'examen est {@link StatutDossier#EXAMINE} (navette ouverte) ; dès la signature du PV
 * ({@link StatutDossier#PV_SIGNE}) l'examen devient définitif et toute écriture est refusée (409).</p>
 */
@Service
@Transactional
public class ExamenDetailService {

    private final ExamenDetailRepository repository;
    private final ExamenRepository examenRepository;

    public ExamenDetailService(ExamenDetailRepository repository, ExamenRepository examenRepository) {
        this.repository = repository;
        this.examenRepository = examenRepository;
    }

    @Transactional(readOnly = true)
    public List<ExamenDetailDto> findAll() {
        return repository.findAll().stream().map(ExamenDetailMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ExamenDetailDto findById(Integer id) {
        ExamenDetail entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExamenDetail introuvable : " + id));
        return ExamenDetailMapper.toDto(entity);
    }

    public ExamenDetailDto create(ExamenDetailDto dto) {
        exigerExamenModifiable(dto.getIdExamen());
        ExamenDetail entity = ExamenDetailMapper.toEntity(dto);
        return ExamenDetailMapper.toDto(repository.save(entity));
    }

    public ExamenDetailDto update(Integer id, ExamenDetailDto dto) {
        ExamenDetail existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExamenDetail introuvable : " + id));
        exigerExamenModifiable(existing.getIdExamen());
        existing.setIdExamen(dto.getIdExamen());
        existing.setIdPtControle(dto.getIdPtControle());
        existing.setConforme(dto.getConforme());
        existing.setObservation(dto.getObservation());
        existing.setObsSiNonConforme(dto.getObsSiNonConforme());
        return ExamenDetailMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        ExamenDetail existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExamenDetail introuvable : " + id));
        exigerExamenModifiable(existing.getIdExamen());
        repository.delete(existing);
    }

    /**
     * Verrou (§2.6) : écriture d'un détail d'examen possible uniquement tant que le dossier est
     * {@link StatutDossier#EXAMINE} ; refusée (409) dès {@link StatutDossier#PV_SIGNE}.
     */
    private void exigerExamenModifiable(Integer idExamen) {
        String statut = idExamen == null ? null
                : examenRepository.findStatutDossierByExamen(idExamen).orElse(null);
        if (!StatutDossier.EXAMINE.name().equals(statut)) {
            throw new BusinessRuleException(
                    "Examen verrouillé : modification possible uniquement tant que le dossier est EXAMINE "
                            + "(statut actuel « " + statut + " », examen définitif après signature du PV, §2.6).");
        }
    }
}
