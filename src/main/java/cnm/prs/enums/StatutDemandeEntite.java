package cnm.prs.enums;

/**
 * État d'une déclaration d'entité faite à l'inscription (colonne
 * {@code t_prmp_entite_demande.STATUT_DEMANDE}).
 */
public enum StatutDemandeEntite {

    /** Déclarée, en attente de la décision de l'Administrateur. */
    EN_ATTENTE,

    /** Acceptée : une affectation active ({@code t_prmp_entite}) a été créée. */
    VALIDEE,

    /** Refusée (ex. entité déjà rattachée à une autre PRMP active) — cf. {@code MOTIF}. */
    REFUSEE
}
