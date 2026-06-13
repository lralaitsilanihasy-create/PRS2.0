package cnm.prs.mapper;

import cnm.prs.dto.EntiteContractDto;
import cnm.prs.entity.EntiteContract;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link EntiteContract}.
 */
public final class EntiteContractMapper {

    private EntiteContractMapper() {
    }

    public static EntiteContractDto toDto(EntiteContract entity) {
        if (entity == null) {
            return null;
        }
        EntiteContractDto dto = new EntiteContractDto();
        dto.setIdEntiteContract(entity.getIdEntiteContract());
        dto.setLibelleEntite(entity.getLibelleEntite());
        dto.setAdresse(entity.getAdresse());
        dto.setCategorieEntite(entity.getCategorieEntite());
        dto.setIdOrganigramme(entity.getIdOrganigramme());
        dto.setIdEntiteParent(entity.getIdEntiteParent());
        dto.setNiveauHierarchique(entity.getNiveauHierarchique());
        return dto;
    }

    public static EntiteContract toEntity(EntiteContractDto dto) {
        if (dto == null) {
            return null;
        }
        EntiteContract entity = new EntiteContract();
        entity.setIdEntiteContract(dto.getIdEntiteContract());
        entity.setLibelleEntite(dto.getLibelleEntite());
        entity.setAdresse(dto.getAdresse());
        entity.setCategorieEntite(dto.getCategorieEntite());
        entity.setIdOrganigramme(dto.getIdOrganigramme());
        entity.setIdEntiteParent(dto.getIdEntiteParent());
        entity.setNiveauHierarchique(dto.getNiveauHierarchique());
        return entity;
    }
}
