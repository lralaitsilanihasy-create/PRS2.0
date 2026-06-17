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
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;

import cnm.prs.dto.VerificationDto;
import cnm.prs.service.VerificationService;

/**
 * Contrôleur REST pour la ressource {@code verifications} (table {@code t_verification}).
 */
@RestController
@RequestMapping("/api/verifications")
public class VerificationController {

    private final VerificationService service;

    public VerificationController(VerificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<VerificationDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public VerificationDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    // Autorisation large à l'entrée ; le service exige STRICTEMENT le profil Contrôleur vérificateur
    // (⚠️ règle ajoutée — pas de délégation), enregistre l'identité du JWT et vérifie avis FAVR + dossier non clos.
    @PreAuthorize("@perm.peutExercer('VERIFICATEUR')")
    @PostMapping
    public ResponseEntity<VerificationDto> create(@Valid @RequestBody VerificationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PreAuthorize("@perm.peutExercer('VERIFICATEUR')")
    @PutMapping("/{id}")
    public VerificationDto update(@PathVariable Integer id, @Valid @RequestBody VerificationDto dto) {
        return service.update(id, dto);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
