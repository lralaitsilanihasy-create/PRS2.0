package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.PrmpEntiteDto;
import cnm.prs.entity.PrmpEntite;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.PrmpEntiteMapper;
import cnm.prs.repository.PrmpEntiteRepository;

/**
 * Logique métier pour {@link PrmpEntite}.
 */
@Service
@Transactional
public class PrmpEntiteService {

    private final PrmpEntiteRepository repository;

    public PrmpEntiteService(PrmpEntiteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PrmpEntiteDto> findAll() {
        return repository.findAll().stream().map(PrmpEntiteMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PrmpEntiteDto findById(Integer id) {
        PrmpEntite entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PrmpEntite introuvable : " + id));
        return PrmpEntiteMapper.toDto(entity);
    }

    public PrmpEntiteDto create(PrmpEntiteDto dto) {
        PrmpEntite entity = PrmpEntiteMapper.toEntity(dto);
        return PrmpEntiteMapper.toDto(repository.save(entity));
    }

    public PrmpEntiteDto update(Integer id, PrmpEntiteDto dto) {
        PrmpEntite existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PrmpEntite introuvable : " + id));
        existing.setIdPrmp(dto.getIdPrmp());
        existing.setIdEntiteContract(dto.getIdEntiteContract());
        existing.setDateAffectation(dto.getDateAffectation());
        existing.setActif(dto.getActif());
        return PrmpEntiteMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("PrmpEntite introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
