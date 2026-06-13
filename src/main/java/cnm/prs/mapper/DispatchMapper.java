package cnm.prs.mapper;

import cnm.prs.dto.DispatchDto;
import cnm.prs.entity.Dispatch;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Dispatch}.
 */
public final class DispatchMapper {

    private DispatchMapper() {
    }

    public static DispatchDto toDto(Dispatch entity) {
        if (entity == null) {
            return null;
        }
        DispatchDto dto = new DispatchDto();
        dto.setIdDispatch(entity.getIdDispatch());
        dto.setIdReception(entity.getIdReception());
        dto.setImCtrlDispatch(entity.getImCtrlDispatch());
        dto.setImCtrlCc(entity.getImCtrlCc());
        dto.setImCtrlMembre(entity.getImCtrlMembre());
        dto.setDateDispatch(entity.getDateDispatch());
        dto.setDateCtrlAssigne(entity.getDateCtrlAssigne());
        dto.setInstructions(entity.getInstructions());
        dto.setInterimDispatch(entity.getInterimDispatch());
        return dto;
    }

    public static Dispatch toEntity(DispatchDto dto) {
        if (dto == null) {
            return null;
        }
        Dispatch entity = new Dispatch();
        entity.setIdDispatch(dto.getIdDispatch());
        entity.setIdReception(dto.getIdReception());
        entity.setImCtrlDispatch(dto.getImCtrlDispatch());
        entity.setImCtrlCc(dto.getImCtrlCc());
        entity.setImCtrlMembre(dto.getImCtrlMembre());
        entity.setDateDispatch(dto.getDateDispatch());
        entity.setDateCtrlAssigne(dto.getDateCtrlAssigne());
        entity.setInstructions(dto.getInstructions());
        entity.setInterimDispatch(dto.getInterimDispatch());
        return entity;
    }
}
