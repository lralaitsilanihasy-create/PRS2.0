package cnm.prs.dto;

/**
 * Compteurs de contenu par section du menu Secrétaire — filtrés sur sa localité.
 *
 * @param aReceptionner dossiers à réceptionner ({@code SOUMIS} sans réception, de sa localité)
 * @param receptions    réceptions enregistrées dans sa localité (historique de ses réceptions)
 */
public record CompteursSecretaireDto(
        long aReceptionner,
        long receptions) {
}
