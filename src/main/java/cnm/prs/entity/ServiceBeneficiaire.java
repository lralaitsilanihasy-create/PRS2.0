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
 * Entité JPA mappée sur la table {@code t_service_beneficiaire}.
 * Générée à partir du MLD (db_ppm110626.pgerd).
 */
@Entity
@Table(name = "t_service_beneficiaire")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBeneficiaire {

    @Id
    @Column(name = "ID_BENEF", nullable = false)
    private Integer idBenef;

    @Column(name = "ANC_MONT_BENEF")
    private BigDecimal ancMontBenef;

    @Column(name = "NOUV_MONT_BENEF")
    private BigDecimal nouvMontBenef;

    @Column(name = "SOA_CODE", length = 15)
    private String soaCode;

    @Column(name = "ID_DETAIL", nullable = false)
    private Integer idDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DETAIL", insertable = false, updatable = false)
    @JsonIgnore
    private Marche detail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SOA_CODE", insertable = false, updatable = false)
    @JsonIgnore
    private SoaBeneficiaire soa;
}
