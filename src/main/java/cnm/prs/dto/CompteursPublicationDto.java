package cnm.prs.dto;

/**
 * Compteurs de contenu par section du menu Chargé de publication — comptes globaux du workflow de
 * publication (rôle transversal, sans localité).
 *
 * @param aPublier publications en attente de publication ({@code t_publication.STATUT_PUBLI = EN_ATTENTE})
 * @param publiees publications publiées ({@code STATUT_PUBLI = PUBLIE})
 * @param retirees publications retirées ({@code STATUT_PUBLI = RETIRE})
 */
public record CompteursPublicationDto(
        long aPublier,
        long publiees,
        long retirees) {
}
