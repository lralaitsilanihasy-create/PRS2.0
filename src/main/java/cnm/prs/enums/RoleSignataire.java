package cnm.prs.enums;

/**
 * Rôle d'un co-signataire du PV définitif (§3.2, §3.3, §3.5).
 *
 * <p>Le PV passe à {@link StatutPv#SIGNE} lorsque le Membre a signé
 * <em>et</em> qu'au moins l'un des deux — Président ou CC — a co-signé
 * (contrainte {@code t_pv_examen_cosignataire_check}).</p>
 */
public enum RoleSignataire {

    /** Membre rédacteur — renseigne {@code DATE_SIGNATURE_MEMBRE}. */
    MEMBRE,

    /** Président — renseigne {@code DATE_SIGNATURE_PRESIDENT}. */
    PRESIDENT,

    /** Chef de commission — renseigne {@code DATE_SIGNATURE_CC}. */
    CC
}
