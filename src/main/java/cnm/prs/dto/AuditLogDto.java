package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.AuditLog}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {

    private Long idLog;

    @NotNull
    private LocalDateTime dateAction;

    @Size(max = 7)
    private String imActeur;

    @Size(max = 50)
    private String nomTable;

    @Size(max = 20)
    private String idEnregistrement;

    @Size(max = 10)
    private String typeAction;

    @Size(max = 50)
    private String champModifie;

    private String ancienneValeur;

    private String nouvelleValeur;

    @Size(max = 45)
    private String ipAdresse;

    @Size(max = 100)
    private String sessionId;
}
