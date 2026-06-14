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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;

import cnm.prs.dto.ReceptionDto;
import cnm.prs.dto.ReceptionExisteDto;
import cnm.prs.service.ReceptionService;

/**
 * Contrôleur REST pour la ressource {@code receptions} (table {@code t_reception}).
 */
@RestController
@RequestMapping("/api/receptions")
public class ReceptionController {

    private final ReceptionService service;

    public ReceptionController(ReceptionService service) {
        this.service = service;
    }

    /**
     * Réceptions visibles. Avec {@code ?idDossier=}, ne renvoie que celles du dossier indiqué
     * (filtre serveur — évite de charger tout l'historique). Toujours scopé au périmètre de l'appelant.
     */
    @GetMapping
    public List<ReceptionDto> findAll(@RequestParam(required = false) Integer idDossier) {
        return idDossier != null ? service.findByDossier(idDossier) : service.findAll();
    }

    /**
     * Test léger « ce dossier est-il déjà réceptionné ? » — à utiliser avant d'enregistrer une
     * réception. Ne charge pas l'historique. (Pour lister les dossiers à réceptionner, utiliser
     * plutôt {@code GET /api/dossiers/a-receptionner}.)
     */
    @GetMapping("/dossier/{idDossier}/existe")
    public ReceptionExisteDto existe(@PathVariable Integer idDossier) {
        return new ReceptionExisteDto(idDossier, service.dejaReceptionne(idDossier));
    }

    @GetMapping("/{id}")
    public ReceptionDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    // Réception / complétude : Secrétaire, CC ou Président (§2.1, §3.4).
    @PreAuthorize("@perm.peutExercer('SECRETAIRE')")
    @PostMapping
    public ResponseEntity<ReceptionDto> create(@Valid @RequestBody ReceptionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PreAuthorize("@perm.peutExercer('SECRETAIRE')")
    @PutMapping("/{id}")
    public ReceptionDto update(@PathVariable Integer id, @Valid @RequestBody ReceptionDto dto) {
        return service.update(id, dto);
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
