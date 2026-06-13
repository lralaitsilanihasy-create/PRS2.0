package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.IndicateurPrmp;

@Repository
public interface IndicateurPrmpRepository extends JpaRepository<IndicateurPrmp, Integer> {
}
