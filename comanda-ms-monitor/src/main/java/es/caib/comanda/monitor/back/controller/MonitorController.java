package es.caib.comanda.monitor.back.controller;

import es.caib.comanda.monitor.logic.intf.model.Monitor;
import es.caib.comanda.monitor.logic.service.MonitorSchedulerService;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Servei de consulta d'informació de indicadors estadístics.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RestController("monitorController")
@RequestMapping(BaseConfig.API_PATH + "/monitors")
@Tag(name = "Monitor", description = "Servei de consulta del monitor")
public class MonitorController extends BaseMutableResourceController<Monitor, Long> {

    @Autowired
    private MonitorSchedulerService schedulerService;

    @PostMapping("/programarBorrat")
    public ResponseEntity<Void> programarBorrat() {
        log.info("Rebuda petició d'actualització de procés de borrat");
        schedulerService.programarBorrat();
        return ResponseEntity.ok().build();
    }

}
