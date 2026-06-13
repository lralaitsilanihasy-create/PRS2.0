package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Publication}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicationDto {

    private Integer idPublication;

    @NotBlank
    @Size(max = 20)
    private String typeObjet;

    @NotNull
    private Integer idObjet;

    private LocalDateTime datePublication;

    @Size(max = 7)
    private String imPubliePar;

    @Size(max = 20)
    private String statutPubli;

    private LocalDate dateRetrait;

    @Size(max = 300)
    private String motifRetrait;

    private Integer nbConsultations;
}
