package cnm.prs.mapper;

import cnm.prs.dto.SeuilDto;
import cnm.prs.entity.Seuil;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Seuil}.
 */
public final class SeuilMapper {

    private SeuilMapper() {
    }

    public static SeuilDto toDto(Seuil entity) {
        if (entity == null) {
            return null;
        }
        SeuilDto dto = new SeuilDto();
        dto.setIdSeuil(entity.getIdSeuil());
        dto.setMontantMin(entity.getMontantMin());
        dto.setMontantMax(entity.getMontantMax());
        dto.setIdNature(entity.getIdNature());
        dto.setIdLocalite(entity.getIdLocalite());
        return dto;
    }

    public static Seuil toEntity(SeuilDto dto) {
        if (dto == null) {
            return null;
        }
        Seuil entity = new Seuil();
        entity.setIdSeuil(dto.getIdSeuil());
        entity.setMontantMin(dto.getMontantMin());
        entity.setMontantMax(dto.getMontantMax());
        entity.setIdNature(dto.getIdNature());
        entity.setIdLocalite(dto.getIdLocalite());
        return entity;
    }
}
