package cnm.prs.enums;

/**
 * Cycle de vie d'un compte d'authentification (colonne {@code t_compte_auth.STATUT}).
 *
 * <p>Une inscription PRMP est créée {@link #EN_ATTENTE} ; l'Administrateur la passe à
 * {@link #ACTIF} (compte activable) ou {@link #REFUSE} (avec motif). Invariant maintenu avec
 * le booléen historique {@code ACTIF} : {@code ACTIF=true} ⟺ {@code STATUT=ACTIF} (le login
 * continue de s'appuyer sur {@code ACTIF}).</p>
 */
public enum StatutCompte {

    /** Inscription déposée, en attente de vérification de l'arrêté par l'Administrateur. */
    EN_ATTENTE,

    /** Compte validé : la connexion est autorisée. */
    ACTIF,

    /** Inscription refusée par l'Administrateur (cf. {@code MOTIF_REFUS}). */
    REFUSE
}
