package cnm.prs.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.PpmDto;
import cnm.prs.entity.Ppm;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.PpmMapper;
import cnm.prs.repository.PpmRepository;
import cnm.prs.security.CurrentUser;
import cnm.prs.security.Visibilite;

/**
 * Logique métier pour {@link Ppm}.
 */
@Service
@Transactional
public class PpmService {

    private final PpmRepository repository;
    private final DossierIntegriteService dossierIntegrite;

    public PpmService(PpmRepository repository, DossierIntegriteService dossierIntegrite) {
        this.repository = repository;
        this.dossierIntegrite = dossierIntegrite;
    }

    /**
     * Liste des PPM <strong>scopée au périmètre de l'appelant</strong> (§1, §3.1) — jamais la table
     * entière : Président/Administrateur voient tout ; la PRMP ne voit que <strong>les siens</strong>
     * ({@code t_ppm.ID_PRMP}) ; les contrôleurs ne voient que ceux de <strong>leur localité</strong>
     * (dossier non brouillon) ; tout autre profil (ou sans localité) → liste vide.
     */
    @Transactional(readOnly = true)
    public List<PpmDto> findAll() {
        if (Visibilite.voitTout()) {
            return repository.findAll().stream().map(PpmMapper::toDto).toList();
        }
        if (Visibilite.estPrmp()) {
            String idPrmp = CurrentUser.ref().filter(s -> !s.isBlank()).orElse(null);
            return idPrmp == null ? List.of()
                    : repository.findByIdPrmp(idPrmp).stream().map(PpmMapper::toDto).toList();
        }
        return Visibilite.localite()
                .map(loc -> repository.findVisiblesParLocalite(loc).stream().map(PpmMapper::toDto).toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public PpmDto findById(Integer id) {
        Ppm entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ppm introuvable : " + id));
        controlerVisibilite(entity);
        return PpmMapper.toDto(entity);
    }

    /** Vérifie que le PPM est dans le périmètre de l'appelant (§1, §3.1) — sinon 403. */
    private void controlerVisibilite(Ppm ppm) {
        if (Visibilite.voitTout()) {
            return;
        }
        if (CurrentUser.profil().orElse(null) == ProfilUtilisateur.PRMP) {
            String idPrmp = CurrentUser.ref().filter(s -> !s.isBlank()).orElse(null);
            if (idPrmp != null && idPrmp.equals(ppm.getIdPrmp())) {
                return;
            }
            throw new AccessDeniedException("PPM hors de votre périmètre (§3.1).");
        }
        boolean ok = Visibilite.localite()
                .map(loc -> repository.existsVisibleParLocalite(ppm.getIdPpm(), loc)).orElse(false);
        if (!ok) {
            throw new AccessDeniedException("PPM hors de votre périmètre de visibilité (§1).");
        }
    }

    public PpmDto create(PpmDto dto) {
        // Le PPM ne se rattache qu'à un dossier de type PPM, en brouillon, et propriété de la PRMP courante.
        dossierIntegrite.exigerBrouillonModifiable(dto.getIdDossier());
        dossierIntegrite.exigerTypePpm(dto.getIdDossier());
        Ppm entity = PpmMapper.toEntity(dto);
        return PpmMapper.toDto(repository.save(entity));
    }

    public PpmDto update(Integer id, PpmDto dto) {
        Ppm existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ppm introuvable : " + id));
        dossierIntegrite.exigerBrouillonModifiable(existing.getIdDossier());
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
