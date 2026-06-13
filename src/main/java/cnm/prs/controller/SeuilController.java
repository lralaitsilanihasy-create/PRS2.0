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

import cnm.prs.dto.SeuilDto;
import cnm.prs.service.SeuilService;

/**
 * Contrôleur REST pour la ressource {@code seuils} (table {@code t_seuil}).
 */
@RestController
@RequestMapping("/api/seuils")
public class SeuilController {

    private final SeuilService service;

    public SeuilController(SeuilService service) {
        this.service = service;
    }

    @GetMapping
    public List<SeuilDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public SeuilDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<SeuilDto> create(@Valid @RequestBody SeuilDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public SeuilDto update(@PathVariable Integer id, @Valid @RequestBody SeuilDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
