package cnm.prs.mapper;

import cnm.prs.dto.RegleAlerteDto;
import cnm.prs.entity.RegleAlerte;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link RegleAlerte}.
 */
public final class RegleAlerteMapper {

    private RegleAlerteMapper() {
    }

    public static RegleAlerteDto toDto(RegleAlerte entity) {
        if (entity == null) {
            return null;
        }
        RegleAlerteDto dto = new RegleAlerteDto();
        dto.setIdRegleAlerte(entity.getIdRegleAlerte());
        dto.setTypeJalon(entity.getTypeJalon());
        dto.setJoursAvant(entity.getJoursAvant());
        dto.setDestinataireProfil(entity.getDestinataireProfil());
        dto.setActif(entity.getActif());
        return dto;
    }

    public static RegleAlerte toEntity(RegleAlerteDto dto) {
        if (dto == null) {
            return null;
        }
        RegleAlerte entity = new RegleAlerte();
        entity.setIdRegleAlerte(dto.getIdRegleAlerte());
        entity.setTypeJalon(dto.getTypeJalon());
        entity.setJoursAvant(dto.getJoursAvant());
        entity.setDestinataireProfil(dto.getDestinataireProfil());
        entity.setActif(dto.getActif());
        return entity;
    }
}
