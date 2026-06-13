package cnm.prs.mapper;

import cnm.prs.dto.SessionUtilisateurDto;
import cnm.prs.entity.SessionUtilisateur;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link SessionUtilisateur}.
 */
public final class SessionUtilisateurMapper {

    private SessionUtilisateurMapper() {
    }

    public static SessionUtilisateurDto toDto(SessionUtilisateur entity) {
        if (entity == null) {
            return null;
        }
        SessionUtilisateurDto dto = new SessionUtilisateurDto();
        dto.setIdSession(entity.getIdSession());
        dto.setImControleur(entity.getImControleur());
        dto.setDateConnexion(entity.getDateConnexion());
        dto.setDateDeconnexion(entity.getDateDeconnexion());
        dto.setIpAdresse(entity.getIpAdresse());
        dto.setUserAgent(entity.getUserAgent());
        dto.setSucces(entity.getSucces());
        return dto;
    }

    public static SessionUtilisateur toEntity(SessionUtilisateurDto dto) {
        if (dto == null) {
            return null;
        }
        SessionUtilisateur entity = new SessionUtilisateur();
        entity.setIdSession(dto.getIdSession());
        entity.setImControleur(dto.getImControleur());
        entity.setDateConnexion(dto.getDateConnexion());
        entity.setDateDeconnexion(dto.getDateDeconnexion());
        entity.setIpAdresse(dto.getIpAdresse());
        entity.setUserAgent(dto.getUserAgent());
        entity.setSucces(dto.getSucces());
        return entity;
    }
}
