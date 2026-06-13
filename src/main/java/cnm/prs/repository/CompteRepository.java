package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Compte;

@Repository
public interface CompteRepository extends JpaRepository<Compte, String> {
}
