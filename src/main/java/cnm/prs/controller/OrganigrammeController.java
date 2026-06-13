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

import cnm.prs.dto.OrganigrammeDto;
import cnm.prs.service.OrganigrammeService;

/**
 * Contrôleur REST pour la ressource {@code organigrammes} (table {@code t_organigramme}).
 */
@RestController
@RequestMapping("/api/organigrammes")
public class OrganigrammeController {

    private final OrganigrammeService service;

    public OrganigrammeController(OrganigrammeService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrganigrammeDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public OrganigrammeDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<OrganigrammeDto> create(@Valid @RequestBody OrganigrammeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public OrganigrammeDto update(@PathVariable Integer id, @Valid @RequestBody OrganigrammeDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
