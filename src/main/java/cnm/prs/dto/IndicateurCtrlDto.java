package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.IndicateurCtrl}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicateurCtrlDto {

    private Integer idIndicateur;

    @NotBlank
    @Size(max = 7)
    private String imControleur;

    @NotBlank
    @Size(max = 7)
    private String periode;

    private Integer nbExamens;

    private Integer nbConformes;

    private BigDecimal delaiMoyenExamen;

    private Integer nbObsEmises;
}
