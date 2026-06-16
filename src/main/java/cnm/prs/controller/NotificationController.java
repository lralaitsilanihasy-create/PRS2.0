package cnm.prs.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import cnm.prs.dto.NotificationDto;
import cnm.prs.service.NotificationService;

/**
 * Notifications (table {@code t_notification}).
 *
 * <p><strong>Mes notifications</strong> ({@code /mes}, {@code /mes/non-lues/count},
 * {@code /{id}/lu}, {@code /lire-tout}) sont scopées à l'utilisateur courant. La <strong>liste
 * globale</strong> et le CRUD sont réservés à l'<strong>Administrateur</strong> (supervision).</p>
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    // --- Mes notifications (scopées à l'utilisateur courant) ---

    @GetMapping("/mes")
    public List<NotificationDto> mes(@RequestParam(required = false) Boolean lu) {
        return service.mes(lu);
    }

    @GetMapping("/mes/non-lues/count")
    public Map<String, Long> compterNonLues() {
        return Map.of("nonLues", service.compterNonLues());
    }

    @PostMapping("/{id}/lu")
    public NotificationDto marquerLu(@PathVariable Integer id) {
        return service.marquerLu(id);
    }

    @PostMapping("/lire-tout")
    public Map<String, Integer> lireTout() {
        return Map.of("traitees", service.marquerToutLu());
    }

    // --- Supervision (Administrateur) ---

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public List<NotificationDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public NotificationDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<NotificationDto> create(@Valid @RequestBody NotificationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public NotificationDto update(@PathVariable Integer id, @Valid @RequestBody NotificationDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
