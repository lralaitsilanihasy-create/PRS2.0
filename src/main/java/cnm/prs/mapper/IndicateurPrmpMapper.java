package cnm.prs.mapper;

import cnm.prs.dto.IndicateurPrmpDto;
import cnm.prs.entity.IndicateurPrmp;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link IndicateurPrmp}.
 */
public final class IndicateurPrmpMapper {

    private IndicateurPrmpMapper() {
    }

    public static IndicateurPrmpDto toDto(IndicateurPrmp entity) {
        if (entity == null) {
            return null;
        }
        IndicateurPrmpDto dto = new IndicateurPrmpDto();
        dto.setIdIndicateurPrmp(entity.getIdIndicateurPrmp());
        dto.setIdPrmp(entity.getIdPrmp());
        dto.setExercice(entity.getExercice());
        dto.setNbPpmSoumis(entity.getNbPpmSoumis());
        dto.setNbDossiersSoumis(entity.getNbDossiersSoumis());
        dto.setNbDossiersConformes(entity.getNbDossiersConformes());
        dto.setNbDossiersNonConformes(entity.getNbDossiersNonConformes());
        dto.setNbRetours(entity.getNbRetours());
        dto.setNbRetraits(entity.getNbRetraits());
        dto.setTauxConformite(entity.getTauxConformite());
        dto.setDelaiMoyCorrectionJours(entity.getDelaiMoyCorrectionJours());
        dto.setMontTotalSoumis(entity.getMontTotalSoumis());
        dto.setDateMaj(entity.getDateMaj());
        return dto;
    }

    public static IndicateurPrmp toEntity(IndicateurPrmpDto dto) {
        if (dto == null) {
            return null;
        }
        IndicateurPrmp entity = new IndicateurPrmp();
        entity.setIdIndicateurPrmp(dto.getIdIndicateurPrmp());
        entity.setIdPrmp(dto.getIdPrmp());
        entity.setExercice(dto.getExercice());
        entity.setNbPpmSoumis(dto.getNbPpmSoumis());
        entity.setNbDossiersSoumis(dto.getNbDossiersSoumis());
        entity.setNbDossiersConformes(dto.getNbDossiersConformes());
        entity.setNbDossiersNonConformes(dto.getNbDossiersNonConformes());
        entity.setNbRetours(dto.getNbRetours());
        entity.setNbRetraits(dto.getNbRetraits());
        entity.setTauxConformite(dto.getTauxConformite());
        entity.setDelaiMoyCorrectionJours(dto.getDelaiMoyCorrectionJours());
        entity.setMontTotalSoumis(dto.getMontTotalSoumis());
        entity.setDateMaj(dto.getDateMaj());
        return entity;
    }
}
