package cnm.prs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité JPA mappée sur la table {@code tr_mode_passation}.
 * Générée à partir du MLD (db_ppm110626.pgerd).
 */
@Entity
@Table(name = "tr_mode_passation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModePassation {

    @Id
    @Column(name = "ID_MODE", nullable = false)
    private Integer idMode;

    @Column(name = "LIBELLE", length = 100)
    private String libelle;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "PUBLICITE_REQUISE")
    private Boolean publiciteRequise;

    @Column(name = "DELAI_MIN_JOURS")
    private Integer delaiMinJours;

    @Column(name = "BASE_LEGALE", length = 200)
    private String baseLegale;
}
