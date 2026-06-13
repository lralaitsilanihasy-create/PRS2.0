package cnm.prs.mapper;

import cnm.prs.dto.OrganigrammeDto;
import cnm.prs.entity.Organigramme;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Organigramme}.
 */
public final class OrganigrammeMapper {

    private OrganigrammeMapper() {
    }

    public static OrganigrammeDto toDto(Organigramme entity) {
        if (entity == null) {
            return null;
        }
        OrganigrammeDto dto = new OrganigrammeDto();
        dto.setIdOrganigramme(entity.getIdOrganigramme());
        dto.setIdMinistere(entity.getIdMinistere());
        dto.setLibelle(entity.getLibelle());
        dto.setVersion(entity.getVersion());
        dto.setDateValidation(entity.getDateValidation());
        dto.setActif(entity.getActif());
        return dto;
    }

    public static Organigramme toEntity(OrganigrammeDto dto) {
        if (dto == null) {
            return null;
        }
        Organigramme entity = new Organigramme();
        entity.setIdOrganigramme(dto.getIdOrganigramme());
        entity.setIdMinistere(dto.getIdMinistere());
        entity.setLibelle(dto.getLibelle());
        entity.setVersion(dto.getVersion());
        entity.setDateValidation(dto.getDateValidation());
        entity.setActif(dto.getActif());
        return entity;
    }
}
