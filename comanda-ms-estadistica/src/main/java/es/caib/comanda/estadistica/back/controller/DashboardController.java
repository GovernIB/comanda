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
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private final Map<Long, Queue<TimedEvent<DashboardEvent>>> pendingLoadedEvents = new HashMap<>();


    private enum DashboardEventType {
        DASHBOARD_CONNECT, ITEM_CARREGAT, ITEM_ERROR;
        public String getEventName() { return name().toLowerCase(); }
        public static DashboardEventType fromEventName(String name) { return DashboardEventType.valueOf(name.toUpperCase()); }
    }

    @GetMapping("/subscribe/{dashboardId}")
    public SseEmitter streamDashboard(@PathVariable Long dashboardId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // Add completion callback to handle emitter completion
        emitter.onCompletion(() -> {
            log.debug("Emitter completed for dashboard {}, emitter {}", dashboardId, emitter.hashCode());
            List<SseEmitter> emitters = clientsDashboard.get(dashboardId);
            if (emitters != null) {
                emitters.remove(emitter);
                if (emitters.isEmpty()) {
                    clientsDashboard.remove(dashboardId);
                    log.debug("Removed dashboard {} from clients as it has no active emitters", dashboardId);
                }
            }
        });

        // Add timeout callback to handle emitter timeout
        emitter.onTimeout(() -> {
            log.debug("Emitter timeout for dashboard {}, emitter {}", dashboardId, emitter.hashCode());
            List<SseEmitter> emitters = clientsDashboard.get(dashboardId);
            if (emitters != null) {
                emitters.remove(emitter);
                if (emitters.isEmpty()) {
                    clientsDashboard.remove(dashboardId);
                    log.debug("Removed dashboard {} from clients as it has no active emitters", dashboardId);
                }
            }
        });

        // Add the emitter to the clients map
        clientsDashboard.computeIfAbsent(dashboardId, key -> new ArrayList<>()).add(emitter);

        log.debug("Dashboard {} subscrit a events amb emissor {}", dashboardId, emitter.hashCode());

        // Process any pending events for this dashboard
        locks.putIfAbsent(dashboardId, new Object());
        Object lock = locks.get(dashboardId);

        synchronized (lock) {
            onSubscribeEmisorExpedient(dashboardId, emitter);
            removeOldElements(pendingLoadedEvents);
        }

        return emitter;
    }

    private void onSubscribeEmisorExpedient(Long dashboardId, SseEmitter emitter) {
        // Al moment de subscriure enviem un missatge de connexió
        try {
            emitter.send(SseEmitter.event()
                    .name(DashboardEventType.DASHBOARD_CONNECT.getEventName())
                    .data("Connexió establerta a " + LocalDateTime.now())
                    .id(String.valueOf(System.currentTimeMillis())));

            // Si hi ha events pendents, s'envien
            if (pendingLoadedEvents.containsKey(dashboardId)) {
                Queue<TimedEvent<DashboardEvent>> cua = pendingLoadedEvents.get(dashboardId);
                log.debug("Found {} pending events for dashboard {}", cua.size(), dashboardId);

                // Create a list to store events that we'll process
                List<TimedEvent<DashboardEvent>> eventsToProcess = new ArrayList<>();

                // Drain the queue into our list
                TimedEvent<DashboardEvent> event;
                while ((event = cua.poll()) != null) {
                    eventsToProcess.add(event);
                }

                // Process all events
                for (TimedEvent<DashboardEvent> timedEvent : eventsToProcess) {
                    boolean isErrorEvent = !(timedEvent.getValue() instanceof DashboardLoadedEvent);
                    log.debug("Sending pending {} event for dashboard {}, item {}", 
                            isErrorEvent ? "error" : "loaded", 
                            dashboardId, 
                            timedEvent.getValue() instanceof DashboardLoadedEvent ? 
                                ((DashboardLoadedEvent)timedEvent.getValue()).getDashboardItemId() : 
                                ((DashboardLoadindErrorEvent)timedEvent.getValue()).getDashboardItemId());

                    sendDashboardEventToEmisor(
                            emitter,
                            timedEvent.getValue(),
                            dashboardId,
                            new ArrayList<>(),
                            isErrorEvent);
                }

                log.debug("Processed {} pending events for dashboard {}", eventsToProcess.size(), dashboardId);
            } else {
                log.debug("No pending events found for dashboard {}", dashboardId);
            }
        } catch (IOException e) {
            log.error("Error enviant esdeveniment inicial SSE", e);
            handleEmitterError(dashboardId, emitter, e);
        } catch (Exception e) {
            log.error("Error inesperat onSubscribe", e);
            handleEmitterError(dashboardId, emitter, e);
        }
    }

    private void handleEmitterError(Long dashboardId, SseEmitter emitter, Exception e) {
        emitter.completeWithError(e);
        clientsDashboard.getOrDefault(dashboardId, new ArrayList<>()).remove(emitter);
    }


    @Async("asyncTaskExecutor")
    @EventListener
    public void handleEventLoad(DashboardLoadedEvent dashboardLoadedEvent) {
        log.debug("Càrrega de dades del dashboard...");
        if (dashboardLoadedEvent == null || dashboardLoadedEvent.getDashboardId() == null) {
            return;
        }

        Long dashboardId = dashboardLoadedEvent.getDashboardId();
        log.debug("..." + dashboardId);

        locks.putIfAbsent(dashboardId, new Object());
        Object lock = locks.get(dashboardId);

        synchronized (lock) {
            log.debug("Processant...");
            // Check if there are any emitters for this dashboard
            List<SseEmitter> dashboardEmitters = clientsDashboard.get(dashboardId);

            if (dashboardEmitters == null || dashboardEmitters.isEmpty()) {
                // No emitters found for this dashboard, add to pending events
                log.debug("No emitters found for dashboard {}, adding to pending events", dashboardId);
                pendingLoadedEvents.computeIfAbsent(dashboardId, key -> new ConcurrentLinkedQueue<>()).add(new TimedEvent<>(dashboardLoadedEvent));
            } else {
                // Process emitters for this dashboard
                List<SseEmitter> inactiveEmitters = processEmitters(dashboardEmitters, dashboardLoadedEvent, dashboardId);

                // Remove inactive emitters and clean up the dashboard if necessary
                cleanupInactiveDashboards(dashboardId, dashboardEmitters, inactiveEmitters);

                // If all emitters were inactive and removed, add to pending events
                if (dashboardEmitters.isEmpty()) {
                    log.debug("All emitters were inactive for dashboard {}, adding to pending events", dashboardId);
                    pendingLoadedEvents.computeIfAbsent(dashboardId, key -> new ConcurrentLinkedQueue<>()).add(new TimedEvent<>(dashboardLoadedEvent));
                }
            }
        }
    }

    @Async("asyncTaskExecutor")
    @EventListener
    public void handleEventLoadingError(DashboardLoadindErrorEvent dashboardLoadindErrorEvent) {
        log.debug("Error en la càrrega de dades del dashboard...");
        if (dashboardLoadindErrorEvent == null || dashboardLoadindErrorEvent.getDashboardId() == null) {
            return;
        }

        Long dashboardId = dashboardLoadindErrorEvent.getDashboardId();
        log.debug("..." + dashboardId);

        locks.putIfAbsent(dashboardId, new Object());
        Object lock = locks.get(dashboardId);

        synchronized (lock) {
            log.debug("Processant...");
            // Check if there are any emitters for this dashboard
            List<SseEmitter> dashboardEmitters = clientsDashboard.get(dashboardId);

            if (dashboardEmitters == null || dashboardEmitters.isEmpty()) {
                // No emitters found for this dashboard, add to pending events
                log.debug("No emitters found for dashboard {}, adding to pending events", dashboardId);
                pendingLoadedEvents.computeIfAbsent(dashboardId, key -> new ConcurrentLinkedQueue<>()).add(new TimedEvent<>(dashboardLoadindErrorEvent));
            } else {
                // Process emitters for this dashboard
                List<SseEmitter> inactiveEmitters = processEmitters(dashboardEmitters, dashboardLoadindErrorEvent, dashboardId, true);

                // Remove inactive emitters and clean up the dashboard if necessary
                cleanupInactiveDashboards(dashboardId, dashboardEmitters, inactiveEmitters);

                // If all emitters were inactive and removed, add to pending events
                if (dashboardEmitters.isEmpty()) {
                    log.debug("All emitters were inactive for dashboard {}, adding to pending events", dashboardId);
                    pendingLoadedEvents.computeIfAbsent(dashboardId, key -> new ConcurrentLinkedQueue<>()).add(new TimedEvent<>(dashboardLoadindErrorEvent));
                }
            }
        }
    }

    private List<SseEmitter> processEmitters(List<SseEmitter> dashboardEmitters, DashboardEvent event, Long dashboardKey) {
        return processEmitters(dashboardEmitters, event, dashboardKey, false);
    }

    private List<SseEmitter> processEmitters(List<SseEmitter> dashboardEmitters, DashboardEvent event, Long dashboardKey, boolean errorEvent) {
        List<SseEmitter> inactiveEmitters = new ArrayList<>();
        if (dashboardEmitters != null && !dashboardEmitters.isEmpty()) {
            for (SseEmitter emitter : dashboardEmitters) {
                sendDashboardEventToEmisor(emitter, event, dashboardKey, inactiveEmitters, errorEvent);
            }

            // Log the result of the processing
            int activeEmitters = dashboardEmitters.size() - inactiveEmitters.size();
            log.debug("Processed event for dashboard {}: {} active emitters, {} inactive emitters", 
                    dashboardKey, activeEmitters, inactiveEmitters.size());
        } else {
            log.debug("No emitters to process for dashboard {}", dashboardKey);
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
            emitter.completeWithError(e);
        }
    }

    private void cleanupInactiveDashboards(Long dashboardKey, List<SseEmitter> dashboardEmitters, List<SseEmitter> inactiveEmitters) {
        dashboardEmitters.removeAll(inactiveEmitters);
        if (dashboardEmitters.isEmpty()) {
            clientsDashboard.remove(dashboardKey);
            log.debug("... eliminat dashboard " + dashboardKey + " de la llista de events per no tenir cap emisor actiu.");
        }
    }


    static class TimedEvent<T> {
        private final T value;
        private final Instant timestamp; // La marca de temps

        public TimedEvent(T value) {
            this.value = value;
            this.timestamp = Instant.now(); // Hora actual quan es crea
        }

        public T getValue() {
            return value;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    // Mètode per eliminar elements més antics d'1 minut
    private static void removeOldElements(Map<Long, Queue<TimedEvent<DashboardEvent>>> queueMap) {
        if (queueMap == null || queueMap.isEmpty()) {
            return;
        }
        Instant cutoffTime = Instant.now().minusSeconds(60); // 1 minut enrere
        queueMap.entrySet().stream().forEach(queueEntry ->
            queueEntry.getValue().removeIf(element -> element.getTimestamp().isBefore(cutoffTime))
        );
    }



}
