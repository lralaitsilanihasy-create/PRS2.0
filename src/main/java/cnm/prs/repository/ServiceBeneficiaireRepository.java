package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.ServiceBeneficiaire;

@Repository
public interface ServiceBeneficiaireRepository extends JpaRepository<ServiceBeneficiaire, Integer> {
}
