package cnm.prs.enums;

/**
 * Statuts du projet de PV (colonne {@code t_pv_examen.STATUT_PV}).
 *
 * <p>Valeurs reprises littéralement de {@code docs/regles-gestion.md}
 * (§2 « Statuts de navette du PV » et §3.5 / §3.2).</p>
 *
 * <pre>
 * BROUILLON ──soumettre──▶ PROJET_SOUMIS ──accepter──▶ PROJET_ACCEPTE ──signer──▶ SIGNE
 *                              ▲   │
 *                              │   └──retourner──▶ EN_RECTIFICATION
 *                              └────soumettre────────────┘
 * </pre>
 */
public enum StatutPv {

    /** Projet rédigé par le Membre, modifiable librement (§3.5). */
    BROUILLON,

    /** Projet soumis au Président / CC (§3.5, navette SENS = SOUMISSION). */
    PROJET_SOUMIS,

    /** Projet retourné pour correction (§3.2, navette SENS = RETOUR_RECTIF). */
    EN_RECTIFICATION,

    /** Projet validé, devient signable (§3.2, navette SENS = ACCEPTATION). */
    PROJET_ACCEPTE,

    /** PV co-signé Membre + (Président ou CC) (§3.5). */
    SIGNE
}
