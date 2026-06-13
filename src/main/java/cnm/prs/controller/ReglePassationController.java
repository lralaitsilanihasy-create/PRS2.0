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

import cnm.prs.dto.ReglePassationDto;
import cnm.prs.dto.SuggestionModeRequest;
import cnm.prs.dto.SuggestionModeResponse;
import cnm.prs.service.ReglePassationService;

/**
 * Contrôleur REST pour la ressource {@code regle-passations} (table {@code t_regle_passation}).
 */
@RestController
@RequestMapping("/api/regle-passations")
public class ReglePassationController {

    private final ReglePassationService service;

    public ReglePassationController(ReglePassationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReglePassationDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ReglePassationDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<ReglePassationDto> create(@Valid @RequestBody ReglePassationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ReglePassationDto update(@PathVariable Integer id, @Valid @RequestBody ReglePassationDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Détermination automatique du mode de passation (§3.1, Module 02) — outil de la PRMP.
     */
    @PreAuthorize("hasRole('PRMP')")
    @PostMapping("/suggestion-mode")
    public SuggestionModeResponse suggererMode(@Valid @RequestBody SuggestionModeRequest req) {
        return service.suggererMode(req);
    }
}
