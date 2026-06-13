package cnm.prs.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import cnm.prs.dto.CompteAuthResumeDto;
import cnm.prs.dto.ReinitMotDePasseRequest;
import cnm.prs.service.CompteAuthService;

/**
 * Gestion des comptes d'authentification — réservée à l'Administrateur (§3.8) :
 * validation des inscriptions (activation/désactivation).
 */
@RestController
@RequestMapping("/api/comptes-auth")
@PreAuthorize("hasRole('ADMINISTRATEUR')")
public class CompteAuthController {

    private final CompteAuthService service;

    public CompteAuthController(CompteAuthService service) {
        this.service = service;
    }

    /** Liste des comptes en attente de validation (inactifs). */
    @GetMapping("/en-attente")
    public List<CompteAuthResumeDto> enAttente() {
        return service.enAttente();
    }

    /** Active un compte (valide une inscription). */
    @PostMapping("/{login}/activer")
    public CompteAuthResumeDto activer(@PathVariable String login) {
        return service.activer(login);
    }

    /** Désactive un compte. */
    @PostMapping("/{login}/desactiver")
    public CompteAuthResumeDto desactiver(@PathVariable String login) {
        return service.desactiver(login);
    }

    /** Réinitialise le mot de passe d'un compte (utilisateur ayant oublié le sien). */
    @PostMapping("/{login}/reinitialiser-mot-de-passe")
    public CompteAuthResumeDto reinitialiserMotDePasse(@PathVariable String login,
            @Valid @RequestBody ReinitMotDePasseRequest request) {
        return service.reinitialiserMotDePasse(login, request.nouveauMotDePasse());
    }
}
