package cnm.prs.dto;

import java.time.LocalDateTime;

/**
 * Métadonnées d'une pièce jointe (sans le contenu binaire) — exposées dans les listes et le
 * détail d'une inscription. Le contenu se récupère via l'endpoint de téléchargement dédié.
 */
public record PieceJointeMetaDto(
        String login,
        String typePiece,
        String libelle,
        String format,
        Long tailleOctets,
        LocalDateTime dateDepot,
        String hashSha256) {
}
