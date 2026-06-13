package cnm.prs.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Compte}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteDto {

    private String numCompte;

    @Size(max = 100)
    private String libelle;

    @Size(max = 10)
    private String idCatCompte;
}
