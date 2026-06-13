package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de la demande de retrait (dépublication) d'une publication (§3.7).
 *
 * @param motifRetrait motif documenté du retrait (obligatoire)
 */
public record RetraitPublicationRequest(

        @NotBlank
        @Size(max = 300)
        String motifRetrait) {
}
