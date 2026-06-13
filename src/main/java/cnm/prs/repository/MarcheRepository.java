package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Marche;

@Repository
public interface MarcheRepository extends JpaRepository<Marche, Integer> {
}
