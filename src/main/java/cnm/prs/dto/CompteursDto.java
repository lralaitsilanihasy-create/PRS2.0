package cnm.prs.dto;

/**
 * Compteurs de contenu par section du menu (tableau de bord Président). Comptes globaux
 * (toutes localités) du nombre d'éléments en attente de traitement dans chaque section.
 *
 * @param predispatch     dossiers prêts à dispatcher ({@code t_dossier.STATUT = PRET_DISPATCH})
 * @param dispatch        dossiers dispatchés ({@code t_dossier.STATUT = DISPATCHE})
 * @param projetsPV       projets de PV non signés ({@code t_pv_examen.STATUT_PV <> SIGNE})
 * @param lettresRenvoi   lettres de renvoi soumises ({@code t_lettre_renvoi.STATUT = SOUMIS})
 * @param pvDefinitifs    PV signés ({@code t_pv_examen.STATUT_PV = SIGNE})
 * @param demandesRetrait demandes de retrait en attente ({@code t_demande_retrait.STATUT = EN_ATTENTE})
 */
public record CompteursDto(
        long predispatch,
        long dispatch,
        long projetsPV,
        long lettresRenvoi,
        long pvDefinitifs,
        long demandesRetrait) {
}
