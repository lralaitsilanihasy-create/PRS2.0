package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Reception}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionDto {

    private Integer idReception;

    @NotNull
    private Integer idDossier;

    @NotNull
    private Integer numPassage;

    @NotBlank
    @Size(max = 10)
    private String typePassage;

    @Size(max = 7)
    private String imCtrlRecept;

    private LocalDate dateReception;

    @Size(max = 500)
    private String observation;

    private Boolean complet;

    private Integer idReceptionPrec;
}
