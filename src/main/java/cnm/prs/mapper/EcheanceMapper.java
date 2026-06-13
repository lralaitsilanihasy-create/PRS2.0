package cnm.prs.mapper;

import cnm.prs.dto.EcheanceDto;
import cnm.prs.entity.Echeance;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Echeance}.
 */
public final class EcheanceMapper {

    private EcheanceMapper() {
    }

    public static EcheanceDto toDto(Echeance entity) {
        if (entity == null) {
            return null;
        }
        EcheanceDto dto = new EcheanceDto();
        dto.setIdEcheance(entity.getIdEcheance());
        dto.setIdDetail(entity.getIdDetail());
        dto.setTypeJalon(entity.getTypeJalon());
        dto.setDatePrevue(entity.getDatePrevue());
        dto.setDateReelle(entity.getDateReelle());
        dto.setStatutJalon(entity.getStatutJalon());
        dto.setEcartJours(entity.getEcartJours());
        dto.setAlerteEnvoyee(entity.getAlerteEnvoyee());
        return dto;
    }

    public static Echeance toEntity(EcheanceDto dto) {
        if (dto == null) {
            return null;
        }
        Echeance entity = new Echeance();
        entity.setIdEcheance(dto.getIdEcheance());
        entity.setIdDetail(dto.getIdDetail());
        entity.setTypeJalon(dto.getTypeJalon());
        entity.setDatePrevue(dto.getDatePrevue());
        entity.setDateReelle(dto.getDateReelle());
        entity.setStatutJalon(dto.getStatutJalon());
        entity.setEcartJours(dto.getEcartJours());
        entity.setAlerteEnvoyee(dto.getAlerteEnvoyee());
        return entity;
    }
}
