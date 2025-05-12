package es.caib.comanda.salut.logic.service;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.KeycloakHelper;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class SalutSchedulerService {

    private final TaskScheduler taskScheduler;
    private final EntornAppServiceClient entornAppServiceClient;
    private final SalutInfoHelper salutInfoHelper;
    private final KeycloakHelper keycloakHelper;

    private final Map<Long, ScheduledFuture<?>> tasquesActives = new ConcurrentHashMap<>();

    public SalutSchedulerService(
            @Qualifier("salutTaskScheduler") TaskScheduler taskScheduler,
            EntornAppServiceClient entornAppServiceClient,
            SalutInfoHelper salutInfoHelper,
            KeycloakHelper keycloakHelper) {
        this.taskScheduler = taskScheduler;
        this.entornAppServiceClient = entornAppServiceClient;
        this.salutInfoHelper = salutInfoHelper;
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
        try {
            log.info("Executant procés per l'entornApp {}", entornApp.getId());

            // Obtenció de informació de salut per entorn-app
            salutInfoHelper.getSalutInfo(entornApp);

        } catch (Exception e) {
            log.error("Error en l'execució del procés de refresc de la informació per l'entornApp {}", entornApp.getId(), e);
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
    @Scheduled(cron = "0 5 */1 * * *")
    public void comprovarRefrescInfo() {
        log.debug("Comprovant refresc periòdic dels entorn-app - Salut");
        List<EntornApp> entornAppsActives = entornAppFindByActivaTrue();
        entornAppsActives.forEach(ea -> {
            ScheduledFuture<?> tasca = tasquesActives.get(ea.getId());
            if (tasca == null) {
                programarTasca(ea);
            }
        });
    }

}
