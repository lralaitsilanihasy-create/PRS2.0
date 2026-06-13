package cnm.prs.enums;

/**
 * Statuts d'une publication du portail de transparence (colonne {@code t_publication.STATUT_PUBLI}, §3.7).
 *
 * <pre>EN_ATTENTE ──publier──▶ PUBLIE ──retirer(motif)──▶ RETIRE</pre>
 */
public enum StatutPublication {

    /** En attente de publication. */
    EN_ATTENTE,

    /** Publiée sur le portail. */
    PUBLIE,

    /** Dépubliée (retrait documenté : motif + date). */
    RETIRE
}
