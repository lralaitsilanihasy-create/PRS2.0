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

import cnm.prs.dto.IndicateurPrmpDto;
import cnm.prs.service.IndicateurPrmpService;

/**
 * Contrôleur REST pour la ressource {@code indicateur-prmps} (table {@code t_indicateur_prmp}).
 */
@RestController
@RequestMapping("/api/indicateur-prmps")
public class IndicateurPrmpController {

    private final IndicateurPrmpService service;

    public IndicateurPrmpController(IndicateurPrmpService service) {
        this.service = service;
    }

    @GetMapping
    public List<IndicateurPrmpDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public IndicateurPrmpDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<IndicateurPrmpDto> create(@Valid @RequestBody IndicateurPrmpDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public IndicateurPrmpDto update(@PathVariable Integer id, @Valid @RequestBody IndicateurPrmpDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
