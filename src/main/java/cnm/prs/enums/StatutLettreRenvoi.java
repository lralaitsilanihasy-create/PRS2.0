package cnm.prs.enums;

/**
 * Statuts d'une lettre de renvoi (colonne {@code t_lettre_renvoi.STATUT}).
 * Cycle : {@code BROUILLON → SOUMIS → SIGNE}.
 */
public enum StatutLettreRenvoi {

    /** Créée à la soumission de l'examen (alternative au Projet de PV) ; éditable par le Membre. */
    BROUILLON,

    /** Soumise par le Membre propriétaire ; en attente de signature (CC ou Président). */
    SOUMIS,

    /** Signée par le CC ou le Président — définitive. */
    SIGNE
}
