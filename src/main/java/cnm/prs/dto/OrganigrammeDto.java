package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Organigramme}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganigrammeDto {

    private Integer idOrganigramme;

    @NotNull
    private Integer idMinistere;

    @Size(max = 200)
    private String libelle;

    @Size(max = 20)
    private String version;

    private LocalDate dateValidation;

    @NotNull
    private Boolean actif;
}
