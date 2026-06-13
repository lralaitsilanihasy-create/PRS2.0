package cnm.prs.mapper;

import cnm.prs.dto.AnomalieDto;
import cnm.prs.entity.Anomalie;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Anomalie}.
 */
public final class AnomalieMapper {

    private AnomalieMapper() {
    }

    public static AnomalieDto toDto(Anomalie entity) {
        if (entity == null) {
            return null;
        }
        AnomalieDto dto = new AnomalieDto();
        dto.setIdAnomalie(entity.getIdAnomalie());
        dto.setIdDetail(entity.getIdDetail());
        dto.setIdPpm(entity.getIdPpm());
        dto.setIdRegleAnomalie(entity.getIdRegleAnomalie());
        dto.setTypeAnomalie(entity.getTypeAnomalie());
        dto.setGravite(entity.getGravite());
        dto.setDescription(entity.getDescription());
        dto.setDateDetection(entity.getDateDetection());
        dto.setSource(entity.getSource());
        dto.setStatut(entity.getStatut());
        dto.setImTraitement(entity.getImTraitement());
        dto.setDateTraitement(entity.getDateTraitement());
        dto.setCommentaireTraitement(entity.getCommentaireTraitement());
        return dto;
    }

    public static Anomalie toEntity(AnomalieDto dto) {
        if (dto == null) {
            return null;
        }
        Anomalie entity = new Anomalie();
        entity.setIdAnomalie(dto.getIdAnomalie());
        entity.setIdDetail(dto.getIdDetail());
        entity.setIdPpm(dto.getIdPpm());
        entity.setIdRegleAnomalie(dto.getIdRegleAnomalie());
        entity.setTypeAnomalie(dto.getTypeAnomalie());
        entity.setGravite(dto.getGravite());
        entity.setDescription(dto.getDescription());
        entity.setDateDetection(dto.getDateDetection());
        entity.setSource(dto.getSource());
        entity.setStatut(dto.getStatut());
        entity.setImTraitement(dto.getImTraitement());
        entity.setDateTraitement(dto.getDateTraitement());
        entity.setCommentaireTraitement(dto.getCommentaireTraitement());
        return entity;
    }
}
