package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Réinitialisation d'un mot de passe par l'Administrateur (nouveau mot de passe imposé).
 *
 * @param nouveauMotDePasse nouveau mot de passe (min 8 caractères)
 */
public record ReinitMotDePasseRequest(

        @NotBlank
        @Size(min = 8, max = 72)
        String nouveauMotDePasse) {
}
