package cnm.prs.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Profile}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {

    private Integer idProfile;

    @Size(max = 50)
    private String profile;
}
