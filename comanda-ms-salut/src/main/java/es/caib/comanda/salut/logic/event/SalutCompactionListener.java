package es.caib.comanda.salut.logic.event;

import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener d'esdeveniments per a llançar la compactació després de cada consulta de salut.
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
            salutInfoHelper.buidatIcompactat(event.getEntornAppId(), event.getSalutId(), event.getNumeroDiesAgrupacio());
        } catch (Exception ex) {
            log.warn("Error durant el procés de buidat i compactació després de l'esdeveniment: {}", ex.getMessage());
        }
    }
}
