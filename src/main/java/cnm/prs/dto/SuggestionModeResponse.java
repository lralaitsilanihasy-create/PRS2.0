package cnm.prs.dto;

/**
 * Mode de passation suggéré (§3.1, Module 02). Suggestion non contraignante.
 *
 * @param idMode   mode de passation retenu
 * @param idRegle  règle appliquée ({@code t_regle_passation})
 * @param idSeuil  seuil correspondant ({@code t_seuil})
 * @param priorite priorité de la règle retenue
 */
public record SuggestionModeResponse(
        Integer idMode,
        Integer idRegle,
        Integer idSeuil,
        Integer priorite) {
}
