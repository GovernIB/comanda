package es.caib.comanda.alarmes.logic.service.sse;

import es.caib.comanda.alarmes.back.sse.ComandaSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ComandaSseEventListener {

    private final ComandaSseService comandaSseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPublishRequest(ComandaSsePublishRequest request) {
        comandaSseService.publish(request.getEvent());
    }

}
