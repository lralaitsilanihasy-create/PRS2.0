package cnm.prs.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Dossier;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, Integer> {

    /** Dossiers dont la date de référence est dans la période (pour les rapports périodiques). */
    List<Dossier> findByDateRefBetween(LocalDate debut, LocalDate fin);

    /** Comptage des dossiers par statut (pipeline du tableau de bord, §3.2). [statut, nombre] */
    @Query("select d.statut, count(d) from Dossier d group by d.statut")
    List<Object[]> compterParStatut();

    /** Statut du dossier rattaché à une réception (précondition du dispatch, §2.2/§2.3). */
    @Query("select d.statut from Dossier d, Reception r where r.idReception = :idReception and d.idDossier = r.idDossier")
    Optional<String> findStatutByReception(@Param("idReception") Integer idReception);

    /** Statut du dossier rattaché à un dispatch, via sa réception (précondition de l'examen, §2.4). */
    @Query("""
            select d.statut from Dossier d, Reception r, Dispatch di
            where di.idDispatch = :idDispatch and r.idReception = di.idReception and d.idDossier = r.idDossier
            """)
    Optional<String> findStatutByDispatch(@Param("idDispatch") Integer idDispatch);

    /**
     * Dossiers visibles d'une localité (§1) : un dossier appartient à la localité du contrôleur
     * qui l'a réceptionné ({@code Reception.imCtrlRecept → Controleur.idLocalite}) <strong>ou</strong>,
     * pour un dossier soumis pas encore réceptionné, de la localité de son PPM
     * ({@code Ppm.idLocalite}) — afin que le Secrétaire le voie avant la réception (Option A).
     */
    @Query("""
            select d from Dossier d where
                (d.statut is null or d.statut <> 'BROUILLON')
            and (
                d.idLocalite = :localite
             or exists (select 1 from Reception r
                        where r.idDossier = d.idDossier and r.ctrlRecept.idLocalite = :localite)
             or exists (select 1 from Ppm p
                        where p.idDossier = d.idDossier and p.idLocalite = :localite))
            """)
    List<Dossier> findVisiblesParLocalite(@Param("localite") String localite);

    /**
     * Vrai si le dossier est visible dans la localité donnée — via sa propre localité, sa réception
     * ou son PPM — et qu'il n'est pas un brouillon (les brouillons sont masqués aux contrôleurs).
     */
    @Query("""
            select (count(d) > 0) from Dossier d where d.idDossier = :idDossier
            and (d.statut is null or d.statut <> 'BROUILLON')
            and (
                d.idLocalite = :localite
             or exists (select 1 from Reception r
                        where r.idDossier = d.idDossier and r.ctrlRecept.idLocalite = :localite)
             or exists (select 1 from Ppm p
                        where p.idDossier = d.idDossier and p.idLocalite = :localite))
            """)
    boolean existsDansLocalite(@Param("idDossier") Integer idDossier, @Param("localite") String localite);

    /**
     * Dossiers d'une PRMP (§3.1) : ceux dont elle est <strong>propriétaire</strong>
     * ({@code t_dossier.ID_PRMP}, posé à la saisie — y compris les brouillons DAO/MAOO sans PPM),
     * ses PPM ({@code t_ppm.ID_PRMP}) et les marchés rattachés à ses PPM.
     */
    @Query("""
            select d from Dossier d where
               d.idPrmp = :idPrmp
               or exists (select 1 from Ppm p where p.idDossier = d.idDossier and p.idPrmp = :idPrmp)
               or exists (select 1 from Marche m, Ppm p2
                          where m.idDossier = d.idDossier and m.idPpm = p2.idPpm and p2.idPrmp = :idPrmp)
            """)
    List<Dossier> findVisiblesPourPrmp(@Param("idPrmp") String idPrmp);

    /** Vrai si le dossier appartient à la PRMP (propriétaire {@code t_dossier.ID_PRMP}, ou via PPM/marché). */
    @Query("""
            select (count(d) > 0) from Dossier d where d.idDossier = :idDossier and (
               d.idPrmp = :idPrmp
               or exists (select 1 from Ppm p where p.idDossier = d.idDossier and p.idPrmp = :idPrmp)
               or exists (select 1 from Marche m, Ppm p2
                          where m.idDossier = d.idDossier and m.idPpm = p2.idPpm and p2.idPrmp = :idPrmp))
            """)
    boolean existsVisiblePourPrmp(@Param("idDossier") Integer idDossier, @Param("idPrmp") String idPrmp);
}
