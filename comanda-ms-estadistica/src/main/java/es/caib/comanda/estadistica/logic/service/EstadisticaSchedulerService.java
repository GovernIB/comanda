package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.helper.EstadisticaHelper;
import es.caib.comanda.ms.logic.helper.KeycloakHelper;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EstadisticaSchedulerService {

    private final TaskScheduler taskScheduler;
    private final EntornAppServiceClient entornAppServiceClient;
    private final EstadisticaHelper estadisticaHelper;
    private final KeycloakHelper keycloakHelper;

    @Value("${" + BaseConfig.PROP_SCHEDULER_LEADER + ":#{false}}")
    private Boolean schedulerLeader;

    private final Map<Long, ScheduledFuture<?>> tasquesActives = new ConcurrentHashMap<>();

    public EstadisticaSchedulerService(
            @Qualifier("estadisticaTaskScheduler") TaskScheduler taskScheduler,
            EntornAppServiceClient entornAppServiceClient,
            EstadisticaHelper estadisticaHelper,
            KeycloakHelper keycloakHelper) {
        this.taskScheduler = taskScheduler;
        this.entornAppServiceClient = entornAppServiceClient;
        this.estadisticaHelper = estadisticaHelper;
        this.keycloakHelper = keycloakHelper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void inicialitzarTasques() {
        List<EntornApp> entornAppsActives = entornAppFindByActivaTrue();
        entornAppsActives.forEach(this::programarTasca);
    }

    public void programarTasca(EntornApp entornApp) {
        // Cancel·lem la tasca existent si existeix
        cancelarTascaExistent(entornApp.getId());

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
    }

    private void executarProces(EntornApp entornApp) {
        if (isLeader()) {
            try {
                log.info("Executant procés per l'entornApp {}", entornApp.getId());

                // Refrescar informació estadística de entorn-app
                estadisticaHelper.getEstadisticaInfoDades(entornApp);

            } catch (Exception e) {
                log.error("Error en l'execució del procés de refresc de la informació per l'entornApp {}", entornApp.getId(), e);
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

    private boolean isLeader() {
        // TODO: Implementar per microserveis
        return schedulerLeader;
    }


    private List<EntornApp> entornAppFindByActivaTrue() {
        PagedModel<EntityModel<EntornApp>> entornApps = entornAppServiceClient.find(
                null,
                "activa:true and app.activa:true",
                null,
                null,
                "UNPAGED",
                null,
                keycloakHelper.getAuthorizationHeader());
        return entornApps.getContent().stream().
                map(EntityModel::getContent).
                collect(Collectors.toList());
    }


    // Cada hora comprovarem que no hi hagi cap aplicacio-entorn que no s'estigui actualitzant
    @Scheduled(cron = "0 10 */1 * * *")
    public void comprovarRefrescInfo() {
        log.debug("Comprovant refresc periòdic dels entorn-app - Estadístiques");
        List<EntornApp> entornAppsActives = entornAppFindByActivaTrue();
        entornAppsActives.forEach(ea -> {
            ScheduledFuture<?> tasca = tasquesActives.get(ea.getId());
            if (tasca == null) {
                programarTasca(ea);
            }
        });
    }

}
