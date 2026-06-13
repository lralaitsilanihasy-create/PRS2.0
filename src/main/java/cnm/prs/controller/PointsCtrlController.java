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

import cnm.prs.dto.PointsCtrlDto;
import cnm.prs.service.PointsCtrlService;

/**
 * Contrôleur REST pour la ressource {@code points-ctrls} (table {@code tr_points_ctrl}).
 */
@RestController
@RequestMapping("/api/points-ctrls")
public class PointsCtrlController {

    private final PointsCtrlService service;

    public PointsCtrlController(PointsCtrlService service) {
        this.service = service;
    }

    @GetMapping
    public List<PointsCtrlDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PointsCtrlDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<PointsCtrlDto> create(@Valid @RequestBody PointsCtrlDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public PointsCtrlDto update(@PathVariable Integer id, @Valid @RequestBody PointsCtrlDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
