package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Avis;

@Repository
public interface AvisRepository extends JpaRepository<Avis, String> {
}
