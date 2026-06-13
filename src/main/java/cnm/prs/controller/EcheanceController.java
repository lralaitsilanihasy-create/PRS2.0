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

import cnm.prs.dto.EcheanceDto;
import cnm.prs.service.EcheanceService;

/**
 * Contrôleur REST pour la ressource {@code echeances} (table {@code t_echeance}).
 */
@RestController
@RequestMapping("/api/echeances")
public class EcheanceController {

    private final EcheanceService service;

    public EcheanceController(EcheanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<EcheanceDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public EcheanceDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<EcheanceDto> create(@Valid @RequestBody EcheanceDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public EcheanceDto update(@PathVariable Integer id, @Valid @RequestBody EcheanceDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
