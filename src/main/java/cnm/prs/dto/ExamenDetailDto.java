package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.ExamenDetail}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamenDetailDto {

    private Integer idDetailExamen;

    @NotNull
    private Integer idExamen;

    @NotNull
    private Integer idPtControle;

    @NotNull
    private Boolean conforme;

    @Size(max = 500)
    private String observation;

    @Size(max = 500)
    private String obsSiNonConforme;
}
