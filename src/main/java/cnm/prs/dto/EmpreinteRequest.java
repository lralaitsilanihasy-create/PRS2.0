package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Contenu d'un document (encodé en Base64) pour le calcul ou la vérification d'empreinte
 * SHA-256 (§3.7 — intégrité des documents publics).
 *
 * @param contenuBase64 contenu du fichier encodé en Base64
 */
public record EmpreinteRequest(

        @NotBlank
        String contenuBase64) {
}
