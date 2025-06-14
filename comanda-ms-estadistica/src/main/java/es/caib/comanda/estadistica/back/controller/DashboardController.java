package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardEvent;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardLoadedEvent;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardLoadindErrorEvent;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servei de consulta d'informació de widgets gràfics.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RestController("dashboardController")
@RequestMapping(BaseConfig.API_PATH + "/dashboards")
@Tag(name = "Dimensio", description = "Servei de consulta de dashboards")
public class DashboardController extends BaseMutableResourceController<Dashboard, Long> {

    private static final long DEFAULT_TIMEOUT = 0L; // Temps de caducitat per a SSE

    private final Map<Long, List<SseEmitter>> clientsDashboard = new HashMap<>();
    private final Map<Long, Object> locks = new ConcurrentHashMap<>();


    private enum DashboardEventType {
        DASHBOARD_CONNECT, ITEM_CARREGAT, ITEM_ERROR;
        public String getEventName() { return name().toLowerCase(); }
        public static DashboardEventType fromEventName(String name) { return DashboardEventType.valueOf(name.toUpperCase()); }
    }

    @GetMapping("/subscribe/{dashboardId}")
    public SseEmitter streamDashboard(@PathVariable Long dashboardId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        clientsDashboard.computeIfAbsent(dashboardId, key -> new ArrayList<>()).add(emitter);

        log.debug("Dashboard " + dashboardId + " subscrit a events amb emissor " + emitter.hashCode());
        onSubscribeEmisorExpedient(dashboardId, emitter);
        return emitter;
    }

    private void onSubscribeEmisorExpedient(Long dashboardId, SseEmitter emitter) {
        // Al moment de subscriure enviem un missatge de connexió
        try {
            emitter.send(SseEmitter.event()
                    .name(DashboardEventType.DASHBOARD_CONNECT.getEventName())
                    .data("Connexió establerta a " + LocalDateTime.now())
                    .id(String.valueOf(System.currentTimeMillis())));
        } catch (IOException e) {
            log.error("Error enviant esdeveniment inicial SSE", e);
            handleEmitterError(dashboardId, emitter, e);
        } catch (Exception e) {
            log.error("Error inesperat onSubscribe", e);
            handleEmitterError(dashboardId, emitter, e);
        }
    }

    private void handleEmitterError(Long dashboardId, SseEmitter emitter, Exception e) {
        emitter.complete();
        clientsDashboard.getOrDefault(dashboardId, new ArrayList<>()).remove(emitter);
    }


//    @Async("asyncTaskExecutor")
    @EventListener
    public void handleEventLoad(DashboardLoadedEvent dashboardLoadedEvent) {
        if (dashboardLoadedEvent == null || dashboardLoadedEvent.getDashboardId() == null) {
            return;
        }

        log.debug("Càrrega de dades del dashboard...");
        Long dashboardId = dashboardLoadedEvent.getDashboardId();

        locks.putIfAbsent(dashboardId, new Object());
        Object lock = locks.get(dashboardId);

        synchronized (lock) {
            for (Map.Entry<Long, List<SseEmitter>> entry : clientsDashboard.entrySet()) {
                if (!dashboardId.equals(entry.getKey())) {
                    continue;
                }

                List<SseEmitter> dashboardEmitters = entry.getValue();
                List<SseEmitter> inactiveEmitters = processEmitters(dashboardEmitters, dashboardLoadedEvent, entry.getKey());

                // Remove inactive emitters and clean up the dashboard if necessary
                cleanupInactiveDashboards(entry.getKey(), dashboardEmitters, inactiveEmitters);
            }
        }
    }

//    @Async("asyncTaskExecutor")
    @EventListener
    public void handleEventLoadingError(DashboardLoadindErrorEvent dashboardLoadindErrorEvent) {
        if (dashboardLoadindErrorEvent == null || dashboardLoadindErrorEvent.getDashboardId() == null) {
            return;
        }

        log.debug("Error en la càrrega de dades del dashboard...");
        Long dashboardId = dashboardLoadindErrorEvent.getDashboardId();

        locks.putIfAbsent(dashboardId, new Object());
        Object lock = locks.get(dashboardId);

        synchronized (lock) {
            for (Map.Entry<Long, List<SseEmitter>> entry : clientsDashboard.entrySet()) {
                if (!dashboardId.equals(entry.getKey())) {
                    continue;
                }

                List<SseEmitter> dashboardEmitters = entry.getValue();
                List<SseEmitter> inactiveEmitters = processEmitters(dashboardEmitters, dashboardLoadindErrorEvent, entry.getKey(), true);

                // Remove inactive emitters and clean up the dashboard if necessary
                cleanupInactiveDashboards(entry.getKey(), dashboardEmitters, inactiveEmitters);
            }
        }
    }

    private List<SseEmitter> processEmitters(List<SseEmitter> dashboardEmitters, DashboardEvent event, Long dashboardKey) {
        return processEmitters(dashboardEmitters, event, dashboardKey, false);
    }

    private List<SseEmitter> processEmitters(List<SseEmitter> dashboardEmitters, DashboardEvent event, Long dashboardKey, boolean errorEvent) {
        List<SseEmitter> inactiveEmitters = new ArrayList<>();
        if (dashboardEmitters != null) {
            for (SseEmitter emitter : dashboardEmitters) {
                sendDashboardEventToEmisor(emitter, event, dashboardKey, inactiveEmitters, errorEvent);
            }
        }
        return inactiveEmitters;
    }

    private void sendDashboardEventToEmisor(SseEmitter emitter, DashboardEvent event, Long dashboardKey, List<SseEmitter> inactiveEmitters, boolean errorEvent) {
        try {
            emitter.send(SseEmitter.event()
                    .name(errorEvent
                            ? DashboardEventType.ITEM_ERROR.getEventName()
                            : DashboardEventType.ITEM_CARREGAT.getEventName())
                    .data(event));
            log.debug("... comunicat " + (errorEvent ? "DashboardLoadindErrorEvent" : "DashboardLoadedEvent") + " al dashboard " + dashboardKey + " a travers del emissor " + emitter.hashCode() + ".");
        } catch (Exception e) {
            inactiveEmitters.add(emitter);
            log.debug("... eliminat emisor de Dashboard " + emitter.hashCode() + " per error " + e.getMessage() + ".");
        }
    }

    private void cleanupInactiveDashboards(Long dashboardKey, List<SseEmitter> dashboardEmitters, List<SseEmitter> inactiveEmitters) {
        dashboardEmitters.removeAll(inactiveEmitters);
        if (dashboardEmitters.isEmpty()) {
            clientsDashboard.remove(dashboardKey);
            log.debug("... eliminat dashboard " + dashboardKey + " de la llista de events per no tenir cap emisor actiu.");
        }
    }



}
