package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Notification}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Integer idNotification;

    private Integer idDossier;

    @NotBlank
    @Size(max = 30)
    private String typeNotif;

    @Size(max = 7)
    private String destinataireIm;

    @Size(max = 100)
    private String destinataireEmail;

    @Size(max = 200)
    private String titre;

    private String corps;

    private LocalDateTime dateEnvoi;

    private Boolean lu;

    private LocalDateTime dateLecture;

    @Size(max = 20)
    private String canal;
}
