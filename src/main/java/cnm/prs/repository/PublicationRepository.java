package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Publication;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Integer> {

    /** Nombre de publications à un statut donné (compteurs du menu Chargé de publication). */
    long countByStatutPubli(String statutPubli);
}
