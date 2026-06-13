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

import cnm.prs.dto.CompteDto;
import cnm.prs.service.CompteService;

/**
 * Contrôleur REST pour la ressource {@code comptes} (table {@code tr_compte}).
 */
@RestController
@RequestMapping("/api/comptes")
public class CompteController {

    private final CompteService service;

    public CompteController(CompteService service) {
        this.service = service;
    }

    @GetMapping
    public List<CompteDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CompteDto findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<CompteDto> create(@Valid @RequestBody CompteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public CompteDto update(@PathVariable String id, @Valid @RequestBody CompteDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
