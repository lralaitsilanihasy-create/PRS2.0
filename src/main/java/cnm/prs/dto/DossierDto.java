package cnm.prs.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Dossier}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DossierDto {

    private Integer idDossier;

    @Size(max = 10)
    private String idTypeDossier;

    private Integer idDossierParent;

    @Size(max = 100)
    private String refeDossier;

    private LocalDate dateRef;

    @Size(max = 20)
    private String statut;

    @Size(max = 5)
    private String idLocalite;

    @Size(max = 10)
    private String idPrmp;

    private Integer idEntiteContract;
}
