package cnm.prs.mapper;

import cnm.prs.dto.LotDto;
import cnm.prs.entity.Lot;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Lot}.
 */
public final class LotMapper {

    private LotMapper() {
    }

    public static LotDto toDto(Lot entity) {
        if (entity == null) {
            return null;
        }
        LotDto dto = new LotDto();
        dto.setIdLot(entity.getIdLot());
        dto.setIdDossier(entity.getIdDossier());
        dto.setIdDetail(entity.getIdDetail());
        dto.setDesignationLot(entity.getDesignationLot());
        dto.setMontLot(entity.getMontLot());
        dto.setQteLot(entity.getQteLot());
        dto.setUniteLot(entity.getUniteLot());
        return dto;
    }

    public static Lot toEntity(LotDto dto) {
        if (dto == null) {
            return null;
        }
        Lot entity = new Lot();
        entity.setIdLot(dto.getIdLot());
        entity.setIdDossier(dto.getIdDossier());
        entity.setIdDetail(dto.getIdDetail());
        entity.setDesignationLot(dto.getDesignationLot());
        entity.setMontLot(dto.getMontLot());
        entity.setQteLot(dto.getQteLot());
        entity.setUniteLot(dto.getUniteLot());
        return entity;
    }
}
