package cnm.prs.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import cnm.prs.dto.PrmpEntiteDto;
import cnm.prs.service.PrmpEntiteService;

/**
 * Contrôleur REST pour la ressource {@code prmp-entites} (table {@code t_prmp_entite}, §3.1).
 *
 * <p><strong>Lecture</strong> : ouverte aux utilisateurs authentifiés mais <em>scopée</em> côté
 * service (Admin tout / PRMP ses propres entités / autres vide). <strong>Écriture</strong>
 * (POST/PUT/DELETE) : réservée à l'Administrateur — il gère les affectations PRMP↔entité.</p>
 */
@RestController
@RequestMapping("/api/prmp-entites")
public class PrmpEntiteController {

    private final PrmpEntiteService service;

    public PrmpEntiteController(PrmpEntiteService service) {
        this.service = service;
    }

    @GetMapping
    public List<PrmpEntiteDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PrmpEntiteDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<PrmpEntiteDto> create(@Valid @RequestBody PrmpEntiteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public PrmpEntiteDto update(@PathVariable Integer id, @Valid @RequestBody PrmpEntiteDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
