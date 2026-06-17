package cnm.prs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Dispatch;

@Repository
public interface DispatchRepository extends JpaRepository<Dispatch, Integer> {

    /** Matricule du Membre attributaire d'un dispatch — pour réserver l'examen à l'attributaire (§2.4). */
    @Query("select d.imCtrlMembre from Dispatch d where d.idDispatch = :id")
    Optional<String> findImCtrlMembreById(@Param("id") Integer id);

    @Query("select d from Dispatch d where d.reception.ctrlRecept.idLocalite = :loc")
    List<Dispatch> findVisiblesParLocalite(@Param("loc") String loc);

    @Query("select (count(d) > 0) from Dispatch d where d.idDispatch = :id and d.reception.ctrlRecept.idLocalite = :loc")
    boolean existsDansLocalite(@Param("id") Integer id, @Param("loc") String loc);

    /** Localité d'un dispatch (via réception → contrôleur réceptionnaire). */
    @Query("select d.reception.ctrlRecept.idLocalite from Dispatch d where d.idDispatch = :id")
    String findLocaliteById(@Param("id") Integer id);

    /** Vrai si un dispatch existe déjà pour cette réception (anti-doublon, §3.2). */
    boolean existsByIdReception(Integer idReception);
}
