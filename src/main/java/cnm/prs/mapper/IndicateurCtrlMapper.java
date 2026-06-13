package cnm.prs.mapper;

import cnm.prs.dto.IndicateurCtrlDto;
import cnm.prs.entity.IndicateurCtrl;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link IndicateurCtrl}.
 */
public final class IndicateurCtrlMapper {

    private IndicateurCtrlMapper() {
    }

    public static IndicateurCtrlDto toDto(IndicateurCtrl entity) {
        if (entity == null) {
            return null;
        }
        IndicateurCtrlDto dto = new IndicateurCtrlDto();
        dto.setIdIndicateur(entity.getIdIndicateur());
        dto.setImControleur(entity.getImControleur());
        dto.setPeriode(entity.getPeriode());
        dto.setNbExamens(entity.getNbExamens());
        dto.setNbConformes(entity.getNbConformes());
        dto.setDelaiMoyenExamen(entity.getDelaiMoyenExamen());
        dto.setNbObsEmises(entity.getNbObsEmises());
        return dto;
    }

    public static IndicateurCtrl toEntity(IndicateurCtrlDto dto) {
        if (dto == null) {
            return null;
        }
        IndicateurCtrl entity = new IndicateurCtrl();
        entity.setIdIndicateur(dto.getIdIndicateur());
        entity.setImControleur(dto.getImControleur());
        entity.setPeriode(dto.getPeriode());
        entity.setNbExamens(dto.getNbExamens());
        entity.setNbConformes(dto.getNbConformes());
        entity.setDelaiMoyenExamen(dto.getDelaiMoyenExamen());
        entity.setNbObsEmises(dto.getNbObsEmises());
        return entity;
    }
}
