package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Lot}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LotDto {

    private Integer idLot;

    @NotNull
    private Integer idDossier;

    @NotNull
    private Integer idDetail;

    @NotBlank
    @Size(max = 200)
    private String designationLot;

    private BigDecimal montLot;

    private Integer qteLot;

    @Size(max = 10)
    private String uniteLot;
}
