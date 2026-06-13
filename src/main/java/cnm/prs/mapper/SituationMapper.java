package cnm.prs.mapper;

import cnm.prs.dto.SituationDto;
import cnm.prs.entity.Situation;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Situation}.
 */
public final class SituationMapper {

    private SituationMapper() {
    }

    public static SituationDto toDto(Situation entity) {
        if (entity == null) {
            return null;
        }
        SituationDto dto = new SituationDto();
        dto.setIdSituation(entity.getIdSituation());
        dto.setLibelle(entity.getLibelle());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    public static Situation toEntity(SituationDto dto) {
        if (dto == null) {
            return null;
        }
        Situation entity = new Situation();
        entity.setIdSituation(dto.getIdSituation());
        entity.setLibelle(dto.getLibelle());
        entity.setDescription(dto.getDescription());
        return entity;
    }
}
