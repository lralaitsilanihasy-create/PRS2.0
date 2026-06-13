package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.CatCompte;

@Repository
public interface CatCompteRepository extends JpaRepository<CatCompte, String> {
}
