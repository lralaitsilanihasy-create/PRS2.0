package cnm.prs.mapper;

import cnm.prs.dto.MinistereDto;
import cnm.prs.entity.Ministere;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Ministere}.
 */
public final class MinistereMapper {

    private MinistereMapper() {
    }

    public static MinistereDto toDto(Ministere entity) {
        if (entity == null) {
            return null;
        }
        MinistereDto dto = new MinistereDto();
        dto.setIdMinistere(entity.getIdMinistere());
        dto.setLibelleMinistere(entity.getLibelleMinistere());
        dto.setSigle(entity.getSigle());
        return dto;
    }

    public static Ministere toEntity(MinistereDto dto) {
        if (dto == null) {
            return null;
        }
        Ministere entity = new Ministere();
        entity.setIdMinistere(dto.getIdMinistere());
        entity.setLibelleMinistere(dto.getLibelleMinistere());
        entity.setSigle(dto.getSigle());
        return entity;
    }
}
