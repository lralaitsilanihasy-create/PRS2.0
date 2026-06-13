package cnm.prs.mapper;

import cnm.prs.dto.TrancheDto;
import cnm.prs.entity.Tranche;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Tranche}.
 */
public final class TrancheMapper {

    private TrancheMapper() {
    }

    public static TrancheDto toDto(Tranche entity) {
        if (entity == null) {
            return null;
        }
        TrancheDto dto = new TrancheDto();
        dto.setIdTranche(entity.getIdTranche());
        dto.setLieuTrc(entity.getLieuTrc());
        dto.setMontTrc(entity.getMontTrc());
        dto.setIdLot(entity.getIdLot());
        return dto;
    }

    public static Tranche toEntity(TrancheDto dto) {
        if (dto == null) {
            return null;
        }
        Tranche entity = new Tranche();
        entity.setIdTranche(dto.getIdTranche());
        entity.setLieuTrc(dto.getLieuTrc());
        entity.setMontTrc(dto.getMontTrc());
        entity.setIdLot(dto.getIdLot());
        return entity;
    }
}
