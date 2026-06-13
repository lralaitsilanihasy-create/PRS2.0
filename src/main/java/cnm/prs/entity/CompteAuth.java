package cnm.prs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Compte d'authentification (table {@code t_compte_auth}).
 *
 * <p>Table dédiée à la connexion : elle unifie les deux populations qui s'authentifient
 * — contrôleurs ({@code tr_controleur}) et PRMP ({@code t_prmp}) — sans ajouter de mot de
 * passe aux tables métier. {@code refActeur} pointe vers {@code IM_CONTROLEUR} ou
 * {@code ID_PRMP} selon {@code typeActeur}.</p>
 */
@Entity
@Table(name = "t_compte_auth")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteAuth {

    @Id
    @Column(name = "LOGIN", nullable = false, length = 100)
    private String login;

    /** Hash BCrypt du mot de passe (jamais le mot de passe en clair). */
    @Column(name = "MOT_DE_PASSE", nullable = false, length = 100)
    private String motDePasse;

    /** Type d'acteur : CONTROLEUR ou PRMP (cf. {@code cnm.prs.enums.TypeActeur}). */
    @Column(name = "TYPE_ACTEUR", nullable = false, length = 20)
    private String typeActeur;

    /** Matricule du contrôleur ({@code IM_CONTROLEUR}) ou identifiant PRMP ({@code ID_PRMP}). */
    @Column(name = "REF_ACTEUR", nullable = false, length = 10)
    private String refActeur;

    @Column(name = "ACTIF", nullable = false)
    private Boolean actif;
}
