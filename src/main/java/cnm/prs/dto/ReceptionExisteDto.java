package cnm.prs.dto;

/**
 * Réponse du test léger « ce dossier est-il déjà réceptionné ? »
 * ({@code GET /api/receptions/dossier/{idDossier}/existe}).
 *
 * @param idDossier dossier interrogé
 * @param recu      vrai si au moins une réception existe pour ce dossier (dans le périmètre de l'appelant)
 */
public record ReceptionExisteDto(Integer idDossier, boolean recu) {
}
