package cnm.prs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité JPA mappée sur la table {@code tr_profile}.
 * Générée à partir du MLD (db_ppm110626.pgerd).
 */
@Entity
@Table(name = "tr_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @Column(name = "ID_PROFILE", nullable = false)
    private Integer idProfile;

    @Column(name = "PROFILE", length = 50)
    private String profile;
}
