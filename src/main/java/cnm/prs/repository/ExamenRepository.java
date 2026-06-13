package cnm.prs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Examen;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Integer> {

    @Query("select e from Examen e where e.dispatch.reception.ctrlRecept.idLocalite = :loc")
    List<Examen> findVisiblesParLocalite(@Param("loc") String loc);

    @Query("select (count(e) > 0) from Examen e where e.idExamen = :id and e.dispatch.reception.ctrlRecept.idLocalite = :loc")
    boolean existsDansLocalite(@Param("id") Integer id, @Param("loc") String loc);
}
