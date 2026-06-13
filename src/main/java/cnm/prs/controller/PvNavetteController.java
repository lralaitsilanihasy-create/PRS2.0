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

import cnm.prs.dto.PvNavetteDto;
import cnm.prs.service.PvNavetteService;

/**
 * Contrôleur REST pour la ressource {@code pv-navettes} (table {@code t_pv_navette}).
 */
@RestController
@RequestMapping("/api/pv-navettes")
public class PvNavetteController {

    private final PvNavetteService service;

    public PvNavetteController(PvNavetteService service) {
        this.service = service;
    }

    @GetMapping
    public List<PvNavetteDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PvNavetteDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<PvNavetteDto> create(@Valid @RequestBody PvNavetteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public PvNavetteDto update(@PathVariable Integer id, @Valid @RequestBody PvNavetteDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
