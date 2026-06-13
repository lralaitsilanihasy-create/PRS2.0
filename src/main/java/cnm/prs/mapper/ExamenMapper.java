package cnm.prs.mapper;

import cnm.prs.dto.ExamenDto;
import cnm.prs.entity.Examen;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Examen}.
 */
public final class ExamenMapper {

    private ExamenMapper() {
    }

    public static ExamenDto toDto(Examen entity) {
        if (entity == null) {
            return null;
        }
        ExamenDto dto = new ExamenDto();
        dto.setIdExamen(entity.getIdExamen());
        dto.setIdDispatch(entity.getIdDispatch());
        dto.setImCtrlMembre(entity.getImCtrlMembre());
        dto.setDateExamen(entity.getDateExamen());
        return dto;
    }

    public static Examen toEntity(ExamenDto dto) {
        if (dto == null) {
            return null;
        }
        Examen entity = new Examen();
        entity.setIdExamen(dto.getIdExamen());
        entity.setIdDispatch(dto.getIdDispatch());
        entity.setImCtrlMembre(dto.getImCtrlMembre());
        entity.setDateExamen(dto.getDateExamen());
        return entity;
    }
}
