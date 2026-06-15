package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.EntiteContractDto;
import cnm.prs.dto.EntitePubliqueDto;
import cnm.prs.entity.EntiteContract;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.EntiteContractMapper;
import cnm.prs.mapper.EntitePubliqueMapper;
import cnm.prs.repository.EntiteContractRepository;

/**
 * Logique métier pour {@link EntiteContract}.
 */
@Service
@Transactional
public class EntiteContractService {

    private final EntiteContractRepository repository;

    public EntiteContractService(EntiteContractRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<EntiteContractDto> findAll() {
        return repository.findAll().stream().map(EntiteContractMapper::toDto).toList();
    }

    /** Liste publique réduite (pour l'écran d'inscription PRMP, route non authentifiée). */
    @Transactional(readOnly = true)
    public List<EntitePubliqueDto> listePublique() {
        return repository.findAll().stream().map(EntitePubliqueMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public EntiteContractDto findById(Integer id) {
        EntiteContract entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EntiteContract introuvable : " + id));
        return EntiteContractMapper.toDto(entity);
    }

    public EntiteContractDto create(EntiteContractDto dto) {
        EntiteContract entity = EntiteContractMapper.toEntity(dto);
        return EntiteContractMapper.toDto(repository.save(entity));
    }

    public EntiteContractDto update(Integer id, EntiteContractDto dto) {
        EntiteContract existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EntiteContract introuvable : " + id));
        existing.setLibelleEntite(dto.getLibelleEntite());
        existing.setAdresse(dto.getAdresse());
        existing.setCategorieEntite(dto.getCategorieEntite());
        existing.setIdOrganigramme(dto.getIdOrganigramme());
        existing.setIdEntiteParent(dto.getIdEntiteParent());
        existing.setNiveauHierarchique(dto.getNiveauHierarchique());
        return EntiteContractMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("EntiteContract introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
