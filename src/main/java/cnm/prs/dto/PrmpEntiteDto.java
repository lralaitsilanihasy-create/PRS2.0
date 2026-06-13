package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.PrmpEntite}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrmpEntiteDto {

    private Integer idPrmpEntite;

    @NotBlank
    @Size(max = 10)
    private String idPrmp;

    @NotNull
    private Integer idEntiteContract;

    private LocalDate dateAffectation;

    @NotNull
    private Boolean actif;
}
