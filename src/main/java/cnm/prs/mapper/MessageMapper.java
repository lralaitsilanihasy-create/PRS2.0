package cnm.prs.mapper;

import cnm.prs.dto.MessageDto;
import cnm.prs.entity.Message;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Message}.
 */
public final class MessageMapper {

    private MessageMapper() {
    }

    public static MessageDto toDto(Message entity) {
        if (entity == null) {
            return null;
        }
        MessageDto dto = new MessageDto();
        dto.setIdMessage(entity.getIdMessage());
        dto.setIdDossier(entity.getIdDossier());
        dto.setExpediteurIm(entity.getExpediteurIm());
        dto.setDestinataireIm(entity.getDestinataireIm());
        dto.setSujet(entity.getSujet());
        dto.setCorps(entity.getCorps());
        dto.setDateEnvoi(entity.getDateEnvoi());
        dto.setLu(entity.getLu());
        dto.setIdMessageParent(entity.getIdMessageParent());
        return dto;
    }

    public static Message toEntity(MessageDto dto) {
        if (dto == null) {
            return null;
        }
        Message entity = new Message();
        entity.setIdMessage(dto.getIdMessage());
        entity.setIdDossier(dto.getIdDossier());
        entity.setExpediteurIm(dto.getExpediteurIm());
        entity.setDestinataireIm(dto.getDestinataireIm());
        entity.setSujet(dto.getSujet());
        entity.setCorps(dto.getCorps());
        entity.setDateEnvoi(dto.getDateEnvoi());
        entity.setLu(dto.getLu());
        entity.setIdMessageParent(dto.getIdMessageParent());
        return entity;
    }
}
