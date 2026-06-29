package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.LettreRenvoiLue;

@Repository
public interface LettreRenvoiLueRepository extends JpaRepository<LettreRenvoiLue, Integer> {

    /** Vrai si la PRMP a déjà lu la lettre (anti-doublon + flag {@code lue} du DTO). */
    boolean existsByIdLettreAndIdPrmp(Integer idLettre, String idPrmp);
}
