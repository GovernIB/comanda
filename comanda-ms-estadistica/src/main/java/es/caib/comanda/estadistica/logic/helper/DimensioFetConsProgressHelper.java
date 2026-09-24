package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.ms.sse.ComandaSseEvent;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.ms.sse.ComandaSsePublishRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Publica per SSE el progrés de l'actualització de conselleries (acció FET_CONS de Dimensio), perquè el
 * frontend pugui mostrar una barra de progrés real mentre dura la crida.
 * <p>
 * FetConsActionExecutor.exec() s'executa dins la transacció REQUIRED oberta per
 * BaseMutableResourceService.artifactActionExec(), que no fa commit fins que acaba tota la petició HTTP.
 * Com que ComandaSseEventListener escolta amb @TransactionalEventListener(phase = AFTER_COMMIT), si
 * publicàssim l'event dins d'aquesta transacció, tots els events de progrés s'enviarien de cop en acabar
 * (perdent l'efecte de progrés en temps real). Per això aquest mètode se suspèn de la transacció exterior amb
 * NOT_SUPPORTED (i no amb REQUIRES_NEW: aquesta obriria una segona transacció/connexió JDBC innecessària, ja
 * que aquí no es fa cap operació JPA — vegeu ConsultaEstadisticaHelper.getDadesWidget, on REQUIRES_NEW amb
 * consultes JPA reals va provocar un interbloqueig del pool de connexions en executar-se moltes peticions
 * concurrents). Sense cap transacció activa, ComandaSseEventListener (fallbackExecution=true) executa el
 * listener a l'instant en lloc d'esperar cap commit, aconseguint el mateix efecte de progrés en temps real.
 */
@Component
@RequiredArgsConstructor
public class DimensioFetConsProgressHelper {

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void publishProgress(Long dimensioId, int processats, int total) {
        eventPublisher.publishEvent(new ComandaSsePublishRequest(
            new ComandaSseEvent(
                ComandaSseEventTypes.DIMENSIO_FET_CONS_PROGRESS,
                new Progress(dimensioId, processats, total),
                LocalDateTime.now())));
    }

    @Getter
    @AllArgsConstructor
    public static class Progress implements Serializable {
        private final Long dimensioId;
        private final int processats;
        private final int total;
    }

}
