package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Lot;

@Repository
public interface LotRepository extends JpaRepository<Lot, Integer> {
}
