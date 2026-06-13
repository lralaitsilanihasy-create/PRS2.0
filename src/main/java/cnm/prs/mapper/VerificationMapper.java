package cnm.prs.mapper;

import cnm.prs.dto.VerificationDto;
import cnm.prs.entity.Verification;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Verification}.
 */
public final class VerificationMapper {

    private VerificationMapper() {
    }

    public static VerificationDto toDto(Verification entity) {
        if (entity == null) {
            return null;
        }
        VerificationDto dto = new VerificationDto();
        dto.setIdVerification(entity.getIdVerification());
        dto.setIdReception(entity.getIdReception());
        dto.setIdPv(entity.getIdPv());
        dto.setImCtrlVerif(entity.getImCtrlVerif());
        dto.setDateVerif(entity.getDateVerif());
        dto.setObservation(entity.getObservation());
        dto.setObsLevees(entity.getObsLevees());
        return dto;
    }

    public static Verification toEntity(VerificationDto dto) {
        if (dto == null) {
            return null;
        }
        Verification entity = new Verification();
        entity.setIdVerification(dto.getIdVerification());
        entity.setIdReception(dto.getIdReception());
        entity.setIdPv(dto.getIdPv());
        entity.setImCtrlVerif(dto.getImCtrlVerif());
        entity.setDateVerif(dto.getDateVerif());
        entity.setObservation(dto.getObservation());
        entity.setObsLevees(dto.getObsLevees());
        return entity;
    }
}
