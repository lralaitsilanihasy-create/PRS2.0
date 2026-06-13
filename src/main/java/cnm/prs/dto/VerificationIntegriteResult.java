package cnm.prs.dto;

/**
 * Résultat d'une vérification d'intégrité SHA-256 d'un document public (§3.7).
 *
 * @param conforme     vrai si l'empreinte calculée correspond à celle enregistrée
 * @param hashAttendu  empreinte SHA-256 enregistrée sur le document
 * @param hashCalcule  empreinte SHA-256 recalculée à partir du contenu fourni
 */
public record VerificationIntegriteResult(
        boolean conforme,
        String hashAttendu,
        String hashCalcule) {
}
