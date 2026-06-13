package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Tranche}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrancheDto {

    private Integer idTranche;

    @Size(max = 100)
    private String lieuTrc;

    private BigDecimal montTrc;

    @NotNull
    private Integer idLot;
}
