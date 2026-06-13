package cnm.prs.mapper;

import cnm.prs.dto.DocumentPublicDto;
import cnm.prs.entity.DocumentPublic;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link DocumentPublic}.
 */
public final class DocumentPublicMapper {

    private DocumentPublicMapper() {
    }

    public static DocumentPublicDto toDto(DocumentPublic entity) {
        if (entity == null) {
            return null;
        }
        DocumentPublicDto dto = new DocumentPublicDto();
        dto.setIdDocPublic(entity.getIdDocPublic());
        dto.setIdPublication(entity.getIdPublication());
        dto.setTypeDoc(entity.getTypeDoc());
        dto.setLibelleDoc(entity.getLibelleDoc());
        dto.setCheminFichier(entity.getCheminFichier());
        dto.setFormat(entity.getFormat());
        dto.setTailleOctets(entity.getTailleOctets());
        dto.setDateDepot(entity.getDateDepot());
        dto.setHashSha256(entity.getHashSha256());
        return dto;
    }

    public static DocumentPublic toEntity(DocumentPublicDto dto) {
        if (dto == null) {
            return null;
        }
        DocumentPublic entity = new DocumentPublic();
        entity.setIdDocPublic(dto.getIdDocPublic());
        entity.setIdPublication(dto.getIdPublication());
        entity.setTypeDoc(dto.getTypeDoc());
        entity.setLibelleDoc(dto.getLibelleDoc());
        entity.setCheminFichier(dto.getCheminFichier());
        entity.setFormat(dto.getFormat());
        entity.setTailleOctets(dto.getTailleOctets());
        entity.setDateDepot(dto.getDateDepot());
        entity.setHashSha256(dto.getHashSha256());
        return entity;
    }
}
