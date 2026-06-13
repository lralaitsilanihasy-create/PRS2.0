package cnm.prs.dto;

/**
 * Réponse de connexion : jeton JWT et informations de session.
 *
 * @param token       jeton JWT à placer dans l'en-tête {@code Authorization: Bearer ...}
 * @param login       login authentifié
 * @param role        profil métier reconnu (ou {@code null})
 * @param typeActeur  CONTROLEUR ou PRMP
 * @param ref         matricule contrôleur ou identifiant PRMP
 * @param localite    localité de rattachement ({@code null} = toutes, cas Président)
 * @param expiresIn   durée de validité du jeton en secondes
 */
public record LoginResponse(
        String token,
        String login,
        String role,
        String typeActeur,
        String ref,
        String localite,
        long expiresIn) {
}
