package es.caib.comanda.avisos.logic.service;

import es.caib.comanda.avisos.persist.repository.AvisRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
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
public class AvisSchedulerService {

    private final ParametresHelper parametresHelper;

    private final AvisRepository avisRepository;

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void buidatAvisScheduler() {
        log.debug("BuidatAvisScheduler start");

        Integer diesBorrar = parametresHelper.getParametreEnter(BaseConfig.PROP_AVIS_BORRAT_DIES, 0);
        if (diesBorrar <= 0) return;

        LocalDateTime dataLimit = LocalDateTime.now().minusDays(diesBorrar);
        int eliminats = avisRepository.deleteByLastModifiedDateBefore(dataLimit);

        log.debug("BuidatAvisScheduler end - Eliminats {} avisos", eliminats);
    }

}
