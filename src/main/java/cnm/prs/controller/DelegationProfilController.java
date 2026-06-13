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

import cnm.prs.dto.DelegationProfilDto;
import cnm.prs.service.DelegationProfilService;

/**
 * Contrôleur REST pour la ressource {@code delegation-profils} (table {@code t_delegation_profil}).
 */
@RestController
@RequestMapping("/api/delegation-profils")
public class DelegationProfilController {

    private final DelegationProfilService service;

    public DelegationProfilController(DelegationProfilService service) {
        this.service = service;
    }

    @GetMapping
    public List<DelegationProfilDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public DelegationProfilDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<DelegationProfilDto> create(@Valid @RequestBody DelegationProfilDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public DelegationProfilDto update(@PathVariable Integer id, @Valid @RequestBody DelegationProfilDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
