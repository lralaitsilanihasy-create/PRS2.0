package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Capm;

@Repository
public interface CapmRepository extends JpaRepository<Capm, Integer> {
}
