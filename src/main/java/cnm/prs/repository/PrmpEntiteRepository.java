package cnm.prs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.PrmpEntite;

@Repository
public interface PrmpEntiteRepository extends JpaRepository<PrmpEntite, Integer> {

    /** Vrai si l'entité fait partie des entités <strong>actives</strong> de la PRMP (§3.1). */
    boolean existsByIdPrmpAndIdEntiteContractAndActifTrue(String idPrmp, Integer idEntiteContract);

    /** Affectations actives d'une PRMP (ses entités contractantes). */
    List<PrmpEntite> findByIdPrmpAndActifTrue(String idPrmp);
}
