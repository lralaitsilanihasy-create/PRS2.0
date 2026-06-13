package cnm.prs.dto;

/**
 * Taux de non-conformité d'un point de contrôle (§3.2 / §3.7).
 *
 * @param idPointCtrl            identifiant du point de contrôle
 * @param libelle                libellé du point
 * @param nbTotal                nombre total d'occurrences examinées
 * @param nbNonConforme          nombre d'occurrences non conformes
 * @param tauxNonConformitePct   taux de non-conformité en pourcentage
 */
public record PointNonConformiteDto(
        Integer idPointCtrl,
        String libelle,
        long nbTotal,
        long nbNonConforme,
        double tauxNonConformitePct) {
}
