package es.caib.comanda.salut.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SalutSchedulerService {

    private final TaskScheduler taskScheduler;
    private final SalutClientHelper salutClientHelper;
    private final SalutInfoHelper salutInfoHelper;

    @Value("${" + BaseConfig.PROP_SCHEDULER_LEADER + ":#{true}}")
    private Boolean schedulerLeader;
    @Value("${" + BaseConfig.PROP_SCHEDULER_BACK + ":#{false}}")
    private Boolean schedulerBack;

    private static final Integer PERIODE_CONSULTA_SALUT = 1;

    private final Map<Long, ScheduledFuture<?>> tasquesActives = new ConcurrentHashMap<>();

    public SalutSchedulerService(
            @Qualifier("salutTaskScheduler") TaskScheduler taskScheduler,
            SalutClientHelper salutClientHelper,
            SalutInfoHelper salutInfoHelper,
            HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper) {
        this.taskScheduler = taskScheduler;
        this.salutClientHelper = salutClientHelper;
        this.salutInfoHelper = salutInfoHelper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void inicialitzarTasques() {
        if (!isLeader()) {
            log.warn("Inicialització de tasques de salut ignorada: aquesta instància no és leader per als schedulers");
            return;
        }

        // Esperarem 1 minut a inicialitzar les tasques en segon pla
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        long currentTimeMillis = System.currentTimeMillis();
        long nextMinute = (currentTimeMillis / 60000 + 1) * 60000;
        long delayMillis = nextMinute - currentTimeMillis;

        executor.schedule(() -> {
            try {
                List<EntornApp> entornAppsActives = salutClientHelper.entornAppFindByActivaTrue();
                if (entornAppsActives.isEmpty()) {
                    log.debug("No hi ha cap entorn-app activa per a programar les tasques de salut");
                    return;
                }
                var entornAppIds = entornAppsActives.stream().map(ea -> ea.getId().toString()).collect(Collectors.joining(", "));
                log.debug("Es van a programar les tasques de salut per {} entorn-apps: {}", entornAppsActives.size(), entornAppIds);
                entornAppsActives.forEach(this::programarTasca);
            } finally {
                executor.shutdown();
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    public void programarTasca(EntornApp entornApp) {
        log.debug("Programar tasca de salut per l'entornApp: {}", entornApp.getId());
        // Cancel·lem la tasca existent si existeix
        cancelarTascaExistent(entornApp.getId());

        if (!entornApp.isActiva()) {
            log.debug("Tasca de obtenció de informació de salut no programada per l'entornApp: {}, degut a que no està activa", entornApp.getId());
            return;
        }

        try {
//            PeriodicTrigger periodicTrigger = new PeriodicTrigger(TimeUnit.MINUTES.toMillis(entornApp.getSalutInterval()), TimeUnit.MILLISECONDS);
            PeriodicTrigger periodicTrigger = new PeriodicTrigger(TimeUnit.MINUTES.toMillis(PERIODE_CONSULTA_SALUT), TimeUnit.MILLISECONDS);
            periodicTrigger.setFixedRate(true);

            ScheduledFuture<?> futuraTasca = taskScheduler.schedule(
                    () -> executarProces(entornApp),
                    periodicTrigger
            );

            tasquesActives.put(entornApp.getId(), futuraTasca);
            log.debug("Tasca programada de obtenció de informació de salut per l'entornApp: {}, amb període: {}",
                    entornApp.getId(),
                    PERIODE_CONSULTA_SALUT);
        } catch (IllegalArgumentException e) {
            log.error("Error en programar la tasca de obtenció de informació de salut per l'entornApp: {}. Període invàlid: {}",
                    entornApp.getId(),
                    PERIODE_CONSULTA_SALUT,
                    e);
        }
    }

    private void executarProces(EntornApp entornApp) {
        if (isLeader()) {
            try {
                log.debug("Executant procés per l'entornApp {}", entornApp.getId());
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
        return schedulerLeader && schedulerBack;
    }

    // Cada hora comprovarem que no hi hagi cap aplicacio-entorn que no s'estigui actualitzant
    @Scheduled(cron = "0 5 */1 * * *")
    public void comprovarRefrescInfo() {
        if (!isLeader()) {
            log.debug("Comprovació de refresc de salut ignorada: aquesta instància no és leader per als schedulers");
            return;
        }
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
