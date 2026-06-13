package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.DelegationProfil}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DelegationProfilDto {

    private Integer idDelegation;

    @NotNull
    private Integer idProfileDelegant;

    @NotNull
    private Integer idProfileDelegue;

    @NotNull
    private Boolean actif;
}
