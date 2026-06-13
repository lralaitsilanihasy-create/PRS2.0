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
 * Entité JPA mappée sur la table {@code t_examen_detail}.
 * Générée à partir du MLD (db_ppm110626.pgerd).
 */
@Entity
@Table(name = "t_examen_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamenDetail {

    @Id
    @Column(name = "ID_DETAIL_EXAMEN", nullable = false)
    private Integer idDetailExamen;

    @Column(name = "ID_EXAMEN", nullable = false)
    private Integer idExamen;

    @Column(name = "ID_PT_CONTROLE", nullable = false)
    private Integer idPtControle;

    @Column(name = "CONFORME", nullable = false)
    private Boolean conforme;

    @Column(name = "OBSERVATION", length = 500)
    private String observation;

    @Column(name = "OBS_SI_NON_CONFORME", length = 500)
    private String obsSiNonConforme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EXAMEN", insertable = false, updatable = false)
    @JsonIgnore
    private Examen examen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PT_CONTROLE", insertable = false, updatable = false)
    @JsonIgnore
    private PointsCtrl ptControle;
}
