package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Prmp;

@Repository
public interface PrmpRepository extends JpaRepository<Prmp, String> {
}
