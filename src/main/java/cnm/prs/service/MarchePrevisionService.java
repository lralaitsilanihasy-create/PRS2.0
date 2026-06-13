package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.MarchePrevisionDto;
import cnm.prs.entity.MarchePrevision;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.MarchePrevisionMapper;
import cnm.prs.repository.MarchePrevisionRepository;

/**
 * Logique métier pour {@link MarchePrevision} (dates prévisionnelles des marchés).
 */
@Service
@Transactional
public class MarchePrevisionService {

    private final MarchePrevisionRepository repository;

    public MarchePrevisionService(MarchePrevisionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<MarchePrevisionDto> findAll() {
        return repository.findAll().stream().map(MarchePrevisionMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<MarchePrevisionDto> findByMarche(Integer idDetail) {
        return repository.findByIdDetail(idDetail).stream().map(MarchePrevisionMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public MarchePrevisionDto findById(Integer id) {
        MarchePrevision entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Date prévisionnelle introuvable : " + id));
        return MarchePrevisionMapper.toDto(entity);
    }

    public MarchePrevisionDto create(MarchePrevisionDto dto) {
        MarchePrevision entity = MarchePrevisionMapper.toEntity(dto);
        return MarchePrevisionMapper.toDto(repository.save(entity));
    }

    public MarchePrevisionDto update(Integer id, MarchePrevisionDto dto) {
        MarchePrevision existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Date prévisionnelle introuvable : " + id));
        existing.setIdDetail(dto.getIdDetail());
        existing.setTypeDate(dto.getTypeDate());
        existing.setDatePrev(dto.getDatePrev());
        return MarchePrevisionMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Date prévisionnelle introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
