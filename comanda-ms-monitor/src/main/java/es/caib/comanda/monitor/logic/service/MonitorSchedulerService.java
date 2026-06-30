package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.monitor.logic.helper.MonitorHelper;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import es.caib.comanda.ms.logic.helper.SchedulerTaskRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MonitorSchedulerService {

    private static final String TASK_ID = "MONITOR_BORRAT";

    private final TaskScheduler taskScheduler;
    private final ParametresHelper parametresHelper;
    private final MonitorHelper monitorHelper;
    private final SchedulerTaskRegistryService schedulerTaskRegistry;

    @Value("${" + BaseConfig.PROP_SCHEDULER_LEADER + ":#{true}}")
    private Boolean schedulerLeader;
    @Value("${" + BaseConfig.PROP_SCHEDULER_BACK + ":#{false}}")
    private Boolean schedulerBack;

    private final Map<Long, ScheduledFuture<?>> tasquesActives = new ConcurrentHashMap<>();

    public MonitorSchedulerService(
            @Qualifier("monitorTaskScheduler") TaskScheduler taskScheduler,
            ParametresHelper parametresHelper,
            MonitorHelper monitorHelper,
            SchedulerTaskRegistryService schedulerTaskRegistry) {
        this.taskScheduler = taskScheduler;
        this.parametresHelper = parametresHelper;
        this.monitorHelper = monitorHelper;
        this.schedulerTaskRegistry = schedulerTaskRegistry;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void inicialitzarTasques() {
        if (!isLeader()) {
            log.debug("Inicialització de tasques de monitor ignorada: aquesta instància no és leader per als schedulers");
            return;
        }
        // Esperarem 1 minut i pico a inicialitzar les tasques en segon pla
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            try {
                programarBorrat();
            } finally {
                executor.shutdown();
            }
        }, 83, TimeUnit.SECONDS);
    }

    public void programarBorrat() {
        final Boolean borratActiu = parametresHelper.getParametreBoolean(BaseConfig.PROP_MONITOR_BUIDAT_ACTIU, true);

        // Cancel·lem la tasca existent si existeix
        cancelarBorratExistent();

        if (isLeader() && borratActiu) {
            final Integer retencio = parametresHelper.getParametreEnter(BaseConfig.PROP_MONITOR_BUIDAT_RETENCIO_DIES, 7);
            final Integer periode = parametresHelper.getParametreEnter(BaseConfig.PROP_MONITOR_BUIDAT_PERIODE_MINUTS, 60);
            schedulerTaskRegistry.registerFixedRate(TASK_ID, "Purga del monitor", "Elimina les entrades antigues del monitor", periode, () -> executarBorrat(retencio));

            try {
                PeriodicTrigger periodicTrigger = new PeriodicTrigger(periode, TimeUnit.MINUTES);
                periodicTrigger.setFixedRate(false);

                ScheduledFuture<?> futuraTasca = taskScheduler.schedule(
                        () -> executarBorrat(retencio),
                        periodicTrigger
                );

                tasquesActives.put(0L, futuraTasca);
                log.debug("Tasca programada de borrat de dades del monitor amb període de {} minuts", periode);
            } catch (IllegalArgumentException e) {
                log.error("Error en programar la tasca de borrat de monitor. Període invàlid: {}", periode, e);
            }
        }
    }

    public void executarBorrat(Integer retencio) {
        if (isLeader()) {
            schedulerTaskRegistry.recordStart(TASK_ID);
            try {
                log.debug("Executant borrat de monitor...");
                monitorHelper.buidat(retencio);
                schedulerTaskRegistry.recordSuccess(TASK_ID);
            } catch (Exception e) {
                log.error("Error en l'execució del borrat del monitor", e);
                schedulerTaskRegistry.recordError(TASK_ID);
            }
        }
    }

    public void cancelarBorratExistent() {
        ScheduledFuture<?> tasca = tasquesActives.get(0L);
        if (tasca != null) {
            tasca.cancel(false);
            tasquesActives.remove(0L);
            log.info("Tasca de borrat de monitor cancel·lada");
        }
    }

    private boolean isLeader() {
        // TODO: Implementar per microserveis
        return schedulerLeader && schedulerBack;
    }
}
