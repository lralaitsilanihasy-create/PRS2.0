package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.SeuilDto;
import cnm.prs.entity.Seuil;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.SeuilMapper;
import cnm.prs.repository.SeuilRepository;

/**
 * Logique métier pour {@link Seuil}.
 */
@Service
@Transactional
public class SeuilService {

    private final SeuilRepository repository;

    public SeuilService(SeuilRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SeuilDto> findAll() {
        return repository.findAll().stream().map(SeuilMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SeuilDto findById(Integer id) {
        Seuil entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seuil introuvable : " + id));
        return SeuilMapper.toDto(entity);
    }

    public SeuilDto create(SeuilDto dto) {
        Seuil entity = SeuilMapper.toEntity(dto);
        return SeuilMapper.toDto(repository.save(entity));
    }

    public SeuilDto update(Integer id, SeuilDto dto) {
        Seuil existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seuil introuvable : " + id));
        existing.setMontantMin(dto.getMontantMin());
        existing.setMontantMax(dto.getMontantMax());
        existing.setIdNature(dto.getIdNature());
        existing.setIdLocalite(dto.getIdLocalite());
        return SeuilMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Seuil introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
