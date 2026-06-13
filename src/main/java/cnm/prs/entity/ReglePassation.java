package cnm.prs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité JPA mappée sur la table {@code t_regle_passation}.
 * Générée à partir du MLD (db_ppm110626.pgerd).
 */
@Entity
@Table(name = "t_regle_passation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReglePassation {

    @Id
    @Column(name = "ID_REGLE", nullable = false)
    private Integer idRegle;

    @Column(name = "ID_SITUATION", nullable = false)
    private Integer idSituation;

    @Column(name = "ID_SEUIL", nullable = false)
    private Integer idSeuil;

    @Column(name = "ID_MODE", nullable = false)
    private Integer idMode;

    @Column(name = "PRIORITE")
    private Integer priorite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_MODE", insertable = false, updatable = false)
    @JsonIgnore
    private ModePassation mode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SEUIL", insertable = false, updatable = false)
    @JsonIgnore
    private Seuil seuil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SITUATION", insertable = false, updatable = false)
    @JsonIgnore
    private Situation situation;
}
