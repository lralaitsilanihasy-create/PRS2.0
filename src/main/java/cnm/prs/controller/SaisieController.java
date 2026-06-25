package cnm.prs.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import cnm.prs.dto.DossierDto;
import cnm.prs.dto.EditionPpmRequest;
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
    @PostMapping(value = "/ppm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DossierDto> saisirPpm(@Valid @RequestBody SaisiePpmRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saisirPpm(req));
    }

    /**
     * Saisie d'un PPM avec ses pièces jointes initiales (multipart). Part {@code data} = JSON
     * {@link SaisiePpmRequest} ; parts fichiers nommés {@code piece_<idTypePiece>}. Chaque pièce est
     * persistée avec {@code apresLettreRenvoi=false}.
     */
    @PreAuthorize("hasRole('PRMP')")
    @PostMapping(value = "/ppm", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DossierDto> saisirPpmAvecPieces(
            @Valid @RequestPart("data") SaisiePpmRequest req, HttpServletRequest request) {
        Map<Integer, MultipartFile> pieces = new HashMap<>();
        if (request instanceof MultipartHttpServletRequest multipart) {
            for (Iterator<String> it = multipart.getFileNames(); it.hasNext();) {
                String nom = it.next();
                if (nom != null && nom.startsWith("piece_")) {
                    try {
                        Integer idTypePiece = Integer.valueOf(nom.substring("piece_".length()));
                        pieces.put(idTypePiece, multipart.getFile(nom));
                    } catch (NumberFormatException ignore) {
                        // part fichier au nom non conforme : ignorée (pas un piece_<idTypePiece>)
                    }
                }
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saisirPpm(req, pieces));
    }

    /** Saisie d'un dossier sans contenu (DAO, MAOO, …). */
    @PreAuthorize("hasRole('PRMP')")
    @PostMapping("/dossier")
    public ResponseEntity<DossierDto> saisirDossier(@Valid @RequestBody SaisieDossierRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saisirDossier(req));
    }

    /** Édition d'un brouillon PPM : en-tête + réconciliation des lignes de marché, en une transaction. */
    @PreAuthorize("hasRole('PRMP')")
    @PutMapping("/ppm/{idDossier}")
    public DossierDto editerPpm(@PathVariable Integer idDossier, @Valid @RequestBody EditionPpmRequest req) {
        return service.editerPpm(idDossier, req);
    }
}
