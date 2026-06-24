package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Corps de {@code POST /api/examens/{id}/soumettre} : le Membre choisit le résultat de l'examen —
 * un <strong>Projet de PV</strong> ({@code PV}) ou une <strong>lettre de renvoi</strong>
 * ({@code LETTRE_RENVOI}). {@code objetLettre} n'est requis que pour {@code LETTRE_RENVOI}.
 */
public record ExamenSoumissionRequest(

        @NotNull(message = "Le type de résultat est obligatoire (PV ou LETTRE_RENVOI).")
        String typeResultat,

        /** Avis du PV (FAV/FAVR/DEF/NSP) — requis pour {@code typeResultat=PV} (obligatoire sur le PV). */
        String idAvis,

        String objetLettre) {
}
