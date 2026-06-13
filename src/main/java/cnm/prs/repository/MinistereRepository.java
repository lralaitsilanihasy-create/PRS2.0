package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Ministere;

@Repository
public interface MinistereRepository extends JpaRepository<Ministere, Integer> {
}
