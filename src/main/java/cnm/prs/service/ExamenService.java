package cnm.prs.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.ExamenDto;
import cnm.prs.entity.Examen;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.enums.StatutDossier;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.ExamenMapper;
import cnm.prs.repository.DispatchRepository;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.ExamenRepository;
import cnm.prs.security.CurrentUser;
import cnm.prs.security.Visibilite;

/**
 * Logique métier pour {@link Examen}.
 */
@Service
@Transactional
public class ExamenService {

    private final ExamenRepository repository;
    private final DispatchRepository dispatchRepository;
    private final DossierRepository dossierRepository;

    public ExamenService(ExamenRepository repository, DispatchRepository dispatchRepository,
            DossierRepository dossierRepository) {
        this.repository = repository;
        this.dispatchRepository = dispatchRepository;
        this.dossierRepository = dossierRepository;
    }

    @Transactional(readOnly = true)
    public List<ExamenDto> findAll() {
        return Visibilite.filtrer(repository::findAll, repository::findVisiblesParLocalite)
                .stream().map(ExamenMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ExamenDto findById(Integer id) {
        Examen entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examen introuvable : " + id));
        Visibilite.controler(loc -> repository.existsDansLocalite(id, loc));
        return ExamenMapper.toDto(entity);
    }

    public ExamenDto create(ExamenDto dto) {
        Visibilite.exigerLocalite(dispatchRepository.findLocaliteById(dto.getIdDispatch()));
        exigerMembreAttributaire(dto.getIdDispatch());
        exigerDossierDispatche(dto.getIdDispatch());
        Examen entity = ExamenMapper.toEntity(dto);
        Examen saved = repository.save(entity);
        // [Auto] Le dossier avance DISPATCHE → EXAMINE (il quitte « à examiner »), même transaction.
        avancerDossierVersExamine(dto.getIdDispatch());
        return ExamenMapper.toDto(saved);
    }

    /**
     * [Auto] À la création d'un examen, le dossier passe de {@link StatutDossier#DISPATCHE} à
     * {@link StatutDossier#EXAMINE} (même transaction). Idempotent : on ne réécrit que si le dossier
     * est bien {@code DISPATCHE} (jamais un dossier déjà examiné/signé/clôturé).
     */
    private void avancerDossierVersExamine(Integer idDispatch) {
        Integer idDossier = idDispatch == null ? null
                : dossierRepository.findIdDossierByDispatch(idDispatch).orElse(null);
        if (idDossier == null) {
            return;
        }
        dossierRepository.findById(idDossier).ifPresent(d -> {
            if (StatutDossier.DISPATCHE.name().equals(d.getStatut())) {
                d.setStatut(StatutDossier.EXAMINE.name());
                dossierRepository.save(d);
            }
        });
    }

    /**
     * Autorisation (§2.4, §3.5) : un Membre <strong>titulaire</strong> n'examine que les dossiers qui
     * lui sont <strong>attribués</strong> ({@code Dispatch.imCtrlMembre}). Un CC / Président instruisant
     * <strong>par délégation</strong> (profil ≠ MEMBRE, déjà contrôlé en localité) reste autorisé.
     *
     * @throws AccessDeniedException (→ 403) si un Membre tente d'examiner le dossier d'un autre Membre
     */
    private void exigerMembreAttributaire(Integer idDispatch) {
        if (CurrentUser.profil().orElse(null) != ProfilUtilisateur.MEMBRE) {
            return; // délégation (CC/Président) : autorisé, localité déjà vérifiée
        }
        String attributaire = idDispatch == null ? null
                : dispatchRepository.findImCtrlMembreById(idDispatch).orElse(null);
        String moi = CurrentUser.ref().orElse(null);
        if (attributaire == null || !attributaire.equals(moi)) {
            throw new AccessDeniedException(
                    "Examen réservé au Membre attributaire du dispatch (§2.4) : vous n'êtes pas l'attributaire.");
        }
    }

    /**
     * Précondition du circuit (§2.3 → §2.4) : on n'examine qu'un dossier <strong>déjà dispatché</strong>
     * (statut {@link StatutDossier#DISPATCHE}). Le dispatch précède l'examen et fait passer le dossier
     * de PRET_DISPATCH à DISPATCHE ; un dossier non dispatché (ou clôturé/retiré) est refusé.
     */
    private void exigerDossierDispatche(Integer idDispatch) {
        String statut = idDispatch == null ? null
                : dossierRepository.findStatutByDispatch(idDispatch).orElse(null);
        if (!StatutDossier.DISPATCHE.name().equals(statut)) {
            throw new BusinessRuleException(
                    "Examen impossible : le dossier doit avoir été dispatché (statut DISPATCHE) (§2.4), "
                            + "statut actuel « " + statut + " ».");
        }
    }

    public ExamenDto update(Integer id, ExamenDto dto) {
        Visibilite.exigerLocalite(dispatchRepository.findLocaliteById(dto.getIdDispatch()));
        exigerExamenModifiable(id);
        Examen existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examen introuvable : " + id));
        existing.setIdDispatch(dto.getIdDispatch());
        existing.setImCtrlMembre(dto.getImCtrlMembre());
        existing.setDateExamen(dto.getDateExamen());
        return ExamenMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Examen introuvable : " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Verrou d'édition de l'examen (§2.6) : modifiable uniquement tant que le dossier est
     * {@link StatutDossier#EXAMINE} (avant signature du PV) ; dès {@link StatutDossier#PV_SIGNE}
     * l'examen est <strong>définitif</strong> → toute modification est refusée (409).
     */
    private void exigerExamenModifiable(Integer idExamen) {
        String statut = idExamen == null ? null
                : repository.findStatutDossierByExamen(idExamen).orElse(null);
        if (!StatutDossier.EXAMINE.name().equals(statut)) {
            throw new BusinessRuleException(
                    "Examen verrouillé : modification possible uniquement tant que le dossier est EXAMINE "
                            + "(statut actuel « " + statut + " », examen définitif après signature du PV, §2.6).");
        }
    }
}
