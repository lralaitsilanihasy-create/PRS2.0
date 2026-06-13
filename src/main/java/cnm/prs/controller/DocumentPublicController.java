package cnm.prs.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;

import cnm.prs.dto.DocumentPublicDto;
import cnm.prs.dto.EmpreinteRequest;
import cnm.prs.dto.VerificationIntegriteResult;
import cnm.prs.service.DocumentPublicService;

/**
 * Contrôleur REST pour la ressource {@code document-publics} (table {@code t_document_public}).
 */
@RestController
@RequestMapping("/api/document-publics")
public class DocumentPublicController {

    private final DocumentPublicService service;

    public DocumentPublicController(DocumentPublicService service) {
        this.service = service;
    }

    @GetMapping
    public List<DocumentPublicDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public DocumentPublicDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    // Dépôt de documents publics : réservé au Chargé de publication (§3.7, Module 09).
    @PreAuthorize("hasRole('CHARGE_PUBLICATION')")
    @PostMapping
    public ResponseEntity<DocumentPublicDto> create(@Valid @RequestBody DocumentPublicDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PreAuthorize("hasRole('CHARGE_PUBLICATION')")
    @PutMapping("/{id}")
    public DocumentPublicDto update(@PathVariable Integer id, @Valid @RequestBody DocumentPublicDto dto) {
        return service.update(id, dto);
    }

    @PreAuthorize("hasRole('CHARGE_PUBLICATION')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------------
    // Intégrité SHA-256 des documents publics (§3.7)
    // ----------------------------------------------------------------------

    /** Enregistre l'empreinte SHA-256 du document à partir de son contenu (Base64). */
    @PreAuthorize("hasRole('CHARGE_PUBLICATION')")
    @PostMapping("/{id}/empreinte")
    public DocumentPublicDto enregistrerEmpreinte(@PathVariable Integer id,
            @Valid @RequestBody EmpreinteRequest req) {
        return service.enregistrerEmpreinte(id, req.contenuBase64());
    }

    /** Vérifie l'intégrité du document (empreinte recalculée vs enregistrée). */
    @PreAuthorize("hasRole('CHARGE_PUBLICATION')")
    @PostMapping("/{id}/verifier-integrite")
    public VerificationIntegriteResult verifierIntegrite(@PathVariable Integer id,
            @Valid @RequestBody EmpreinteRequest req) {
        return service.verifierIntegrite(id, req.contenuBase64());
    }
}
