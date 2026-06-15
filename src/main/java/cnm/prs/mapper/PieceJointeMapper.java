package cnm.prs.mapper;

import cnm.prs.dto.PieceJointeMetaDto;
import cnm.prs.entity.PieceJointe;

/**
 * Projette une {@link PieceJointe} sur ses métadonnées {@link PieceJointeMetaDto} (jamais le
 * contenu binaire).
 */
public final class PieceJointeMapper {

    private PieceJointeMapper() {
    }

    public static PieceJointeMetaDto toDto(PieceJointe p) {
        if (p == null) {
            return null;
        }
        return new PieceJointeMetaDto(p.getLogin(), p.getTypePiece(), p.getLibelle(),
                p.getFormat(), p.getTailleOctets(), p.getDateDepot(), p.getHashSha256());
    }
}
