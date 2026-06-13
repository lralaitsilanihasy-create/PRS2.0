package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.MarchePrevision}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarchePrevisionDto {

    @NotNull
    private Integer idPrevision;

    @NotNull
    private Integer idDetail;

    @NotNull
    @Size(max = 20)
    private String typeDate;

    private LocalDate datePrev;
}
