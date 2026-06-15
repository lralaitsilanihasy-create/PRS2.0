package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Refus d'une inscription PRMP par l'Administrateur : motif communiqué à la PRMP.
 */
public record RefusInscriptionRequest(

        @NotBlank @Size(max = 500) String motif) {
}
