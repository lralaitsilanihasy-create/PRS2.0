package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.ControleurDto;
import cnm.prs.entity.Controleur;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.ControleurMapper;
import cnm.prs.repository.ControleurRepository;

/**
 * Logique métier pour {@link Controleur}.
 */
@Service
@Transactional
public class ControleurService {

    private final ControleurRepository repository;

    public ControleurService(ControleurRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ControleurDto> findAll() {
        return repository.findAll().stream().map(ControleurMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ControleurDto findById(String id) {
        Controleur entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Controleur introuvable : " + id));
        return ControleurMapper.toDto(entity);
    }

    public ControleurDto create(ControleurDto dto) {
        Controleur entity = ControleurMapper.toEntity(dto);
        return ControleurMapper.toDto(repository.save(entity));
    }

    public ControleurDto update(String id, ControleurDto dto) {
        Controleur existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Controleur introuvable : " + id));
        existing.setNomCont(dto.getNomCont());
        existing.setPrenomsCont(dto.getPrenomsCont());
        existing.setEmailCont(dto.getEmailCont());
        existing.setTelCont(dto.getTelCont());
        existing.setIdProfile(dto.getIdProfile());
        existing.setIdLocalite(dto.getIdLocalite());
        existing.setIdSuperieur(dto.getIdSuperieur());
        existing.setTransversal(dto.getTransversal());
        return ControleurMapper.toDto(repository.save(existing));
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Controleur introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
