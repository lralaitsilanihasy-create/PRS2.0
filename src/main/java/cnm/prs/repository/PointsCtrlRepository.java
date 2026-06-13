package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.PointsCtrl;

@Repository
public interface PointsCtrlRepository extends JpaRepository<PointsCtrl, Integer> {
}
