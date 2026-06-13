package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Nature;

@Repository
public interface NatureRepository extends JpaRepository<Nature, Integer> {
}
