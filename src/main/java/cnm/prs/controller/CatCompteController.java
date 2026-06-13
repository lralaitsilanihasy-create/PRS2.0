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

import cnm.prs.dto.CatCompteDto;
import cnm.prs.service.CatCompteService;

/**
 * Contrôleur REST pour la ressource {@code cat-comptes} (table {@code tr_cat_compte}).
 */
@RestController
@RequestMapping("/api/cat-comptes")
public class CatCompteController {

    private final CatCompteService service;

    public CatCompteController(CatCompteService service) {
        this.service = service;
    }

    @GetMapping
    public List<CatCompteDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CatCompteDto findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<CatCompteDto> create(@Valid @RequestBody CatCompteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public CatCompteDto update(@PathVariable String id, @Valid @RequestBody CatCompteDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
