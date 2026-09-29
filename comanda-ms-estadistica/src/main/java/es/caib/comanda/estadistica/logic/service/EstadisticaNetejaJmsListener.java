package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.base.config.Cues;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaNetejaService;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.jms.NetejaAppMessage;
import es.caib.comanda.ms.logic.intf.jms.NetejaEntornAppMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstadisticaNetejaJmsListener {

    private final EstadisticaNetejaService estadisticaNetejaService;
    private final MonitorServiceClient monitorServiceClient;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @JmsListener(destination = Cues.CUA_NETEJA_ESTADISTICA)
    public void processaNeteja(@Payload Object message, Message jmsMessage) throws JMSException {
        int deliveryCount = jmsMessage.getIntProperty("JMSXDeliveryCount");
        if (message instanceof NetejaAppMessage) {
            NetejaAppMessage appMessage = (NetejaAppMessage) message;
            Long appId = appMessage.getAppId();
            try {
                estadisticaNetejaService.netejaPerApp(
                        appId,
                        appMessage.getEntornAppIds(),
                        appMessage.isEsborrarCataleg(),
                        appMessage.isEsborrarWidgets()
                );
                jmsMessage.acknowledge();
                log.info("Neteja Estadistica completada per appId {}", appId);
            } catch (Exception e) {
                handleError(appMessage.getEntornAppIds(), "appId: " + appId, deliveryCount, jmsMessage, e);
            }
        } else if (message instanceof NetejaEntornAppMessage) {
            NetejaEntornAppMessage entornMessage = (NetejaEntornAppMessage) message;
            Long entornAppId = entornMessage.getEntornAppId();
            log.info("Neteja Estadistica per entornApp {} (intent {})", entornAppId, deliveryCount);
            try {
                estadisticaNetejaService.netejaPerEntornApp(entornAppId);
                jmsMessage.acknowledge();
                log.info("Neteja Estadistica completada per entornApp {}", entornAppId);
            } catch (Exception e) {
                List<Long> entornAppIds = entornAppId != null ? List.of(entornAppId) : Collections.emptyList();
                handleError(entornAppIds, "entornAppId: " + entornAppId, deliveryCount, jmsMessage, e);
            }
        } else {
            log.warn("Missatge desconegut rebut a CUA_NETEJA_ESTADISTICA: {}", message != null ? message.getClass() : "null");
            jmsMessage.acknowledge();
        }
    }

    private void handleError(List<Long> entornAppIds, String targetDesc, int deliveryCount, Message jmsMessage, Exception e) {
        if (deliveryCount >= 3) {
            log.error("Neteja Estadistica fallida per {} després de {} intents", targetDesc, deliveryCount, e);
            try {
                jmsMessage.acknowledge();
            } catch (JMSException jmsEx) {
                log.error("Error en acknowledge per {}: Artemis aturarà el missatge pel límit de reentregues", targetDesc, jmsEx);
            }
            if (entornAppIds != null && !entornAppIds.isEmpty()) {
                for (Long entornAppId : entornAppIds) {
                    crearEntradaMonitorError(entornAppId, ModulEnum.ESTADISTICA, e);
                }
            } else {
                crearEntradaMonitorError(null, ModulEnum.ESTADISTICA, e);
            }
        } else {
            log.warn("Neteja Estadistica fallida per {} (intent {}), es reintentarà", targetDesc, deliveryCount, e);
            throw new RuntimeException("Error en neteja, es reintentarà", e);
        }
    }

    private void crearEntradaMonitorError(Long entornAppId, ModulEnum modul, Exception e) {
        try {
            String desc = "Error en la neteja del mòdul " + modul;
            if (entornAppId != null) {
                desc += " per entornApp " + entornAppId;
            }
            Monitor monitor = Monitor.builder()
                    .entornAppId(entornAppId)
                    .modul(modul)
                    .tipus(AccioTipusEnum.INTERNA)
                    .data(LocalDateTime.now())
                    .operacio("netejaEntornApp")
                    .estat(EstatEnum.ERROR)
                    .errorDescripcio(desc)
                    .excepcioMessage(e.getMessage())
                    .build();
            monitorServiceClient.create(monitor, httpAuthorizationHeaderHelper.getAuthorizationHeader());
        } catch (Exception ex) {
            log.error("No s'ha pogut crear l'entrada al monitor per entornApp {}", entornAppId, ex);
        }
    }
}
