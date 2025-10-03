package es.caib.comanda.salut.logic.event;

import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener d'esdeveniments per a llançar la compactació i el buidat després de cada consulta de salut.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalutCompactionListener {

    private final SalutInfoHelper salutInfoHelper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSalutInfoUpdated(SalutInfoUpdatedEvent event) {
        try {
            salutInfoHelper.compactar(event.getEntornAppId(), event.getSalutId());
        } catch (Exception ex) {
            log.warn("Error durant el procés de compactació després de l'esdeveniment: {}", ex.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSalutCompactionFinished(SalutCompactionFinishedEvent event) {
        try {
            salutInfoHelper.buidar(event.getEntornAppId(), event.getSalutId());
        } catch (Exception ex) {
            log.warn("Error durant el procés de buidat després de l'esdeveniment: {}", ex.getMessage());
        }
    }
}
