package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Demande de changement de mot de passe de l'utilisateur authentifié.
 *
 * @param ancienMotDePasse  mot de passe actuel (vérifié)
 * @param nouveauMotDePasse nouveau mot de passe (min 8 caractères)
 */
public record ChangePasswordRequest(

        @NotBlank
        String ancienMotDePasse,

        @NotBlank
        @Size(min = 8, max = 72)
        String nouveauMotDePasse) {
}
