package cnm.prs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.PrmpEntiteDemande;

@Repository
public interface PrmpEntiteDemandeRepository extends JpaRepository<PrmpEntiteDemande, Integer> {

    /** Toutes les déclarations d'une inscription. */
    List<PrmpEntiteDemande> findByLogin(String login);

    /** Déclarations d'une inscription dans un état donné (ex. EN_ATTENTE). */
    List<PrmpEntiteDemande> findByLoginAndStatutDemande(String login, String statutDemande);

    /** Plus grand ID_DEMANDE existant (0 si table vide) — pour générer la PK assignée. */
    @Query("select coalesce(max(d.idDemande), 0) from PrmpEntiteDemande d")
    Integer findMaxId();
}
