package cnm.prs.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;

import cnm.prs.dto.PublicationDto;
import cnm.prs.dto.RetraitPublicationRequest;
import cnm.prs.service.PublicationService;

/**
 * Contrôleur REST pour la ressource {@code publications} (table {@code t_publication}).
 */
@RestController
@RequestMapping("/api/publications")
public class PublicationController {

    private final PublicationService service;

    public PublicationController(PublicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<PublicationDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PublicationDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    // Workflow de publication : réservé au Chargé de publication (§3.7, Module 09).
    @PreAuthorize("hasRole('CHARGE_PUBLICATION')")
    @PostMapping
    public ResponseEntity<PublicationDto> create(@Valid @RequestBody PublicationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PreAuthorize("hasRole('CHARGE_PUBLICATION')")
    @PutMapping("/{id}")
    public PublicationDto update(@PathVariable Integer id, @Valid @RequestBody PublicationDto dto) {
        return service.update(id, dto);
    }

    @PreAuthorize("hasRole('CHARGE_PUBLICATION')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------------
    // Workflow du portail de publication (§3.7, Module 09)
    // ----------------------------------------------------------------------

    /** Publication : EN_ATTENTE → PUBLIE. */
    @PreAuthorize("hasRole('CHARGE_PUBLICATION')")
    @PostMapping("/{id}/publier")
    public PublicationDto publier(@PathVariable Integer id) {
        return service.publier(id);
    }

    /** Retrait documenté : PUBLIE → RETIRE (motif obligatoire). */
    @PreAuthorize("hasRole('CHARGE_PUBLICATION')")
    @PostMapping("/{id}/retirer")
    public PublicationDto retirer(@PathVariable Integer id, @Valid @RequestBody RetraitPublicationRequest req) {
        return service.retirer(id, req.motifRetrait());
    }

    /** Incrément du compteur de consultations (consultation du portail). */
    @PostMapping("/{id}/consulter")
    public PublicationDto consulter(@PathVariable Integer id) {
        return service.consulter(id);
    }
}
