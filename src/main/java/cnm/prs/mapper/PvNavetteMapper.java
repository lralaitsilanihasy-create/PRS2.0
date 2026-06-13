package cnm.prs.mapper;

import cnm.prs.dto.PvNavetteDto;
import cnm.prs.entity.PvNavette;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link PvNavette}.
 */
public final class PvNavetteMapper {

    private PvNavetteMapper() {
    }

    public static PvNavetteDto toDto(PvNavette entity) {
        if (entity == null) {
            return null;
        }
        PvNavetteDto dto = new PvNavetteDto();
        dto.setIdNavette(entity.getIdNavette());
        dto.setIdPv(entity.getIdPv());
        dto.setNumNavette(entity.getNumNavette());
        dto.setSens(entity.getSens());
        dto.setImActeur(entity.getImActeur());
        dto.setDateAction(entity.getDateAction());
        dto.setCommentaire(entity.getCommentaire());
        return dto;
    }

    public static PvNavette toEntity(PvNavetteDto dto) {
        if (dto == null) {
            return null;
        }
        PvNavette entity = new PvNavette();
        entity.setIdNavette(dto.getIdNavette());
        entity.setIdPv(dto.getIdPv());
        entity.setNumNavette(dto.getNumNavette());
        entity.setSens(dto.getSens());
        entity.setImActeur(dto.getImActeur());
        entity.setDateAction(dto.getDateAction());
        entity.setCommentaire(dto.getCommentaire());
        return entity;
    }
}
