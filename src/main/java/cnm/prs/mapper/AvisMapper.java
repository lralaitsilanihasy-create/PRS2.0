package cnm.prs.mapper;

import cnm.prs.dto.AvisDto;
import cnm.prs.entity.Avis;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Avis}.
 */
public final class AvisMapper {

    private AvisMapper() {
    }

    public static AvisDto toDto(Avis entity) {
        if (entity == null) {
            return null;
        }
        AvisDto dto = new AvisDto();
        dto.setIdAvis(entity.getIdAvis());
        dto.setLibelleAvis(entity.getLibelleAvis());
        return dto;
    }

    public static Avis toEntity(AvisDto dto) {
        if (dto == null) {
            return null;
        }
        Avis entity = new Avis();
        entity.setIdAvis(dto.getIdAvis());
        entity.setLibelleAvis(dto.getLibelleAvis());
        return entity;
    }
}
