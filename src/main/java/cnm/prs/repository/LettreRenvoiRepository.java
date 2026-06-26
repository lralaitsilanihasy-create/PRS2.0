package cnm.prs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.LettreRenvoi;

@Repository
public interface LettreRenvoiRepository extends JpaRepository<LettreRenvoi, Integer> {

    /** Nombre de lettres de renvoi à un statut donné (compteur du tableau de bord). */
    long countByStatut(String statut);

    /** Lettres d'un Membre : celles de ses examens (attributaire {@code Examen.imCtrlMembre}). */
    @Query("select l from LettreRenvoi l where l.examen.imCtrlMembre = :im")
    List<LettreRenvoi> findByMembre(@Param("im") String im);

    /** Lettres d'un statut donné dont l'examen relève d'une localité (via examen→dispatch→réception). */
    @Query("""
            select l from LettreRenvoi l
            where l.statut = :statut and l.examen.dispatch.reception.ctrlRecept.idLocalite = :loc
            """)
    List<LettreRenvoi> findByStatutEtLocalite(@Param("statut") String statut, @Param("loc") String loc);

    /** Lettres SIGNE concernant les dossiers d'une PRMP (via PPM du dossier). */
    @Query("""
            select l from LettreRenvoi l
            where l.statut = 'SIGNE'
              and exists (select 1 from Ppm p where p.idDossier = l.idDossier and p.idPrmp = :idPrmp)
            """)
    List<LettreRenvoi> findSigneesPourPrmp(@Param("idPrmp") String idPrmp);

    /** Vrai si la lettre relève de la localité (contrôle d'accès au {@code GET /{id}}). */
    @Query("""
            select (count(l) > 0) from LettreRenvoi l
            where l.idLettre = :id and l.examen.dispatch.reception.ctrlRecept.idLocalite = :loc
            """)
    boolean existsDansLocalite(@Param("id") Integer id, @Param("loc") String loc);
}
