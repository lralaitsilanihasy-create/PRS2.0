package cnm.prs.mapper;

import cnm.prs.dto.CatCompteDto;
import cnm.prs.entity.CatCompte;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link CatCompte}.
 */
public final class CatCompteMapper {

    private CatCompteMapper() {
    }

    public static CatCompteDto toDto(CatCompte entity) {
        if (entity == null) {
            return null;
        }
        CatCompteDto dto = new CatCompteDto();
        dto.setIdCatCompte(entity.getIdCatCompte());
        dto.setCatCompte(entity.getCatCompte());
        return dto;
    }

    public static CatCompte toEntity(CatCompteDto dto) {
        if (dto == null) {
            return null;
        }
        CatCompte entity = new CatCompte();
        entity.setIdCatCompte(dto.getIdCatCompte());
        entity.setCatCompte(dto.getCatCompte());
        return entity;
    }
}
