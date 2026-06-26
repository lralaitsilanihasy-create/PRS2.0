package cnm.prs.dto;

import java.util.List;
import java.util.Map;

/**
 * Tableau de bord / KPIs (§3.2, §3.8).
 *
 * @param pipelineParStatut    nombre de dossiers par statut (flux de traitement)
 * @param nbDossiersSoumis     nombre total de dossiers soumis
 * @param nbDossiersConformes  nombre de dossiers conformes (observations levées)
 * @param tauxConformitePct    taux de conformité = conformes / soumis × 100
 * @param topNonConformite     top 5 des points de contrôle les plus non conformes
 * @param compteurs            compteurs de contenu par section du menu (Président)
 */
public record TableauBordDto(
        Map<String, Long> pipelineParStatut,
        long nbDossiersSoumis,
        long nbDossiersConformes,
        double tauxConformitePct,
        List<PointNonConformiteDto> topNonConformite,
        CompteursDto compteurs) {
}
