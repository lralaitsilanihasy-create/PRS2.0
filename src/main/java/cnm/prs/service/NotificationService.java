package cnm.prs.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.NotificationDto;
import cnm.prs.entity.Notification;
import cnm.prs.entity.Prmp;
import cnm.prs.enums.TypeActeur;
import cnm.prs.enums.TypeNotification;
import cnm.prs.enums.TypeObjet;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.NotificationMapper;
import cnm.prs.repository.NotificationRepository;
import cnm.prs.repository.PrmpRepository;
import cnm.prs.security.CurrentUser;

/**
 * Logique métier pour {@link Notification} : émission (comportement {@code [Auto]}) et
 * consultation <strong>scopée</strong> à l'utilisateur courant (chacun ne voit que ses
 * notifications). La liste globale reste réservée à l'Administrateur (supervision).
 */
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository repository;
    private final PrmpRepository prmpRepository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository repository, PrmpRepository prmpRepository,
            EmailService emailService) {
        this.repository = repository;
        this.prmpRepository = prmpRepository;
        this.emailService = emailService;
    }

    /** Liste globale (supervision) — réservée à l'Administrateur via le contrôleur. */
    @Transactional(readOnly = true)
    public List<NotificationDto> findAll() {
        return repository.findAll().stream().map(NotificationMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public NotificationDto findById(Integer id) {
        Notification entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable : " + id));
        return NotificationMapper.toDto(entity);
    }

    public NotificationDto create(NotificationDto dto) {
        Notification entity = NotificationMapper.toEntity(dto);
        return NotificationMapper.toDto(repository.save(entity));
    }

    public NotificationDto update(Integer id, NotificationDto dto) {
        Notification existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable : " + id));
        existing.setIdDossier(dto.getIdDossier());
        existing.setTypeNotif(dto.getTypeNotif());
        existing.setDestinataireIm(dto.getDestinataireIm());
        existing.setDestinataireEmail(dto.getDestinataireEmail());
        existing.setTitre(dto.getTitre());
        existing.setCorps(dto.getCorps());
        existing.setDateEnvoi(dto.getDateEnvoi());
        existing.setLu(dto.getLu());
        existing.setDateLecture(dto.getDateLecture());
        existing.setCanal(dto.getCanal());
        existing.setDestinataireRef(dto.getDestinataireRef());
        existing.setDestinataireType(dto.getDestinataireType());
        existing.setIdObjet(dto.getIdObjet());
        existing.setTypeObjet(dto.getTypeObjet());
        return NotificationMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Notification introuvable : " + id);
        }
        repository.deleteById(id);
    }

    // ------------------------------------------------------------------
    // Émission (comportement [Auto], transactionnel avec l'événement déclencheur)
    // ------------------------------------------------------------------

    /**
     * Émet une notification (signature historique). Le destinataire est un contrôleur
     * ({@code destinataireIm}) ou une PRMP ({@code destinataireEmail}) ; la clé unifiée
     * {@code DESTINATAIRE_REF}/{@code TYPE} et l'objet sont déduits (dossier).
     */
    public Notification emettre(Integer idDossier, TypeNotification type, String destinataireIm,
            String destinataireEmail, String titre, String corps) {
        String ref = destinataireIm;
        String destType = destinataireIm != null ? TypeActeur.CONTROLEUR.name() : null;
        Integer idObjet = idDossier;
        String typeObjet = idDossier != null ? TypeObjet.DOSSIER.name() : null;
        return creer(type, ref, destType, destinataireIm, destinataireEmail, idObjet, typeObjet, idDossier, titre, corps);
    }

    /** Émet une notification vers un <strong>contrôleur</strong>, avec objet métier explicite. */
    public Notification emettreControleur(TypeNotification type, String im, String email,
            Integer idObjet, TypeObjet typeObjet, Integer idDossier, String titre, String corps) {
        return creer(type, im, TypeActeur.CONTROLEUR.name(), im, email, idObjet,
                typeObjet != null ? typeObjet.name() : null, idDossier, titre, corps);
    }

    /** Émet une notification vers une <strong>PRMP</strong> (clé {@code ref}=idPrmp), avec objet explicite. */
    public Notification emettrePrmp(TypeNotification type, String idPrmp, String email,
            Integer idObjet, TypeObjet typeObjet, Integer idDossier, String titre, String corps) {
        return creer(type, idPrmp, TypeActeur.PRMP.name(), null, email, idObjet,
                typeObjet != null ? typeObjet.name() : null, idDossier, titre, corps);
    }

    private Notification creer(TypeNotification type, String ref, String destType, String im, String email,
            Integer idObjet, String typeObjet, Integer idDossier, String titre, String corps) {
        Notification n = new Notification();
        n.setIdNotification(repository.findMaxId() + 1);
        n.setIdDossier(idDossier);
        n.setTypeNotif(type.name());
        n.setDestinataireRef(ref);
        n.setDestinataireType(destType);
        n.setDestinataireIm(im);
        n.setDestinataireEmail(email);
        n.setIdObjet(idObjet);
        n.setTypeObjet(typeObjet);
        n.setTitre(titre);
        n.setCorps(corps);
        n.setDateEnvoi(LocalDateTime.now());
        n.setLu(false);
        n.setCanal("SYSTEME");
        Notification saved = repository.save(n);
        // Diffusion e-mail (asynchrone, sans effet si désactivée ou destinataire absent).
        emailService.envoyer(email, titre, corps);
        return saved;
    }

    // ------------------------------------------------------------------
    // Consultation scopée à l'utilisateur courant
    // ------------------------------------------------------------------

    /** Mes notifications (triées récentes d'abord) ; {@code lu} filtre optionnel (true/false/null). */
    @Transactional(readOnly = true)
    public List<NotificationDto> mes(Boolean lu) {
        List<Notification> list = mesNotifications();
        if (lu != null) {
            list = list.stream().filter(n -> lu.equals(n.getLu())).toList();
        }
        return list.stream().map(NotificationMapper::toDto).toList();
    }

    /** Nombre de mes notifications non lues. */
    @Transactional(readOnly = true)
    public long compterNonLues() {
        return mesNotifications().stream().filter(n -> !Boolean.TRUE.equals(n.getLu())).count();
    }

    /** Marque une de mes notifications comme lue (refus si elle ne m'appartient pas). */
    public NotificationDto marquerLu(Integer id) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable : " + id));
        if (!estPourMoi(n)) {
            throw new AccessDeniedException("Cette notification ne vous appartient pas.");
        }
        n.setLu(true);
        n.setDateLecture(LocalDateTime.now());
        return NotificationMapper.toDto(repository.save(n));
    }

    /** Marque toutes mes notifications non lues comme lues ; renvoie le nombre traité. */
    public int marquerToutLu() {
        List<Notification> nonLues = mesNotifications().stream()
                .filter(n -> !Boolean.TRUE.equals(n.getLu())).toList();
        LocalDateTime maintenant = LocalDateTime.now();
        for (Notification n : nonLues) {
            n.setLu(true);
            n.setDateLecture(maintenant);
            repository.save(n);
        }
        return nonLues.size();
    }

    private List<Notification> mesNotifications() {
        String ref = CurrentUser.ref().filter(s -> !s.isBlank()).orElse(null);
        if (ref == null) {
            return List.of();
        }
        if (TypeActeur.PRMP.name().equals(CurrentUser.acteurType().orElse(null))) {
            String email = prmpRepository.findById(ref).map(Prmp::getEmailPrmp).orElse(null);
            return repository.findPourPrmp(ref, email);
        }
        return repository.findPourControleur(ref);
    }

    private boolean estPourMoi(Notification n) {
        String ref = CurrentUser.ref().filter(s -> !s.isBlank()).orElse(null);
        if (ref == null) {
            return false;
        }
        if (TypeActeur.PRMP.name().equals(CurrentUser.acteurType().orElse(null))) {
            boolean parRef = ref.equals(n.getDestinataireRef()) && TypeActeur.PRMP.name().equals(n.getDestinataireType());
            String email = prmpRepository.findById(ref).map(Prmp::getEmailPrmp).orElse(null);
            boolean parEmail = email != null && email.equals(n.getDestinataireEmail());
            return parRef || parEmail;
        }
        return ref.equals(n.getDestinataireRef())
                && TypeActeur.CONTROLEUR.name().equals(n.getDestinataireType());
    }
}
