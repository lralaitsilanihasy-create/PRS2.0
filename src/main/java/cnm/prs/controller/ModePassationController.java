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

import cnm.prs.dto.ModePassationDto;
import cnm.prs.service.ModePassationService;

/**
 * Contrôleur REST pour la ressource {@code mode-passations} (table {@code tr_mode_passation}).
 */
@RestController
@RequestMapping("/api/mode-passations")
public class ModePassationController {

    private final ModePassationService service;

    public ModePassationController(ModePassationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ModePassationDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ModePassationDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<ModePassationDto> create(@Valid @RequestBody ModePassationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ModePassationDto update(@PathVariable Integer id, @Valid @RequestBody ModePassationDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
