package cnm.prs.mapper;

import java.math.BigDecimal;

import cnm.prs.dto.ReglePassationDto;
import cnm.prs.entity.ReglePassation;
import cnm.prs.entity.Seuil;

/**
 * Convertisseur entité &lt;-&gt; DTO pour {@link ReglePassation}.
 */
public final class ReglePassationMapper {

    private ReglePassationMapper() {
    }

    /**
     * Peuple aussi les <strong>libellés</strong> (lecture seule) de situation, mode et seuil via les
     * relations chargées de l'entité — appelé dans un contexte transactionnel (chargement lazy). Les
     * {@code id*} restent présents (nécessaires à la création/édition).
     */
    public static ReglePassationDto toDto(ReglePassation entity) {
        if (entity == null) {
            return null;
        }
        ReglePassationDto dto = new ReglePassationDto();
        dto.setIdRegle(entity.getIdRegle());
        dto.setIdSituation(entity.getIdSituation());
        dto.setIdSeuil(entity.getIdSeuil());
        dto.setIdMode(entity.getIdMode());
        dto.setPriorite(entity.getPriorite());
        dto.setLibelleSituation(entity.getSituation() == null ? null : entity.getSituation().getLibelle());
        dto.setLibelleMode(entity.getMode() == null ? null : entity.getMode().getLibelle());
        dto.setLibelleSeuil(libelleSeuil(entity.getSeuil()));
        return dto;
    }

    /** « montantMin à montantMax », « ≥ montantMin » (max nul), « ≤ montantMax » (min nul), ou {@code null}. */
    private static String libelleSeuil(Seuil seuil) {
        if (seuil == null) {
            return null;
        }
        BigDecimal min = seuil.getMontantMin();
        BigDecimal max = seuil.getMontantMax();
        if (min != null && max != null) {
            return montant(min) + " à " + montant(max);
        }
        if (min != null) {
            return "≥ " + montant(min);
        }
        if (max != null) {
            return "≤ " + montant(max);
        }
        return null;
    }

    /** Montant lisible sans zéros décimaux superflus (ex. {@code 100000000}). */
    private static String montant(BigDecimal m) {
        return m.stripTrailingZeros().toPlainString();
    }

    public static ReglePassation toEntity(ReglePassationDto dto) {
        if (dto == null) {
            return null;
        }
        ReglePassation entity = new ReglePassation();
        entity.setIdRegle(dto.getIdRegle());
        entity.setIdSituation(dto.getIdSituation());
        entity.setIdSeuil(dto.getIdSeuil());
        entity.setIdMode(dto.getIdMode());
        entity.setPriorite(dto.getPriorite());
        return entity;
    }
}
