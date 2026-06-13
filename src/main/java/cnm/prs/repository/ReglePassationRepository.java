package cnm.prs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.ReglePassation;

@Repository
public interface ReglePassationRepository extends JpaRepository<ReglePassation, Integer> {

    /**
     * Règles pour une situation et un ensemble de seuils, par priorité croissante
     * (la première est la suggestion retenue). §3.1, Module 02.
     */
    @Query("""
            select r from ReglePassation r
            where r.idSituation = :idSituation and r.idSeuil in :idSeuils
            order by r.priorite asc
            """)
    List<ReglePassation> findParSituationEtSeuils(@Param("idSituation") Integer idSituation,
            @Param("idSeuils") List<Integer> idSeuils);
}
