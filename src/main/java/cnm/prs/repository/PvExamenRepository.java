package cnm.prs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.PvExamen;

@Repository
public interface PvExamenRepository extends JpaRepository<PvExamen, Integer> {

    /**
     * Identifiant(s) PRMP rattaché(s) à un PV, via la chaîne
     * PV → examen → dispatch → réception → dossier → PPM. Sert à notifier la PRMP du PV signé.
     */
    @Query("""
            select distinct p.idPrmp from PvExamen pv, Examen e, Dispatch d, Reception r, Ppm p
            where pv.idPv = :idPv and e.idExamen = pv.idExamen and d.idDispatch = e.idDispatch
              and r.idReception = d.idReception and p.idDossier = r.idDossier and p.idPrmp is not null
            """)
    List<String> findIdPrmpByPv(@Param("idPv") Integer idPv);

    /** Statut d'un PV (précondition de la vérification : doit être SIGNE, §3.6). */
    @Query("select pv.statutPv from PvExamen pv where pv.idPv = :id")
    Optional<String> findStatutById(@Param("id") Integer id);

    @Query("select pv from PvExamen pv where pv.examen.dispatch.reception.ctrlRecept.idLocalite = :loc")
    List<PvExamen> findVisiblesParLocalite(@Param("loc") String loc);

    @Query("select (count(pv) > 0) from PvExamen pv where pv.idPv = :id "
            + "and pv.examen.dispatch.reception.ctrlRecept.idLocalite = :loc")
    boolean existsDansLocalite(@Param("id") Integer id, @Param("loc") String loc);
}
