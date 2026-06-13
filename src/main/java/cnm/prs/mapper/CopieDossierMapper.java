package cnm.prs.mapper;

import cnm.prs.dto.CopieDossierDto;
import cnm.prs.entity.CopieDossier;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link CopieDossier}.
 */
public final class CopieDossierMapper {

    private CopieDossierMapper() {
    }

    public static CopieDossierDto toDto(CopieDossier entity) {
        if (entity == null) {
            return null;
        }
        CopieDossierDto dto = new CopieDossierDto();
        dto.setIdCopie(entity.getIdCopie());
        dto.setIdDispatch(entity.getIdDispatch());
        dto.setIdDossier(entity.getIdDossier());
        dto.setImDestinataire(entity.getImDestinataire());
        dto.setTypeCopie(entity.getTypeCopie());
        dto.setDateTransmission(entity.getDateTransmission());
        dto.setAccuseReception(entity.getAccuseReception());
        dto.setDateAccuse(entity.getDateAccuse());
        dto.setObservation(entity.getObservation());
        return dto;
    }

    public static CopieDossier toEntity(CopieDossierDto dto) {
        if (dto == null) {
            return null;
        }
        CopieDossier entity = new CopieDossier();
        entity.setIdCopie(dto.getIdCopie());
        entity.setIdDispatch(dto.getIdDispatch());
        entity.setIdDossier(dto.getIdDossier());
        entity.setImDestinataire(dto.getImDestinataire());
        entity.setTypeCopie(dto.getTypeCopie());
        entity.setDateTransmission(dto.getDateTransmission());
        entity.setAccuseReception(dto.getAccuseReception());
        entity.setDateAccuse(dto.getDateAccuse());
        entity.setObservation(dto.getObservation());
        return entity;
    }
}
