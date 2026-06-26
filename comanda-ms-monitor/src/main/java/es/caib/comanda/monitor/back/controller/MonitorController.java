package es.caib.comanda.monitor.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.base.config.Cues;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.monitor.logic.intf.model.Monitor;
import es.caib.comanda.monitor.logic.service.MonitorSchedulerService;
import es.caib.comanda.monitor.persist.entity.MonitorEntity;
import es.caib.comanda.monitor.persist.repository.MonitorRepository;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import es.caib.comanda.ms.logic.intf.jms.NetejaEntornAppMessage;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
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
@Tag(name = "22. Monitor", description = "Servei de consulta del monitor")
public class MonitorController extends BaseMutableResourceController<Monitor, Long> {

    @Autowired
    private MonitorSchedulerService schedulerService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private MonitorRepository monitorRepository;

    @PostMapping("/programarBorrat")
    @PreAuthorize("hasRole(T(es.caib.comanda.base.config.BaseConfig).ROLE_ADMIN)")
    public ResponseEntity<Void> programarBorrat() {
        log.info("Rebuda petició d'actualització de procés de borrat");
        schedulerService.programarBorrat();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reintentarNeteja")
    @PreAuthorize("hasRole(T(es.caib.comanda.base.config.BaseConfig).ROLE_ADMIN)")
    public ResponseEntity<Void> reintentarNeteja(@PathVariable Long id) {
        MonitorEntity monitorEntity = monitorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Monitor no trobat: " + id));
        if (!"netejaEntornApp".equals(monitorEntity.getOperacio())) {
            return ResponseEntity.badRequest().build();
        }
        String cua = getCuaPerModul(monitorEntity.getModul());
        if (cua == null) {
            log.warn("No hi ha cua per al mòdul {}", monitorEntity.getModul());
            return ResponseEntity.badRequest().build();
        }
        jmsTemplate.convertAndSend(cua, new NetejaEntornAppMessage(monitorEntity.getEntornAppId()));
        log.info("Reintent de neteja encuat per entornApp {} mòdul {}", monitorEntity.getEntornAppId(), monitorEntity.getModul());
        return ResponseEntity.ok().build();
    }

    private String getCuaPerModul(ModulEnum modul) {
        if (modul == null) return null;
        switch (modul) {
            case SALUT: return Cues.CUA_NETEJA_SALUT;
            case TASCA: return Cues.CUA_NETEJA_TASQUES;
            case AVIS: return Cues.CUA_NETEJA_AVISOS;
            case ALARMES: return Cues.CUA_NETEJA_ALARMES;
            case ESTADISTICA: return Cues.CUA_NETEJA_ESTADISTICA;
            default: return null;
        }
    }

}
