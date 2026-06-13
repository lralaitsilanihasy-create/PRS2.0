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

    /** §2.2 — déclenché automatiquement dès {@code COMPLET = true}. */
    PRET_DISPATCH,

    /** §3.3 — retrait approuvé par le Chef de commission. */
    RETIRE,

    /** §2.8 / §3.6 — clôture automatique après levée des observations. */
    CLOTURE
}
