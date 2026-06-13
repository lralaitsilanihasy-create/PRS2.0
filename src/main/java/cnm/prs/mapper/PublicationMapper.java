package cnm.prs.mapper;

import cnm.prs.dto.PublicationDto;
import cnm.prs.entity.Publication;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Publication}.
 */
public final class PublicationMapper {

    private PublicationMapper() {
    }

    public static PublicationDto toDto(Publication entity) {
        if (entity == null) {
            return null;
        }
        PublicationDto dto = new PublicationDto();
        dto.setIdPublication(entity.getIdPublication());
        dto.setTypeObjet(entity.getTypeObjet());
        dto.setIdObjet(entity.getIdObjet());
        dto.setDatePublication(entity.getDatePublication());
        dto.setImPubliePar(entity.getImPubliePar());
        dto.setStatutPubli(entity.getStatutPubli());
        dto.setDateRetrait(entity.getDateRetrait());
        dto.setMotifRetrait(entity.getMotifRetrait());
        dto.setNbConsultations(entity.getNbConsultations());
        return dto;
    }

    public static Publication toEntity(PublicationDto dto) {
        if (dto == null) {
            return null;
        }
        Publication entity = new Publication();
        entity.setIdPublication(dto.getIdPublication());
        entity.setTypeObjet(dto.getTypeObjet());
        entity.setIdObjet(dto.getIdObjet());
        entity.setDatePublication(dto.getDatePublication());
        entity.setImPubliePar(dto.getImPubliePar());
        entity.setStatutPubli(dto.getStatutPubli());
        entity.setDateRetrait(dto.getDateRetrait());
        entity.setMotifRetrait(dto.getMotifRetrait());
        entity.setNbConsultations(dto.getNbConsultations());
        return entity;
    }
}
