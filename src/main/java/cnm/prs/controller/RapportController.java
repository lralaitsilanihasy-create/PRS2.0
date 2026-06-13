package cnm.prs.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cnm.prs.service.ReportService;

/**
 * Rapports périodiques (§3.2 / §3.8, Module 10) — réservés au Président et à l'Administrateur
 * (vue toutes commissions).
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
     * (format ISO {@code yyyy-MM-dd}) pour borner la période.
     */
    @PreAuthorize("hasAnyRole('PRESIDENT','ADMINISTRATEUR')")
    @GetMapping(value = "/dossiers", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> rapportDossiers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] pdf = reportService.rapportDossiers(from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rapport-dossiers.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Même rapport des dossiers traités, au format Excel (.xlsx).
     */
    @PreAuthorize("hasAnyRole('PRESIDENT','ADMINISTRATEUR')")
    @GetMapping(value = "/dossiers/excel", produces = XLSX)
    public ResponseEntity<byte[]> rapportDossiersExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] excel = reportService.rapportDossiersExcel(from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rapport-dossiers.xlsx\"")
                .contentType(MediaType.parseMediaType(XLSX))
                .body(excel);
    }
}
