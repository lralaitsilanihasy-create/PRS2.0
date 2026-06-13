package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.ReglePassation}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReglePassationDto {

    private Integer idRegle;

    @NotNull
    private Integer idSituation;

    @NotNull
    private Integer idSeuil;

    @NotNull
    private Integer idMode;

    private Integer priorite;
}
