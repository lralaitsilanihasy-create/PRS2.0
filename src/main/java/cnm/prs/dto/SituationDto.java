package cnm.prs.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Situation}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SituationDto {

    private Integer idSituation;

    @Size(max = 100)
    private String libelle;

    @Size(max = 500)
    private String description;
}
