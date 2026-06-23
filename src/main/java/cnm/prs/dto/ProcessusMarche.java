package cnm.prs.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/**
 * Processus de marché saisi dans une ligne de marché ({@link SaisieMarcheLigne}) : un
 * {@code idCapm} (FK {@code t_capm}) avec ses dates prévisionnelles début/fin. Persisté en une
 * ligne {@code t_marche_prevision}. Les messages portent le nom de champ attendu par le contrat.
 */
public record ProcessusMarche(

        @NotNull(message = "Le processus est obligatoire.")
        Integer idCapm,

        @NotNull(message = "La date de début est obligatoire.")
        LocalDate dateDebut,

        @NotNull(message = "La date de fin est obligatoire.")
        LocalDate dateFin) {
}
