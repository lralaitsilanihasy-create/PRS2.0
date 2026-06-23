package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.MarchePrevisionDto;
import cnm.prs.entity.MarchePrevision;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.MarchePrevisionMapper;
import cnm.prs.repository.CapmRepository;
import cnm.prs.repository.MarchePrevisionRepository;

/**
 * Logique métier pour {@link MarchePrevision} (dates prévisionnelles des marchés).
 */
@Service
@Transactional
public class MarchePrevisionService {

    private final MarchePrevisionRepository repository;
    private final CapmRepository capmRepository;

    public MarchePrevisionService(MarchePrevisionRepository repository, CapmRepository capmRepository) {
        this.repository = repository;
        this.capmRepository = capmRepository;
    }

    @Transactional(readOnly = true)
    public List<MarchePrevisionDto> findAll() {
        return repository.findAll().stream().map(MarchePrevisionMapper::toDto).map(this::peuplerOrdre).toList();
    }

    @Transactional(readOnly = true)
    public List<MarchePrevisionDto> findByMarche(Integer idDetail) {
        // Triées par l'ordre du processus (t_capm.ORDRE) ASC.
        return repository.findByMarcheOrdonne(idDetail).stream()
                .map(MarchePrevisionMapper::toDto).map(this::peuplerOrdre).toList();
    }

    @Transactional(readOnly = true)
    public MarchePrevisionDto findById(Integer id) {
        MarchePrevision entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Date prévisionnelle introuvable : " + id));
        return peuplerOrdre(MarchePrevisionMapper.toDto(entity));
    }

    public MarchePrevisionDto create(MarchePrevisionDto dto) {
        MarchePrevision entity = MarchePrevisionMapper.toEntity(dto);
        return peuplerOrdre(MarchePrevisionMapper.toDto(repository.save(entity)));
    }

    /** Renseigne l'{@code ordre} (lecture seule) depuis le référentiel {@code t_capm}. */
    private MarchePrevisionDto peuplerOrdre(MarchePrevisionDto dto) {
        if (dto != null && dto.getIdCapm() != null) {
            capmRepository.findById(dto.getIdCapm()).ifPresent(c -> dto.setOrdre(c.getOrdre()));
        }
        return dto;
    }

    public MarchePrevisionDto update(Integer id, MarchePrevisionDto dto) {
        MarchePrevision existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Date prévisionnelle introuvable : " + id));
        existing.setIdDetail(dto.getIdDetail());
        existing.setIdCapm(dto.getIdCapm());
        existing.setDateDebut(dto.getDateDebut());
        existing.setDateFin(dto.getDateFin());
        return peuplerOrdre(MarchePrevisionMapper.toDto(repository.save(existing)));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Date prévisionnelle introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
