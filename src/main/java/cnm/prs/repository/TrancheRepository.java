package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Tranche;

@Repository
public interface TrancheRepository extends JpaRepository<Tranche, Integer> {
}
