package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.RegleAlerte;

@Repository
public interface RegleAlerteRepository extends JpaRepository<RegleAlerte, Integer> {
}
