package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de la resoumission d'un dossier rectifié par la PRMP (⚠️ règle ajoutée) :
 * {@code POST /api/dossiers/{id}/resoumettre}. Le motif de rectification est obligatoire (sinon 400).
 */
public record DossierResoumissionRequest(

        @NotBlank
        @Size(max = 255)
        String motifRectification) {
}
