package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ConfiguracioSchedulerService {

    private final TaskScheduler taskScheduler;
    private final EntornAppRepository entornAppRepository;
    private final AppInfoHelper appInfoHelper;

    @Value("${" + BaseConfig.PROP_SCHEDULER_LEADER + ":#{true}}")
    private Boolean schedulerLeader;

    private final Map<Long, ScheduledFuture<?>> tasquesActives = new ConcurrentHashMap<>();
    // Emmagatzemam l'interval programat per a cada entornApp per poder detectar canvis
    private final Map<Long, Integer> intervalsActius = new ConcurrentHashMap<>();

    public ConfiguracioSchedulerService(
            @Qualifier("configuracioTaskScheduler") TaskScheduler taskScheduler,
            EntornAppRepository entornAppRepository,
            AppInfoHelper appInfoHelper) {
        this.taskScheduler = taskScheduler;
        this.entornAppRepository = entornAppRepository;
        this.appInfoHelper = appInfoHelper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void inicialitzarTasques() {
        // Esperarem mig minut a inicialitzar les tasques en segon pla
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            try {
                List<EntornAppEntity> entornAppsActives = entornAppRepository.findByActivaTrueAndAppActivaTrue();
                entornAppsActives.forEach(this::programarTasca);
            } finally {
                executor.shutdown();
            }
        }, 30, TimeUnit.SECONDS);
    }

    public void programarTasca(EntornAppEntity entornApp) {
        if (entornApp.getInfoInterval() == null || entornApp.getInfoInterval() <= 0) {
            log.warn("EntornApp " + entornApp.getId() + ":" + entornApp.getInfoInterval() + " no és un període vàlid.");
            return;
        }

        // Cancel·lem la tasca existent si existeix
        cancelarTascaExistent(entornApp.getId());

        if (!entornApp.isActiva()) {
            log.info("Tasca de refresc de la informació no programada per l'entornApp: {}, degut a que no està activa", entornApp.getId());
            return;
        }

        try {
            PeriodicTrigger periodicTrigger = new PeriodicTrigger(TimeUnit.MINUTES.toMillis(entornApp.getInfoInterval()), TimeUnit.MILLISECONDS);
            long initialDelay = TimeUnit.SECONDS.toMillis(new Random().nextInt(60));
            periodicTrigger.setInitialDelay(initialDelay); // Entre 0 i 60 segons

            ScheduledFuture<?> futuraTasca = taskScheduler.schedule(
                    () -> executarProces(entornApp.getId()),
                    periodicTrigger
            );

            tasquesActives.put(entornApp.getId(), futuraTasca);
            intervalsActius.put(entornApp.getId(), entornApp.getInfoInterval());
            log.info("Tasca programada de refresc de la informació per l'entornApp: {}, amb període: {}",
                    entornApp.getId(),
                    entornApp.getInfoInterval());
        } catch (IllegalArgumentException e) {
            log.error("Error en programar la tasca de refresc de la informació per l'entornApp: {}. Període invàlid: {}",
                    entornApp.getId(),
                    entornApp.getInfoInterval(),
                    e);
        }
    }

    private void executarProces(Long entornAppId) {
        if (isLeader()) {
            try {
                log.info("Executant procés de refresc de la informació per l'entornApp {}", entornAppId);

                // Refrescar informació de entorn-app
                appInfoHelper.refreshAppInfo(entornAppId);

            } catch (Exception e) {
                log.error("Error en l'execució del procés de refresc de la informació per l'entornApp {}", entornAppId, e);
            }
        }
    }

    public void cancelarTascaExistent(Long entornAppId) {
        ScheduledFuture<?> tasca = tasquesActives.get(entornAppId);
        if (tasca != null) {
            tasca.cancel(false);
            tasquesActives.remove(entornAppId);
            intervalsActius.remove(entornAppId);
            log.info("Tasca de refresc de la informació cancel·lada per l'entornAppId: {}", entornAppId);
        }
    }

    private boolean isLeader() {
        // TODO: Implementar per microserveis
        return schedulerLeader;
    }


    // Cada hora comprovarem que no hi hagi cap aplicacio-entorn que no s'estigui actualitzant
    @Scheduled(cron = "0 0 */1 * * *")
    public void comprovarRefrescInfo() {
        log.debug("Comprovant refresc periòdic dels entorn-app");
        List<EntornAppEntity> entornAppsActives = entornAppRepository.findByActivaTrueAndAppActivaTrue();
        entornAppsActives.forEach(ea -> {
            ScheduledFuture<?> tasca = tasquesActives.get(ea.getId());
            Integer intervalActual = intervalsActius.get(ea.getId());
            if (tasca == null) {
                programarTasca(ea);
            } else if (ea.getInfoInterval() != null && !ea.getInfoInterval().equals(intervalActual)) {
                log.info("Detectat canvi d'interval de info per l'entornApp {}: {} -> {}. Reprogramant tasca...", ea.getId(), intervalActual, ea.getInfoInterval());
                programarTasca(ea);
            }
        });
    }

}
