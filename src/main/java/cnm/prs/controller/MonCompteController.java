package cnm.prs.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import cnm.prs.dto.ChangePasswordRequest;
import cnm.prs.dto.MessageResponse;
import cnm.prs.service.AuthService;

/**
 * Actions de l'utilisateur sur son propre compte (tout utilisateur authentifié,
 * contrôleur ou PRMP).
 */
@RestController
@RequestMapping("/api/mon-compte")
public class MonCompteController {

    private final AuthService service;

    public MonCompteController(AuthService service) {
        this.service = service;
    }

    /** Change le mot de passe de l'utilisateur courant. */
    @PostMapping("/changer-mot-de-passe")
    public MessageResponse changerMotDePasse(@Valid @RequestBody ChangePasswordRequest request) {
        service.changerMotDePasse(request);
        return new MessageResponse("Mot de passe modifié avec succès.");
    }
}
