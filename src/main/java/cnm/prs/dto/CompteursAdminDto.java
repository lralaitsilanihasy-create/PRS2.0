package cnm.prs.dto;

/**
 * Compteurs de contenu par section du menu Administrateur — comptes globaux (rôle transversal).
 *
 * @param inscriptionsEnAttente inscriptions PRMP en attente de validation
 *                              ({@code t_compte_auth.STATUT = EN_ATTENTE}, type PRMP)
 * @param comptes               nombre total de comptes d'authentification
 * @param journalAudit          nombre total d'entrées du journal d'audit
 */
public record CompteursAdminDto(
        long inscriptionsEnAttente,
        long comptes,
        long journalAudit) {
}
