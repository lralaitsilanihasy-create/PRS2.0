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

import cnm.prs.dto.RegleAnomalieDto;
import cnm.prs.service.RegleAnomalieService;

/**
 * Contrôleur REST pour la ressource {@code regle-anomalies} (table {@code t_regle_anomalie}).
 */
@RestController
@RequestMapping("/api/regle-anomalies")
public class RegleAnomalieController {

    private final RegleAnomalieService service;

    public RegleAnomalieController(RegleAnomalieService service) {
        this.service = service;
    }

    @GetMapping
    public List<RegleAnomalieDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public RegleAnomalieDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<RegleAnomalieDto> create(@Valid @RequestBody RegleAnomalieDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public RegleAnomalieDto update(@PathVariable Integer id, @Valid @RequestBody RegleAnomalieDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
