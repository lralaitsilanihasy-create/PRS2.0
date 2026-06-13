package cnm.prs.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Ligne de marché saisie via la façade PPM. {@code idDossier}, {@code idPpm} et {@code idMode}
 * sont renseignés par le service (le mode est déterminé automatiquement, §3.1 M02).
 */
public record SaisieMarcheLigne(

        @NotNull
        Integer idDetail,

        @Size(max = 500)
        String designationMarche,

        @Size(max = 20)
        String numCompte,

        BigDecimal montEstim,

        @Size(max = 20)
        String financement,

        @Size(max = 20)
        String statut,

        Integer idSituation,

        Integer idNature) {
}
