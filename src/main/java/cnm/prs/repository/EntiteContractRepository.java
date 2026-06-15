package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.EntiteContract;

@Repository
public interface EntiteContractRepository extends JpaRepository<EntiteContract, Integer> {

    /** Plus grand ID_ENTITE_CONTRACT existant (0 si table vide) — pour générer la PK assignée
     *  lors de la création d'une entité proposée à la validation d'une inscription. */
    @Query("select coalesce(max(e.idEntiteContract), 0) from EntiteContract e")
    Integer findMaxId();
}
