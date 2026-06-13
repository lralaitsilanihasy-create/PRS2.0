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

import jakarta.validation.Valid;

import cnm.prs.dto.AvisDto;
import cnm.prs.service.AvisService;

/**
 * Contrôleur REST pour la ressource {@code aviss} (table {@code tr_avis}).
 */
@RestController
@RequestMapping("/api/aviss")
public class AvisController {

    private final AvisService service;

    public AvisController(AvisService service) {
        this.service = service;
    }

    @GetMapping
    public List<AvisDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AvisDto findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<AvisDto> create(@Valid @RequestBody AvisDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public AvisDto update(@PathVariable String id, @Valid @RequestBody AvisDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
