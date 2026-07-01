package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.ReglePassation}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReglePassationDto {

    private Integer idRegle;

    @NotNull
    private Integer idSituation;

    @NotNull
    private Integer idSeuil;

    @NotNull
    private Integer idMode;

    private Integer priorite;

    /** Libellé de la situation (lecture seule, peuplé serveur) — ex. « Situation normale ». */
    private String libelleSituation;

    /** Libellé lisible du seuil (lecture seule) — « montantMin à montantMax », ou « ≥ montantMin » si max nul. */
    private String libelleSeuil;

    /** Libellé du mode de passation (lecture seule, peuplé serveur) — ex. « Appel d'offres ouvert ». */
    private String libelleMode;
}
