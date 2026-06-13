package cnm.prs.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import cnm.prs.dto.LoginRequest;
import cnm.prs.dto.LoginResponse;
import cnm.prs.dto.RegisterPrmpRequest;
import cnm.prs.dto.RegisterResponse;
import cnm.prs.service.AuthService;

/**
 * Authentification : émission de jetons JWT. Endpoint public (cf. SecurityConfig).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    /**
     * Auto-inscription d'une PRMP (route publique). Crée un compte inactif, en attente de
     * validation par l'Administrateur. Le login ne fonctionnera qu'après activation.
     */
    @PostMapping("/register/prmp")
    public ResponseEntity<RegisterResponse> registerPrmp(@Valid @RequestBody RegisterPrmpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registerPrmp(request));
    }
}
