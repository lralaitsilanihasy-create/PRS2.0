package cnm.prs.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.SoaBeneficiaire}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoaBeneficiaireDto {

    private String soaCode;

    @Size(max = 100)
    private String libelle;
}
