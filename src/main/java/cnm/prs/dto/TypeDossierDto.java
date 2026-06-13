package cnm.prs.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.TypeDossier}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeDossierDto {

    private String idTypeDossier;

    @Size(max = 100)
    private String libelleType;
}
