package cnm.prs.mapper;

import cnm.prs.dto.ExamenDetailDto;
import cnm.prs.entity.ExamenDetail;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link ExamenDetail}.
 */
public final class ExamenDetailMapper {

    private ExamenDetailMapper() {
    }

    public static ExamenDetailDto toDto(ExamenDetail entity) {
        if (entity == null) {
            return null;
        }
        ExamenDetailDto dto = new ExamenDetailDto();
        dto.setIdDetailExamen(entity.getIdDetailExamen());
        dto.setIdExamen(entity.getIdExamen());
        dto.setIdPtControle(entity.getIdPtControle());
        dto.setConforme(entity.getConforme());
        dto.setObservation(entity.getObservation());
        dto.setObsSiNonConforme(entity.getObsSiNonConforme());
        return dto;
    }

    public static ExamenDetail toEntity(ExamenDetailDto dto) {
        if (dto == null) {
            return null;
        }
        ExamenDetail entity = new ExamenDetail();
        entity.setIdDetailExamen(dto.getIdDetailExamen());
        entity.setIdExamen(dto.getIdExamen());
        entity.setIdPtControle(dto.getIdPtControle());
        entity.setConforme(dto.getConforme());
        entity.setObservation(dto.getObservation());
        entity.setObsSiNonConforme(dto.getObsSiNonConforme());
        return entity;
    }
}
