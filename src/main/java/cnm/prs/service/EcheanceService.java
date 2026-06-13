package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.EcheanceDto;
import cnm.prs.entity.Echeance;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.EcheanceMapper;
import cnm.prs.repository.EcheanceRepository;

/**
 * Logique métier pour {@link Echeance}.
 */
@Service
@Transactional
public class EcheanceService {

    private final EcheanceRepository repository;

    public EcheanceService(EcheanceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<EcheanceDto> findAll() {
        return repository.findAll().stream().map(EcheanceMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public EcheanceDto findById(Integer id) {
        Echeance entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Echeance introuvable : " + id));
        return EcheanceMapper.toDto(entity);
    }

    public EcheanceDto create(EcheanceDto dto) {
        Echeance entity = EcheanceMapper.toEntity(dto);
        return EcheanceMapper.toDto(repository.save(entity));
    }

    public EcheanceDto update(Integer id, EcheanceDto dto) {
        Echeance existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Echeance introuvable : " + id));
        existing.setIdDetail(dto.getIdDetail());
        existing.setTypeJalon(dto.getTypeJalon());
        existing.setDatePrevue(dto.getDatePrevue());
        existing.setDateReelle(dto.getDateReelle());
        existing.setStatutJalon(dto.getStatutJalon());
        existing.setEcartJours(dto.getEcartJours());
        existing.setAlerteEnvoyee(dto.getAlerteEnvoyee());
        return EcheanceMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Echeance introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
