package cnm.prs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.ExamenDetail;

@Repository
public interface ExamenDetailRepository extends JpaRepository<ExamenDetail, Integer> {

    /**
     * Statistiques de non-conformité par point de contrôle (§3.2 / §3.7).
     * Renvoie [idPointCtrl, libellé, nb total d'occurrences, nb non conformes].
     */
    @Query("""
            select ed.idPtControle, ed.ptControle.libelPointCtrl, count(ed),
                   sum(case when ed.conforme = false then 1L else 0L end)
            from ExamenDetail ed
            group by ed.idPtControle, ed.ptControle.libelPointCtrl
            """)
    List<Object[]> statsNonConformiteParPoint();
}
