package cnm.prs.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Seuil;

@Repository
public interface SeuilRepository extends JpaRepository<Seuil, Integer> {

    /**
     * Seuils correspondant à une nature, une localité et un montant (bornes incluses ;
     * une borne nulle = non bornée). Sert à la suggestion du mode de passation (§3.1, M02).
     */
    @Query("""
            select s from Seuil s
            where s.idNature = :idNature and s.idLocalite = :idLocalite
              and (s.montantMin is null or :montant >= s.montantMin)
              and (s.montantMax is null or :montant <= s.montantMax)
            """)
    List<Seuil> findCorrespondants(@Param("idNature") Integer idNature,
            @Param("idLocalite") String idLocalite, @Param("montant") BigDecimal montant);
}
