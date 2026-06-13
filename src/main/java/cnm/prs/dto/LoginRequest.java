package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Identifiants de connexion soumis à {@code POST /api/auth/login}.
 */
public record LoginRequest(

        @NotBlank
        String login,

        @NotBlank
        String motDePasse) {
}
