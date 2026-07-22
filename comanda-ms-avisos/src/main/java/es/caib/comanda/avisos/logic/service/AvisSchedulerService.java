package es.caib.comanda.avisos.logic.service;

import es.caib.comanda.avisos.persist.repository.AvisRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import es.caib.comanda.ms.logic.helper.SchedulerTaskRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
public class AvisSchedulerService {

    private static final String TASK_ID = "AVIS_BORRAT";

    private final ParametresHelper parametresHelper;
    private final AvisRepository avisRepository;
    private final SchedulerTaskRegistryService schedulerTaskRegistry;

    @PostConstruct
    public void registrarTasques() {
        schedulerTaskRegistry.registerFixedRate(TASK_ID, "Purga d'avisos", "Elimina els avisos antics", 60L, this::buidatAvisScheduler);
    }

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void buidatAvisScheduler() {
        schedulerTaskRegistry.recordStart(TASK_ID);
        log.debug("BuidatAvisScheduler start");
        try {
            Integer diesBorrar = parametresHelper.getParametreEnter(BaseConfig.PROP_AVIS_BORRAT_DIES, 0);
            if (diesBorrar > 0) {
                LocalDateTime dataLimit = LocalDateTime.now().minusDays(diesBorrar);
                int eliminats = avisRepository.deleteByLastModifiedDateBefore(dataLimit);
                log.debug("BuidatAvisScheduler - Eliminats {} avisos", eliminats);
            }
            schedulerTaskRegistry.recordSuccess(TASK_ID);
        } catch (Exception e) {
            schedulerTaskRegistry.recordError(TASK_ID);
            throw e;
        }
        log.debug("BuidatAvisScheduler end");
    }

}
