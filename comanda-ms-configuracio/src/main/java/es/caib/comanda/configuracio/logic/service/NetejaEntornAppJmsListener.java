package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.ms.logic.intf.event.EntornAppEsborratEvent;
import es.caib.comanda.ms.logic.intf.jms.NetejaEntornAppMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.jms.JMSException;
import javax.jms.Message;

import static es.caib.comanda.base.config.Cues.*;

/**
 * Listener JMS per a la neteja de dades associades a una aplicació-entorn esborrada.
 * Publica un missatge a la cua JMS després del commit de la transacció i el processa
 * enviant missatges JMS individuals a cada microservei.
 *
 * @author Límit Tecnologies
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NetejaEntornAppJmsListener {

    private final ApplicationEventPublisher eventPublisher;
    private final JmsTemplate jmsTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onEntornAppEsborrat(EntornAppEsborratEvent event) {
        log.debug("Enviant missatge de neteja per entornApp {} a la cua {}", event.getEntornAppId(), CUA_NETEJA_ENTORN_APP);
        jmsTemplate.convertAndSend(CUA_NETEJA_ENTORN_APP, new NetejaEntornAppMessage(
                event.getEntornAppId(),
                event.getAppId(),
                event.getEntornId(),
                true));
    }

    @JmsListener(destination = CUA_NETEJA_ENTORN_APP)
    public void processaNeteja(@Payload NetejaEntornAppMessage message, Message jmsMessage) throws JMSException {
        Long entornAppId = message.getEntornAppId();
        log.info("Processant neteja per entornApp {} de la cua {}", entornAppId, CUA_NETEJA_ENTORN_APP);
        jmsTemplate.convertAndSend(CUA_NETEJA_SALUT, new NetejaEntornAppMessage(entornAppId));
        jmsTemplate.convertAndSend(CUA_NETEJA_TASQUES, new NetejaEntornAppMessage(entornAppId));
        jmsTemplate.convertAndSend(CUA_NETEJA_AVISOS, new NetejaEntornAppMessage(entornAppId));
        jmsTemplate.convertAndSend(CUA_NETEJA_ALARMES, new NetejaEntornAppMessage(entornAppId));
        // L'entornApp s'ha esborrat: el mòdul d'estadístiques ha d'esborrar també el catàleg, els widgets
        // i els dashboards associats (no només els fets), per això s'hi propaguen l'app i l'entorn.
        jmsTemplate.convertAndSend(CUA_NETEJA_ESTADISTICA, new NetejaEntornAppMessage(
                entornAppId,
                message.getAppId(),
                message.getEntornId(),
                true), msg -> {
            msg.setStringProperty(SELECTOR_NETEJA_ESTADISTICA, VALOR_ENTORN_APP);
            return msg;
        });
        jmsMessage.acknowledge();
        log.info("Missatges de neteja enviats per entornApp {}", entornAppId);
    }

}
