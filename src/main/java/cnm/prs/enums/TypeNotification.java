package cnm.prs.enums;

/**
 * Types de notification (colonne {@code t_notification.TYPE_NOTIF}).
 *
 * <p>Valeurs reprises littéralement de {@code docs/regles-gestion.md}
 * (§2, §3.1, §3.3, §3.4).</p>
 */
public enum TypeNotification {

    /** Dossier complet prêt à être dispatché — vers Président / CC (§2.2, §3.4). */
    PRET_DISPATCH,

    /** Copie de dispatch reçue par le CC (§3.3). */
    DISPATCH_CC,

    /** Demande de retrait soumise par une PRMP — vers le CC (§3.3). */
    DEMANDE_RETRAIT,

    /** Retrait approuvé par le CC — vers la PRMP (§3.1, §3.3). */
    RETRAIT_APPROUVE,

    /** Retrait rejeté par le CC — vers la PRMP (§3.1, §3.3). */
    RETRAIT_REJETE,

    /** PV passé au statut SIGNE — vers la PRMP (§3.1). */
    PV_SIGNE,

    /** Alerte de fin de mandat de la PRMP, J-90 / J-30 / J-7 (§3.1). */
    FIN_MANDAT,

    /** Alerte de délai / jalon (§3.2, §3.3). */
    ALERTE_DELAI,

    /** Dossier conforme clôturé, éligible à publication — vers le Chargé de publication (§3.7). */
    CLOTURE_ELIGIBLE,

    /** Nouvelle inscription (auto-inscription PRMP) en attente de validation — vers l'Administrateur. */
    NOUVELLE_INSCRIPTION,

    /** Mode de passation non déterminé automatiquement (aucune règle correspondante) — vers la PRMP (§3.1, Module 02). */
    MODE_NON_DETERMINE,

    /** Dossier officiellement soumis par la PRMP, en attente de réception — vers le Secrétaire/CC de la localité (§3.1, Module 03). */
    DOSSIER_SOUMIS,

    /** Inscription PRMP validée par l'Administrateur (compte activé) — vers la PRMP (§3.1). */
    INSCRIPTION_VALIDEE,

    /** Inscription PRMP refusée par l'Administrateur (avec motif) — vers la PRMP (§3.1). */
    INSCRIPTION_REFUSEE,

    /** Message reçu dans la messagerie interne — vers le destinataire du message (Module 04). */
    NOUVEAU_MESSAGE,

    /** Dossier dispatché à examiner — vers le Membre assigné (§2.3, transmission dispatch→examen). */
    EXAMEN_A_FAIRE,

    /** Projet de PV soumis, à valider/signer — vers le CC et le Président de la localité (§3.2, §3.5). */
    PV_A_VALIDER,

    /** Projet de PV retourné pour rectification (avec commentaire) — vers le Membre auteur (§3.2). */
    PV_A_RECTIFIER,

    /** Projet de PV accepté — vers le Membre auteur (§3.2). */
    PV_ACCEPTE,

    /** PV signé (favorable avec réserves) à vérifier — vers le Vérificateur de la localité (⚠️ règle ajoutée). */
    PV_A_VERIFIER,

    /** PV signé, dossier auto-clôturé — vers le Vérificateur pour information/lecture seule (⚠️ règle ajoutée). */
    PV_POUR_INFO
}
