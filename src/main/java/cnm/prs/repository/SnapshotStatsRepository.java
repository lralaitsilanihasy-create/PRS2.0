package cnm.prs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.SnapshotStats;

@Repository
public interface SnapshotStatsRepository extends JpaRepository<SnapshotStats, Integer> {
}
