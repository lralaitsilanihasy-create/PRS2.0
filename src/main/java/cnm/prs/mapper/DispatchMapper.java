package cnm.prs.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import cnm.prs.dto.DispatchDto;
import cnm.prs.entity.Dispatch;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Dispatch}.
 */
public final class DispatchMapper {

    /** Format d'échange de la date/heure de dispatch : {@code yyyy-MM-dd HH:mm}. */
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DispatchMapper() {
    }

    /** {@link LocalDateTime} &rarr; {@code yyyy-MM-dd HH:mm} (ou {@code null}). */
    public static String format(LocalDateTime dt) {
        return dt == null ? null : dt.format(FMT);
    }

    /** {@code yyyy-MM-dd HH:mm} &rarr; {@link LocalDateTime} ({@code null}/vide &rarr; {@code null}). */
    public static LocalDateTime toLocalDateTime(String s) {
        return (s == null || s.isBlank()) ? null : LocalDateTime.parse(s.trim(), FMT);
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
        dto.setDateDispatch(format(entity.getDateDispatch()));
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
        entity.setDateDispatch(toLocalDateTime(dto.getDateDispatch()));
        entity.setDateCtrlAssigne(dto.getDateCtrlAssigne());
        entity.setInstructions(dto.getInstructions());
        entity.setInterimDispatch(dto.getInterimDispatch());
        return entity;
    }
}
