package cnm.prs.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import cnm.prs.dto.LettreRenvoiDto;
import cnm.prs.service.LettreRenvoiService;

/**
 * Contrôleur REST pour la ressource {@code lettre-renvois} (table {@code t_lettre_renvoi}).
 * Lecture : authentifié (filtré localité). Édition/soumission : Membre. Signature : CC ou Président.
 */
@RestController
@RequestMapping("/api/lettre-renvois")
public class LettreRenvoiController {

    private final LettreRenvoiService service;

    public LettreRenvoiController(LettreRenvoiService service) {
        this.service = service;
    }

    @GetMapping
    public List<LettreRenvoiDto> findAll() {
        return service.findAll();
    }

    /** Lettres de renvoi signées concernant les dossiers de la PRMP connectée (lecture seule). */
    @PreAuthorize("hasRole('PRMP')")
    @GetMapping("/mes-lettres")
    public List<LettreRenvoiDto> mesLettres() {
        return service.mesLettres();
    }

    @GetMapping("/{id}")
    public LettreRenvoiDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    /** Création d'une lettre de renvoi pendant l'examen (statut BROUILLON). */
    @PreAuthorize("@perm.peutExercer('MEMBRE')")
    @PostMapping
    public ResponseEntity<LettreRenvoiDto> create(@Valid @RequestBody LettreRenvoiDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PreAuthorize("@perm.peutExercer('MEMBRE')")
    @PutMapping("/{id}")
    public LettreRenvoiDto update(@PathVariable Integer id, @Valid @RequestBody LettreRenvoiDto dto) {
        return service.update(id, dto);
    }

    @PreAuthorize("@perm.peutExercer('MEMBRE')")
    @PostMapping("/{id}/soumettre")
    public LettreRenvoiDto soumettre(@PathVariable Integer id) {
        return service.soumettre(id);
    }

    @PreAuthorize("hasAnyRole('CHEF_COMMISSION','PRESIDENT')")
    @PostMapping("/{id}/signer")
    public LettreRenvoiDto signer(@PathVariable Integer id) {
        return service.signer(id);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
