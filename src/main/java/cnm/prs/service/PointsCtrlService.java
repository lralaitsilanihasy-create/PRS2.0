package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.PointsCtrlDto;
import cnm.prs.entity.PointsCtrl;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.PointsCtrlMapper;
import cnm.prs.repository.PointsCtrlRepository;

/**
 * Logique métier pour {@link PointsCtrl}.
 */
@Service
@Transactional
public class PointsCtrlService {

    private final PointsCtrlRepository repository;

    public PointsCtrlService(PointsCtrlRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PointsCtrlDto> findAll() {
        return repository.findAll().stream().map(PointsCtrlMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PointsCtrlDto findById(Integer id) {
        PointsCtrl entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PointsCtrl introuvable : " + id));
        return PointsCtrlMapper.toDto(entity);
    }

    public PointsCtrlDto create(PointsCtrlDto dto) {
        PointsCtrl entity = PointsCtrlMapper.toEntity(dto);
        return PointsCtrlMapper.toDto(repository.save(entity));
    }

    public PointsCtrlDto update(Integer id, PointsCtrlDto dto) {
        PointsCtrl existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PointsCtrl introuvable : " + id));
        existing.setLibelPointCtrl(dto.getLibelPointCtrl());
        existing.setDecriptPointCtrl(dto.getDecriptPointCtrl());
        existing.setOrdrePointCtrl(dto.getOrdrePointCtrl());
        existing.setObligatoire(dto.getObligatoire());
        existing.setIdTypeDossier(dto.getIdTypeDossier());
        return PointsCtrlMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("PointsCtrl introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
