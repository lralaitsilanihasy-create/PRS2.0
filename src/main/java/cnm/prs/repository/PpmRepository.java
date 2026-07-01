package cnm.prs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Ppm;

@Repository
public interface PpmRepository extends JpaRepository<Ppm, Integer> {

    /** PPM rattachés à un dossier (pour résoudre localité/exercice à la soumission, §3.1). */
    List<Ppm> findByIdDossier(Integer idDossier);

    /** Vrai si le dossier porte au moins un PPM (cohérence type↔contenu). */
    boolean existsByIdDossier(Integer idDossier);

    /** PPM d'une PRMP (§3.1) — son périmètre propre, brouillons compris. */
    List<Ppm> findByIdPrmp(String idPrmp);

    /** Nombre de PPM d'une PRMP (compteur « Mes PPM & marchés » du menu PRMP). */
    long countByIdPrmp(String idPrmp);

    /** Vrai si le PPM appartient à la PRMP (contrôle de visibilité de {@code GET /{id}}). */
    boolean existsByIdPpmAndIdPrmp(Integer idPpm, String idPrmp);

    /** Vrai si le dossier appartient à la PRMP (via son PPM) — propriété pour l'accès aux lettres. */
    boolean existsByIdDossierAndIdPrmp(Integer idDossier, String idPrmp);

    /**
     * PPM visibles d'une localité (§1) : ceux dont le dossier est de la localité donnée et
     * <strong>n'est pas un brouillon</strong> (les brouillons restent invisibles des contrôleurs).
     * La localité fait foi via {@code t_dossier.ID_LOCALITE}, estampillée à la soumission.
     */
    @Query("""
            select p from Ppm p, Dossier d
            where d.idDossier = p.idDossier
              and (d.statut is null or d.statut <> 'BROUILLON')
              and d.idLocalite = :localite
            """)
    List<Ppm> findVisiblesParLocalite(@Param("localite") String localite);

    /**
     * PPM de la PRMP pour l'écran « Mes PPM &amp; marchés » : ceux dont elle est propriétaire et
     * dont le dossier <strong>n'est pas un brouillon</strong> (les brouillons ont leur propre écran
     * « Mes brouillons »). Filtrage serveur — ne pas se reposer sur un masquage front.
     */
    @Query("""
            select p from Ppm p, Dossier d
            where d.idDossier = p.idDossier
              and p.idPrmp = :idPrmp
              and (d.statut is null or d.statut <> 'BROUILLON')
            """)
    List<Ppm> findVisiblesParPrmp(@Param("idPrmp") String idPrmp);

    /**
     * Compteur du badge « Mes PPM &amp; marchés » — même critère que {@link #findVisiblesParPrmp}
     * (PPM de la PRMP, dossier non brouillon) : le badge colle à la taille de la liste.
     */
    @Query("""
            select count(p) from Ppm p, Dossier d
            where d.idDossier = p.idDossier
              and p.idPrmp = :idPrmp
              and (d.statut is null or d.statut <> 'BROUILLON')
            """)
    long countVisiblesParPrmp(@Param("idPrmp") String idPrmp);

    /** Vrai si le PPM est visible dans la localité (dossier non brouillon, même localité). */
    @Query("""
            select (count(p) > 0) from Ppm p, Dossier d
            where p.idPpm = :idPpm and d.idDossier = p.idDossier
              and (d.statut is null or d.statut <> 'BROUILLON')
              and d.idLocalite = :localite
            """)
    boolean existsVisibleParLocalite(@Param("idPpm") Integer idPpm, @Param("localite") String localite);

    /** Prochaine PK PPM, allouée par la séquence serveur (Voie B — l'id client est ignoré). */
    @Query(value = "select nextval('seq_ppm')", nativeQuery = true)
    Long nextIdPpm();
}
