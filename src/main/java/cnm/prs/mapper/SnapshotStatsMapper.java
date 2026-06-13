package cnm.prs.mapper;

import cnm.prs.dto.SnapshotStatsDto;
import cnm.prs.entity.SnapshotStats;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link SnapshotStats}.
 */
public final class SnapshotStatsMapper {

    private SnapshotStatsMapper() {
    }

    public static SnapshotStatsDto toDto(SnapshotStats entity) {
        if (entity == null) {
            return null;
        }
        SnapshotStatsDto dto = new SnapshotStatsDto();
        dto.setIdSnapshot(entity.getIdSnapshot());
        dto.setDateSnapshot(entity.getDateSnapshot());
        dto.setIdLocalite(entity.getIdLocalite());
        dto.setExercice(entity.getExercice());
        dto.setNbDossiersRecus(entity.getNbDossiersRecus());
        dto.setNbDossiersClotures(entity.getNbDossiersClotures());
        dto.setNbDossiersEnCours(entity.getNbDossiersEnCours());
        dto.setTauxConformite(entity.getTauxConformite());
        dto.setDelaiMoyenJours(entity.getDelaiMoyenJours());
        dto.setMontTotalControle(entity.getMontTotalControle());
        dto.setNbRetoursMoyen(entity.getNbRetoursMoyen());
        return dto;
    }

    public static SnapshotStats toEntity(SnapshotStatsDto dto) {
        if (dto == null) {
            return null;
        }
        SnapshotStats entity = new SnapshotStats();
        entity.setIdSnapshot(dto.getIdSnapshot());
        entity.setDateSnapshot(dto.getDateSnapshot());
        entity.setIdLocalite(dto.getIdLocalite());
        entity.setExercice(dto.getExercice());
        entity.setNbDossiersRecus(dto.getNbDossiersRecus());
        entity.setNbDossiersClotures(dto.getNbDossiersClotures());
        entity.setNbDossiersEnCours(dto.getNbDossiersEnCours());
        entity.setTauxConformite(dto.getTauxConformite());
        entity.setDelaiMoyenJours(dto.getDelaiMoyenJours());
        entity.setMontTotalControle(dto.getMontTotalControle());
        entity.setNbRetoursMoyen(dto.getNbRetoursMoyen());
        return entity;
    }
}
