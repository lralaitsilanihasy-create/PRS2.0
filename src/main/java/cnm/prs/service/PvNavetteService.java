package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.PvNavetteDto;
import cnm.prs.entity.PvNavette;
import cnm.prs.enums.SensNavette;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.PvNavetteMapper;
import cnm.prs.repository.PvNavetteRepository;

/**
 * Logique métier pour {@link PvNavette}.
 */
@Service
@Transactional
public class PvNavetteService {

    private final PvNavetteRepository repository;

    public PvNavetteService(PvNavetteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PvNavetteDto> findAll() {
        return repository.findAll().stream().map(PvNavetteMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PvNavetteDto findById(Integer id) {
        PvNavette entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PvNavette introuvable : " + id));
        return PvNavetteMapper.toDto(entity);
    }

    public PvNavetteDto create(PvNavetteDto dto) {
        validateSens(dto.getSens());
        PvNavette entity = PvNavetteMapper.toEntity(dto);
        return PvNavetteMapper.toDto(repository.save(entity));
    }

    public PvNavetteDto update(Integer id, PvNavetteDto dto) {
        validateSens(dto.getSens());
        PvNavette existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PvNavette introuvable : " + id));
        existing.setIdPv(dto.getIdPv());
        existing.setNumNavette(dto.getNumNavette());
        existing.setSens(dto.getSens());
        existing.setImActeur(dto.getImActeur());
        existing.setDateAction(dto.getDateAction());
        existing.setCommentaire(dto.getCommentaire());
        return PvNavetteMapper.toDto(repository.save(existing));
    }

    /**
     * Suppression interdite : la traçabilité de la navette est immuable
     * (§3.5 — « aucune navette ne peut être supprimée »).
     */
    public void delete(Integer id) {
        throw new BusinessRuleException("Une navette de PV ne peut pas être supprimée (§3.5 — traçabilité).");
    }

    private void validateSens(String sens) {
        if (sens == null || sens.isBlank()) {
            throw new BusinessRuleException("Le sens de la navette est obligatoire (SOUMISSION / RETOUR_RECTIF / ACCEPTATION).");
        }
        try {
            SensNavette.valueOf(sens.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Sens de navette invalide : « " + sens + " ».");
        }
    }
}
