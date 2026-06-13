package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.CopieDossier;

@Repository
public interface CopieDossierRepository extends JpaRepository<CopieDossier, Integer> {
}
