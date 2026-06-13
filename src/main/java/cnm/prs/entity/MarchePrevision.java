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
 * Entité JPA mappée sur la table {@code t_marche_prevision}.
 *
 * <p>Une date prévisionnelle d'un marché. Relation 1,N : un marché ({@link Marche})
 * possède plusieurs dates prévisionnelles, chacune typée par {@code TYPE_DATE}
 * (LANCEMENT, DAO, OUVERTURE, ATTRIBUTION). Remplace les colonnes de dates qui
 * figuraient auparavant directement dans {@code t_marche}.</p>
 */
@Entity
@Table(name = "t_marche_prevision")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarchePrevision {

    @Id
    @Column(name = "ID_PREVISION", nullable = false)
    private Integer idPrevision;

    /** Marché concerné (FK vers {@code t_marche.ID_DETAIL}). */
    @Column(name = "ID_DETAIL", nullable = false)
    private Integer idDetail;

    /** Type de date prévisionnelle : LANCEMENT, DAO, OUVERTURE, ATTRIBUTION. */
    @Column(name = "TYPE_DATE", length = 20, nullable = false)
    private String typeDate;

    /** Date prévisionnelle. */
    @Column(name = "DATE_PREV")
    private LocalDate datePrev;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DETAIL", insertable = false, updatable = false)
    @JsonIgnore
    private Marche marche;
}
