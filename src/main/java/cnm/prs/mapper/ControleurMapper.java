package cnm.prs.mapper;

import cnm.prs.dto.ControleurDto;
import cnm.prs.entity.Controleur;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Controleur}.
 */
public final class ControleurMapper {

    private ControleurMapper() {
    }

    public static ControleurDto toDto(Controleur entity) {
        if (entity == null) {
            return null;
        }
        ControleurDto dto = new ControleurDto();
        dto.setImControleur(entity.getImControleur());
        dto.setNomCont(entity.getNomCont());
        dto.setPrenomsCont(entity.getPrenomsCont());
        dto.setEmailCont(entity.getEmailCont());
        dto.setTelCont(entity.getTelCont());
        dto.setIdProfile(entity.getIdProfile());
        dto.setIdLocalite(entity.getIdLocalite());
        dto.setIdSuperieur(entity.getIdSuperieur());
        dto.setTransversal(entity.getTransversal());
        return dto;
    }

    public static Controleur toEntity(ControleurDto dto) {
        if (dto == null) {
            return null;
        }
        Controleur entity = new Controleur();
        entity.setImControleur(dto.getImControleur());
        entity.setNomCont(dto.getNomCont());
        entity.setPrenomsCont(dto.getPrenomsCont());
        entity.setEmailCont(dto.getEmailCont());
        entity.setTelCont(dto.getTelCont());
        entity.setIdProfile(dto.getIdProfile());
        entity.setIdLocalite(dto.getIdLocalite());
        entity.setIdSuperieur(dto.getIdSuperieur());
        entity.setTransversal(dto.getTransversal());
        return entity;
    }
}
