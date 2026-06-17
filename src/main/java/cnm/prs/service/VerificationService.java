package cnm.prs.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.VerificationDto;
import cnm.prs.entity.Controleur;
import cnm.prs.entity.Dossier;
import cnm.prs.entity.Verification;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.enums.StatutDossier;
import cnm.prs.enums.StatutPv;
import cnm.prs.enums.TypeNotification;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.VerificationMapper;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.PvExamenRepository;
import cnm.prs.repository.ReceptionRepository;
import cnm.prs.repository.VerificationRepository;
import cnm.prs.security.CurrentUser;
import cnm.prs.security.Visibilite;

/**
 * Logique métier pour {@link Verification}.
 */
@Service
@Transactional
public class VerificationService {

    private final VerificationRepository repository;
    private final ReceptionRepository receptionRepository;
    private final DossierRepository dossierRepository;
    private final PvExamenRepository pvExamenRepository;
    private final ControleurDirectory controleurDirectory;
    private final NotificationService notificationService;

    public VerificationService(VerificationRepository repository, ReceptionRepository receptionRepository,
            DossierRepository dossierRepository, PvExamenRepository pvExamenRepository,
            ControleurDirectory controleurDirectory, NotificationService notificationService) {
        this.repository = repository;
        this.receptionRepository = receptionRepository;
        this.dossierRepository = dossierRepository;
        this.pvExamenRepository = pvExamenRepository;
        this.controleurDirectory = controleurDirectory;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<VerificationDto> findAll() {
        return Visibilite.filtrer(repository::findAll, repository::findVisiblesParLocalite)
                .stream().map(VerificationMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public VerificationDto findById(Integer id) {
        Verification entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Verification introuvable : " + id));
        Visibilite.controler(loc -> repository.existsDansLocalite(id, loc));
        return VerificationMapper.toDto(entity);
    }

    public VerificationDto create(VerificationDto dto) {
        Visibilite.exigerLocalite(receptionRepository.findLocaliteById(dto.getIdReception()));
        exigerVerificateur();                  // strict profil VERIFICATEUR (⚠️ règle ajoutée)
        exigerPvSigne(dto.getIdPv());
        exigerCibleVerifiable(dto.getIdPv());  // avis FAVR + dossier non clos (⚠️ règle ajoutée)
        Verification entity = VerificationMapper.toEntity(dto);
        entity.setIdVerification(null);                    // ID auto-généré (D6)
        entity.setImCtrlVerif(verificateurAuthentifie());  // identité = JWT, jamais le corps
        entity.setDateVerif(LocalDate.now());              // date serveur
        Verification saved = repository.save(entity);
        declencherCloture(saved);
        return VerificationMapper.toDto(saved);
    }

    /**
     * Précondition du circuit (§3.6) : la vérification ne porte que sur un PV au statut
     * {@link StatutPv#SIGNE} (« Travaille uniquement sur PV au statut SIGNE »).
     */
    private void exigerPvSigne(Integer idPv) {
        String statut = idPv == null ? null : pvExamenRepository.findStatutById(idPv).orElse(null);
        if (!StatutPv.SIGNE.name().equals(statut)) {
            throw new BusinessRuleException(
                    "Vérification impossible : le PV doit être au statut SIGNE (§3.6), "
                            + "statut actuel « " + statut + " ».");
        }
    }

    /** Seul le profil Contrôleur vérificateur peut vérifier (§3.6, ⚠️ règle ajoutée — pas de délégation). */
    private void exigerVerificateur() {
        if (CurrentUser.profil().orElse(null) != ProfilUtilisateur.VERIFICATEUR) {
            throw new AccessDeniedException("Seul un Contrôleur vérificateur peut vérifier (§3.6).");
        }
    }

    /** Matricule du vérificateur authentifié (principal JWT). */
    private String verificateurAuthentifie() {
        return CurrentUser.ref().filter(s -> !s.isBlank())
                .orElseThrow(() -> new AccessDeniedException("Vérificateur non identifié."));
    }

    /**
     * ⚠️ Règle ajoutée — la vérification ne porte que sur un PV d'avis « favorable avec réserves »
     * (FAVR) dont le dossier n'est pas clôturé ; sinon 409.
     */
    private void exigerCibleVerifiable(Integer idPv) {
        String avis = idPv == null ? null : pvExamenRepository.findIdAvisByPv(idPv).orElse(null);
        if (!"FAVR".equals(avis)) {
            throw new BusinessRuleException(
                    "Vérification réservée aux PV « favorable avec réserves » (FAVR) ; avis actuel « " + avis + " ».");
        }
        Integer idDossier = pvExamenRepository.findIdDossierByPv(idPv).orElse(null);
        String statut = idDossier == null ? null
                : dossierRepository.findById(idDossier).map(Dossier::getStatut).orElse(null);
        if (StatutDossier.CLOTURE.name().equals(statut)) {
            throw new BusinessRuleException("Vérification impossible : le dossier est déjà clôturé.");
        }
    }

    public VerificationDto update(Integer id, VerificationDto dto) {
        Visibilite.exigerLocalite(receptionRepository.findLocaliteById(dto.getIdReception()));
        exigerVerificateur();
        exigerPvSigne(dto.getIdPv());
        exigerCibleVerifiable(dto.getIdPv());
        Verification existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Verification introuvable : " + id));
        existing.setIdReception(dto.getIdReception());
        existing.setIdPv(dto.getIdPv());
        existing.setImCtrlVerif(verificateurAuthentifie());  // identité = JWT
        existing.setDateVerif(LocalDate.now());              // date serveur
        existing.setObservation(dto.getObservation());
        existing.setObsLevees(dto.getObsLevees());
        Verification saved = repository.save(existing);
        declencherCloture(saved);
        return VerificationMapper.toDto(saved);
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Verification introuvable : " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Comportement {@code [Auto]} (§2.8 / §3.6) : lorsque les observations sont levées
     * ({@code OBS_LEVEES = true}), le dossier rattaché passe automatiquement au statut
     * {@code CLOTURE}. Le dossier est atteint via la réception
     * ({@code idReception → t_reception.ID_DOSSIER}).
     *
     * <p>NB : le cas {@code OBS_LEVEES = false} (« nouveau passage, NUM_PASSAGE + 1 »,
     * §3.6) n'est pas automatisé ici — la valeur de TYPE_PASSAGE d'un passage de retour
     * n'est pas spécifiée dans les règles (ambiguïté signalée au Lot 2).</p>
     */
    private void declencherCloture(Verification verification) {
        if (!Boolean.TRUE.equals(verification.getObsLevees()) || verification.getIdReception() == null) {
            return;
        }
        receptionRepository.findById(verification.getIdReception()).ifPresent(reception -> {
            if (reception.getIdDossier() == null) {
                return;
            }
            dossierRepository.findById(reception.getIdDossier()).ifPresent(dossier -> {
                // ⚠️ Règle ajoutée — la clôture par observations levées ne s'applique qu'à un dossier
                // EN_VERIFICATION (les autres statuts ne sont pas réécrits).
                if (StatutDossier.EN_VERIFICATION.name().equals(dossier.getStatut())) {
                    dossier.setStatut(StatutDossier.CLOTURE.name());
                    dossierRepository.save(dossier);
                    notifierClotureEligible(dossier.getIdDossier());
                }
            });
        });
    }

    /**
     * Alerte de clôture éligible (§3.7) : un dossier conforme clôturé est notifié au(x)
     * Chargé(s) de publication pour mise en ligne sur le portail.
     */
    private void notifierClotureEligible(Integer idDossier) {
        String titre = "Dossier clôturé éligible à publication";
        String corps = "Le dossier " + idDossier + " est clôturé conforme et éligible à publication.";
        for (Controleur charge : controleurDirectory.chargesPublication()) {
            notificationService.emettre(idDossier, TypeNotification.CLOTURE_ELIGIBLE,
                    charge.getImControleur(), charge.getEmailCont(), titre, corps);
        }
    }
}
