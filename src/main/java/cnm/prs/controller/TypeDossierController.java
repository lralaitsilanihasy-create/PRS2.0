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

import cnm.prs.dto.TypeDossierDto;
import cnm.prs.service.TypeDossierService;

/**
 * Contrôleur REST pour la ressource {@code type-dossiers} (table {@code tr_type_dossier}).
 */
@RestController
@RequestMapping("/api/type-dossiers")
public class TypeDossierController {

    private final TypeDossierService service;

    public TypeDossierController(TypeDossierService service) {
        this.service = service;
    }

    @GetMapping
    public List<TypeDossierDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TypeDossierDto findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<TypeDossierDto> create(@Valid @RequestBody TypeDossierDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public TypeDossierDto update(@PathVariable String id, @Valid @RequestBody TypeDossierDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
