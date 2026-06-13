package cnm.prs.mapper;

import cnm.prs.dto.RegleAnomalieDto;
import cnm.prs.entity.RegleAnomalie;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link RegleAnomalie}.
 */
public final class RegleAnomalieMapper {

    private RegleAnomalieMapper() {
    }

    public static RegleAnomalieDto toDto(RegleAnomalie entity) {
        if (entity == null) {
            return null;
        }
        RegleAnomalieDto dto = new RegleAnomalieDto();
        dto.setIdRegleAnomalie(entity.getIdRegleAnomalie());
        dto.setCodeRegle(entity.getCodeRegle());
        dto.setLibelle(entity.getLibelle());
        dto.setParametreNum(entity.getParametreNum());
        dto.setParametreTxt(entity.getParametreTxt());
        dto.setActif(entity.getActif());
        dto.setGraviteDefaut(entity.getGraviteDefaut());
        return dto;
    }

    public static RegleAnomalie toEntity(RegleAnomalieDto dto) {
        if (dto == null) {
            return null;
        }
        RegleAnomalie entity = new RegleAnomalie();
        entity.setIdRegleAnomalie(dto.getIdRegleAnomalie());
        entity.setCodeRegle(dto.getCodeRegle());
        entity.setLibelle(dto.getLibelle());
        entity.setParametreNum(dto.getParametreNum());
        entity.setParametreTxt(dto.getParametreTxt());
        entity.setActif(dto.getActif());
        entity.setGraviteDefaut(dto.getGraviteDefaut());
        return entity;
    }
}
