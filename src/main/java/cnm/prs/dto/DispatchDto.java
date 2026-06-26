package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Dispatch}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispatchDto {

    private Integer idDispatch;

    @NotNull
    private Integer idReception;

    @Size(max = 7)
    private String imCtrlDispatch;

    @Size(max = 7)
    private String imCtrlCc;

    @Size(max = 7)
    private String imCtrlMembre;

    /** Date et heure du dispatch, formatée {@code yyyy-MM-dd HH:mm}. */
    private String dateDispatch;

    private LocalDate dateCtrlAssigne;

    @Size(max = 500)
    private String instructions;

    @NotNull
    private Boolean interimDispatch;
}
