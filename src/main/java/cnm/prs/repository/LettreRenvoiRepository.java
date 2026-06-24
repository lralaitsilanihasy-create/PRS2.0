package cnm.prs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.LettreRenvoi;

@Repository
public interface LettreRenvoiRepository extends JpaRepository<LettreRenvoi, Integer> {

    /** Vrai si une lettre existe déjà pour cet examen (un examen → au plus une lettre). */
    boolean existsByIdExamen(Integer idExamen);

    /** Lettres de la localité (lettre → examen → dispatch → réception → contrôleur récepteur). */
    @Query("select l from LettreRenvoi l where l.examen.dispatch.reception.ctrlRecept.idLocalite = :loc")
    List<LettreRenvoi> findVisiblesParLocalite(@Param("loc") String loc);

    /** Vrai si la lettre relève de la localité (contrôle d'accès au {@code GET /{id}}). */
    @Query("""
            select (count(l) > 0) from LettreRenvoi l
            where l.idLettre = :id and l.examen.dispatch.reception.ctrlRecept.idLocalite = :loc
            """)
    boolean existsDansLocalite(@Param("id") Integer id, @Param("loc") String loc);
}
