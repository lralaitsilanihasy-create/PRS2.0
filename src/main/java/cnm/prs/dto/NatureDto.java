package cnm.prs.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Nature}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NatureDto {

    private Integer idNature;

    @Size(max = 100)
    private String libelle;

    @Size(max = 500)
    private String description;
}
