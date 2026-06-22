package cnm.prs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité JPA mappée sur la table {@code t_pv_examen}.
 * Générée à partir du MLD (db_ppm110626.pgerd).
 */
@Entity
@Table(name = "t_pv_examen",
        uniqueConstraints = @UniqueConstraint(name = "uq_pv_examen_refe_pv", columnNames = "REFE_PV"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PvExamen {

    @Id
    @Column(name = "ID_PV", nullable = false)
    private Integer idPv;

    @Column(name = "ID_EXAMEN", nullable = false)
    private Integer idExamen;

    @Column(name = "ID_AVIS", nullable = false, length = 10)
    private String idAvis;

    @Column(name = "IM_CTRL_PRESIDENT", length = 7)
    private String imCtrlPresident;

    @Column(name = "IM_CTRL_CC", length = 7)
    private String imCtrlCc;

    @Column(name = "IM_CTRL_MEMBRE", nullable = false, length = 7)
    private String imCtrlMembre;

    @Column(name = "SYNTHESE_OBSERVATIONS", columnDefinition = "text")
    private String syntheseObservations;

    @Column(name = "STATUT_PV", nullable = false, length = 20)
    private String statutPv;

    @Column(name = "NB_NAVETTES", nullable = false)
    private Integer nbNavettes;

    @Column(name = "DATE_SOUMISSION_INITIALE")
    private LocalDate dateSoumissionInitiale;

    @Column(name = "DATE_ACCEPTATION")
    private LocalDate dateAcceptation;

    @Column(name = "DATE_SIGNATURE_PRESIDENT")
    private LocalDate dateSignaturePresident;

    @Column(name = "DATE_SIGNATURE_CC")
    private LocalDate dateSignatureCc;

    @Column(name = "DATE_SIGNATURE_MEMBRE")
    private LocalDate dateSignatureMembre;

    @Column(name = "DATE_PV")
    private LocalDate datePv;

    @Column(name = "REFERENCE_PV", length = 100)
    private String referencePv;

    /** Référence dérivée du dossier (refeDossier avec /PV avant l'année) — auto-générée à la création, unique. */
    @Column(name = "REFE_PV", length = 120)
    private String refePv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_AVIS", insertable = false, updatable = false)
    @JsonIgnore
    private Avis avis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EXAMEN", insertable = false, updatable = false)
    @JsonIgnore
    private Examen examen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IM_CTRL_CC", insertable = false, updatable = false)
    @JsonIgnore
    private Controleur ctrlCc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IM_CTRL_MEMBRE", insertable = false, updatable = false)
    @JsonIgnore
    private Controleur ctrlMembre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IM_CTRL_PRESIDENT", insertable = false, updatable = false)
    @JsonIgnore
    private Controleur ctrlPresident;
}
