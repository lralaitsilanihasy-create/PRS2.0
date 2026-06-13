package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Verification}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationDto {

    private Integer idVerification;

    @NotNull
    private Integer idReception;

    @NotNull
    private Integer idPv;

    @Size(max = 7)
    private String imCtrlVerif;

    private LocalDate dateVerif;

    @Size(max = 500)
    private String observation;

    private Boolean obsLevees;
}
