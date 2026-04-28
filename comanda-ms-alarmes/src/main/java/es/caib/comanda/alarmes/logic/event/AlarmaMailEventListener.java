package es.caib.comanda.alarmes.logic.event;

import es.caib.comanda.alarmes.logic.helper.AlarmaMailHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmaMailEventListener {

    private final AlarmaMailHelper alarmaMailHelper;

    @Async("alarmesMailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPublishRequest(AlarmaMailPublishRequest request) {
        try {
            alarmaMailHelper.sendAlarmaUser(request.getAlarma());

            if (request.getAlarma().getAlarmaConfig().isCorreuGeneric()) {
                alarmaMailHelper.sendAlarmaGeneric(request.getAlarma());
            }
        } catch (Exception ex) {
            log.error("Error enviant correus d'alarma de forma asíncrona", ex);
        }
    }
}
