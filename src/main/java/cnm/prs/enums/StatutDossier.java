package cnm.prs.enums;

/**
 * Statuts d'un dossier (colonne {@code t_dossier.STATUT}).
 *
 * <p><strong>Attention :</strong> seules les valeurs ci-dessous sont nommées
 * explicitement (littéralement) dans {@code docs/regles-gestion.md}. Les statuts
 * intermédiaires du circuit (reçu, dispatché, en examen, PV en cours,
 * vérification) n'y figurent pas sous forme de littéraux — ils sont décrits
 * uniquement comme des <em>étapes</em> du pipeline (§2). Ils restent à valider
 * avant d'être ajoutés ici (cf. ambiguïté A3 de l'analyse d'écart).</p>
 */
public enum StatutDossier {

    /** Saisie en cours par la PRMP (façade), pas encore soumise — invisible des contrôleurs. */
    BROUILLON,

    /** Soumis par la PRMP (action {@code …/soumettre}), entré dans le circuit. */
    SOUMIS,

    /** §2.2 — déclenché automatiquement dès {@code COMPLET = true}. */
    PRET_DISPATCH,

    /**
     * ⚠️ Règle ajoutée (non issue de la brochure) — dossier <strong>dispatché</strong> vers un Membre,
     * en attente d'examen (§2.3 → §2.4). Posé automatiquement à la création d'un dispatch.
     */
    DISPATCHE,

    /**
     * ⚠️ Règle ajoutée (non issue de la brochure) — dossier <strong>examiné</strong> : un examen a été
     * créé (§2.4 → §2.5). Posé automatiquement à la création de l'examen. Le dossier quitte « à examiner » ;
     * l'examen et le projet de PV restent <strong>modifiables</strong> via la navette tant que le PV n'est
     * pas signé.
     */
    EXAMINE,

    /**
     * ⚠️ Règle ajoutée (non issue de la brochure) — <strong>PV signé</strong> (§2.6) : posé automatiquement
     * à la signature du PV. L'examen devient <strong>définitif (verrouillé)</strong> ; le dossier est en
     * attente de vérification.
     */
    PV_SIGNE,

    /** §3.3 — retrait approuvé par le Chef de commission. */
    RETIRE,

    /** §2.8 / §3.6 — clôture automatique après levée des observations. */
    CLOTURE
}
