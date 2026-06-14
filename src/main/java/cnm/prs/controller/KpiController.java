package cnm.prs.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cnm.prs.dto.TableauBordDto;
import cnm.prs.service.KpiService;

/**
 * Tableau de bord & KPIs (§3.2, §3.8) : vue globale pour le Président/Administrateur,
 * vue filtrée sur sa localité pour le Chef de commission (§3.3).
 */
@RestController
@RequestMapping("/api/kpis")
public class KpiController {

    private final KpiService kpiService;

    public KpiController(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @PreAuthorize("hasAnyRole('PRESIDENT','ADMINISTRATEUR','CHEF_COMMISSION')")
    @GetMapping("/tableau-bord")
    public TableauBordDto tableauBord() {
        return kpiService.tableauBord();
    }
}
