package cnm.prs.security;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import cnm.prs.enums.TypeActeur;

/**
 * Génère les jetons JWT (HMAC HS256) émis à la connexion.
 *
 * <p>Claims portées : {@code sub} (login), {@code role} (profil métier), {@code acteurType}
 * (CONTROLEUR / PRMP), {@code ref} (matricule ou id PRMP) et {@code localite} (périmètre de
 * visibilité ; absente pour le Président = voit tout).</p>
 */
@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final long expirationSeconds;

    public TokenService(JwtEncoder encoder,
            @Value("${app.jwt.expiration-seconds}") long expirationSeconds) {
        this.encoder = encoder;
        this.expirationSeconds = expirationSeconds;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String generer(String login, String role, TypeActeur type, String refActeur, String localite) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("prs")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(login)
                .claim("acteurType", type.name())
                .claim("ref", refActeur);
        if (role != null) {
            claims.claim("role", role);
        }
        if (localite != null) {
            claims.claim("localite", localite);
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
