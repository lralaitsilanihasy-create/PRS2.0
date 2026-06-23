package cnm.prs.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.MarchePrevisionDto;
import cnm.prs.entity.Capm;
import cnm.prs.entity.MarchePrevision;
import cnm.prs.exception.ChampsInvalidesException;
import cnm.prs.exception.ErrorResponse;
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
        validerChronologie(dto, null);
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
        validerChronologie(dto, id);   // la ligne éditée remplace l'existante dans la séquence
        existing.setIdDetail(dto.getIdDetail());
        existing.setIdCapm(dto.getIdCapm());
        existing.setDateDebut(dto.getDateDebut());
        existing.setDateFin(dto.getDateFin());
        return peuplerOrdre(MarchePrevisionMapper.toDto(repository.save(existing)));
    }

    /**
     * ⚠️ Règle ajoutée — valide la cohérence chronologique des processus du marché après ajout/édition
     * de cette ligne : la prévision (dto) + les autres lignes du marché (hors {@code idAExclure}),
     * triées par ordre CAPM. Violation → 400 (champ {@code dateDebut}/{@code dateFin}).
     */
    private void validerChronologie(MarchePrevisionDto dto, Integer idAExclure) {
        List<ProcessusChronologie.Proc> procs = new ArrayList<>();
        procs.add(procDe(dto.getIdCapm(), dto.getDateDebut(), dto.getDateFin()));
        for (MarchePrevision sib : repository.findByIdDetail(dto.getIdDetail())) {
            if (idAExclure != null && idAExclure.equals(sib.getIdPrevision())) {
                continue;
            }
            procs.add(procDe(sib.getIdCapm(), sib.getDateDebut(), sib.getDateFin()));
        }
        ErrorResponse.FieldError violation = ProcessusChronologie.premiereViolation(procs);
        if (violation != null) {
            throw new ChampsInvalidesException(List.of(violation));
        }
    }

    /** Construit un {@code Proc} (chemin = nom de champ seul) en résolvant ordre/libellé via {@code t_capm}. */
    private ProcessusChronologie.Proc procDe(Integer idCapm, LocalDate dateDebut, LocalDate dateFin) {
        Capm c = idCapm == null ? null : capmRepository.findById(idCapm).orElse(null);
        int ordre = (c == null || c.getOrdre() == null) ? 0 : c.getOrdre();
        String libelle = c == null ? String.valueOf(idCapm) : c.getLibelleProcessus();
        return new ProcessusChronologie.Proc("", ordre, libelle, dateDebut, dateFin);
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Date prévisionnelle introuvable : " + id);
        }
        repository.deleteById(id);
    }
}
