package cnm.prs.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.SessionUtilisateur}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionUtilisateurDto {

    private String idSession;

    @Size(max = 7)
    private String imControleur;

    private LocalDateTime dateConnexion;

    private LocalDateTime dateDeconnexion;

    @Size(max = 45)
    private String ipAdresse;

    @Size(max = 300)
    private String userAgent;

    private Boolean succes;
}
