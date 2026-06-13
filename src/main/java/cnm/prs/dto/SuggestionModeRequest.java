package cnm.prs.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Critères de détermination automatique du mode de passation (§3.1, Module 02) :
 * situation, montant, nature et localité. Le mode est suggéré via {@code t_regle_passation}
 * (croisé avec {@code t_seuil} qui porte nature, localité et bornes de montant).
 */
public record SuggestionModeRequest(

        @NotNull
        Integer idSituation,

        @NotNull
        BigDecimal montant,

        @NotNull
        Integer idNature,

        @NotBlank
        @Size(max = 5)
        String idLocalite) {
}
