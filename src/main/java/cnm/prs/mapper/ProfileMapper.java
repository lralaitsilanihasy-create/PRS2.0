package cnm.prs.mapper;

import cnm.prs.dto.ProfileDto;
import cnm.prs.entity.Profile;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link Profile}.
 */
public final class ProfileMapper {

    private ProfileMapper() {
    }

    public static ProfileDto toDto(Profile entity) {
        if (entity == null) {
            return null;
        }
        ProfileDto dto = new ProfileDto();
        dto.setIdProfile(entity.getIdProfile());
        dto.setProfile(entity.getProfile());
        return dto;
    }

    public static Profile toEntity(ProfileDto dto) {
        if (dto == null) {
            return null;
        }
        Profile entity = new Profile();
        entity.setIdProfile(dto.getIdProfile());
        entity.setProfile(dto.getProfile());
        return entity;
    }
}
