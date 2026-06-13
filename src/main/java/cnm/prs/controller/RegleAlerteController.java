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

import cnm.prs.dto.RegleAlerteDto;
import cnm.prs.service.RegleAlerteService;

/**
 * Contrôleur REST pour la ressource {@code regle-alertes} (table {@code t_regle_alerte}).
 */
@RestController
@RequestMapping("/api/regle-alertes")
public class RegleAlerteController {

    private final RegleAlerteService service;

    public RegleAlerteController(RegleAlerteService service) {
        this.service = service;
    }

    @GetMapping
    public List<RegleAlerteDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public RegleAlerteDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<RegleAlerteDto> create(@Valid @RequestBody RegleAlerteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public RegleAlerteDto update(@PathVariable Integer id, @Valid @RequestBody RegleAlerteDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
