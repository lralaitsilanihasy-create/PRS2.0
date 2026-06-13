package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Localite}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocaliteDto {

    private String idLocalite;

    @NotBlank
    @Size(max = 50)
    private String libelleLocalite;

    @NotBlank
    @Size(max = 50)
    private String referencement;

    @NotBlank
    @Size(max = 3)
    private String localite;
}
