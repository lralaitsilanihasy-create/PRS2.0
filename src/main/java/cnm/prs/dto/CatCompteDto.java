package cnm.prs.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.CatCompte}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatCompteDto {

    private String idCatCompte;

    @Size(max = 50)
    private String catCompte;
}
