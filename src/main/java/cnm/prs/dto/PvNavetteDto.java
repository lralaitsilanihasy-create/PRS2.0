package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.PvNavette}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PvNavetteDto {

    private Integer idNavette;

    @NotNull
    private Integer idPv;

    @NotNull
    private Integer numNavette;

    @NotBlank
    @Size(max = 20)
    private String sens;

    @NotBlank
    @Size(max = 7)
    private String imActeur;

    @NotNull
    private LocalDateTime dateAction;

    private String commentaire;
}
