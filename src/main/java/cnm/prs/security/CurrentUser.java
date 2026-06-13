package cnm.prs.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import cnm.prs.enums.ProfilUtilisateur;

/**
 * Accès à l'utilisateur authentifié (claims du jeton JWT courant).
 *
 * <p>Utilitaire utilisé par les couches service/contrôleur pour appliquer la visibilité et
 * les autorisations. La localité est la claim {@code localite} ; son absence signifie
 * « toutes localités » (Président).</p>
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    private static Optional<Jwt> jwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken token) {
            return Optional.of(token.getToken());
        }
        if (auth != null && auth.getPrincipal() instanceof Jwt principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    /** Login (sujet du jeton). */
    public static Optional<String> login() {
        return jwt().map(Jwt::getSubject);
    }

    /** Matricule contrôleur ou identifiant PRMP. */
    public static Optional<String> ref() {
        return jwt().map(j -> j.getClaimAsString("ref"));
    }

    /** Profil métier, ou vide si non reconnu. */
    public static Optional<ProfilUtilisateur> profil() {
        return jwt()
                .map(j -> j.getClaimAsString("role"))
                .filter(r -> r != null && !r.isBlank())
                .map(ProfilUtilisateur::valueOf);
    }

    /** Localité de rattachement ; vide = toutes localités (Président). */
    public static Optional<String> localite() {
        return jwt().map(j -> j.getClaimAsString("localite"));
    }

    /** Vrai si l'utilisateur voit toutes les localités (Président, ou pas de filtre). */
    public static boolean voitToutesLocalites() {
        return localite().isEmpty();
    }
}
