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

import cnm.prs.dto.ReceptionDto;
import cnm.prs.service.ReceptionService;

/**
 * Contrôleur REST pour la ressource {@code receptions} (table {@code t_reception}).
 */
@RestController
@RequestMapping("/api/receptions")
public class ReceptionController {

    private final ReceptionService service;

    public ReceptionController(ReceptionService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReceptionDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ReceptionDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    // Réception / complétude : Secrétaire, CC ou Président (§2.1, §3.4).
    @PreAuthorize("@perm.peutExercer('SECRETAIRE')")
    @PostMapping
    public ResponseEntity<ReceptionDto> create(@Valid @RequestBody ReceptionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PreAuthorize("@perm.peutExercer('SECRETAIRE')")
    @PutMapping("/{id}")
    public ReceptionDto update(@PathVariable Integer id, @Valid @RequestBody ReceptionDto dto) {
        return service.update(id, dto);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
