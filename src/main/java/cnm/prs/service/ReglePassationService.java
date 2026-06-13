package cnm.prs.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.ReglePassationDto;
import cnm.prs.dto.SuggestionModeRequest;
import cnm.prs.dto.SuggestionModeResponse;
import cnm.prs.entity.ReglePassation;
import cnm.prs.entity.Seuil;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.ReglePassationMapper;
import cnm.prs.repository.ReglePassationRepository;
import cnm.prs.repository.SeuilRepository;

/**
 * Logique métier pour {@link ReglePassation}.
 */
@Service
@Transactional
public class ReglePassationService {

    private final ReglePassationRepository repository;
    private final SeuilRepository seuilRepository;

    public ReglePassationService(ReglePassationRepository repository, SeuilRepository seuilRepository) {
        this.repository = repository;
        this.seuilRepository = seuilRepository;
    }

    @Transactional(readOnly = true)
    public List<ReglePassationDto> findAll() {
        return repository.findAll().stream().map(ReglePassationMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ReglePassationDto findById(Integer id) {
        ReglePassation entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReglePassation introuvable : " + id));
        return ReglePassationMapper.toDto(entity);
    }

    public ReglePassationDto create(ReglePassationDto dto) {
        ReglePassation entity = ReglePassationMapper.toEntity(dto);
        return ReglePassationMapper.toDto(repository.save(entity));
    }

    public ReglePassationDto update(Integer id, ReglePassationDto dto) {
        ReglePassation existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReglePassation introuvable : " + id));
        existing.setIdSituation(dto.getIdSituation());
        existing.setIdSeuil(dto.getIdSeuil());
        existing.setIdMode(dto.getIdMode());
        existing.setPriorite(dto.getPriorite());
        return ReglePassationMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("ReglePassation introuvable : " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Détermine la règle de passation applicable (§3.1, Module 02) à partir des quatre critères :
     * on sélectionne les seuils correspondant à (nature, localité, montant), puis la règle de plus
     * haute priorité pour la situation donnée. Brique commune à la suggestion (endpoint PRMP) et à
     * la détermination automatique du mode à la création/mise à jour d'un marché.
     *
     * @return la règle retenue, ou {@link Optional#empty()} si un critère est nul ou si aucun
     *         seuil/règle ne correspond (le mode reste alors indéterminé).
     */
    @Transactional(readOnly = true)
    public Optional<ReglePassation> determinerRegle(Integer idSituation, BigDecimal montant,
            Integer idNature, String idLocalite) {
        if (idSituation == null || montant == null || idNature == null || idLocalite == null) {
            return Optional.empty();
        }
        List<Seuil> seuils = seuilRepository.findCorrespondants(idNature, idLocalite, montant);
        if (seuils.isEmpty()) {
            return Optional.empty();
        }
        List<Integer> idSeuils = seuils.stream().map(Seuil::getIdSeuil).toList();
        List<ReglePassation> regles = repository.findParSituationEtSeuils(idSituation, idSeuils);
        return regles.isEmpty() ? Optional.empty() : Optional.of(regles.get(0));
    }

    /**
     * Détermination automatique du mode de passation (§3.1, Module 02) — outil de suggestion de
     * la PRMP. Suggestion non contraignante.
     *
     * @throws ResourceNotFoundException si aucun seuil ou aucune règle ne correspond
     */
    @Transactional(readOnly = true)
    public SuggestionModeResponse suggererMode(SuggestionModeRequest req) {
        ReglePassation retenue = determinerRegle(req.idSituation(), req.montant(),
                req.idNature(), req.idLocalite())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune règle de passation correspondante (nature/localité/montant/situation)."));
        return new SuggestionModeResponse(retenue.getIdMode(), retenue.getIdRegle(),
                retenue.getIdSeuil(), retenue.getPriorite());
    }
}
