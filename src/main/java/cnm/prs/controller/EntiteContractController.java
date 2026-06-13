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

import cnm.prs.dto.EntiteContractDto;
import cnm.prs.service.EntiteContractService;

/**
 * Contrôleur REST pour la ressource {@code entite-contracts} (table {@code tr_entite_contract}).
 */
@RestController
@RequestMapping("/api/entite-contracts")
public class EntiteContractController {

    private final EntiteContractService service;

    public EntiteContractController(EntiteContractService service) {
        this.service = service;
    }

    @GetMapping
    public List<EntiteContractDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public EntiteContractDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<EntiteContractDto> create(@Valid @RequestBody EntiteContractDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public EntiteContractDto update(@PathVariable Integer id, @Valid @RequestBody EntiteContractDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
