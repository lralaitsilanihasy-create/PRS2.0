package cnm.prs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.DemandeRetraitVue;

@Repository
public interface DemandeRetraitVueRepository extends JpaRepository<DemandeRetraitVue, Integer> {

    /** Dernière consultation de l'écran « Demandes de retrait » par la PRMP (une seule ligne). */
    Optional<DemandeRetraitVue> findByIdPrmp(String idPrmp);
}
