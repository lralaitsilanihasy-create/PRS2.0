package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.SnapshotStats}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotStatsDto {

    private Integer idSnapshot;

    @NotNull
    private LocalDate dateSnapshot;

    @Size(max = 5)
    private String idLocalite;

    @NotNull
    private Integer exercice;

    private Integer nbDossiersRecus;

    private Integer nbDossiersClotures;

    private Integer nbDossiersEnCours;

    private BigDecimal tauxConformite;

    private BigDecimal delaiMoyenJours;

    private BigDecimal montTotalControle;

    private BigDecimal nbRetoursMoyen;
}
