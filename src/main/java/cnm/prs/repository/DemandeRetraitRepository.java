package cnm.prs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.DemandeRetrait;

@Repository
public interface DemandeRetraitRepository extends JpaRepository<DemandeRetrait, Integer> {

    /** Demandes d'une PRMP (suivi de ses propres demandes, §3.1). */
    List<DemandeRetrait> findByIdPrmp(String idPrmp);

    @Query("""
            select dr from DemandeRetrait dr where exists (
                select 1 from Reception r where r.idDossier = dr.idDossier and r.ctrlRecept.idLocalite = :loc)
            """)
    List<DemandeRetrait> findVisiblesParLocalite(@Param("loc") String loc);

    @Query("""
            select (count(dr) > 0) from DemandeRetrait dr where dr.idDemandeRetrait = :id and exists (
                select 1 from Reception r where r.idDossier = dr.idDossier and r.ctrlRecept.idLocalite = :loc)
            """)
    boolean existsDansLocalite(@Param("id") Integer id, @Param("loc") String loc);
}
