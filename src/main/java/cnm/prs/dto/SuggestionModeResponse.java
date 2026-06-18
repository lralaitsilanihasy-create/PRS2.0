package cnm.prs.dto;

import java.util.List;

/**
 * Réponse de détermination du mode de passation (§3.1, Module 02). ⚠️ Règle ajoutée : la PRMP
 * choisit parmi l'<strong>ensemble autorisé</strong> ; le serveur le calcule et le valide.
 *
 * @param modeRecommande   idMode recommandé (règle de plus haute priorité), {@code null} si aucune règle
 * @param modesAutorises   tous les modes autorisés (recommandé en tête), avec libellés
 * @param modeNonDetermine {@code true} si aucune règle ne correspond (ensemble vide) — saisie manuelle
 */
public record SuggestionModeResponse(
        Integer modeRecommande,
        List<ModeAutorise> modesAutorises,
        boolean modeNonDetermine) {
}
