package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.RegleAlerte}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegleAlerteDto {

    private Integer idRegleAlerte;

    @NotBlank
    @Size(max = 30)
    private String typeJalon;

    @NotNull
    private Integer joursAvant;

    private Integer destinataireProfil;

    private Boolean actif;
}
