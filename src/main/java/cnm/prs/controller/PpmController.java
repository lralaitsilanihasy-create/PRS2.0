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

import cnm.prs.dto.PpmDto;
import cnm.prs.service.PpmService;

/**
 * Contrôleur REST pour la ressource {@code ppms} (table {@code t_ppm}).
 */
@RestController
@RequestMapping("/api/ppms")
public class PpmController {

    private final PpmService service;

    public PpmController(PpmService service) {
        this.service = service;
    }

    @GetMapping
    public List<PpmDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PpmDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    // Création brute réservée Admin ; la saisie passe par /api/saisies/ppm (PRMP).
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @PostMapping
    public ResponseEntity<PpmDto> create(@Valid @RequestBody PpmDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    // Édition de l'en-tête PPM d'un brouillon : PRMP (propriétaire) ou Admin ; validé en service.
    @PreAuthorize("hasAnyRole('PRMP','ADMINISTRATEUR')")
    @PutMapping("/{id}")
    public PpmDto update(@PathVariable Integer id, @Valid @RequestBody PpmDto dto) {
        return service.update(id, dto);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
