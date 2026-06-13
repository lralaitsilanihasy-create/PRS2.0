package cnm.prs.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Avis}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvisDto {

    private String idAvis;

    @Size(max = 100)
    private String libelleAvis;
}
