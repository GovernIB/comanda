package es.caib.comanda.alarmes.logic.service.sse;

import es.caib.comanda.alarmes.back.sse.ComandaSseService;
import es.caib.comanda.alarmes.logic.intf.model.Alarma.AlarmaReduidaResource;
import es.caib.comanda.alarmes.logic.intf.service.AlarmaService;
import es.caib.comanda.ms.sse.ComandaSseEvent;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.ms.sse.ComandaSseSubscriberRegistry;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComandaSseServiceImpl implements ComandaSseService, ComandaSseSubscriberRegistry {

    private static final long SSE_TIMEOUT_MS = 0L;
    // El proxy revers (Apache mod_proxy) davant del backend talla les connexions proxied inactives als 30 s
    // (Timeout per defecte, no configurable en aquest desplegament). Sense tràfic, el client detecta el tall
    // i reconnecta contínuament (vegeu SseProvider.tsx al frontend), provocant refrescs visibles sense cap
    // interacció de l'usuari. Enviant un comentari SSE cada 25 s mantenim la connexió activa per sota d'aquest
    // llindar.
    private static final long HEARTBEAT_INTERVAL_MS = 25_000L;

    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();
    private final AuthenticationHelper authenticationHelper;
    private final AlarmaService alarmaService;

    public SseEmitter subscribe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        Subscription subscription = new Subscription(
                emitter,
                authentication.getName(),
                authenticationHelper.isCurrentUserInRole(authentication, BaseConfig.ROLE_ADMIN));
        subscriptions.add(subscription);
        emitter.onCompletion(() -> subscriptions.remove(subscription));
        emitter.onTimeout(() -> {
            subscriptions.remove(subscription);
            emitter.complete();
        });
        emitter.onError(error -> subscriptions.remove(subscription));
        sendToSubscription(subscription, new ComandaSseEvent(
                ComandaSseEventTypes.CONNECTION_READY,
                null,
                LocalDateTime.now()));
        return emitter;
    }

    @Override
    public boolean hasActiveSubscribers() {
        return !subscriptions.isEmpty();
    }

    public void publish(ComandaSseEvent event) {
        List<Subscription> inactiveSubscriptions = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
            if (!sendToSubscription(subscription, event)) {
                inactiveSubscriptions.add(subscription);
            }
        }
        subscriptions.removeAll(inactiveSubscriptions);
    }

    /**
     * Envia un comentari SSE (sense nom d'event ni dades) a totes les subscripcions actives, per mantenir viva
     * la connexió a través del proxy revers. El client (SseProvider.tsx) ignora els comentaris SSE.
     */
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
    public void sendHeartbeat() {
        if (subscriptions.isEmpty()) {
            return;
        }
        List<Subscription> inactiveSubscriptions = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
            try {
                subscription.emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (IOException ex) {
                log.debug("No s'ha pogut enviar l'heartbeat SSE", ex);
                subscription.emitter.completeWithError(ex);
                inactiveSubscriptions.add(subscription);
            }
        }
        subscriptions.removeAll(inactiveSubscriptions);
    }

    private boolean sendToSubscription(Subscription subscription, ComandaSseEvent event) {
        try {
            subscription.emitter.send(SseEmitter.event()
                    .id(UUID.randomUUID().toString())
                    .name(ComandaSseEvent.SSE_EVENT_NAME)
                    .data(resolveEventPayload(subscription, event)));
            return true;
        } catch (IOException ex) {
            log.debug("No s'ha pogut enviar l'event SSE {}", event.getType(), ex);
            subscription.emitter.completeWithError(ex);
            return false;
        }
    }

    private ComandaSseEvent resolveEventPayload(Subscription subscription, ComandaSseEvent event) {
        if (!ComandaSseEventTypes.ACTIVE_ALARMS_CHANGED.equals(event.getType())) {
            return event;
        }
        List<AlarmaReduidaResource> activeAlarms = alarmaService.findActiveAlarmIdsForSubscriber(
                subscription.userName,
                subscription.admin);
        return new ComandaSseEvent(event.getType(), (Serializable) activeAlarms, event.getTimestamp());
    }

    @AllArgsConstructor
    private static class Subscription {
        private final SseEmitter emitter;
        private final String userName;
        private final boolean admin;
    }

}
