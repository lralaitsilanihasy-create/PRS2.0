package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.SituationDto;
import cnm.prs.entity.Situation;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.SituationMapper;
import cnm.prs.repository.SituationRepository;

/**
 * Logique métier pour {@link Situation}.
 */
@Service
@Transactional
public class SituationService {

    private final SituationRepository repository;

    public SituationService(SituationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SituationDto> findAll() {
        return repository.findAll().stream().map(SituationMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SituationDto findById(Integer id) {
        Situation entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Situation introuvable : " + id));
        return SituationMapper.toDto(entity);
    }

    public SituationDto create(SituationDto dto) {
        Situation entity = SituationMapper.toEntity(dto);
        return SituationMapper.toDto(repository.save(entity));
    }

    public SituationDto update(Integer id, SituationDto dto) {
        Situation existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Situation introuvable : " + id));
        existing.setLibelle(dto.getLibelle());
        existing.setDescription(dto.getDescription());
        return SituationMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Situation introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
