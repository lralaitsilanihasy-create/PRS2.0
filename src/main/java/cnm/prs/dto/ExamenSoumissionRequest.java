package cnm.prs.dto;

/**
 * Corps de {@code POST /api/examens/{id}/soumettre} : la soumission de l'examen produit le
 * <strong>Projet de PV</strong>. {@code idAvis} = avis du PV (FAV/FAVR/DEF/NSP), obligatoire sur le PV.
 * {@code idSecretaireSeance} = matricule du Vérificateur désigné Secrétaire de séance (obligatoire,
 * doit être un VERIFICATEUR de la localité du dossier — validé en service, sinon 400).
 * <p>(La lettre de renvoi est désormais une action séparée — ressource {@code /api/lettre-renvois}.)</p>
 */
public record ExamenSoumissionRequest(String idAvis, String idSecretaireSeance) {
}
