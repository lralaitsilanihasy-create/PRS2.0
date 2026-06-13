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

import cnm.prs.dto.MinistereDto;
import cnm.prs.service.MinistereService;

/**
 * Contrôleur REST pour la ressource {@code ministeres} (table {@code tr_ministere}).
 */
@RestController
@RequestMapping("/api/ministeres")
public class MinistereController {

    private final MinistereService service;

    public MinistereController(MinistereService service) {
        this.service = service;
    }

    @GetMapping
    public List<MinistereDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public MinistereDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<MinistereDto> create(@Valid @RequestBody MinistereDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public MinistereDto update(@PathVariable Integer id, @Valid @RequestBody MinistereDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
