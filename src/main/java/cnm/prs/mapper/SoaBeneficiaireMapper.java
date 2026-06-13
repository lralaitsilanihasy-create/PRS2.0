package cnm.prs.mapper;

import cnm.prs.dto.SoaBeneficiaireDto;
import cnm.prs.entity.SoaBeneficiaire;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link SoaBeneficiaire}.
 */
public final class SoaBeneficiaireMapper {

    private SoaBeneficiaireMapper() {
    }

    public static SoaBeneficiaireDto toDto(SoaBeneficiaire entity) {
        if (entity == null) {
            return null;
        }
        SoaBeneficiaireDto dto = new SoaBeneficiaireDto();
        dto.setSoaCode(entity.getSoaCode());
        dto.setLibelle(entity.getLibelle());
        return dto;
    }

    public static SoaBeneficiaire toEntity(SoaBeneficiaireDto dto) {
        if (dto == null) {
            return null;
        }
        SoaBeneficiaire entity = new SoaBeneficiaire();
        entity.setSoaCode(dto.getSoaCode());
        entity.setLibelle(dto.getLibelle());
        return entity;
    }
}
