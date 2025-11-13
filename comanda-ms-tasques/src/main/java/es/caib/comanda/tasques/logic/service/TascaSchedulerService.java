package es.caib.comanda.tasques.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import es.caib.comanda.tasques.persist.repository.TascaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
public class TascaSchedulerService {

    private final ParametresHelper parametresHelper;

    private final TascaRepository tascaRepository;

    @Scheduled(fixedRate = 61, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void buidatTasquesScheduler() {
        log.debug("BuidatTasquesScheduler start");

        Integer diesBorrar = parametresHelper.getParametreEnter(BaseConfig.PROP_TASCA_BORRAT_DIES, 0);
        Integer pendentsDiesBorrar = parametresHelper.getParametreEnter(BaseConfig.PROP_TASCA_PEND_BORRAT_DIES, 0);

        if (diesBorrar >= 0) {
            LocalDateTime dataLimit = LocalDateTime.now().minusDays(diesBorrar);
            int eliminats = tascaRepository.deleteByDataFiBefore(dataLimit);
            log.debug("BuidatTasquesScheduler - Eliminats {} tasques", eliminats);
        }

        if (pendentsDiesBorrar > 0) {
            LocalDateTime dataLimitPendents = LocalDateTime.now().minusDays(pendentsDiesBorrar);
            int eliminats = tascaRepository.deleteByDataIniciBeforeAndDataFiIsNull(dataLimitPendents);
            log.debug("BuidatTasquesScheduler - Pendents - Eliminats {} tasques", eliminats);
        }

        log.debug("BuidatTasquesScheduler end");
    }

}
