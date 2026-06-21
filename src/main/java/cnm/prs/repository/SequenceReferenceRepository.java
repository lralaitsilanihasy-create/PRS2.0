package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cnm.prs.entity.SequenceReference;

/**
 * Accès au compteur de référence (table {@code t_sequence_reference}). L'incrément est fait
 * par la base, en SQL natif portable (H2 en test, Postgres en dev) :
 * <ol>
 *   <li>{@link #incrementerExistant} : UPDATE atomique {@code +1} (verrou de ligne) si la
 *       combinaison existe déjà ;</li>
 *   <li>sinon {@link #creer} : INSERT à 1 (première référence du contexte) ;</li>
 *   <li>{@link #valeurCourante} : lecture de la valeur dans la même transaction.</li>
 * </ol>
 * Aucun compteur applicatif ni {@code SELECT FOR UPDATE} : la PK composite garantit l'unicité
 * (jamais de doublon), l'UPDATE {@code +1} sérialise les incréments concurrents.
 */
public interface SequenceReferenceRepository
        extends JpaRepository<SequenceReference, SequenceReference.Cle> {

    /** Incrémente DERNIERE_VALEUR de 1 côté SGBD ; renvoie le nombre de lignes affectées (0 si absente). */
    @Modifying(flushAutomatically = true)
    @Query(value = """
            update "t_sequence_reference" set "DERNIERE_VALEUR" = "DERNIERE_VALEUR" + 1
            where "TYPE_DOSSIER" = :type and "CODE_LOCALITE" = :code and "ANNEE_EXERCICE" = :annee
            """, nativeQuery = true)
    int incrementerExistant(@Param("type") String type, @Param("code") String code,
            @Param("annee") Integer annee);

    /** Crée la ligne du contexte à la valeur 1 (première référence). */
    @Modifying(flushAutomatically = true)
    @Query(value = """
            insert into "t_sequence_reference"
                ("TYPE_DOSSIER", "CODE_LOCALITE", "ANNEE_EXERCICE", "DERNIERE_VALEUR")
            values (:type, :code, :annee, 1)
            """, nativeQuery = true)
    void creer(@Param("type") String type, @Param("code") String code,
            @Param("annee") Integer annee);

    /** Lit la valeur courante du compteur de la combinaison (dans la même transaction). */
    @Query(value = """
            select "DERNIERE_VALEUR" from "t_sequence_reference"
            where "TYPE_DOSSIER" = :type and "CODE_LOCALITE" = :code and "ANNEE_EXERCICE" = :annee
            """, nativeQuery = true)
    Long valeurCourante(@Param("type") String type, @Param("code") String code,
            @Param("annee") Integer annee);
}
