package cnm.prs.dto;

/**
 * Vue résumée d'un compte d'authentification, <strong>sans le mot de passe</strong>
 * (pour la gestion/validation par l'Administrateur).
 *
 * @param login      login du compte
 * @param typeActeur CONTROLEUR ou PRMP
 * @param refActeur  matricule contrôleur ou identifiant PRMP
 * @param actif      true si le compte peut se connecter
 */
public record CompteAuthResumeDto(
        String login,
        String typeActeur,
        String refActeur,
        Boolean actif) {
}
