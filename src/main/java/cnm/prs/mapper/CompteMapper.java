package cnm.prs.mapper;

import cnm.prs.dto.CompteDto;
import cnm.prs.entity.Compte;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Compte}.
 */
public final class CompteMapper {

    private CompteMapper() {
    }

    public static CompteDto toDto(Compte entity) {
        if (entity == null) {
            return null;
        }
        CompteDto dto = new CompteDto();
        dto.setNumCompte(entity.getNumCompte());
        dto.setLibelle(entity.getLibelle());
        dto.setIdCatCompte(entity.getIdCatCompte());
        return dto;
    }

    public static Compte toEntity(CompteDto dto) {
        if (dto == null) {
            return null;
        }
        Compte entity = new Compte();
        entity.setNumCompte(dto.getNumCompte());
        entity.setLibelle(dto.getLibelle());
        entity.setIdCatCompte(dto.getIdCatCompte());
        return entity;
    }
}
