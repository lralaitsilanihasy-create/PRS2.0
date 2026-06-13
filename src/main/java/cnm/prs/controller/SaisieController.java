package cnm.prs.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import cnm.prs.dto.DossierDto;
import cnm.prs.dto.SaisieDossierRequest;
import cnm.prs.dto.SaisiePpmRequest;
import cnm.prs.service.SaisieService;

/**
 * Façade de saisie d'un dossier à soumettre (§3.1, Module 02) — réservée au profil {@code PRMP}.
 * « Saisir un PPM/DAO/MAOO » crée le dossier (BROUILLON) et son contenu en une transaction.
 */
@RestController
@RequestMapping("/api/saisies")
public class SaisieController {

    private final SaisieService service;

    public SaisieController(SaisieService service) {
        this.service = service;
    }

    /** Saisie d'un PPM (dossier PPM + PPM + lignes de marché). */
    @PreAuthorize("hasRole('PRMP')")
    @PostMapping("/ppm")
    public ResponseEntity<DossierDto> saisirPpm(@Valid @RequestBody SaisiePpmRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saisirPpm(req));
    }

    /** Saisie d'un dossier sans contenu (DAO, MAOO, …). */
    @PreAuthorize("hasRole('PRMP')")
    @PostMapping("/dossier")
    public ResponseEntity<DossierDto> saisirDossier(@Valid @RequestBody SaisieDossierRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saisirDossier(req));
    }
}
