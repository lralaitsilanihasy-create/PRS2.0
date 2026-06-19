package cnm.prs.dto;

/**
 * Un échange de l'historique d'un dossier clôturé (§3.6, ⚠️ règle ajoutée) :
 * une observation du vérificateur (t_verification) ou une rectification de la PRMP (t_audit_log).
 *
 * @param type      {@code OBSERVATION} (vérificateur) ou {@code RECTIFICATION} (PRMP)
 * @param date      ISO — date (jour) pour OBSERVATION ; date-heure pour RECTIFICATION
 * @param acteur    matricule du vérificateur (OBSERVATION) ou identifiant PRMP (RECTIFICATION)
 * @param texte     texte de l'observation, ou motif de rectification
 * @param obsLevees renseigné pour OBSERVATION (true = passage de clôture) ; null pour RECTIFICATION
 */
public record EchangeDto(String type, String date, String acteur, String texte, Boolean obsLevees) {
}
