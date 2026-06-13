package cnm.prs.mapper;

import cnm.prs.dto.DelegationProfilDto;
import cnm.prs.entity.DelegationProfil;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link DelegationProfil}.
 */
public final class DelegationProfilMapper {

    private DelegationProfilMapper() {
    }

    public static DelegationProfilDto toDto(DelegationProfil entity) {
        if (entity == null) {
            return null;
        }
        DelegationProfilDto dto = new DelegationProfilDto();
        dto.setIdDelegation(entity.getIdDelegation());
        dto.setIdProfileDelegant(entity.getIdProfileDelegant());
        dto.setIdProfileDelegue(entity.getIdProfileDelegue());
        dto.setActif(entity.getActif());
        return dto;
    }

    public static DelegationProfil toEntity(DelegationProfilDto dto) {
        if (dto == null) {
            return null;
        }
        DelegationProfil entity = new DelegationProfil();
        entity.setIdDelegation(dto.getIdDelegation());
        entity.setIdProfileDelegant(dto.getIdProfileDelegant());
        entity.setIdProfileDelegue(dto.getIdProfileDelegue());
        entity.setActif(dto.getActif());
        return entity;
    }
}
