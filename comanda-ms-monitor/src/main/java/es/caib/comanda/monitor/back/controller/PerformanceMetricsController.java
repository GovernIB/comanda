package es.caib.comanda.monitor.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.monitor.logic.intf.model.performance.PerformanceMetricsDto;
import es.caib.comanda.monitor.logic.service.PerformanceMetricsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping(BaseConfig.API_PATH + "/performance-metrics")
@Tag(name = "36. Performance Metrics", description = "Mètriques internes de rendiment")
@PreAuthorize("hasRole(T(es.caib.comanda.base.config.BaseConfig).ROLE_ADMIN)")
public class PerformanceMetricsController {

    private static final DateTimeFormatter EXPORT_FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Autowired
    private PerformanceMetricsServiceImpl service;

    @GetMapping
    @Operation(summary = "Estat actual i històric de les mètriques internes de rendiment")
    public ResponseEntity<PerformanceMetricsDto> getMetrics(
            @RequestParam(required = false) Integer windowSeconds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer maxPoints) {
        return ResponseEntity.ok(service.getMetrics(windowSeconds, from, to, maxPoints));
    }

    @GetMapping("/export")
    @Operation(summary = "Exporta tot l'històric en memòria de les mètriques internes de rendiment en format JSON")
    public ResponseEntity<PerformanceMetricsDto> exportMetrics() {
        PerformanceMetricsDto snapshot = service.exportHistory();
        LocalDateTime ts = snapshot.getCollectedAt() != null ? snapshot.getCollectedAt() : LocalDateTime.now();
        String filename = "performance-metrics-" + ts.format(EXPORT_FILENAME_FORMATTER) + ".json";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(snapshot);
    }
}
