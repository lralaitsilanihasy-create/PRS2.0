package cnm.prs.dto;

/**
 * Compteurs de contenu par section du menu Contrôleur vérificateur — filtrés sur sa localité,
 * miroir de ses trois worklists ({@code /api/dossiers/a-verifier}, {@code /verifies},
 * {@code /en-attente-prmp}).
 *
 * @param aVerifier     dossiers à vérifier ({@code EN_VERIFICATION} ou {@code EN_ATTENTE_DECISION_PRMP})
 * @param verifies      dossiers vérifiés/clôturés ({@code CLOTURE} avec PV signé)
 * @param enAttentePrmp dossiers en attente de décision PRMP ({@code EN_ATTENTE_DECISION_PRMP})
 */
public record CompteursVerificateurDto(
        long aVerifier,
        long verifies,
        long enAttentePrmp) {
}
