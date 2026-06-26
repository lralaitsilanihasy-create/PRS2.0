package cnm.prs.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import cnm.prs.dto.ReceptionDto;
import cnm.prs.entity.Reception;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Reception}.
 */
public final class ReceptionMapper {

    /** Format d'échange des dates/heures de réception : {@code yyyy-MM-dd HH:mm}. */
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ReceptionMapper() {
    }

    /** {@link LocalDateTime} &rarr; {@code yyyy-MM-dd HH:mm} (ou {@code null}). */
    public static String format(LocalDateTime dt) {
        return dt == null ? null : dt.format(FMT);
    }

    /** {@code yyyy-MM-dd HH:mm} &rarr; {@link LocalDateTime} ({@code null}/vide &rarr; {@code null}). */
    public static LocalDateTime toLocalDateTime(String s) {
        return (s == null || s.isBlank()) ? null : LocalDateTime.parse(s.trim(), FMT);
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
        dto.setDateReception(format(entity.getDateReception()));
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
        entity.setDateReception(toLocalDateTime(dto.getDateReception()));
        entity.setObservation(dto.getObservation());
        entity.setComplet(dto.getComplet());
        entity.setIdReceptionPrec(dto.getIdReceptionPrec());
        return entity;
    }
}
