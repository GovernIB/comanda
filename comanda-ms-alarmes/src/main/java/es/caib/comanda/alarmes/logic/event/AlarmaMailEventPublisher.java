package es.caib.comanda.alarmes.logic.event;

import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmaMailEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ParametresHelper parametresHelper;

    public void publish(AlarmaEntity alarma, AlarmaMailEventType tipusEvent) {
        if (isLogActivacio()) {
            log.info("[EML] Publicant event de correu alarmaId={} configId={} tipusEvent={}",
                    alarma.getId(), alarma.getAlarmaConfig().getId(), tipusEvent);
        }
        applicationEventPublisher.publishEvent(new AlarmaMailPublishRequest(alarma, tipusEvent));
    }

    private boolean isLogActivacio() {
        return Boolean.TRUE.equals(parametresHelper.getParametreBoolean(BaseConfig.PROP_ALARMA_LOG_ACTIVACIO, false));
    }
}
