package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps d'envoi d'un message interne (§ Module 04). L'expéditeur n'est pas fourni :
 * il est toujours l'utilisateur authentifié.
 *
 * @param destinataireIm  matricule du contrôleur destinataire (obligatoire)
 * @param sujet           sujet du message
 * @param corps           corps du message
 * @param idDossier       dossier rattaché (facultatif)
 * @param idMessageParent message parent pour une réponse en fil (facultatif)
 */
public record MessageEnvoiRequest(

        @NotBlank
        @Size(max = 7)
        String destinataireIm,

        @Size(max = 200)
        String sujet,

        String corps,

        Integer idDossier,

        Integer idMessageParent) {
}
