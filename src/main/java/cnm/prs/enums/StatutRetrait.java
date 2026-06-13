package cnm.prs.enums;

/**
 * Statuts d'une demande de retrait de dossier (colonne {@code t_demande_retrait.STATUT}).
 *
 * <p>Valeurs reprises littéralement de {@code docs/regles-gestion.md} §3.1
 * (« Suivi de la demande : EN_ATTENTE / APPROUVE / REJETE ») et §3.3.</p>
 */
public enum StatutRetrait {

    /** Demande soumise par la PRMP, en attente de décision du CC. */
    EN_ATTENTE,

    /** Retrait approuvé par le CC ({@code t_dossier.STATUT = RETIRE}, §3.3). */
    APPROUVE,

    /** Retrait rejeté par le CC. */
    REJETE;

    /** Vrai si le statut correspond à une décision du CC (approbation ou rejet). */
    public boolean estDecision() {
        return this == APPROUVE || this == REJETE;
    }
}
