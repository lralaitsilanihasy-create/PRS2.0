package cnm.prs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité JPA mappée sur la table {@code tr_avis}.
 * Générée à partir du MLD (db_ppm110626.pgerd).
 */
@Entity
@Table(name = "tr_avis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Avis {

    @Id
    @Column(name = "ID_AVIS", nullable = false, length = 10)
    private String idAvis;

    @Column(name = "LIBELLE_AVIS", length = 100)
    private String libelleAvis;
}
