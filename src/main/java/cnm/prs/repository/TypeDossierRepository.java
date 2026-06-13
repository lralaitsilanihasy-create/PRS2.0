package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.TypeDossier;

@Repository
public interface TypeDossierRepository extends JpaRepository<TypeDossier, String> {
}
