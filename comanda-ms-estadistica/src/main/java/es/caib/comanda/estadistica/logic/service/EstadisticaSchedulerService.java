package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.helper.CompactacioHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaHelper;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EstadisticaSchedulerService {

    private final TaskScheduler taskScheduler;
    private final EstadisticaHelper estadisticaHelper;
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final CompactacioHelper compactacioHelper;
    private final ParametresHelper parametresHelper;

    @Value("${" + BaseConfig.PROP_SCHEDULER_LEADER + ":#{true}}")
    private Boolean schedulerLeader;

    private final Map<Long, ScheduledFuture<?>> tasquesActives = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> tasquesCompactarActives = new ConcurrentHashMap<>();

    public EstadisticaSchedulerService(
            @Qualifier("estadisticaTaskScheduler") TaskScheduler taskScheduler,
            EstadisticaHelper estadisticaHelper,
            EstadisticaClientHelper estadisticaClientHelper,
            CompactacioHelper compactacioHelper,
            ParametresHelper parametresHelper) {
        this.taskScheduler = taskScheduler;
        this.estadisticaHelper = estadisticaHelper;
        this.estadisticaClientHelper = estadisticaClientHelper;
        this.compactacioHelper = compactacioHelper;
        this.parametresHelper = parametresHelper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void inicialitzarTasques() {
        // Esperarem 1 minut a inicialitzar les tasques en segon pla
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            try {
                List<EntornApp> entornAppsActives = estadisticaClientHelper.entornAppFindByActivaTrue();
                entornAppsActives.forEach(this::programarTasques);
            } finally {
                executor.shutdown();
            }
        }, 1, TimeUnit.MINUTES);
    }

    public void programarTasques(EntornApp entornApp) {
        if (!CronExpression.isValidExpression(entornApp.getEstadisticaCron())) {
            log.warn("EntornApp " + entornApp.getId() + ":" + entornApp.getEstadisticaCron() + " no és un cron vàlid.");
            return;
        }

        // Cancel·lem la tasca existent si existeix
        cancelarTascaExistent(entornApp.getId());
        cancelarTascaCompactatExistent(entornApp.getId());

        if (!entornApp.isActiva()) {
            log.info("Tasca de refresc de la informació no programada per l'entornApp: {}, degut a que no està activa", entornApp.getId());
            return;
        }

        try {
            ScheduledFuture<?> futuraTasca = taskScheduler.schedule(
                    () -> executarProces(entornApp),
                    new CronTrigger(entornApp.getEstadisticaCron())
            );

            tasquesActives.put(entornApp.getId(), futuraTasca);
            log.info("Tasca programada de refresc de la informació per l'entornApp: {}, amb cron: {}",
                    entornApp.getId(),
                    entornApp.getEstadisticaCron());
        } catch (IllegalArgumentException e) {
            log.error("Error en programar la tasca de refresc de la informació per l'entornApp: {}. Cron invàlid: {}",
                    entornApp.getId(),
                    entornApp.getEstadisticaCron(),
                    e);
        }

        // Compactat d'estadístiques
        try {
            Boolean compactarActiu = parametresHelper.getParametreBoolean(BaseConfig.PROP_STATS_COMPACTAR_ACTIU, false);
            if (compactarActiu) {
                String compactarCron = parametresHelper.getParametreText(BaseConfig.PROP_STATS_COMPACTAR_CRON, "0 0 3 * * *");

                ScheduledFuture<?> futuraTascaCompactacio = taskScheduler.schedule(
                        () -> executarProcesCompactacio(entornApp),
                        new CronTrigger(compactarCron)
                );
            }
        } catch (Exception e) {
            log.error("Error en programar la tasca de compactat d'estadístiques per l'entornApp: {}.", entornApp.getId(), e);
        }
    }

    private void executarProces(EntornApp entornApp) {
        if (isLeader()) {
            try {
                log.info("Executant procés d'obtenció de dades estadístiques per l'entornApp {}", entornApp.getId());

                // Refrescar informació estadística de entorn-app
                estadisticaHelper.getEstadisticaInfoDades(entornApp);

            } catch (Exception e) {
                log.error("Error en l'execució del procés d'obtenció de dades estadístiques per l'entornApp {}", entornApp.getId(), e);
            }
        }
    }

    public void cancelarTascaExistent(Long entornAppId) {
        ScheduledFuture<?> tascaInfo = tasquesActives.get(entornAppId);
        if (tascaInfo != null) {
            tascaInfo.cancel(false);
            tasquesActives.remove(entornAppId);
            log.info("Tasca de obtenció d'informació estadística cancel·lada per l'entornAppId: {}", entornAppId);
        }
    }

    private void executarProcesCompactacio(EntornApp entornApp) {
        if (isLeader()) {
            try {
                log.info("Executant procés de compactació de dades estadístiques per l'entornApp {}", entornApp.getId());

                // Compactació de dades estadístiques de entorn-app
                compactacioHelper.compactar(entornApp);

            } catch (Exception e) {
                log.error("Error en l'execució del procés de compactació de dades estadístiques per l'entornApp {}", entornApp.getId(), e);
            }
        }
    }

    public void cancelarTascaCompactatExistent(Long entornAppId) {
        ScheduledFuture<?> tascaInfo = tasquesCompactarActives.get(entornAppId);
        if (tascaInfo != null) {
            tascaInfo.cancel(false);
            tasquesCompactarActives.remove(entornAppId);
            log.info("Tasca de compactació d'estadístiques cancel·lada per l'entornAppId: {}", entornAppId);
        }
    }

    private boolean isLeader() {
        // TODO: Implementar per microserveis
        return schedulerLeader;
    }


    // Cada hora comprovarem que no hi hagi cap aplicacio-entorn que no s'estigui actualitzant
    @Scheduled(cron = "0 10 */1 * * *")
    public void comprovarRefrescInfo() {
        log.debug("Comprovant refresc periòdic dels entorn-app - Estadístiques");
        List<EntornApp> entornAppsActives = estadisticaClientHelper.entornAppFindByActivaTrue();
        entornAppsActives.forEach(ea -> {
            ScheduledFuture<?> tasca = tasquesActives.get(ea.getId());
            if (tasca == null) {
                programarTasques(ea);
            }
        });
    }

}
