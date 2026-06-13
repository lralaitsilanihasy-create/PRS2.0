package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Publication;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Integer> {
}
