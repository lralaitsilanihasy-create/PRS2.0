package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.SoaBeneficiaire;

@Repository
public interface SoaBeneficiaireRepository extends JpaRepository<SoaBeneficiaire, String> {
}
