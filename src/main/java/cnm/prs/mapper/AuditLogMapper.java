package cnm.prs.mapper;

import cnm.prs.dto.AuditLogDto;
import cnm.prs.entity.AuditLog;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link AuditLog}.
 */
public final class AuditLogMapper {

    private AuditLogMapper() {
    }

    public static AuditLogDto toDto(AuditLog entity) {
        if (entity == null) {
            return null;
        }
        AuditLogDto dto = new AuditLogDto();
        dto.setIdLog(entity.getIdLog());
        dto.setDateAction(entity.getDateAction());
        dto.setImActeur(entity.getImActeur());
        dto.setNomTable(entity.getNomTable());
        dto.setIdEnregistrement(entity.getIdEnregistrement());
        dto.setTypeAction(entity.getTypeAction());
        dto.setChampModifie(entity.getChampModifie());
        dto.setAncienneValeur(entity.getAncienneValeur());
        dto.setNouvelleValeur(entity.getNouvelleValeur());
        dto.setIpAdresse(entity.getIpAdresse());
        dto.setSessionId(entity.getSessionId());
        return dto;
    }

    public static AuditLog toEntity(AuditLogDto dto) {
        if (dto == null) {
            return null;
        }
        AuditLog entity = new AuditLog();
        entity.setIdLog(dto.getIdLog());
        entity.setDateAction(dto.getDateAction());
        entity.setImActeur(dto.getImActeur());
        entity.setNomTable(dto.getNomTable());
        entity.setIdEnregistrement(dto.getIdEnregistrement());
        entity.setTypeAction(dto.getTypeAction());
        entity.setChampModifie(dto.getChampModifie());
        entity.setAncienneValeur(dto.getAncienneValeur());
        entity.setNouvelleValeur(dto.getNouvelleValeur());
        entity.setIpAdresse(dto.getIpAdresse());
        entity.setSessionId(dto.getSessionId());
        return entity;
    }
}
