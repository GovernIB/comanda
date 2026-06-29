package es.caib.comanda.monitor.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.monitor.logic.intf.model.db.DbOverviewDto;
import es.caib.comanda.monitor.logic.service.DbMetricsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(BaseConfig.API_PATH + "/db-metrics")
@Tag(name = "23. DB Metrics", description = "Mètriques de la base de dades Oracle")
@PreAuthorize("hasRole(T(es.caib.comanda.base.config.BaseConfig).ROLE_ADMIN)")
public class DbMetricsController {

    @Autowired
    private DbMetricsServiceImpl service;

    @GetMapping("/overview")
    @Operation(summary = "Resum general de la BD")
    public ResponseEntity<DbOverviewDto> getOverview() {
        return ResponseEntity.ok(service.getOverview());
    }
}
