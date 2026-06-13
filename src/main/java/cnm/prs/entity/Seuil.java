package cnm.prs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité JPA mappée sur la table {@code t_seuil}.
 * Générée à partir du MLD (db_ppm110626.pgerd).
 */
@Entity
@Table(name = "t_seuil")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seuil {

    @Id
    @Column(name = "ID_SEUIL", nullable = false)
    private Integer idSeuil;

    @Column(name = "MONTANT_MIN")
    private BigDecimal montantMin;

    @Column(name = "MONTANT_MAX")
    private BigDecimal montantMax;

    @Column(name = "ID_NATURE", nullable = false)
    private Integer idNature;

    @Column(name = "ID_LOCALITE", nullable = false, length = 5)
    private String idLocalite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_LOCALITE", insertable = false, updatable = false)
    @JsonIgnore
    private Localite localite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_NATURE", insertable = false, updatable = false)
    @JsonIgnore
    private Nature nature;
}
