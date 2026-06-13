package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    /** Plus grand ID_NOTIFICATION existant (0 si table vide) — pour générer la PK assignée. */
    @Query("select coalesce(max(n.idNotification), 0) from Notification n")
    Integer findMaxId();
}
