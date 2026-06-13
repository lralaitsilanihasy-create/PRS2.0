package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Seuil}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeuilDto {

    private Integer idSeuil;

    private BigDecimal montantMin;

    private BigDecimal montantMax;

    @NotNull
    private Integer idNature;

    @NotBlank
    @Size(max = 5)
    private String idLocalite;
}
