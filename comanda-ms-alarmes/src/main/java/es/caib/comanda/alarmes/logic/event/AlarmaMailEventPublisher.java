package es.caib.comanda.alarmes.logic.event;

import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlarmaMailEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(AlarmaEntity alarma) {
        applicationEventPublisher.publishEvent(new AlarmaMailPublishRequest(alarma));
    }
}
