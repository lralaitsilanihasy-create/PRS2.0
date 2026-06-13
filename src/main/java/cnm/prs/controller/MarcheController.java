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

import cnm.prs.dto.MarcheDto;
import cnm.prs.service.MarcheService;

/**
 * Contrôleur REST pour la ressource {@code marches} (table {@code t_marche}).
 */
@RestController
@RequestMapping("/api/marches")
public class MarcheController {

    private final MarcheService service;

    public MarcheController(MarcheService service) {
        this.service = service;
    }

    @GetMapping
    public List<MarcheDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public MarcheDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<MarcheDto> create(@Valid @RequestBody MarcheDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public MarcheDto update(@PathVariable Integer id, @Valid @RequestBody MarcheDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
