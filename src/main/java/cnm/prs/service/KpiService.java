package cnm.prs.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.PointNonConformiteDto;
import cnm.prs.dto.TableauBordDto;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.ExamenDetailRepository;
import cnm.prs.repository.VerificationRepository;
import cnm.prs.security.CurrentUser;

/**
 * Calcul des KPIs du tableau de bord (§3.2, §3.7, §3.8) à partir des tables opérationnelles :
 * pipeline par statut, taux de conformité, top non-conformité par point de contrôle.
 */
@Service
@Transactional(readOnly = true)
public class KpiService {

    private final DossierRepository dossierRepository;
    private final VerificationRepository verificationRepository;
    private final ExamenDetailRepository examenDetailRepository;

    public KpiService(DossierRepository dossierRepository, VerificationRepository verificationRepository,
            ExamenDetailRepository examenDetailRepository) {
        this.dossierRepository = dossierRepository;
        this.verificationRepository = verificationRepository;
        this.examenDetailRepository = examenDetailRepository;
    }

    /**
     * Tableau de bord du contrôleur courant : <strong>global</strong> pour le Président et
     * l'Administrateur ; <strong>filtré sur sa localité</strong> pour le Chef de commission (§3.3).
     */
    public TableauBordDto tableauBord() {
        ProfilUtilisateur profil = CurrentUser.profil().orElse(null);
        if (profil == ProfilUtilisateur.CHEF_COMMISSION) {
            String localite = CurrentUser.localite().filter(s -> !s.isBlank()).orElse(null);
            if (localite == null) {
                return new TableauBordDto(new LinkedHashMap<>(), 0, 0, 0.0, List.of());
            }
            return calculer(localite);
        }
        return calculer(null); // Président / Administrateur : toutes localités
    }

    /** Calcule le tableau de bord, global si {@code localite == null}, sinon limité à cette localité. */
    private TableauBordDto calculer(String localite) {
        List<Object[]> statuts = localite == null
                ? dossierRepository.compterParStatut()
                : dossierRepository.compterParStatutParLocalite(localite);
        Map<String, Long> pipeline = new LinkedHashMap<>();
        for (Object[] ligne : statuts) {
            String statut = ligne[0] != null ? (String) ligne[0] : "(non défini)";
            pipeline.put(statut, ((Number) ligne[1]).longValue());
        }

        long nbSoumis = localite == null
                ? dossierRepository.compterSoumis()
                : dossierRepository.compterSoumisParLocalite(localite);
        long nbConformes = localite == null
                ? verificationRepository.compterDossiersConformes()
                : verificationRepository.compterDossiersConformesParLocalite(localite);
        double tauxConformite = nbSoumis == 0 ? 0.0 : arrondi(nbConformes * 100.0 / nbSoumis);

        List<Object[]> stats = localite == null
                ? examenDetailRepository.statsNonConformiteParPoint()
                : examenDetailRepository.statsNonConformiteParPointParLocalite(localite);
        List<PointNonConformiteDto> topNonConformite = stats.stream()
                .map(this::versPointNonConformite)
                .sorted(Comparator.comparingDouble(PointNonConformiteDto::tauxNonConformitePct).reversed())
                .limit(5)
                .toList();

        return new TableauBordDto(pipeline, nbSoumis, nbConformes, tauxConformite, topNonConformite);
    }

    private PointNonConformiteDto versPointNonConformite(Object[] ligne) {
        long total = ((Number) ligne[2]).longValue();
        long nonConforme = ligne[3] != null ? ((Number) ligne[3]).longValue() : 0L;
        double taux = total == 0 ? 0.0 : arrondi(nonConforme * 100.0 / total);
        return new PointNonConformiteDto((Integer) ligne[0], (String) ligne[1], total, nonConforme, taux);
    }

    private double arrondi(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }
}
