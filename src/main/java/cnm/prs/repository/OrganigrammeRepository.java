package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Organigramme;

@Repository
public interface OrganigrammeRepository extends JpaRepository<Organigramme, Integer> {
}
