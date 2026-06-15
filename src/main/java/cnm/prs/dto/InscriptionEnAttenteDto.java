package cnm.prs.dto;

import java.util.List;

/**
 * Vue d'une inscription PRMP en attente de validation, pour l'Administrateur : identité,
 * entités déclarées (existantes/proposées, avec disponibilité) et métadonnées des pièces.
 * Le contenu des pièces se récupère via l'endpoint de téléchargement dédié.
 */
public record InscriptionEnAttenteDto(
        String login,
        String idPrmp,
        String nomPrmp,
        String prenomsPrmp,
        String emailPrmp,
        List<DeclarationEntiteDto> entitesDeclarees,
        List<PieceJointeMetaDto> pieces) {
}
