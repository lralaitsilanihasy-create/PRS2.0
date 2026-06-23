package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Capm} (processus de marché).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapmDto {

    private Integer idCapm;

    @Size(max = 100)
    private String libelleProcessus;

    @NotNull
    private Integer ordre;
}
