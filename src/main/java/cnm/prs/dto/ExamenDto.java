package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Examen}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamenDto {

    private Integer idExamen;

    @NotNull
    private Integer idDispatch;

    @Size(max = 7)
    private String imCtrlMembre;

    private LocalDate dateExamen;
}
