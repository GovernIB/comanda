package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de widgets gràfics.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RestController("dashboardController")
@RequestMapping(BaseConfig.API_PATH + "/dashboards")
@Tag(name = "11. Dashboard", description = "Servei de consulta de dashboards")
public class DashboardController extends BaseMutableResourceController<Dashboard, Long> {

//    private static final long DEFAULT_TIMEOUT = 0L; // Temps de caducitat per a SSE
//
//    private final Map<Long, List<SseEmitter>> clientsDashboard = new HashMap<>();
//    private final Map<Long, Object> locks = new ConcurrentHashMap<>();
//    private final Map<Long, Queue<TimedEvent<DashboardEvent>>> pendingLoadedEvents = new HashMap<>();
//    private final DashboardService dashboardService;
//
//    public DashboardController(DashboardService dashboardService) {
//        super();
//        this.dashboardService = dashboardService;
//    }
//
//
//    private enum DashboardEventType {
//        DASHBOARD_CONNECT, ITEM_CARREGAT, ITEM_ERROR;
//        public String getEventName() { return name().toLowerCase(); }
//        public static DashboardEventType fromEventName(String name) { return DashboardEventType.valueOf(name.toUpperCase()); }
//    }
//
//    @GetMapping("/subscribe/{dashboardId}")
//    public SseEmitter streamDashboard(@PathVariable Long dashboardId) {
//        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
//
//        // Afegeix un callback de finalització per gestionar el completion de l'emissor.
//        emitter.onCompletion(() -> {
//            log.debug("Emissor finalitzat pel dashboard {}, emissor {}", dashboardId, emitter.hashCode());
//            List<SseEmitter> emitters = clientsDashboard.get(dashboardId);
//            if (emitters != null) {
//                emitters.remove(emitter);
//                if (emitters.isEmpty()) {
//                    clientsDashboard.remove(dashboardId);
//                    log.debug("Eliminat dashboard {} de llistat de clients degut a que no tenia enissors actius", dashboardId);
//                }
//            }
//        });
//
//        // Afegiex un callback de timeout per gestionar el timeout de l'emissos
//        emitter.onTimeout(() -> {
//            log.debug("Timeout d'emissor pel dashboard {}, emissor {}", dashboardId, emitter.hashCode());
//            List<SseEmitter> emitters = clientsDashboard.get(dashboardId);
//            if (emitters != null) {
//                emitters.remove(emitter);
//                if (emitters.isEmpty()) {
//                    clientsDashboard.remove(dashboardId);
//                    log.debug("Dashboard eliminat {} del llistat de clients degut a que no tenia emissors actius", dashboardId);
//                }
//            }
//        });
//
//        // Afegeix un emissor al mapa de clients
//        clientsDashboard.computeIfAbsent(dashboardId, key -> new ArrayList<>()).add(emitter);
//
//        log.info("Dashboard {} subscrit a events amb emissor {}", dashboardId, emitter.hashCode());
//
//        // Processa qualsevol event pendent per aquest dashboard
//        locks.putIfAbsent(dashboardId, new Object());
//        Object lock = locks.get(dashboardId);
//
//        synchronized (lock) {
//            onSubscribeEmisorExpedient(dashboardId, emitter);
//            // Eliminam events pendents antics (eñs que fa més d'un minuts que estan en cua)
//            removeOldElements(pendingLoadedEvents);
//        }
//
//        return emitter;
//    }
//
//    private void onSubscribeEmisorExpedient(Long dashboardId, SseEmitter emitter) {
//        // Al moment de subscriure enviem un missatge de connexió
//        try {
//            emitter.send(SseEmitter.event()
//                    .name(DashboardEventType.DASHBOARD_CONNECT.getEventName())
//                    .data("Connexió establerta a " + LocalDateTime.now())
//                    .id(String.valueOf(System.currentTimeMillis())));
//
//            dashboardService.generateAsyncDataForAllItems(dashboardId);
//
//            // Si hi ha events pendents, s'envien
//            if (pendingLoadedEvents.containsKey(dashboardId)) {
//                Queue<TimedEvent<DashboardEvent>> cua = pendingLoadedEvents.get(dashboardId);
//                log.debug("Found {} pending events for dashboard {}", cua.size(), dashboardId);
//
//                // Crea una llista per emmagatzemar events que processarem
//                List<TimedEvent<DashboardEvent>> eventsToProcess = new ArrayList<>();
//
//                // Drain the queue into our list
//                TimedEvent<DashboardEvent> event;
//                while ((event = cua.poll()) != null) {
//                    eventsToProcess.add(event);
//                }
//
//                // Processa tots els events
//                for (TimedEvent<DashboardEvent> timedEvent : eventsToProcess) {
//                    boolean isErrorEvent = !(timedEvent.getValue() instanceof DashboardLoadedEvent);
//                    log.debug("Sending pending {} event for dashboard {}, item {}",
//                            isErrorEvent ? "error" : "loaded",
//                            dashboardId,
//                            timedEvent.getValue() instanceof DashboardLoadedEvent ?
//                                ((DashboardLoadedEvent)timedEvent.getValue()).getDashboardItemId() :
//                                ((DashboardLoadindErrorEvent)timedEvent.getValue()).getDashboardItemId());
//
//                    sendDashboardEventToEmisor(
//                            emitter,
//                            timedEvent.getValue(),
//                            dashboardId,
//                            new ArrayList<>(),
//                            isErrorEvent);
//                }
//
//                log.debug("Processats {} events pendents pel dashboard {}", eventsToProcess.size(), dashboardId);
//            } else {
//                log.debug("No hi ha events pendents pel dashboard {}", dashboardId);
//            }
//        } catch (IOException e) {
//            log.error("Error enviant esdeveniment inicial SSE", e);
//            handleEmitterError(dashboardId, emitter, e);
//        } catch (Exception e) {
//            log.error("Error inesperat onSubscribe", e);
//            handleEmitterError(dashboardId, emitter, e);
//        }
//    }
//
//    private void handleEmitterError(Long dashboardId, SseEmitter emitter, Exception e) {
//        emitter.completeWithError(e);
//        clientsDashboard.getOrDefault(dashboardId, new ArrayList<>()).remove(emitter);
//    }
//
//
//    @EventListener
//    public void handleEventLoad(DashboardLoadedEvent dashboardLoadedEvent) {
//        log.debug("Càrrega de dades del dashboard...");
//        if (dashboardLoadedEvent == null || dashboardLoadedEvent.getDashboardId() == null) {
//            return;
//        }
//
//        Long dashboardId = dashboardLoadedEvent.getDashboardId();
//        log.debug("..." + dashboardId);
//
//        locks.putIfAbsent(dashboardId, new Object());
//        Object lock = locks.get(dashboardId);
//
//        synchronized (lock) {
//            log.debug("Processant...");
//            // Comprova si hi ha emissors per aquest dashboard
//            List<SseEmitter> dashboardEmitters = clientsDashboard.get(dashboardId);
//
//            if (dashboardEmitters == null || dashboardEmitters.isEmpty()) {
//                // No s'han trobat emissors per aquest dashboard. Ho afegim a events pendents
//                log.debug("No s'han trobat emissors pel dashboard {}, afegint event a pendents", dashboardId);
//                pendingLoadedEvents.computeIfAbsent(dashboardId, key -> new ConcurrentLinkedQueue<>()).add(new TimedEvent<>(dashboardLoadedEvent));
//            } else {
//                // Processa emissors per aquest dashboard
//                List<SseEmitter> inactiveEmitters = processEmitters(dashboardEmitters, dashboardLoadedEvent, dashboardId);
//
//                // Elimina emissors inactius i and neteja el dashboard si és necessari
//                cleanupInactiveDashboards(dashboardId, dashboardEmitters, inactiveEmitters);
//
//                // Si tots els emissors estaven inactius i s'han eliminat, ho afegim als events pendents
//                if (dashboardEmitters.isEmpty()) {
//                    log.debug("Tots els emissots estaven inactius per al {}, afegint event a pendents", dashboardId);
//                    pendingLoadedEvents.computeIfAbsent(dashboardId, key -> new ConcurrentLinkedQueue<>()).add(new TimedEvent<>(dashboardLoadedEvent));
//                }
//            }
//        }
//    }
//
//    @EventListener
//    public void handleEventLoadingError(DashboardLoadindErrorEvent dashboardLoadindErrorEvent) {
//        log.debug("Error en la càrrega de dades del dashboard...");
//        if (dashboardLoadindErrorEvent == null || dashboardLoadindErrorEvent.getDashboardId() == null) {
//            return;
//        }
//
//        Long dashboardId = dashboardLoadindErrorEvent.getDashboardId();
//        log.debug("..." + dashboardId);
//
//        locks.putIfAbsent(dashboardId, new Object());
//        Object lock = locks.get(dashboardId);
//
//        synchronized (lock) {
//            log.debug("Processant...");
//            // Comprova si hi ha emissors per aquest dashboard
//            List<SseEmitter> dashboardEmitters = clientsDashboard.get(dashboardId);
//
//            if (dashboardEmitters == null || dashboardEmitters.isEmpty()) {
//                // No s'han trobat emissors per aquest dashboard. Ho afegim a events pendents
//                log.debug("No s'han trobat emissors pel dashboard {}, afegint event a pendents", dashboardId);
//                pendingLoadedEvents.computeIfAbsent(dashboardId, key -> new ConcurrentLinkedQueue<>()).add(new TimedEvent<>(dashboardLoadindErrorEvent));
//            } else {
//                // Processa emissors per aquest dashboard
//                List<SseEmitter> inactiveEmitters = processEmitters(dashboardEmitters, dashboardLoadindErrorEvent, dashboardId, true);
//
//                // Elimina emissors inactius i and neteja el dashboard si és necessari
//                cleanupInactiveDashboards(dashboardId, dashboardEmitters, inactiveEmitters);
//
//                // Si tots els emissors estaven inactius i s'han eliminat, ho afegim als events pendents
//                if (dashboardEmitters.isEmpty()) {
//                    log.debug("Tots els emissots estaven inactius per al {}, afegint event a pendents", dashboardId);
//                    pendingLoadedEvents.computeIfAbsent(dashboardId, key -> new ConcurrentLinkedQueue<>()).add(new TimedEvent<>(dashboardLoadindErrorEvent));
//                }
//            }
//        }
//    }
//
//    private List<SseEmitter> processEmitters(List<SseEmitter> dashboardEmitters, DashboardEvent event, Long dashboardKey) {
//        return processEmitters(dashboardEmitters, event, dashboardKey, false);
//    }
//
//    private List<SseEmitter> processEmitters(List<SseEmitter> dashboardEmitters, DashboardEvent event, Long dashboardKey, boolean errorEvent) {
//        List<SseEmitter> inactiveEmitters = new ArrayList<>();
//        if (dashboardEmitters != null && !dashboardEmitters.isEmpty()) {
//            for (SseEmitter emitter : dashboardEmitters) {
//                sendDashboardEventToEmisor(emitter, event, dashboardKey, inactiveEmitters, errorEvent);
//            }
//
//            // Envia als logs el resultat del processament
//            int activeEmitters = dashboardEmitters.size() - inactiveEmitters.size();
//            log.info("Event enviat al dashboard {}: {} emissors actius, {} emissors inactius", dashboardKey, activeEmitters, inactiveEmitters.size());
//        } else {
//            log.debug("No hi ha emissors per enviar l'event al dashboard {}", dashboardKey);
//        }
//        return inactiveEmitters;
//    }
//
//    private void sendDashboardEventToEmisor(SseEmitter emitter, DashboardEvent event, Long dashboardKey, List<SseEmitter> inactiveEmitters, boolean errorEvent) {
//        try {
//            emitter.send(SseEmitter.event()
//                    .name(errorEvent
//                            ? DashboardEventType.ITEM_ERROR.getEventName()
//                            : DashboardEventType.ITEM_CARREGAT.getEventName())
//                    .data(event));
//            log.info("... comunicat " + (errorEvent ? "DashboardLoadindErrorEvent" : "DashboardLoadedEvent") + " al dashboard " + dashboardKey + " a traves de l'emissor " + emitter.hashCode() + ".");
//        } catch (Exception e) {
//            inactiveEmitters.add(emitter);
//            log.debug("... eliminat emisor de Dashboard " + emitter.hashCode() + " per error " + e.getMessage() + ".");
//            emitter.completeWithError(e);
//        }
//    }
//
//    private void cleanupInactiveDashboards(Long dashboardKey, List<SseEmitter> dashboardEmitters, List<SseEmitter> inactiveEmitters) {
//        dashboardEmitters.removeAll(inactiveEmitters);
//        if (dashboardEmitters.isEmpty()) {
//            clientsDashboard.remove(dashboardKey);
//            log.debug("... eliminat dashboard " + dashboardKey + " de la llista de events per no tenir cap emisor actiu.");
//        }
//    }
//
//
//    static class TimedEvent<T> {
//        private final T value;
//        private final Instant timestamp; // La marca de temps
//
//        public TimedEvent(T value) {
//            this.value = value;
//            this.timestamp = Instant.now(); // Hora actual quan es crea
//        }
//
//        public T getValue() {
//            return value;
//        }
//
//        public Instant getTimestamp() {
//            return timestamp;
//        }
//    }
//
//    // Mètode per eliminar elements més antics d'1 minut
//    private static void removeOldElements(Map<Long, Queue<TimedEvent<DashboardEvent>>> queueMap) {
//        if (queueMap == null || queueMap.isEmpty()) {
//            return;
//        }
//        Instant cutoffTime = Instant.now().minusSeconds(60); // 1 minut enrere
//        queueMap.entrySet().stream().forEach(queueEntry ->
//            queueEntry.getValue().removeIf(element -> element.getTimestamp().isBefore(cutoffTime))
//        );
//    }



}
