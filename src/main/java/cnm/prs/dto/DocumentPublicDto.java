package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.DocumentPublic}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPublicDto {

    private Integer idDocPublic;

    @NotNull
    private Integer idPublication;

    @Size(max = 30)
    private String typeDoc;

    @Size(max = 200)
    private String libelleDoc;

    @Size(max = 500)
    private String cheminFichier;

    @Size(max = 10)
    private String format;

    private Long tailleOctets;

    private LocalDateTime dateDepot;

    @Size(max = 64)
    private String hashSha256;
}
