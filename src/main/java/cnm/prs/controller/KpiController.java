package cnm.prs.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cnm.prs.dto.TableauBordDto;
import cnm.prs.service.KpiService;

/**
 * Tableau de bord & KPIs (§3.2, §3.8) — vue globale réservée au Président et à l'Administrateur.
 */
@RestController
@RequestMapping("/api/kpis")
public class KpiController {

    private final KpiService kpiService;

    public KpiController(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @PreAuthorize("hasAnyRole('PRESIDENT','ADMINISTRATEUR')")
    @GetMapping("/tableau-bord")
    public TableauBordDto tableauBord() {
        return kpiService.tableauBord();
    }
}
