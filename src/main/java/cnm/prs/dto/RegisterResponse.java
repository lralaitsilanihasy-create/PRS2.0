package cnm.prs.dto;

/**
 * Réponse à une auto-inscription : le compte est créé mais inactif jusqu'à validation.
 *
 * @param login      login choisi
 * @param refActeur  identifiant de l'acteur (id PRMP)
 * @param typeActeur type d'acteur (PRMP)
 * @param actif      toujours {@code false} à l'inscription
 * @param statut     statut du compte à l'inscription (toujours {@code EN_ATTENTE})
 * @param message    message d'information pour l'utilisateur
 */
public record RegisterResponse(
        String login,
        String refActeur,
        String typeActeur,
        boolean actif,
        String statut,
        String message) {
}
