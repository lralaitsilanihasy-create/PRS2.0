package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.ServiceBeneficiaireDto;
import cnm.prs.entity.ServiceBeneficiaire;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.ServiceBeneficiaireMapper;
import cnm.prs.repository.ServiceBeneficiaireRepository;

/**
 * Logique métier pour {@link ServiceBeneficiaire}.
 */
@Service
@Transactional
public class ServiceBeneficiaireService {

    private final ServiceBeneficiaireRepository repository;

    public ServiceBeneficiaireService(ServiceBeneficiaireRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ServiceBeneficiaireDto> findAll() {
        return repository.findAll().stream().map(ServiceBeneficiaireMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ServiceBeneficiaireDto findById(Integer id) {
        ServiceBeneficiaire entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceBeneficiaire introuvable : " + id));
        return ServiceBeneficiaireMapper.toDto(entity);
    }

    public ServiceBeneficiaireDto create(ServiceBeneficiaireDto dto) {
        ServiceBeneficiaire entity = ServiceBeneficiaireMapper.toEntity(dto);
        return ServiceBeneficiaireMapper.toDto(repository.save(entity));
    }

    public ServiceBeneficiaireDto update(Integer id, ServiceBeneficiaireDto dto) {
        ServiceBeneficiaire existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceBeneficiaire introuvable : " + id));
        existing.setAncMontBenef(dto.getAncMontBenef());
        existing.setNouvMontBenef(dto.getNouvMontBenef());
        existing.setSoaCode(dto.getSoaCode());
        existing.setIdDetail(dto.getIdDetail());
        return ServiceBeneficiaireMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("ServiceBeneficiaire introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
