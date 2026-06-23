package cnm.prs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.ObservationControle;

@Repository
public interface ObservationControleRepository extends JpaRepository<ObservationControle, Integer> {

    /** Lignes d'observation d'un point de contrôle, triées par ordre de saisie ASC. */
    List<ObservationControle> findByIdDetailOrderByOrdreAsc(Integer idDetail);

    /** Supprime les lignes d'observation d'un point de contrôle (replace-on-save / cascade). */
    void deleteByIdDetail(Integer idDetail);
}
