package cnm.prs.mapper;

import cnm.prs.dto.ReglePassationDto;
import cnm.prs.entity.ReglePassation;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link ReglePassation}.
 */
public final class ReglePassationMapper {

    private ReglePassationMapper() {
    }

    public static ReglePassationDto toDto(ReglePassation entity) {
        if (entity == null) {
            return null;
        }
        ReglePassationDto dto = new ReglePassationDto();
        dto.setIdRegle(entity.getIdRegle());
        dto.setIdSituation(entity.getIdSituation());
        dto.setIdSeuil(entity.getIdSeuil());
        dto.setIdMode(entity.getIdMode());
        dto.setPriorite(entity.getPriorite());
        return dto;
    }

    public static ReglePassation toEntity(ReglePassationDto dto) {
        if (dto == null) {
            return null;
        }
        ReglePassation entity = new ReglePassation();
        entity.setIdRegle(dto.getIdRegle());
        entity.setIdSituation(dto.getIdSituation());
        entity.setIdSeuil(dto.getIdSeuil());
        entity.setIdMode(dto.getIdMode());
        entity.setPriorite(dto.getPriorite());
        return entity;
    }
}
