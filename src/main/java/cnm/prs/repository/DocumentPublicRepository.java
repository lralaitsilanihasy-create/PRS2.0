package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.DocumentPublic;

@Repository
public interface DocumentPublicRepository extends JpaRepository<DocumentPublic, Integer> {
}
