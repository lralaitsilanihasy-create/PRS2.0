package cnm.prs.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.NotificationDto;
import cnm.prs.entity.Notification;
import cnm.prs.enums.TypeNotification;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.NotificationMapper;
import cnm.prs.repository.NotificationRepository;

/**
 * Logique métier pour {@link Notification}.
 */
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository repository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

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
        return NotificationMapper.toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Notification introuvable : " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Émet une notification système (comportement {@code [Auto]}). Génère la PK assignée,
     * horodate l'envoi et marque la notification comme non lue.
     *
     * @param idDossier        dossier concerné (peut être {@code null})
     * @param type             type de notification
     * @param destinataireIm   matricule du contrôleur destinataire, ou {@code null}
     * @param destinataireEmail e-mail destinataire (ex. PRMP), ou {@code null}
     * @param titre            titre court
     * @param corps            corps du message
     * @return la notification persistée
     */
    public Notification emettre(Integer idDossier, TypeNotification type, String destinataireIm,
            String destinataireEmail, String titre, String corps) {
        Notification n = new Notification();
        n.setIdNotification(repository.findMaxId() + 1);
        n.setIdDossier(idDossier);
        n.setTypeNotif(type.name());
        n.setDestinataireIm(destinataireIm);
        n.setDestinataireEmail(destinataireEmail);
        n.setTitre(titre);
        n.setCorps(corps);
        n.setDateEnvoi(LocalDateTime.now());
        n.setLu(false);
        n.setCanal("SYSTEME");
        Notification saved = repository.save(n);
        // Diffusion e-mail (asynchrone, sans effet si désactivée ou destinataire absent).
        emailService.envoyer(destinataireEmail, titre, corps);
        return saved;
    }
}
