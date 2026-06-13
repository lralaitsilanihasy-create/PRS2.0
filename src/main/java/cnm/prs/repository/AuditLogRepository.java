package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** Plus grand ID_LOG existant (0 si table vide) — pour générer la PK assignée. */
    @Query("select coalesce(max(a.idLog), 0) from AuditLog a")
    Long findMaxId();
}
