package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Localite;

@Repository
public interface LocaliteRepository extends JpaRepository<Localite, String> {
}
