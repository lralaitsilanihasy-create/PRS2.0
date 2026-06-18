package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Saisie d'un dossier sans contenu (DAO, MAOO, …) via la façade : crée un {@code t_dossier}
 * (type + localité, BROUILLON), propriété de la PRMP courante. Le type {@code PPM} est refusé
 * (utiliser la façade PPM).
 */
public record SaisieDossierRequest(

        @NotBlank
        @Size(max = 10)
        String idTypeDossier,

        @NotNull
        Integer idEntiteContract) {
}
