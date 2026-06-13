package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.EntiteContract;

@Repository
public interface EntiteContractRepository extends JpaRepository<EntiteContract, Integer> {
}
