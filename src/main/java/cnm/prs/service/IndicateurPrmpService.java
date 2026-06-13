package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.IndicateurPrmpDto;
import cnm.prs.entity.IndicateurPrmp;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.IndicateurPrmpMapper;
import cnm.prs.repository.IndicateurPrmpRepository;

/**
 * Logique métier pour {@link IndicateurPrmp}.
 */
@Service
@Transactional
public class IndicateurPrmpService {

    private final IndicateurPrmpRepository repository;

    public IndicateurPrmpService(IndicateurPrmpRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<IndicateurPrmpDto> findAll() {
        return repository.findAll().stream().map(IndicateurPrmpMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public IndicateurPrmpDto findById(Integer id) {
        IndicateurPrmp entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IndicateurPrmp introuvable : " + id));
        return IndicateurPrmpMapper.toDto(entity);
    }

    public IndicateurPrmpDto create(IndicateurPrmpDto dto) {
        IndicateurPrmp entity = IndicateurPrmpMapper.toEntity(dto);
        return IndicateurPrmpMapper.toDto(repository.save(entity));
    }

    public IndicateurPrmpDto update(Integer id, IndicateurPrmpDto dto) {
        IndicateurPrmp existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IndicateurPrmp introuvable : " + id));
        existing.setIdPrmp(dto.getIdPrmp());
        existing.setExercice(dto.getExercice());
        existing.setNbPpmSoumis(dto.getNbPpmSoumis());
        existing.setNbDossiersSoumis(dto.getNbDossiersSoumis());
        existing.setNbDossiersConformes(dto.getNbDossiersConformes());
        existing.setNbDossiersNonConformes(dto.getNbDossiersNonConformes());
        existing.setNbRetours(dto.getNbRetours());
        existing.setNbRetraits(dto.getNbRetraits());
        existing.setTauxConformite(dto.getTauxConformite());
        existing.setDelaiMoyCorrectionJours(dto.getDelaiMoyCorrectionJours());
        existing.setMontTotalSoumis(dto.getMontTotalSoumis());
        existing.setDateMaj(dto.getDateMaj());
        return IndicateurPrmpMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("IndicateurPrmp introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
