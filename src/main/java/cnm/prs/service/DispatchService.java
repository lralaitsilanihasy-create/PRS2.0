package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.DispatchDto;
import cnm.prs.entity.Dispatch;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.enums.StatutDossier;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.DispatchMapper;
import cnm.prs.repository.ControleurRepository;
import cnm.prs.repository.DispatchRepository;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.ReceptionRepository;
import cnm.prs.security.CurrentUser;
import cnm.prs.security.Visibilite;

/**
 * Logique métier pour {@link Dispatch}.
 */
@Service
@Transactional
public class DispatchService {

    private final DispatchRepository repository;
    private final ReceptionRepository receptionRepository;
    private final ControleurRepository controleurRepository;
    private final DossierRepository dossierRepository;

    public DispatchService(DispatchRepository repository, ReceptionRepository receptionRepository,
            ControleurRepository controleurRepository, DossierRepository dossierRepository) {
        this.repository = repository;
        this.receptionRepository = receptionRepository;
        this.controleurRepository = controleurRepository;
        this.dossierRepository = dossierRepository;
    }

    @Transactional(readOnly = true)
    public List<DispatchDto> findAll() {
        return Visibilite.filtrer(repository::findAll, repository::findVisiblesParLocalite)
                .stream().map(DispatchMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public DispatchDto findById(Integer id) {
        Dispatch entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch introuvable : " + id));
        Visibilite.controler(loc -> repository.existsDansLocalite(id, loc));
        return DispatchMapper.toDto(entity);
    }

    public DispatchDto create(DispatchDto dto) {
        exigerDossierPretDispatch(dto.getIdReception());
        interdireDoublonDispatch(dto.getIdReception());
        validerInterimDispatch(dto);
        Dispatch entity = DispatchMapper.toEntity(dto);
        return DispatchMapper.toDto(repository.save(entity));
    }

    /**
     * Précondition du circuit (§2.2 → §2.3) : on ne dispatche qu'un dossier au statut
     * {@link StatutDossier#PRET_DISPATCH} (donc complet et pas encore clôturé/retiré).
     */
    private void exigerDossierPretDispatch(Integer idReception) {
        String statut = idReception == null ? null
                : dossierRepository.findStatutByReception(idReception).orElse(null);
        if (!StatutDossier.PRET_DISPATCH.name().equals(statut)) {
            throw new BusinessRuleException(
                    "Dispatch impossible : le dossier doit être au statut PRET_DISPATCH (§2.2/§2.3), "
                            + "statut actuel « " + statut + " ».");
        }
    }

    /** Anti-doublon (§3.2, « dossiers complets sans dispatch existant ») : un seul dispatch par réception. */
    private void interdireDoublonDispatch(Integer idReception) {
        if (idReception != null && repository.existsByIdReception(idReception)) {
            throw new BusinessRuleException(
                    "Un dispatch existe déjà pour cette réception (§3.2) ; corrigez-le via PUT /api/dispatchs/{id}.");
        }
    }

    public DispatchDto update(Integer id, DispatchDto dto) {
        validerInterimDispatch(dto);
        Dispatch existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch introuvable : " + id));
        existing.setIdReception(dto.getIdReception());
        existing.setImCtrlDispatch(dto.getImCtrlDispatch());
        existing.setImCtrlCc(dto.getImCtrlCc());
        existing.setImCtrlMembre(dto.getImCtrlMembre());
        existing.setDateDispatch(dto.getDateDispatch());
        existing.setDateCtrlAssigne(dto.getDateCtrlAssigne());
        existing.setInstructions(dto.getInstructions());
        existing.setInterimDispatch(dto.getInterimDispatch());
        return DispatchMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Dispatch introuvable : " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Cohérence de {@code INTERIM_DISPATCH} selon le dispatcheur (§3.3, §3.2) :
     * <ul>
     *   <li>Président → dispatch titulaire, {@code INTERIM_DISPATCH = false} ;</li>
     *   <li>Chef de commission dans sa localité → titulaire, {@code false} ;</li>
     *   <li>Chef de commission hors de sa localité → intérim, {@code true} obligatoire.</li>
     * </ul>
     * Si la localité du dossier ne peut être déterminée, aucune contrainte n'est appliquée.
     */
    private void validerInterimDispatch(DispatchDto dto) {
        ProfilUtilisateur profil = CurrentUser.profil().orElse(null);
        boolean interim = Boolean.TRUE.equals(dto.getInterimDispatch());

        if (profil == ProfilUtilisateur.PRESIDENT) {
            if (interim) {
                throw new BusinessRuleException(
                        "Le Président dispatche en titulaire : INTERIM_DISPATCH doit être false (§3.2).");
            }
            return;
        }
        if (profil == ProfilUtilisateur.CHEF_COMMISSION) {
            String localiteDossier = resoudreLocaliteDossier(dto.getIdReception());
            if (localiteDossier == null) {
                return; // localité indéterminée → pas de contrainte
            }
            String localiteCc = CurrentUser.localite().orElse(null);
            boolean memeLocalite = localiteDossier.equals(localiteCc);
            if (memeLocalite && interim) {
                throw new BusinessRuleException(
                        "Dispatch dans votre localité : INTERIM_DISPATCH doit être false (§3.3).");
            }
            if (!memeLocalite && !interim) {
                throw new BusinessRuleException(
                        "Dispatch hors de votre localité : INTERIM_DISPATCH doit être true (§3.3).");
            }
        }
    }

    /** Localité d'un dossier via sa réception : réception → contrôleur réceptionnaire → localité. */
    private String resoudreLocaliteDossier(Integer idReception) {
        if (idReception == null) {
            return null;
        }
        return receptionRepository.findById(idReception)
                .map(r -> r.getImCtrlRecept())
                .filter(im -> im != null)
                .flatMap(controleurRepository::findById)
                .map(c -> c.getIdLocalite())
                .orElse(null);
    }
}
