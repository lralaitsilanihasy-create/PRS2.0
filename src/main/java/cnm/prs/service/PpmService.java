package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.PpmDto;
import cnm.prs.entity.Ppm;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.PpmMapper;
import cnm.prs.repository.PpmRepository;

/**
 * Logique métier pour {@link Ppm}.
 */
@Service
@Transactional
public class PpmService {

    private final PpmRepository repository;

    public PpmService(PpmRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PpmDto> findAll() {
        return repository.findAll().stream().map(PpmMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PpmDto findById(Integer id) {
        Ppm entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ppm introuvable : " + id));
        return PpmMapper.toDto(entity);
    }

    public PpmDto create(PpmDto dto) {
        Ppm entity = PpmMapper.toEntity(dto);
        return PpmMapper.toDto(repository.save(entity));
    }

    public PpmDto update(Integer id, PpmDto dto) {
        Ppm existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ppm introuvable : " + id));
        existing.setIdDossier(dto.getIdDossier());
        existing.setExercice(dto.getExercice());
        existing.setSignataire(dto.getSignataire());
        existing.setDateSignature(dto.getDateSignature());
        existing.setDatePpmInit(dto.getDatePpmInit());
        existing.setNumMajPrec(dto.getNumMajPrec());
        existing.setDateMajPrec(dto.getDateMajPrec());
        existing.setNumMaj(dto.getNumMaj());
        existing.setDateMaj(dto.getDateMaj());
        existing.setReference(dto.getReference());
        existing.setLibelle(dto.getLibelle());
        existing.setDateReceptionCnm(dto.getDateReceptionCnm());
        existing.setIdLocalite(dto.getIdLocalite());
        existing.setVu(dto.getVu());
        existing.setIdPrmp(dto.getIdPrmp());
        existing.setMotifMaj(dto.getMotifMaj());
        return PpmMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Ppm introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
