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

import cnm.prs.dto.AnomalieDto;
import cnm.prs.service.AnomalieService;

/**
 * Contrôleur REST pour la ressource {@code anomalies} (table {@code t_anomalie}).
 */
@RestController
@RequestMapping("/api/anomalies")
public class AnomalieController {

    private final AnomalieService service;

    public AnomalieController(AnomalieService service) {
        this.service = service;
    }

    @GetMapping
    public List<AnomalieDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AnomalieDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<AnomalieDto> create(@Valid @RequestBody AnomalieDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public AnomalieDto update(@PathVariable Integer id, @Valid @RequestBody AnomalieDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
