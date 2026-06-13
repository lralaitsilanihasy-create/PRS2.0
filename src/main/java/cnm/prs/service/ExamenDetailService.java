package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.ExamenDetailDto;
import cnm.prs.entity.ExamenDetail;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.ExamenDetailMapper;
import cnm.prs.repository.ExamenDetailRepository;

/**
 * Logique métier pour {@link ExamenDetail}.
 */
@Service
@Transactional
public class ExamenDetailService {

    private final ExamenDetailRepository repository;

    public ExamenDetailService(ExamenDetailRepository repository) {
        this.repository = repository;
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
        ExamenDetail entity = ExamenDetailMapper.toEntity(dto);
        return ExamenDetailMapper.toDto(repository.save(entity));
    }

    public ExamenDetailDto update(Integer id, ExamenDetailDto dto) {
        ExamenDetail existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExamenDetail introuvable : " + id));
        existing.setIdExamen(dto.getIdExamen());
        existing.setIdPtControle(dto.getIdPtControle());
        existing.setConforme(dto.getConforme());
        existing.setObservation(dto.getObservation());
        existing.setObsSiNonConforme(dto.getObsSiNonConforme());
        return ExamenDetailMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("ExamenDetail introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
