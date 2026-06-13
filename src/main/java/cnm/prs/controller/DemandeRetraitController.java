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

import cnm.prs.dto.DemandeRetraitDto;
import cnm.prs.service.DemandeRetraitService;

/**
 * Contrôleur REST pour la ressource {@code demande-retraits} (table {@code t_demande_retrait}).
 */
@RestController
@RequestMapping("/api/demande-retraits")
public class DemandeRetraitController {

    private final DemandeRetraitService service;

    public DemandeRetraitController(DemandeRetraitService service) {
        this.service = service;
    }

    @GetMapping
    public List<DemandeRetraitDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public DemandeRetraitDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    // Demande de retrait : action de la PRMP (§3.1, Module 11).
    @PreAuthorize("hasRole('PRMP')")
    @PostMapping
    public ResponseEntity<DemandeRetraitDto> create(@Valid @RequestBody DemandeRetraitDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    // Décision (APPROUVE / REJETE) : réservée au Chef de commission (§3.3).
    @PreAuthorize("hasRole('CHEF_COMMISSION')")
    @PutMapping("/{id}")
    public DemandeRetraitDto update(@PathVariable Integer id, @Valid @RequestBody DemandeRetraitDto dto) {
        return service.update(id, dto);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
