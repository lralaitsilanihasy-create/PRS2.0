package cnm.prs.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.DossierDto;
import cnm.prs.entity.Controleur;
import cnm.prs.entity.Dossier;
import cnm.prs.entity.Ppm;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.enums.StatutDossier;
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
    private final DossierIntegriteService dossierIntegrite;

    public DossierService(DossierRepository repository, PpmRepository ppmRepository,
            ControleurDirectory controleurDirectory, NotificationService notificationService,
            DossierIntegriteService dossierIntegrite) {
        this.repository = repository;
        this.ppmRepository = ppmRepository;
        this.controleurDirectory = controleurDirectory;
        this.notificationService = notificationService;
        this.dossierIntegrite = dossierIntegrite;
    }

    /**
     * Liste des dossiers filtrée par le périmètre de visibilité de l'utilisateur (§1), et
     * <strong>optionnellement par statut</strong> ({@code ?statut=SOUMIS}, filtré côté serveur) :
     * Président / Administrateur voient tout ; les autres profils ne voient que les dossiers
     * de leur localité. La PRMP voit ses propres dossiers ({@code t_dossier.ID_PRMP} / PPM / marché).
     *
     * @param statut filtre serveur sur {@code t_dossier.STATUT} ; {@code null}/vide = tous statuts
     * @throws BadRequestException si {@code statut} est fourni mais n'est pas un statut connu (→ 400)
     */
    @Transactional(readOnly = true)
    public List<DossierDto> findAll(String statut) {
        String filtre = normaliserStatut(statut);
        ProfilUtilisateur profil = CurrentUser.profil().orElse(null);
        if (profil == ProfilUtilisateur.PRESIDENT || profil == ProfilUtilisateur.ADMINISTRATEUR) {
            return repository.findParStatut(filtre).stream().map(DossierMapper::toDto).toList();
        }
        if (profil == ProfilUtilisateur.PRMP) {
            String idPrmp = CurrentUser.ref().orElse(null);
            if (idPrmp == null || idPrmp.isBlank()) {
                return List.of();
            }
            return repository.findVisiblesPourPrmpEtStatut(idPrmp, filtre).stream().map(DossierMapper::toDto).toList();
        }
        String localite = CurrentUser.localite().orElse(null);
        if (localite == null || localite.isBlank()) {
            return List.of();
        }
        return repository.findVisiblesParLocaliteEtStatut(localite, filtre).stream().map(DossierMapper::toDto).toList();
    }

    /** Valide le filtre statut : {@code null}/vide accepté (= tous), sinon doit être un {@link StatutDossier}. */
    private String normaliserStatut(String statut) {
        if (statut == null || statut.isBlank()) {
            return null;
        }
        try {
            return StatutDossier.valueOf(statut).name();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Statut inconnu : « " + statut + " ». Valeurs admises : "
                    + java.util.Arrays.toString(StatutDossier.values()) + ".");
        }
    }

    @Transactional(readOnly = true)
    public DossierDto findById(Integer id) {
        Dossier entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + id));
        controlerVisibilite(id);
        return DossierMapper.toDto(entity);
    }

    /**
     * File « à réceptionner » (§3.4) : dossiers soumis ({@code SOUMIS}) et sans réception, de la
     * localité du contrôleur. Président/Administrateur voient toutes les localités.
     */
    @Transactional(readOnly = true)
    public List<DossierDto> aReceptionner() {
        ProfilUtilisateur profil = CurrentUser.profil().orElse(null);
        if (profil == ProfilUtilisateur.PRESIDENT || profil == ProfilUtilisateur.ADMINISTRATEUR) {
            return repository.findAReceptionner().stream().map(DossierMapper::toDto).toList();
        }
        String localite = CurrentUser.localite().filter(s -> !s.isBlank()).orElse(null);
        if (localite == null) {
            return List.of();
        }
        return repository.findAReceptionnerParLocalite(localite).stream().map(DossierMapper::toDto).toList();
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
        existing.setIdLocalite(dto.getIdLocalite());
        existing.setIdEntiteContract(dto.getIdEntiteContract());
        return DossierMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Dossier introuvable : " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Soumission officielle d'un dossier par la PRMP (§3.1, Module 03). <strong>Génère la
     * référence unique</strong> {@code REFE_DOSSIER} puis notifie le Secrétaire et le Chef de
     * commission de la localité qu'un dossier est en attente de réception.
     *
     * <p><strong>Localité</strong> : celle du dossier (dérivée de l'entité contractante choisie à la
     * saisie, §1), sinon celle de son PPM ({@code Ppm.idLocalite}) ; il n'y a plus de repli sur une
     * localité « propre » de la PRMP (la PRMP n'en a pas). <strong>Appartenance</strong> : si le dossier est rattaché à
     * un PPM, il doit appartenir à la PRMP courante (sinon 403) ; un dossier sans aucun PPM n'a pas
     * de lien d'appartenance en base et est soumis par la PRMP authentifiée. L'exercice de la
     * référence provient du PPM, ou de l'année courante à défaut.</p>
     *
     * @throws ResourceNotFoundException si le dossier n'existe pas
     * @throws AccessDeniedException     si le dossier (rattaché à un PPM) n'appartient pas à la PRMP courante
     * @throws BusinessRuleException     si le dossier a déjà été soumis (référence déjà générée) → 409
     * @throws BadRequestException       si aucune localité ne peut être déterminée (ni dossier, ni PPM) → 400
     */
    public DossierDto soumettre(Integer idDossier) {
        Dossier dossier = repository.findById(idDossier)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + idDossier));

        CurrentUser.ref().filter(s -> !s.isBlank())
                .orElseThrow(() -> new AccessDeniedException("Utilisateur PRMP non identifié."));
        // Propriété : seule la PRMP propriétaire (t_dossier.ID_PRMP) peut soumettre.
        dossierIntegrite.exigerProprietaire(dossier);
        // Cycle de vie : seul un BROUILLON est soumissible → SOUMIS (pas de re-soumission).
        if (!StatutDossier.BROUILLON.name().equals(dossier.getStatut())) {
            throw new BusinessRuleException(
                    "Dossier non soumissible : statut « " + dossier.getStatut() + " » (attendu BROUILLON).");
        }
        // Cohérence type↔contenu (PPM ⇒ a un PPM ; DAO/MAOO ⇒ pas de PPM).
        dossierIntegrite.validerCoherenceAvantSoumission(dossier);

        // Localité : celle du dossier (dérivée de l'entité à la saisie), sinon celle du PPM. Plus de repli PRMP.
        List<Ppm> ppms = ppmRepository.findByIdDossier(idDossier);
        String localite = dossier.getIdLocalite();
        if (localite == null || localite.isBlank()) {
            localite = ppms.stream().map(Ppm::getIdLocalite).filter(l -> l != null && !l.isBlank()).findFirst()
                    .orElse(null);
        }
        if (localite == null) {
            throw new BadRequestException(
                    "Localité indéterminée : elle provient de l'entité contractante choisie à la saisie (§1, §3.1).");
        }
        int exercice = ppms.stream().map(Ppm::getExercice).filter(Objects::nonNull)
                .findFirst().orElse(LocalDate.now().getYear());

        String reference = "CNM-" + localite + "-" + exercice + "-" + String.format("%06d", idDossier);
        dossier.setRefeDossier(reference);
        dossier.setIdLocalite(localite);             // propage la localité (§C) → visible par le Secrétaire
        dossier.setStatut(StatutDossier.SOUMIS.name());
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
