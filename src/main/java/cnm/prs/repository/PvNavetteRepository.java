package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.PvNavette;

@Repository
public interface PvNavetteRepository extends JpaRepository<PvNavette, Integer> {

    /** Plus grand ID_NAVETTE existant (0 si table vide) — pour générer la PK assignée. */
    @Query("select coalesce(max(n.idNavette), 0) from PvNavette n")
    Integer findMaxIdNavette();

    /** Plus grand NUM_NAVETTE pour un PV donné (0 si aucune navette) — pour incrémenter. */
    @Query("select coalesce(max(n.numNavette), 0) from PvNavette n where n.idPv = :idPv")
    Integer findMaxNumNavetteByPv(@Param("idPv") Integer idPv);
}
