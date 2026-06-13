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

import jakarta.validation.Valid;

import cnm.prs.dto.MessageDto;
import cnm.prs.dto.MessageEnvoiRequest;
import cnm.prs.service.MessageService;

/**
 * Contrôleur REST pour la ressource {@code messages} (table {@code t_message}).
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    @GetMapping
    public List<MessageDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public MessageDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<MessageDto> create(@Valid @RequestBody MessageDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public MessageDto update(@PathVariable Integer id, @Valid @RequestBody MessageDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------------
    // Messagerie (§ Module 04)
    // ----------------------------------------------------------------------

    /** Envoi d'un message (expéditeur = utilisateur authentifié). */
    @PostMapping("/envoyer")
    public ResponseEntity<MessageDto> envoyer(@Valid @RequestBody MessageEnvoiRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.envoyer(req));
    }

    /** Boîte de réception de l'utilisateur courant. */
    @GetMapping("/recus")
    public List<MessageDto> recus() {
        return service.recus();
    }

    /** Messages envoyés par l'utilisateur courant. */
    @GetMapping("/envoyes")
    public List<MessageDto> envoyes() {
        return service.envoyes();
    }

    /** Marque un message reçu comme lu (réservé au destinataire). */
    @PostMapping("/{id}/lu")
    public MessageDto marquerLu(@PathVariable Integer id) {
        return service.marquerLu(id);
    }
}
