package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.CapmDto;
import cnm.prs.entity.Capm;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.CapmMapper;
import cnm.prs.repository.CapmRepository;

/**
 * Logique métier pour {@link Capm} (référentiel des processus de marché).
 */
@Service
@Transactional
public class CapmService {

    private final CapmRepository repository;

    public CapmService(CapmRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CapmDto> findAll() {
        return repository.findAll().stream().map(CapmMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public CapmDto findById(Integer id) {
        Capm entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processus (CAPM) introuvable : " + id));
        return CapmMapper.toDto(entity);
    }

    public CapmDto create(CapmDto dto) {
        Capm entity = CapmMapper.toEntity(dto);
        return CapmMapper.toDto(repository.save(entity));
    }

    public CapmDto update(Integer id, CapmDto dto) {
        Capm existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processus (CAPM) introuvable : " + id));
        existing.setLibelleProcessus(dto.getLibelleProcessus());
        existing.setOrdre(dto.getOrdre());
        return CapmMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Processus (CAPM) introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
