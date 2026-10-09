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

    @JmsListener(destination = Cues.CUA_NETEJA_ESTADISTICA, selector = Cues.SELECTOR_NETEJA_ESTADISTICA + " = '" + Cues.VALOR_APP + "'")
    public void processaNetejaApp(NetejaAppMessage appMessage, Message jmsMessage) throws JMSException {
        int deliveryCount = jmsMessage.getIntProperty("JMSXDeliveryCount");
        Long appId = appMessage.getAppId();
        log.info("Procesant neteja APP per appId {} (intent {})", appId, deliveryCount);
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
            handleError(appMessage.getEntornAppIds(), null, "appId: " + appId, deliveryCount, jmsMessage, e);
        }
    }

    @JmsListener(destination = Cues.CUA_NETEJA_ESTADISTICA, selector = Cues.SELECTOR_NETEJA_ESTADISTICA + " = '" + Cues.VALOR_ENTORN_APP + "'")
    public void processaNetejaEntorn(NetejaEntornAppMessage entornMessage, Message jmsMessage) throws JMSException {
        int deliveryCount = jmsMessage.getIntProperty("JMSXDeliveryCount");
        Long entornAppId = entornMessage.getEntornAppId();
        log.info("Neteja Estadistica per entornApp {} (intent {})", entornAppId, deliveryCount);
        try {
            if (entornMessage.isEntornAppEsborrat()) {
                estadisticaNetejaService.netejaPerEntornAppEsborrat(entornAppId, entornMessage.getAppId(), entornMessage.getEntornId());
            } else {
                estadisticaNetejaService.netejaPerEntornApp(entornAppId);
            }
            jmsMessage.acknowledge();
            log.info("Neteja Estadistica completada per entornApp {}", entornAppId);
        } catch (Exception e) {
            List<Long> entornAppIds = entornAppId != null ? List.of(entornAppId) : Collections.emptyList();
            handleError(entornAppIds, entornMessage, "entornAppId: " + entornAppId, deliveryCount, jmsMessage, e);
        }
    }

    /**
     * @param entornMessage missatge de neteja d'entornApp original (null per a la neteja d'app): se'n desen l'app,
     *                      l'entorn i l'indicador d'entornApp esborrat al monitor perquè el reintent faci la mateixa neteja.
     */
    private void handleError(List<Long> entornAppIds, NetejaEntornAppMessage entornMessage, String targetDesc, int deliveryCount, Message jmsMessage, Exception e) {
        if (deliveryCount >= 3) {
            log.error("Neteja Estadistica fallida per {} després de {} intents", targetDesc, deliveryCount, e);
            try {
                jmsMessage.acknowledge();
            } catch (JMSException jmsEx) {
                log.error("Error en acknowledge per {}: Artemis aturarà el missatge pel límit de reentregues", targetDesc, jmsEx);
            }
            if (entornAppIds != null && !entornAppIds.isEmpty()) {
                for (Long entornAppId : entornAppIds) {
                    crearEntradaMonitorError(entornAppId, entornMessage, ModulEnum.ESTADISTICA, e);
                }
            } else {
                crearEntradaMonitorError(null, entornMessage, ModulEnum.ESTADISTICA, e);
            }
        } else {
            log.warn("Neteja Estadistica fallida per {} (intent {}), es reintentarà", targetDesc, deliveryCount, e);
            throw new RuntimeException("Error en neteja, es reintentarà", e);
        }
    }

    private void crearEntradaMonitorError(Long entornAppId, NetejaEntornAppMessage entornMessage, ModulEnum modul, Exception e) {
        try {
            String desc = "Error en la neteja del mòdul " + modul;
            if (entornAppId != null) {
                desc += " per entornApp " + entornAppId;
            }
            Monitor monitor = Monitor.builder()
                    .entornAppId(entornAppId)
                    .appId(entornMessage != null ? entornMessage.getAppId() : null)
                    .entornId(entornMessage != null ? entornMessage.getEntornId() : null)
                    .entornAppEsborrat(entornMessage != null && entornMessage.isEntornAppEsborrat())
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
