package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Ministere}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinistereDto {

    private Integer idMinistere;

    @NotBlank
    @Size(max = 150)
    private String libelleMinistere;

    @Size(max = 20)
    private String sigle;
}
