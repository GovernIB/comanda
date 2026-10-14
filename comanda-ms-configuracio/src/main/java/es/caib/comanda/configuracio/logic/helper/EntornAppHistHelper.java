package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.configuracio.persist.repository.EntornAppHistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class EntornAppHistHelper {

    private final EntornAppHistRepository entornAppHistRepository;

    /**
     * Elimina els registres d'historial anteriors a la data indicada.
     * Aquest mètode gestiona la seva pròpia transacció.
     */
    @Transactional
    public int deleteHistoryBeforeDate(LocalDateTime cutoffDate) {
        log.debug("Iniciant eliminació d'historial anterior a: {}", cutoffDate);
        int eliminats = entornAppHistRepository.deleteByDataBefore(cutoffDate);
        log.debug("Eliminats {} registres d'historial", eliminats);
        return eliminats;
    }

}
