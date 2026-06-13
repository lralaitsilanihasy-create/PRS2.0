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

import cnm.prs.dto.SituationDto;
import cnm.prs.service.SituationService;

/**
 * Contrôleur REST pour la ressource {@code situations} (table {@code tr_situation}).
 */
@RestController
@RequestMapping("/api/situations")
public class SituationController {

    private final SituationService service;

    public SituationController(SituationService service) {
        this.service = service;
    }

    @GetMapping
    public List<SituationDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public SituationDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<SituationDto> create(@Valid @RequestBody SituationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public SituationDto update(@PathVariable Integer id, @Valid @RequestBody SituationDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
