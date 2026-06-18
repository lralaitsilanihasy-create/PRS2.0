package cnm.prs.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.ModeAutorise;
import cnm.prs.dto.ReglePassationDto;
import cnm.prs.dto.SuggestionModeRequest;
import cnm.prs.dto.SuggestionModeResponse;
import cnm.prs.entity.ModePassation;
import cnm.prs.entity.ReglePassation;
import cnm.prs.entity.Seuil;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.ReglePassationMapper;
import cnm.prs.repository.ModePassationRepository;
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
    private final ModePassationRepository modePassationRepository;

    public ReglePassationService(ReglePassationRepository repository, SeuilRepository seuilRepository,
            ModePassationRepository modePassationRepository) {
        this.repository = repository;
        this.seuilRepository = seuilRepository;
        this.modePassationRepository = modePassationRepository;
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
    /**
     * Règles applicables (§3.1, Module 02), triées par priorité (asc, nulls last) puis idRegle —
     * la <strong>première est le mode recommandé</strong>, l'ensemble est l'<strong>ensemble autorisé</strong>.
     *
     * @return liste (éventuellement vide si un critère est nul ou si aucun seuil/règle ne correspond)
     */
    @Transactional(readOnly = true)
    public List<ReglePassation> determinerRegles(Integer idSituation, BigDecimal montant,
            Integer idNature, String idLocalite) {
        if (idSituation == null || montant == null || idNature == null || idLocalite == null) {
            return List.of();
        }
        List<Seuil> seuils = seuilRepository.findCorrespondants(idNature, idLocalite, montant);
        if (seuils.isEmpty()) {
            return List.of();
        }
        List<Integer> idSeuils = seuils.stream().map(Seuil::getIdSeuil).toList();
        return repository.findParSituationEtSeuils(idSituation, idSeuils);
    }

    @Transactional(readOnly = true)
    public Optional<ReglePassation> determinerRegle(Integer idSituation, BigDecimal montant,
            Integer idNature, String idLocalite) {
        return determinerRegles(idSituation, montant, idNature, idLocalite).stream().findFirst();
    }

    /**
     * ⚠️ Règle ajoutée — renvoie l'<strong>ensemble des modes autorisés</strong> (libellés depuis
     * {@code tr_mode}) pour (situation, nature, montant, localité), le <strong>mode recommandé</strong>
     * (règle de plus haute priorité), et un indicateur {@code modeNonDetermine} si aucune règle ne
     * correspond (ensemble vide → 200, pas 404 ; le frontend propose alors une saisie manuelle).
     */
    @Transactional(readOnly = true)
    public SuggestionModeResponse suggererMode(SuggestionModeRequest req) {
        List<ReglePassation> regles = determinerRegles(req.idSituation(), req.montant(),
                req.idNature(), req.idLocalite());
        if (regles.isEmpty()) {
            return new SuggestionModeResponse(null, List.of(), true);
        }
        List<Integer> idModes = regles.stream().map(ReglePassation::getIdMode).distinct().toList();
        Map<Integer, String> libelles = modePassationRepository.findAllById(idModes).stream()
                .collect(Collectors.toMap(ModePassation::getIdMode, ModePassation::getLibelle));
        List<ModeAutorise> modesAutorises = idModes.stream()
                .map(id -> new ModeAutorise(id, libelles.get(id))).toList();
        return new SuggestionModeResponse(regles.get(0).getIdMode(), modesAutorises, false);
    }
}
