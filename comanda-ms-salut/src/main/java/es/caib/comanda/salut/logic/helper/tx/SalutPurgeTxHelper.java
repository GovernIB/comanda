package es.caib.comanda.salut.logic.helper.tx;

import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Bean separat per executar el purge batch dins d'una nova transacció.
 * Necessari per evitar el problema de self-invocation que salta el proxy @Transactional.
 */
@Component
@RequiredArgsConstructor
public class SalutPurgeTxHelper {

    private final SalutRepository salutRepository;
    private final SalutIntegracioRepository salutIntegracioRepository;
    private final SalutSubsistemaRepository salutSubsistemaRepository;
    private final SalutMissatgeRepository salutMissatgeRepository;
    private final SalutDetallRepository salutDetallRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void eliminarBatchEnNovaTransaccio(List<Long> salutIds) {
        // Eliminar fills per assegurar integritat
        salutIntegracioRepository.deleteAllBySalutIdIn(salutIds);
        salutSubsistemaRepository.deleteAllBySalutIdIn(salutIds);
        salutMissatgeRepository.deleteAllBySalutIdIn(salutIds);
        salutDetallRepository.deleteAllBySalutIdIn(salutIds);
        // Eliminació en batch dels registres principals
        salutRepository.deleteAllByIdInBatch(salutIds);
    }
}
