package cnm.prs.mapper;

import cnm.prs.dto.MarchePrevisionDto;
import cnm.prs.entity.MarchePrevision;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link MarchePrevision}.
 */
public final class MarchePrevisionMapper {

    private MarchePrevisionMapper() {
    }

    public static MarchePrevisionDto toDto(MarchePrevision entity) {
        if (entity == null) {
            return null;
        }
        MarchePrevisionDto dto = new MarchePrevisionDto();
        dto.setIdPrevision(entity.getIdPrevision());
        dto.setIdDetail(entity.getIdDetail());
        dto.setTypeDate(entity.getTypeDate());
        dto.setDatePrev(entity.getDatePrev());
        return dto;
    }

    public static MarchePrevision toEntity(MarchePrevisionDto dto) {
        if (dto == null) {
            return null;
        }
        MarchePrevision entity = new MarchePrevision();
        entity.setIdPrevision(dto.getIdPrevision());
        entity.setIdDetail(dto.getIdDetail());
        entity.setTypeDate(dto.getTypeDate());
        entity.setDatePrev(dto.getDatePrev());
        return entity;
    }
}
