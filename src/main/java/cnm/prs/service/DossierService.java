package cnm.prs.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.DossierDto;
import cnm.prs.entity.Controleur;
import cnm.prs.entity.Dossier;
import cnm.prs.entity.Ppm;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.enums.TypeNotification;
import cnm.prs.exception.BadRequestException;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.DossierMapper;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.PpmRepository;
import cnm.prs.security.CurrentUser;

/**
 * Logique métier pour {@link Dossier}.
 */
@Service
@Transactional
public class DossierService {

    private final DossierRepository repository;
    private final PpmRepository ppmRepository;
    private final ControleurDirectory controleurDirectory;
    private final NotificationService notificationService;

    public DossierService(DossierRepository repository, PpmRepository ppmRepository,
            ControleurDirectory controleurDirectory, NotificationService notificationService) {
        this.repository = repository;
        this.ppmRepository = ppmRepository;
        this.controleurDirectory = controleurDirectory;
        this.notificationService = notificationService;
    }

    /**
     * Liste des dossiers filtrée par le périmètre de visibilité de l'utilisateur (§1) :
     * Président / Administrateur voient tout ; les autres profils ne voient que les dossiers
     * de leur localité. La PRMP ne passe pas par ce périmètre (visibilité « ses propres
     * dossiers », non couverte par ce filtre localité).
     */
    @Transactional(readOnly = true)
    public List<DossierDto> findAll() {
        ProfilUtilisateur profil = CurrentUser.profil().orElse(null);
        if (profil == ProfilUtilisateur.PRESIDENT || profil == ProfilUtilisateur.ADMINISTRATEUR) {
            return repository.findAll().stream().map(DossierMapper::toDto).toList();
        }
        if (profil == ProfilUtilisateur.PRMP) {
            String idPrmp = CurrentUser.ref().orElse(null);
            if (idPrmp == null || idPrmp.isBlank()) {
                return List.of();
            }
            return repository.findVisiblesPourPrmp(idPrmp).stream().map(DossierMapper::toDto).toList();
        }
        String localite = CurrentUser.localite().orElse(null);
        if (localite == null || localite.isBlank()) {
            return List.of();
        }
        return repository.findVisiblesParLocalite(localite).stream().map(DossierMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public DossierDto findById(Integer id) {
        Dossier entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + id));
        controlerVisibilite(id);
        return DossierMapper.toDto(entity);
    }

    /** Vérifie que le dossier est dans le périmètre de visibilité de l'utilisateur (§1). */
    private void controlerVisibilite(Integer idDossier) {
        ProfilUtilisateur profil = CurrentUser.profil().orElse(null);
        if (profil == ProfilUtilisateur.PRESIDENT || profil == ProfilUtilisateur.ADMINISTRATEUR) {
            return;
        }
        if (profil == ProfilUtilisateur.PRMP) {
            String idPrmp = CurrentUser.ref().orElse(null);
            if (idPrmp != null && !idPrmp.isBlank() && repository.existsVisiblePourPrmp(idDossier, idPrmp)) {
                return;
            }
            throw new AccessDeniedException("Dossier hors de votre périmètre de visibilité (§1).");
        }
        String localite = CurrentUser.localite().orElse(null);
        if (localite == null || localite.isBlank() || !repository.existsDansLocalite(idDossier, localite)) {
            throw new AccessDeniedException("Dossier hors de votre périmètre de visibilité (§1).");
        }
    }

    public DossierDto create(DossierDto dto) {
        Dossier entity = DossierMapper.toEntity(dto);
        return DossierMapper.toDto(repository.save(entity));
    }

    public DossierDto update(Integer id, DossierDto dto) {
        Dossier existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + id));
        existing.setIdTypeDossier(dto.getIdTypeDossier());
        existing.setIdDossierParent(dto.getIdDossierParent());
        existing.setRefeDossier(dto.getRefeDossier());
        existing.setDateRef(dto.getDateRef());
        existing.setStatut(dto.getStatut());
        return DossierMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Dossier introuvable : " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Soumission officielle d'un dossier par la PRMP (§3.1, Module 03). Vérifie que le dossier
     * appartient à la PRMP courante (via son PPM), <strong>génère la référence unique</strong>
     * {@code REFE_DOSSIER}, puis notifie le Secrétaire et le Chef de commission de la localité
     * (résolue via {@code Ppm.idLocalite}) qu'un dossier est en attente de réception.
     *
     * @throws ResourceNotFoundException si le dossier n'existe pas
     * @throws AccessDeniedException     si le dossier n'appartient pas à la PRMP courante
     * @throws BusinessRuleException     si le dossier a déjà été soumis (référence déjà générée) → 409
     * @throws BadRequestException       si aucun PPM localisé n'est rattaché au dossier → 400
     */
    public DossierDto soumettre(Integer idDossier) {
        Dossier dossier = repository.findById(idDossier)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + idDossier));

        String idPrmp = CurrentUser.ref().filter(s -> !s.isBlank())
                .orElseThrow(() -> new AccessDeniedException("Utilisateur PRMP non identifié."));
        if (!repository.existsVisiblePourPrmp(idDossier, idPrmp)) {
            throw new AccessDeniedException("Ce dossier ne fait pas partie de vos dossiers (§3.1).");
        }
        if (dossier.getRefeDossier() != null && !dossier.getRefeDossier().isBlank()) {
            throw new BusinessRuleException(
                    "Dossier déjà soumis (référence « " + dossier.getRefeDossier() + " »).");
        }

        Ppm ppm = ppmRepository.findByIdDossier(idDossier).stream().findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Rattachez un PPM au dossier avant de le soumettre (§3.1)."));
        String localite = ppm.getIdLocalite();
        if (localite == null || localite.isBlank()) {
            throw new BadRequestException(
                    "Le PPM du dossier n'a pas de localité : soumission impossible (§3.1).");
        }

        String reference = "CNM-" + localite + "-" + ppm.getExercice()
                + "-" + String.format("%06d", idDossier);
        dossier.setRefeDossier(reference);
        if (dossier.getDateRef() == null) {
            dossier.setDateRef(LocalDate.now());
        }
        repository.save(dossier);

        notifierSoumission(dossier, localite);
        return DossierMapper.toDto(dossier);
    }

    /** Notifie le Secrétaire et le CC de la localité qu'un dossier est soumis et attend réception. */
    private void notifierSoumission(Dossier dossier, String localite) {
        String titre = "Nouveau dossier soumis à réceptionner";
        String corps = "Le dossier " + dossier.getIdDossier() + " (réf. " + dossier.getRefeDossier()
                + ") a été soumis et attend sa réception dans la localité " + localite + ".";
        for (Controleur sec : controleurDirectory.secretaires(localite)) {
            notificationService.emettre(dossier.getIdDossier(), TypeNotification.DOSSIER_SOUMIS,
                    sec.getImControleur(), sec.getEmailCont(), titre, corps);
        }
        for (Controleur cc : controleurDirectory.chefsCommission(localite)) {
            notificationService.emettre(dossier.getIdDossier(), TypeNotification.DOSSIER_SOUMIS,
                    cc.getImControleur(), cc.getEmailCont(), titre, corps);
        }
    }
}
