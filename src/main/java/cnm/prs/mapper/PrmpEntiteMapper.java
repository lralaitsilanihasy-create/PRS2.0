package cnm.prs.mapper;

import cnm.prs.dto.PrmpEntiteDto;
import cnm.prs.entity.PrmpEntite;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link PrmpEntite}.
 */
public final class PrmpEntiteMapper {

    private PrmpEntiteMapper() {
    }

    public static PrmpEntiteDto toDto(PrmpEntite entity) {
        if (entity == null) {
            return null;
        }
        PrmpEntiteDto dto = new PrmpEntiteDto();
        dto.setIdPrmpEntite(entity.getIdPrmpEntite());
        dto.setIdPrmp(entity.getIdPrmp());
        dto.setIdEntiteContract(entity.getIdEntiteContract());
        dto.setDateAffectation(entity.getDateAffectation());
        dto.setActif(entity.getActif());
        return dto;
    }

    public static PrmpEntite toEntity(PrmpEntiteDto dto) {
        if (dto == null) {
            return null;
        }
        PrmpEntite entity = new PrmpEntite();
        entity.setIdPrmpEntite(dto.getIdPrmpEntite());
        entity.setIdPrmp(dto.getIdPrmp());
        entity.setIdEntiteContract(dto.getIdEntiteContract());
        entity.setDateAffectation(dto.getDateAffectation());
        entity.setActif(dto.getActif());
        return entity;
    }
}
