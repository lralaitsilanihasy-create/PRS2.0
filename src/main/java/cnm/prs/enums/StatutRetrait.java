package cnm.prs.enums;

/**
 * Statuts d'une demande de retrait de dossier (colonne {@code t_demande_retrait.STATUT}).
 *
 * <p>Valeurs reprises littéralement de {@code docs/regles-gestion.md} §3.1
 * (« Suivi de la demande : EN_ATTENTE / APPROUVE / REJETE ») et §3.3.</p>
 */
public enum StatutRetrait {

    /** Demande soumise par la PRMP, en attente de décision (CC de la localité ou Président). */
    EN_ATTENTE,

    /** Retrait accepté → le dossier repasse en {@code BROUILLON} (⚠️ règle ajoutée). */
    ACCEPTEE,

    /** Retrait refusé → dossier inchangé, motif de refus enregistré. */
    REFUSEE;

    /** Vrai si le statut correspond à une décision (acceptation ou refus). */
    public boolean estDecision() {
        return this == ACCEPTEE || this == REFUSEE;
    }
}
