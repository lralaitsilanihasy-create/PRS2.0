package cnm.prs.enums;

/**
 * Sens d'un mouvement de navette du projet de PV (colonne {@code t_pv_navette.SENS}).
 *
 * <p>Valeurs reprises littéralement de {@code docs/regles-gestion.md}
 * (§3.2, §3.5).</p>
 */
public enum SensNavette {

    /** Soumission du projet par le Membre vers le Président / CC (§3.5). */
    SOUMISSION,

    /** Retour pour rectification par le Président / CC (§3.2). */
    RETOUR_RECTIF,

    /** Acceptation du projet par le Président / CC (§3.2). */
    ACCEPTATION
}
