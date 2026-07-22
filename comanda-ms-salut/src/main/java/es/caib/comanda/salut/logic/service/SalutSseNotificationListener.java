package es.caib.comanda.salut.logic.service;

import es.caib.comanda.ms.sse.ComandaSseEvent;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.ms.sse.ComandaSsePublishRequest;
import es.caib.comanda.ms.sse.ComandaSseSubscriberRegistry;
import es.caib.comanda.salut.logic.event.SalutCompactionFinishedEvent;
import es.caib.comanda.salut.logic.helper.SalutEstatHelper;
import es.caib.comanda.salut.logic.intf.model.SalutInformeEstatItem;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Notifica als clients SSE connectats quan canvia l'estat de salut d'un entorn-app.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalutSseNotificationListener {

    private final SalutRepository salutRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SalutEstatHelper salutEstatHelper;

    /** Opcional: present quan el mòdul d'alarmes és al classpath (desplegament monolític). */
    @Autowired(required = false)
    private ComandaSseSubscriberRegistry sseSubscriberRegistry;

    /**
     * Dispara la notificació SSE un cop el compactat ha acabat, de manera que els clients
     * reben les sèries MINUTS/HORA/DIA ja actualitzades (i no les del minut anterior).
     * S'usa fallbackExecution=true per si l'event es publica fora de transacció.
     */
    @Async("salutWorkerExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onSalutCompactionFinished(SalutCompactionFinishedEvent event) {
        if (event.getSalutId() == null) {
            return;
        }
        try {
            SalutEntity salut = salutRepository.findById(event.getSalutId()).orElse(null);
            if (salut == null) {
                log.debug("No s'ha trobat la salut {} per notificar via SSE", event.getSalutId());
                return;
            }

            Map<String, List<SalutInformeEstatItem>> estatItemsPerAgrupacio = null;
            try {
                boolean includeAll = sseSubscriberRegistry == null
                        || sseSubscriberRegistry.hasActiveSubscribers();
                estatItemsPerAgrupacio = salutEstatHelper.computeEstatItemsPerAgrupacio(
                        salut.getEntornAppId(), includeAll);
            } catch (Exception ex) {
                log.warn("Error calculant estatItems SSE per entornApp {}: {}",
                        salut.getEntornAppId(), ex.getMessage(), ex);
            }

            SalutChangedSsePayload payload = SalutChangedSsePayload.builder()
                    .entornAppId(salut.getEntornAppId())
                    .data(salut.getData())
                    .appEstat(salut.getAppEstat())
                    .appLatencia(salut.getAppLatencia())
                    .bdEstat(salut.getBdEstat())
                    .bdLatencia(salut.getBdLatencia())
                    .peticioError(salut.isPeticioError())
                    .integracioUpCount(salut.getIntegracioUpCount())
                    .integracioWarnCount(salut.getIntegracioWarnCount())
                    .integracioDownCount(salut.getIntegracioDownCount())
                    .integracioDesconegutCount(salut.getIntegracioDesconegutCount())
                    .subsistemaUpCount(salut.getSubsistemaUpCount())
                    .subsistemaWarnCount(salut.getSubsistemaWarnCount())
                    .subsistemaDownCount(salut.getSubsistemaDownCount())
                    .subsistemaDesconegutCount(salut.getSubsistemaDesconegutCount())
                    .missatgeErrorCount(salut.getMissatgeErrorCount())
                    .missatgeWarnCount(salut.getMissatgeWarnCount())
                    .missatgeInfoCount(salut.getMissatgeInfoCount())
                    .estatItemsPerAgrupacio(estatItemsPerAgrupacio)
                    .build();
            eventPublisher.publishEvent(new ComandaSsePublishRequest(
                    new ComandaSseEvent(
                            ComandaSseEventTypes.SALUT_CHANGED,
                            payload,
                            LocalDateTime.now())));
            log.debug("Notificació SSE de canvi de salut enviada per entornApp {} (estatItems: {})",
                    salut.getEntornAppId(), estatItemsPerAgrupacio != null ? "sí" : "no");
        } catch (Exception ex) {
            log.warn("Error notificant canvi de salut via SSE per entornApp {}: {}",
                    event.getEntornAppId(), ex.getMessage(), ex);
        }
    }

}
