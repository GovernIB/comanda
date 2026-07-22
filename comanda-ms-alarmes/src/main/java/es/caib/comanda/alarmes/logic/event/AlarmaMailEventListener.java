package es.caib.comanda.alarmes.logic.event;

import es.caib.comanda.alarmes.logic.helper.AlarmaMailHelper;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
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
    private final ParametresHelper parametresHelper;

    @Async("alarmesMailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPublishRequest(AlarmaMailPublishRequest request) {
        if (isLogActivacio()) {
            log.info("[EML] Processant event de correu alarmaId={} configId={} tipusEvent={}",
                    request.getAlarma().getId(), request.getAlarma().getAlarmaConfig().getId(), request.getTipusEvent());
        }
        try {
            alarmaMailHelper.sendAlarmaUser(request.getAlarma(), request.getTipusEvent());
        } catch (Exception ex) {
            log.error("Error enviant correus d'alarma d'usuari de forma asíncrona", ex);
        }
        try {
            if (request.getAlarma().getAlarmaConfig().isCorreuGeneric()) {
                alarmaMailHelper.sendAlarmaGeneric(request.getAlarma(), request.getTipusEvent());
            }
        } catch (Exception ex) {
            log.error("Error enviant correu d'alarma genèric de forma asíncrona", ex);
        }
    }

    private boolean isLogActivacio() {
        return Boolean.TRUE.equals(parametresHelper.getParametreBoolean(BaseConfig.PROP_ALARMA_LOG_ACTIVACIO, false));
    }
}
