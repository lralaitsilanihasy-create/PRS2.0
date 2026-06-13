package cnm.prs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité JPA mappée sur la table {@code tr_situation}.
 * Générée à partir du MLD (db_ppm110626.pgerd).
 */
@Entity
@Table(name = "tr_situation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Situation {

    @Id
    @Column(name = "ID_SITUATION", nullable = false)
    private Integer idSituation;

    @Column(name = "LIBELLE", length = 100)
    private String libelle;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;
}
