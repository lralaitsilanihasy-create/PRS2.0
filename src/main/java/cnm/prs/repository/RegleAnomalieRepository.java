package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.RegleAnomalie;

@Repository
public interface RegleAnomalieRepository extends JpaRepository<RegleAnomalie, Integer> {
}
