package cnm.prs.dto;

/**
 * Corps de {@code POST /api/examens/{id}/soumettre} : la soumission de l'examen produit le
 * <strong>Projet de PV</strong>. {@code idAvis} = avis du PV (FAV/FAVR/DEF/NSP), obligatoire sur le PV.
 * <p>(La lettre de renvoi est désormais une action séparée — ressource {@code /api/lettre-renvois}.)</p>
 */
public record ExamenSoumissionRequest(String idAvis) {
}
