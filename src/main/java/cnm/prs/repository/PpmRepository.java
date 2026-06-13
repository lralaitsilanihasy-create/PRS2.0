package cnm.prs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Ppm;

@Repository
public interface PpmRepository extends JpaRepository<Ppm, Integer> {

    /** PPM rattachés à un dossier (pour résoudre localité/exercice à la soumission, §3.1). */
    List<Ppm> findByIdDossier(Integer idDossier);

    /** Vrai si le dossier porte au moins un PPM (cohérence type↔contenu). */
    boolean existsByIdDossier(Integer idDossier);
}
