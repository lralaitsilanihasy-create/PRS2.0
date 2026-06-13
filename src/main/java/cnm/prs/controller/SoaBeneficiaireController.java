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

import cnm.prs.dto.SoaBeneficiaireDto;
import cnm.prs.service.SoaBeneficiaireService;

/**
 * Contrôleur REST pour la ressource {@code soa-beneficiaires} (table {@code tr_soa_beneficiaire}).
 */
@RestController
@RequestMapping("/api/soa-beneficiaires")
public class SoaBeneficiaireController {

    private final SoaBeneficiaireService service;

    public SoaBeneficiaireController(SoaBeneficiaireService service) {
        this.service = service;
    }

    @GetMapping
    public List<SoaBeneficiaireDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public SoaBeneficiaireDto findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<SoaBeneficiaireDto> create(@Valid @RequestBody SoaBeneficiaireDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public SoaBeneficiaireDto update(@PathVariable String id, @Valid @RequestBody SoaBeneficiaireDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
