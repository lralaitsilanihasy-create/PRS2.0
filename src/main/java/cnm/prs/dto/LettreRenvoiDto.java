package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.LettreRenvoi}.
 * {@code idDossier}, {@code refLettre}, {@code dateExamen}, {@code dateLettre}, {@code statut} et
 * {@code imSignataire} sont posés/dérivés par le serveur (lecture seule ; ignorés en entrée).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LettreRenvoiDto {

    private Integer idLettre;

    @NotNull
    private Integer idExamen;

    private Integer idDossier;

    private String refLettre;

    @Size(max = 500)
    private String objetLettre;

    private LocalDate dateExamen;

    private LocalDate dateLettre;

    private String statut;

    private String imSignataire;
}
