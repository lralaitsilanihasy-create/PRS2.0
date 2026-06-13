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

import cnm.prs.dto.DossierDto;
import cnm.prs.service.DossierService;

/**
 * Contrôleur REST pour la ressource {@code dossiers} (table {@code t_dossier}).
 */
@RestController
@RequestMapping("/api/dossiers")
public class DossierController {

    private final DossierService service;

    public DossierController(DossierService service) {
        this.service = service;
    }

    @GetMapping
    public List<DossierDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public DossierDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    // Création/édition brutes verrouillées : la saisie passe par la façade /api/saisies (PRMP) ; ici réservé Admin.
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @PostMapping
    public ResponseEntity<DossierDto> create(@Valid @RequestBody DossierDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @PutMapping("/{id}")
    public DossierDto update(@PathVariable Integer id, @Valid @RequestBody DossierDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Soumission officielle d'un dossier par la PRMP (§3.1, Module 03) : génère la référence
     * unique et notifie le Secrétaire/CC de la localité. Réservé au profil {@code PRMP}.
     */
    @PreAuthorize("hasRole('PRMP')")
    @PostMapping("/{id}/soumettre")
    public DossierDto soumettre(@PathVariable Integer id) {
        return service.soumettre(id);
    }
}
