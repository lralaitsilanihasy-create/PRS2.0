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

import cnm.prs.dto.NatureDto;
import cnm.prs.service.NatureService;

/**
 * Contrôleur REST pour la ressource {@code natures} (table {@code tr_nature}).
 */
@RestController
@RequestMapping("/api/natures")
public class NatureController {

    private final NatureService service;

    public NatureController(NatureService service) {
        this.service = service;
    }

    @GetMapping
    public List<NatureDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public NatureDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<NatureDto> create(@Valid @RequestBody NatureDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public NatureDto update(@PathVariable Integer id, @Valid @RequestBody NatureDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
