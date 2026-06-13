package cnm.prs.mapper;

import cnm.prs.dto.DemandeRetraitDto;
import cnm.prs.entity.DemandeRetrait;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link DemandeRetrait}.
 */
public final class DemandeRetraitMapper {

    private DemandeRetraitMapper() {
    }

    public static DemandeRetraitDto toDto(DemandeRetrait entity) {
        if (entity == null) {
            return null;
        }
        DemandeRetraitDto dto = new DemandeRetraitDto();
        dto.setIdDemandeRetrait(entity.getIdDemandeRetrait());
        dto.setIdDossier(entity.getIdDossier());
        dto.setIdPrmp(entity.getIdPrmp());
        dto.setMotifRetrait(entity.getMotifRetrait());
        dto.setDateDemande(entity.getDateDemande());
        dto.setStatut(entity.getStatut());
        dto.setImCtrlCc(entity.getImCtrlCc());
        dto.setDateDecision(entity.getDateDecision());
        dto.setObsDecision(entity.getObsDecision());
        return dto;
    }

    public static DemandeRetrait toEntity(DemandeRetraitDto dto) {
        if (dto == null) {
            return null;
        }
        DemandeRetrait entity = new DemandeRetrait();
        entity.setIdDemandeRetrait(dto.getIdDemandeRetrait());
        entity.setIdDossier(dto.getIdDossier());
        entity.setIdPrmp(dto.getIdPrmp());
        entity.setMotifRetrait(dto.getMotifRetrait());
        entity.setDateDemande(dto.getDateDemande());
        entity.setStatut(dto.getStatut());
        entity.setImCtrlCc(dto.getImCtrlCc());
        entity.setDateDecision(dto.getDateDecision());
        entity.setObsDecision(dto.getObsDecision());
        return entity;
    }
}
