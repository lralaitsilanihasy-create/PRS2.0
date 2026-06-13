package cnm.prs.mapper;

import cnm.prs.dto.TypeDossierDto;
import cnm.prs.entity.TypeDossier;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link TypeDossier}.
 */
public final class TypeDossierMapper {

    private TypeDossierMapper() {
    }

    public static TypeDossierDto toDto(TypeDossier entity) {
        if (entity == null) {
            return null;
        }
        TypeDossierDto dto = new TypeDossierDto();
        dto.setIdTypeDossier(entity.getIdTypeDossier());
        dto.setLibelleType(entity.getLibelleType());
        return dto;
    }

    public static TypeDossier toEntity(TypeDossierDto dto) {
        if (dto == null) {
            return null;
        }
        TypeDossier entity = new TypeDossier();
        entity.setIdTypeDossier(dto.getIdTypeDossier());
        entity.setLibelleType(dto.getLibelleType());
        return entity;
    }
}
