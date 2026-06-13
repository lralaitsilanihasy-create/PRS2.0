package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Echeance}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcheanceDto {

    private Integer idEcheance;

    @NotNull
    private Integer idDetail;

    @NotBlank
    @Size(max = 30)
    private String typeJalon;

    @NotNull
    private LocalDate datePrevue;

    private LocalDate dateReelle;

    @Size(max = 20)
    private String statutJalon;

    private Integer ecartJours;

    private Boolean alerteEnvoyee;
}
