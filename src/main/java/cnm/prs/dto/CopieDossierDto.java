package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.CopieDossier}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CopieDossierDto {

    private Integer idCopie;

    @NotNull
    private Integer idDispatch;

    @NotNull
    private Integer idDossier;

    @NotBlank
    @Size(max = 7)
    private String imDestinataire;

    @NotBlank
    @Size(max = 30)
    private String typeCopie;

    @NotNull
    private LocalDateTime dateTransmission;

    @NotNull
    private Boolean accuseReception;

    private LocalDateTime dateAccuse;

    @Size(max = 300)
    private String observation;
}
