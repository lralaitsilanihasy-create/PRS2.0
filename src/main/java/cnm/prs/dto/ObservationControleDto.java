package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.ObservationControle} : une ligne d'observation
 * « AU LIEU DE / LIRE » d'un point de contrôle ({@code idDetail}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObservationControleDto {

    private Integer idObservation;

    @NotNull(message = "Le point de contrôle est obligatoire.")
    private Integer idDetail;

    @Size(max = 500)
    private String auLieuDe;

    @Size(max = 500)
    private String lire;

    @NotNull
    private Integer ordre;
}
