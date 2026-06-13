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

import cnm.prs.dto.LocaliteDto;
import cnm.prs.service.LocaliteService;

/**
 * Contrôleur REST pour la ressource {@code localites} (table {@code tr_localite}).
 */
@RestController
@RequestMapping("/api/localites")
public class LocaliteController {

    private final LocaliteService service;

    public LocaliteController(LocaliteService service) {
        this.service = service;
    }

    @GetMapping
    public List<LocaliteDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public LocaliteDto findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<LocaliteDto> create(@Valid @RequestBody LocaliteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public LocaliteDto update(@PathVariable String id, @Valid @RequestBody LocaliteDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
