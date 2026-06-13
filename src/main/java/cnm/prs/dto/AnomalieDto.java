package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Anomalie}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnomalieDto {

    private Integer idAnomalie;

    private Integer idDetail;

    private Integer idPpm;

    @NotNull
    private Integer idRegleAnomalie;

    @Size(max = 50)
    private String typeAnomalie;

    @Size(max = 10)
    private String gravite;

    private String description;

    private LocalDateTime dateDetection;

    @Size(max = 20)
    private String source;

    @Size(max = 20)
    private String statut;

    @Size(max = 7)
    private String imTraitement;

    private LocalDateTime dateTraitement;

    private String commentaireTraitement;
}
