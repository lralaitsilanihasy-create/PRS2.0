package cnm.prs.dto;

/**
 * Une entité déclarée par une PRMP à l'inscription, telle que présentée à l'Administrateur
 * pour instruction. {@code idEntiteContract} non nul = entité existante ; sinon entité proposée
 * (champs {@code *Propose}). {@code disponible} indique, pour une entité existante, si elle n'est
 * pas déjà rattachée à une PRMP active.
 */
public record DeclarationEntiteDto(
        Integer idDemande,
        Integer idEntiteContract,
        String libellePropose,
        String adressePropose,
        String idLocalitePropose,
        String categoriePropose,
        String statutDemande,
        String motif,
        Boolean disponible) {
}
