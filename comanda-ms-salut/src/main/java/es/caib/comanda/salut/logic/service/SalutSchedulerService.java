package es.caib.comanda.salut.logic.service;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.KeycloakHelper;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SalutSchedulerService {

    private final TaskScheduler taskScheduler;
    private final SalutClientHelper salutClientHelper;
    private final SalutInfoHelper salutInfoHelper;

    @Value("${" + BaseConfig.PROP_SCHEDULER_LEADER + ":#{false}}")
    private Boolean schedulerLeader;

    private final Map<Long, ScheduledFuture<?>> tasquesActives = new ConcurrentHashMap<>();

    public SalutSchedulerService(
            @Qualifier("salutTaskScheduler") TaskScheduler taskScheduler,
            SalutClientHelper salutClientHelper,
            SalutInfoHelper salutInfoHelper,
            KeycloakHelper keycloakHelper) {
        this.taskScheduler = taskScheduler;
        this.salutClientHelper = salutClientHelper;
        this.salutInfoHelper = salutInfoHelper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void inicialitzarTasques() {
        List<EntornApp> entornAppsActives = salutClientHelper.entornAppFindByActivaTrue();
        entornAppsActives.forEach(this::programarTasca);
    }

    public void programarTasca(EntornApp entornApp) {
        // Cancel·lem la tasca existent si existeix
        cancelarTascaExistent(entornApp.getId());

        if (!entornApp.isActiva()) {
            log.info("Tasca de obtenció de informació de salut no programada per l'entornApp: {}, degut a que no està activa", entornApp.getId());
            return;
        }

        try {
            PeriodicTrigger periodicTrigger = new PeriodicTrigger(TimeUnit.MINUTES.toMillis(entornApp.getSalutInterval()), TimeUnit.MILLISECONDS);
            long initialDelay = TimeUnit.SECONDS.toMillis(new Random().nextInt(60));
            periodicTrigger.setInitialDelay(initialDelay); // Entre 0 i 60 segons

            ScheduledFuture<?> futuraTasca = taskScheduler.schedule(
                    () -> executarProces(entornApp),
                    periodicTrigger
            );

            tasquesActives.put(entornApp.getId(), futuraTasca);
            log.info("Tasca programada de obtenció de informació de salut per l'entornApp: {}, amb període: {}",
                    entornApp.getId(),
                    entornApp.getSalutInterval());
        } catch (IllegalArgumentException e) {
            log.error("Error en programar la tasca de obtenció de informació de salut per l'entornApp: {}. Període invàlid: {}",
                    entornApp.getId(),
                    entornApp.getSalutInterval(),
                    e);
        }
    }

    private void executarProces(EntornApp entornApp) {
        if (isLeader()) {
            try {
                log.info("Executant procés per l'entornApp {}", entornApp.getId());

                // Obtenció de informació de salut per entorn-app
                salutInfoHelper.getSalutInfo(entornApp);

            } catch (Exception e) {
                log.error("Error en l'execució del procés d'obtenció de informació de salut per l'entornApp {}", entornApp.getId(), e);
            }
        }
    }

    public void cancelarTascaExistent(Long entornAppId) {
        ScheduledFuture<?> tasca = tasquesActives.get(entornAppId);
        if (tasca != null) {
            tasca.cancel(false);
            tasquesActives.remove(entornAppId);
            log.info("Tasca de obtenció de informació de salut cancel·lada per l'entornAppId: {}", entornAppId);
        }
    }

    private boolean isLeader() {
        // TODO: Implementar per microserveis
        return schedulerLeader;
    }



    // Cada hora comprovarem que no hi hagi cap aplicacio-entorn que no s'estigui actualitzant
    @Scheduled(cron = "0 5 */1 * * *")
    public void comprovarRefrescInfo() {
        log.debug("Comprovant refresc periòdic dels entorn-app - Salut");
        List<EntornApp> entornAppsActives = salutClientHelper.entornAppFindByActivaTrue();
        entornAppsActives.forEach(ea -> {
            ScheduledFuture<?> tasca = tasquesActives.get(ea.getId());
            if (tasca == null) {
                programarTasca(ea);
            }
        });
    }

}
