package cnm.prs.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Compteur de référence par combinaison (TYPE_DOSSIER, CODE_LOCALITE, ANNEE_EXERCICE).
 * {@code DERNIERE_VALEUR} est incrémentée côté SGBD (UPSERT atomique) — aucun compteur applicatif.
 * Sert uniquement à matérialiser la table (H2 en test, Postgres en dev) ; l'accès se fait en SQL natif.
 */
@Entity
@Table(name = "t_sequence_reference")
@IdClass(SequenceReference.Cle.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SequenceReference {

    @Id
    @Column(name = "TYPE_DOSSIER", nullable = false, length = 10)
    private String typeDossier;

    @Id
    @Column(name = "CODE_LOCALITE", nullable = false, length = 20)
    private String codeLocalite;

    @Id
    @Column(name = "ANNEE_EXERCICE", nullable = false)
    private Integer anneeExercice;

    @Column(name = "DERNIERE_VALEUR", nullable = false)
    private Long derniereValeur;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Cle implements Serializable {
        private String typeDossier;
        private String codeLocalite;
        private Integer anneeExercice;
    }
}
