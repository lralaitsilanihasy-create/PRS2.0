package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.IndicateurPrmp}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicateurPrmpDto {

    private Integer idIndicateurPrmp;

    @NotBlank
    @Size(max = 10)
    private String idPrmp;

    @NotNull
    private Integer exercice;

    @NotNull
    private Integer nbPpmSoumis;

    @NotNull
    private Integer nbDossiersSoumis;

    @NotNull
    private Integer nbDossiersConformes;

    @NotNull
    private Integer nbDossiersNonConformes;

    @NotNull
    private Integer nbRetours;

    @NotNull
    private Integer nbRetraits;

    private BigDecimal tauxConformite;

    private BigDecimal delaiMoyCorrectionJours;

    private BigDecimal montTotalSoumis;

    private LocalDateTime dateMaj;
}
