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

import cnm.prs.dto.SessionUtilisateurDto;
import cnm.prs.service.SessionUtilisateurService;

/**
 * Contrôleur REST pour la ressource {@code session-utilisateurs} (table {@code t_session_utilisateur}).
 * Données de connexion/sécurité — réservées à l'Administrateur (§3.8, Module 10).
 */
@RestController
@RequestMapping("/api/session-utilisateurs")
@PreAuthorize("hasRole('ADMINISTRATEUR')")
public class SessionUtilisateurController {

    private final SessionUtilisateurService service;

    public SessionUtilisateurController(SessionUtilisateurService service) {
        this.service = service;
    }

    @GetMapping
    public List<SessionUtilisateurDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public SessionUtilisateurDto findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<SessionUtilisateurDto> create(@Valid @RequestBody SessionUtilisateurDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public SessionUtilisateurDto update(@PathVariable String id, @Valid @RequestBody SessionUtilisateurDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
