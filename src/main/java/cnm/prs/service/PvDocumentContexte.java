package cnm.prs.service;

import java.time.LocalDate;
import java.util.List;

/**
 * Données nécessaires au remplissage du modèle Word du Projet de PV (découplé des entités/repositories
 * pour rester testable). Les noms {@code nomPresident}/{@code nomChefCommission} sont {@code null}/vides
 * tant que le rôle n'a pas signé → la ligne correspondante du bloc « Étaient présents » est retirée.
 */
public record PvDocumentContexte(
        LocalDate dateExamen,
        String refPv,
        LocalDate dateReception,
        String entiteContractante,
        Integer anneeExercice,
        String localite,
        String nomPresident,
        String nomChefCommission,
        String nomMembre,
        String nomVerificateur,
        List<Observation> observations) {

    /** Une ligne de l'ANNEXE : point de contrôle non conforme et sa correction (au lieu de / lire). */
    public record Observation(String pointControle, String auLieuDe, String lire) {
    }
}
