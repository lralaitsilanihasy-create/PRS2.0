package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.DemandeRetrait}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeRetraitDto {

    private Integer idDemandeRetrait;

    @NotNull
    private Integer idDossier;

    @NotBlank
    @Size(max = 10)
    private String idPrmp;

    @NotBlank
    private String motifRetrait;

    @NotNull
    private LocalDateTime dateDemande;

    @NotBlank
    @Size(max = 20)
    private String statut;

    @Size(max = 7)
    private String imCtrlCc;

    private LocalDateTime dateDecision;

    @Size(max = 500)
    private String obsDecision;
}
