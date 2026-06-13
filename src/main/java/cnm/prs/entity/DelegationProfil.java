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
 * Entité JPA mappée sur la table {@code t_delegation_profil}.
 * Générée à partir du MLD (db_ppm110626.pgerd).
 */
@Entity
@Table(name = "t_delegation_profil")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DelegationProfil {

    @Id
    @Column(name = "ID_DELEGATION", nullable = false)
    private Integer idDelegation;

    @Column(name = "ID_PROFILE_DELEGANT", nullable = false)
    private Integer idProfileDelegant;

    @Column(name = "ID_PROFILE_DELEGUE", nullable = false)
    private Integer idProfileDelegue;

    @Column(name = "ACTIF", nullable = false)
    private Boolean actif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROFILE_DELEGANT", insertable = false, updatable = false)
    @JsonIgnore
    private Profile profileDelegant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROFILE_DELEGUE", insertable = false, updatable = false)
    @JsonIgnore
    private Profile profileDelegue;
}
