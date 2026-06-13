package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.ServiceBeneficiaire}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBeneficiaireDto {

    private Integer idBenef;

    private BigDecimal ancMontBenef;

    private BigDecimal nouvMontBenef;

    @Size(max = 15)
    private String soaCode;

    @NotNull
    private Integer idDetail;
}
