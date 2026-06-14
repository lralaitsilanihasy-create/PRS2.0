package cnm.prs.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.security.CurrentUser;
import cnm.prs.service.ReportService;

/**
 * Rapports périodiques (§3.2 / §3.8, Module 10).
 *
 * <p>Portée par localité (§3.3) : le <strong>Président</strong> et l'<strong>Administrateur</strong>
 * voient toutes les commissions (et peuvent cibler une localité via {@code ?localite=}) ; le
 * <strong>Chef de commission</strong> est <strong>forcé sur sa propre localité</strong>.</p>
 */
@RestController
@RequestMapping("/api/rapports")
public class RapportController {

    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReportService reportService;

    public RapportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Rapport PDF des dossiers traités. Paramètres optionnels {@code from}/{@code to}
     * (format ISO {@code yyyy-MM-dd}) pour borner la période, et {@code localite} pour cibler une
     * commission (Président/Admin uniquement ; ignoré pour le CC, toujours forcé sur sa localité).
     */
    @PreAuthorize("hasAnyRole('PRESIDENT','ADMINISTRATEUR','CHEF_COMMISSION')")
    @GetMapping(value = "/dossiers", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> rapportDossiers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String localite) {
        byte[] pdf = reportService.rapportDossiers(from, to, localiteEffective(localite));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rapport-dossiers.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Même rapport des dossiers traités, au format Excel (.xlsx).
     */
    @PreAuthorize("hasAnyRole('PRESIDENT','ADMINISTRATEUR','CHEF_COMMISSION')")
    @GetMapping(value = "/dossiers/excel", produces = XLSX)
    public ResponseEntity<byte[]> rapportDossiersExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String localite) {
        byte[] excel = reportService.rapportDossiersExcel(from, to, localiteEffective(localite));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rapport-dossiers.xlsx\"")
                .contentType(MediaType.parseMediaType(XLSX))
                .body(excel);
    }

    /**
     * Localité réellement appliquée au rapport : pour un Chef de commission, sa propre localité
     * (le paramètre {@code localite} est ignoré, et son absence est une erreur) ; pour le
     * Président/Administrateur, le paramètre tel quel ({@code null} = toutes commissions).
     */
    private String localiteEffective(String localiteParam) {
        if (CurrentUser.profil().orElse(null) == ProfilUtilisateur.CHEF_COMMISSION) {
            return CurrentUser.localite().filter(s -> !s.isBlank())
                    .orElseThrow(() -> new AccessDeniedException(
                            "Aucune localité associée au Chef de commission : rapport indisponible."));
        }
        return localiteParam != null && !localiteParam.isBlank() ? localiteParam : null;
    }
}
