package es.caib.comanda.monitor.logic.helper;

import es.caib.comanda.monitor.persist.repository.MonitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorHelper {

    private final MonitorRepository monitorRepository;

    private static final Integer BATCH_SIZE = 200;
    private static final int LOG_EVERY_BATCHES = 10;


    @Transactional
    public void buidat(Integer retencio) {
        if (retencio == null || retencio < 0) {
            log.warn("Retenció invàlida per al buidat del monitor: {}", retencio);
            return;
        }

        log.debug("Execució de tasca programada d'esborrar dades del monitor d'integracions amb {} dies d'antiguitat (batch={}).", retencio, BATCH_SIZE);

        final LocalDateTime cutoff = LocalDateTime.now().minusDays(retencio);

        int totalDeleted = 0;
        int batch = 0;

        while (true) {
//            List<Long> ids = monitorRepository.findIdsBatchBefore(cutoff, BATCH_SIZE);
            List<Long> ids = monitorRepository.findIdsBeforeDate(cutoff, PageRequest.of(0, BATCH_SIZE, Sort.by("id")));
            if (ids == null || ids.isEmpty()) {
                break;
            }

            monitorRepository.deleteAllByIdInBatch(ids);
            totalDeleted += ids.size();
            batch++;

            if (batch % LOG_EVERY_BATCHES == 0) {
                log.debug("Esborrat batch {} ({} registres acumulats)", batch, totalDeleted);
            }
        }

        if (totalDeleted > 0) {
            log.debug("{} dades de monitor d'integració antigues esborrades en {} batches.", totalDeleted, batch);
        } else {
            log.debug("No s'han trobat dades de monitor d'integració a esborrar.");
        }
    }

}
