package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.PvExamen}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PvExamenDto {

    private Integer idPv;

    @NotNull
    private Integer idExamen;

    @NotBlank
    @Size(max = 10)
    private String idAvis;

    @Size(max = 7)
    private String imCtrlPresident;

    @Size(max = 7)
    private String imCtrlCc;

    @NotBlank
    @Size(max = 7)
    private String imCtrlMembre;

    private String syntheseObservations;

    @NotBlank
    @Size(max = 20)
    private String statutPv;

    @NotNull
    private Integer nbNavettes;

    private LocalDate dateSoumissionInitiale;

    private LocalDate dateAcceptation;

    private LocalDate dateSignaturePresident;

    private LocalDate dateSignatureCc;

    private LocalDate dateSignatureMembre;

    private LocalDate datePv;

    @Size(max = 100)
    private String referencePv;
}
