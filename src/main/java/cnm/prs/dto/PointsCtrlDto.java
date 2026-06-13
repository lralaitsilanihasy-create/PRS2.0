package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.PointsCtrl}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointsCtrlDto {

    private Integer idPointCtrl;

    private String libelPointCtrl;

    private String decriptPointCtrl;

    private Integer ordrePointCtrl;

    @NotNull
    private Boolean obligatoire;

    @NotBlank
    private String idTypeDossier;
}
