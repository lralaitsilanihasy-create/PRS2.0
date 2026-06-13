package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Anomalie;

@Repository
public interface AnomalieRepository extends JpaRepository<Anomalie, Integer> {
}
