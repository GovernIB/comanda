package es.caib.comanda.alarmes.logic.service;

import es.caib.comanda.alarmes.logic.intf.service.AlarmaConfigService;
import es.caib.comanda.base.config.Cues;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.jms.NetejaEntornAppMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmaConfigNetejaJmsListener {

    private final AlarmaConfigService alarmaConfigService;
    private final MonitorServiceClient monitorServiceClient;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @JmsListener(destination = Cues.CUA_NETEJA_ALARMES)
    public void processaNeteja(@Payload NetejaEntornAppMessage message, Message jmsMessage) throws JMSException {
        Long entornAppId = message.getEntornAppId();
        int deliveryCount = jmsMessage.getIntProperty("JMSXDeliveryCount");
        log.info("Neteja AlarmaConfig per entornApp {} (intent {})", entornAppId, deliveryCount);
        try {
            alarmaConfigService.netejaPerEntornApp(entornAppId);
            jmsMessage.acknowledge();
            log.info("Neteja AlarmaConfig completada per entornApp {}", entornAppId);
        } catch (Exception e) {
            if (deliveryCount >= 3) {
                log.error("Neteja AlarmaConfig fallida per entornApp {} després de {} intents", entornAppId, deliveryCount, e);
                try {
                    jmsMessage.acknowledge();
                } catch (JMSException jmsEx) {
                    log.error("Error en acknowledge per entornApp {}: Artemis aturarà el missatge pel límit de reentregues", entornAppId, jmsEx);
                }
                crearEntradaMonitorError(entornAppId, ModulEnum.ALARMES, e);
            } else {
                log.warn("Neteja AlarmaConfig fallida per entornApp {} (intent {}), es reintentarà", entornAppId, deliveryCount, e);
                throw new RuntimeException("Error en neteja, es reintentarà", e);
            }
        }
    }

    private void crearEntradaMonitorError(Long entornAppId, ModulEnum modul, Exception e) {
        try {
            Monitor monitor = Monitor.builder()
                    .entornAppId(entornAppId)
                    .modul(modul)
                    .tipus(AccioTipusEnum.INTERNA)
                    .data(LocalDateTime.now())
                    .operacio("netejaEntornApp")
                    .estat(EstatEnum.ERROR)
                    .errorDescripcio("Error en la neteja del mòdul " + modul + " per entornApp " + entornAppId)
                    .excepcioMessage(e.getMessage())
                    .build();
            monitorServiceClient.create(monitor, httpAuthorizationHeaderHelper.getAuthorizationHeader());
        } catch (Exception ex) {
            log.error("No s'ha pogut crear l'entrada al monitor per entornApp {}", entornAppId, ex);
        }
    }
}
