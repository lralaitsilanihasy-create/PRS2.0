package cnm.prs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité JPA mappée sur la table référentielle {@code t_capm} : processus de marché
 * (LANCEMENT, DAO, OUVERTURE, ATTRIBUTION…). Référencée par {@code t_marche_prevision.ID_CAPM}.
 */
@Entity
@Table(name = "t_capm")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Capm {

    @Id
    @Column(name = "ID_CAPM", nullable = false)
    private Integer idCapm;

    @Column(name = "LIBELLE_PROCESSUS", length = 100)
    private String libelleProcessus;

    /** Ordre d'affichage des processus (ASC). */
    @Column(name = "ORDRE")
    private Integer ordre;
}
