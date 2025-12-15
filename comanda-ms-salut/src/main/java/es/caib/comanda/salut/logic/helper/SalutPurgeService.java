package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockTimeoutException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servei responsable de l'eliminació en transaccions aïllades (REQUIRES_NEW)
 * per evitar contaminar transaccions més àmplies i garantir que els
 * {@code @Modifying} s'executen dins d'una transacció activa.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalutPurgeService {

    private final SalutRepository salutRepository;
    private final SalutIntegracioRepository salutIntegracioRepository;
    private final SalutSubsistemaRepository salutSubsistemaRepository;
    private final SalutMissatgeRepository salutMissatgeRepository;
    private final SalutDetallRepository salutDetallRepository;

    private static final int BATCH_SIZE = 500;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void eliminarDadesSalutAntigues(Long entornAppId, es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut tipus, LocalDateTime data) {
        log.debug("Eliminant dades de salut antigues. EntornAppId: {}, tipus: {}, data: {}", entornAppId, tipus, data);
        List<Long> idsAntics = salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(entornAppId, tipus, data);

        for (int i = 0; i < idsAntics.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, idsAntics.size());
            List<Long> batch = idsAntics.subList(i, end);
            eliminarLlista(batch);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void eliminarLlista(List<Long> salutIds) {
        if (salutIds == null || salutIds.isEmpty()) {
            log.debug("Cap registre de salut per eliminar (null o buit)");
            return;
        }
        int intents = 0;
        int maxIntents = 3;
        while (true) {
            try {
                log.info("Eliminant {} registres de salut antics...", salutIds.size());
                // Eliminar fills per assegurar integritat
                salutIntegracioRepository.deleteAllBySalutIdIn(salutIds);
                salutSubsistemaRepository.deleteAllBySalutIdIn(salutIds);
                salutMissatgeRepository.deleteAllBySalutIdIn(salutIds);
                salutDetallRepository.deleteAllBySalutIdIn(salutIds);
                // Eliminació en batch per reduir bloquejos
                salutRepository.deleteAllByIdInBatch(salutIds);
                log.info("Eliminat {} registres de salut antics", salutIds.size());
                return;
            } catch (RuntimeException ex) {
                intents++;
                if (isLockAcquisitionException(ex) && intents < maxIntents) {
                    try {
                        long sleepMs = 200L * intents;
                        log.warn("Lock detectat eliminant salut (batch {} elements). Reintent {} després de {}ms: {}", salutIds.size(), intents, sleepMs, ex.getMessage());
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
}
