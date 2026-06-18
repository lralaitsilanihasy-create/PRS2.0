package cnm.prs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.MarchePrevision;

@Repository
public interface MarchePrevisionRepository extends JpaRepository<MarchePrevision, Integer> {

    /** Dates prévisionnelles d'un marché donné. */
    List<MarchePrevision> findByIdDetail(Integer idDetail);

    /** Supprime les dates prévisionnelles d'un marché (cascade applicative à la suppression du marché). */
    long deleteByIdDetail(Integer idDetail);
}
