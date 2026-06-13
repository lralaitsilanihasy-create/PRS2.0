package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.RegleAnomalie}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegleAnomalieDto {

    private Integer idRegleAnomalie;

    @NotBlank
    @Size(max = 30)
    private String codeRegle;

    @Size(max = 200)
    private String libelle;

    private BigDecimal parametreNum;

    @Size(max = 200)
    private String parametreTxt;

    private Boolean actif;

    @Size(max = 10)
    private String graviteDefaut;
}
