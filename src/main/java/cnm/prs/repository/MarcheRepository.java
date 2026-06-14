package cnm.prs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Marche;

@Repository
public interface MarcheRepository extends JpaRepository<Marche, Integer> {

    /** Lignes de marché d'un dossier (réconciliation à l'édition d'un brouillon). */
    List<Marche> findByIdDossier(Integer idDossier);
}
