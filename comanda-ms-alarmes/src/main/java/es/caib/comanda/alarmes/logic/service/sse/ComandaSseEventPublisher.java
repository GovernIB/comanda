package es.caib.comanda.alarmes.logic.service.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ComandaSseEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(String type) {
        publish(type, null);
    }

    public void publish(String type, Serializable payload) {
        applicationEventPublisher.publishEvent(new ComandaSsePublishRequest(
                new ComandaSseEvent(type, payload, LocalDateTime.now())));
    }

}
