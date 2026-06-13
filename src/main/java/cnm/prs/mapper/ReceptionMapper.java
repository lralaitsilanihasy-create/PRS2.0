package cnm.prs.mapper;

import cnm.prs.dto.ReceptionDto;
import cnm.prs.entity.Reception;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Reception}.
 */
public final class ReceptionMapper {

    private ReceptionMapper() {
    }

    public static ReceptionDto toDto(Reception entity) {
        if (entity == null) {
            return null;
        }
        ReceptionDto dto = new ReceptionDto();
        dto.setIdReception(entity.getIdReception());
        dto.setIdDossier(entity.getIdDossier());
        dto.setNumPassage(entity.getNumPassage());
        dto.setTypePassage(entity.getTypePassage());
        dto.setImCtrlRecept(entity.getImCtrlRecept());
        dto.setDateReception(entity.getDateReception());
        dto.setObservation(entity.getObservation());
        dto.setComplet(entity.getComplet());
        dto.setIdReceptionPrec(entity.getIdReceptionPrec());
        return dto;
    }

    public static Reception toEntity(ReceptionDto dto) {
        if (dto == null) {
            return null;
        }
        Reception entity = new Reception();
        entity.setIdReception(dto.getIdReception());
        entity.setIdDossier(dto.getIdDossier());
        entity.setNumPassage(dto.getNumPassage());
        entity.setTypePassage(dto.getTypePassage());
        entity.setImCtrlRecept(dto.getImCtrlRecept());
        entity.setDateReception(dto.getDateReception());
        entity.setObservation(dto.getObservation());
        entity.setComplet(dto.getComplet());
        entity.setIdReceptionPrec(dto.getIdReceptionPrec());
        return entity;
    }
}
