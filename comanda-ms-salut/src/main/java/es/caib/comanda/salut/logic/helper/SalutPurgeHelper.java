package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Component;

import javax.persistence.LockTimeoutException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Semaphore;

@Slf4j
@Component
@RequiredArgsConstructor
public class SalutPurgeHelper {

    public static final int PURGE_BATCH_SIZE = 100;
    private static final Semaphore PURGE_SEMAPHORE = new Semaphore(1);

    private final SalutRepository salutRepository;
    private final SalutIntegracioRepository salutIntegracioRepository;
    private final SalutSubsistemaRepository salutSubsistemaRepository;
    private final SalutMissatgeRepository salutMissatgeRepository;
    private final SalutDetallRepository salutDetallRepository;
    // Important: separar el mètode transaccional en un altre bean per evitar self-invocation sense proxy
    private final es.caib.comanda.salut.logic.helper.tx.SalutPurgeTxHelper purgeTxHelper;

    public void eliminarDadesSalutAntigues(Long entornAppId, TipusRegistreSalut tipus, LocalDateTime data) {
        log.debug("Eliminant dades de salut antigues. EntornAppId: {}, tipus: {}, data: {}", entornAppId, tipus, data);
        List<Long> idsAntics = salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(entornAppId, tipus, data);
        if (idsAntics == null || idsAntics.isEmpty()) return;
        for (int i = 0; i < idsAntics.size(); i += PURGE_BATCH_SIZE) {
            int end = Math.min(i + PURGE_BATCH_SIZE, idsAntics.size());
            List<Long> batch = idsAntics.subList(i, end);
            eliminarLlistaAmbRetry(batch);
        }
    }

    private void eliminarLlistaAmbRetry(List<Long> salutIds) {
        if (salutIds == null || salutIds.isEmpty()) {
            log.debug("Cap registre de salut per eliminar (null o buit)");
            return;
        }
        int intents = 0;
        int maxIntents = 3;
        while (true) {
            try {
                log.info("Eliminant {} registres de salut antics...", salutIds.size());
                try {
                    PURGE_SEMAPHORE.acquire();
                    try {
                        purgeTxHelper.eliminarBatchEnNovaTransaccio(salutIds);
                    } finally {
                        PURGE_SEMAPHORE.release();
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
                log.info("Eliminat {} registres de salut antics", salutIds.size());
                return;
            } catch (RuntimeException ex) {
                intents++;
                if (isLockAcquisitionException(ex) && intents < maxIntents) {
                    long sleepMs = 200L * intents;
                    log.warn("Lock detectat eliminant salut (batch {} elements). Reintent {} després de {}ms: {}", salutIds.size(), intents, sleepMs, ex.getMessage());
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ex;
                    }
                } else {
                    throw ex;
                }
            }
        }
    }

    private boolean isLockAcquisitionException(Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof CannotAcquireLockException || cause instanceof LockTimeoutException || cause instanceof LockAcquisitionException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    // Nota: el mètode transaccional viu a SalutPurgeTxHelper per assegurar que Spring aplica el proxy
}
