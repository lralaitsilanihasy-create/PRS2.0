package cnm.prs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité JPA mappée sur la table {@code t_dossier}.
 * Générée à partir du MLD (db_ppm110626.pgerd).
 */
@Entity
@Table(name = "t_dossier")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dossier {

    @Id
    @Column(name = "ID_DOSSIER", nullable = false)
    private Integer idDossier;

    @Column(name = "ID_TYPE_DOSSIER", length = 10)
    private String idTypeDossier;

    @Column(name = "ID_DOSSIER_PARENT")
    private Integer idDossierParent;

    @Column(name = "REFE_DOSSIER", length = 100)
    private String refeDossier;

    @Column(name = "DATE_REF")
    private LocalDate dateRef;

    @Column(name = "STATUT", length = 20)
    private String statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TYPE_DOSSIER", insertable = false, updatable = false)
    @JsonIgnore
    private TypeDossier typeDossier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DOSSIER_PARENT", insertable = false, updatable = false)
    @JsonIgnore
    private Dossier dossierParent;
}
