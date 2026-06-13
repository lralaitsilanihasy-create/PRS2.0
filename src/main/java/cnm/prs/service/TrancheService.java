package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.TrancheDto;
import cnm.prs.entity.Tranche;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.TrancheMapper;
import cnm.prs.repository.TrancheRepository;

/**
 * Logique métier pour {@link Tranche}.
 */
@Service
@Transactional
public class TrancheService {

    private final TrancheRepository repository;

    public TrancheService(TrancheRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TrancheDto> findAll() {
        return repository.findAll().stream().map(TrancheMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TrancheDto findById(Integer id) {
        Tranche entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tranche introuvable : " + id));
        return TrancheMapper.toDto(entity);
    }

    public TrancheDto create(TrancheDto dto) {
        Tranche entity = TrancheMapper.toEntity(dto);
        return TrancheMapper.toDto(repository.save(entity));
    }

    public TrancheDto update(Integer id, TrancheDto dto) {
        Tranche existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tranche introuvable : " + id));
        existing.setLieuTrc(dto.getLieuTrc());
        existing.setMontTrc(dto.getMontTrc());
        existing.setIdLot(dto.getIdLot());
        return TrancheMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Tranche introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
