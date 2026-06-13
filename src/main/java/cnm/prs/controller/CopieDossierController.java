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

import cnm.prs.dto.CopieDossierDto;
import cnm.prs.service.CopieDossierService;

/**
 * Contrôleur REST pour la ressource {@code copie-dossiers} (table {@code t_copie_dossier}).
 */
@RestController
@RequestMapping("/api/copie-dossiers")
public class CopieDossierController {

    private final CopieDossierService service;

    public CopieDossierController(CopieDossierService service) {
        this.service = service;
    }

    @GetMapping
    public List<CopieDossierDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CopieDossierDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<CopieDossierDto> create(@Valid @RequestBody CopieDossierDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public CopieDossierDto update(@PathVariable Integer id, @Valid @RequestBody CopieDossierDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
