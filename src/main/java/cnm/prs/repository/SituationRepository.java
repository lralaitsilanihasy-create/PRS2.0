package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Situation;

@Repository
public interface SituationRepository extends JpaRepository<Situation, Integer> {
}
