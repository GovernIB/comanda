package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.repository.EntornAppHistRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import es.caib.comanda.ms.logic.helper.SchedulerTaskRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConfiguracioSchedulerService {

    private static final String TASK_ID = "CONFIGURACIO_REFRESC";
    private static final String TASK_ID_HIST = "CONFIGURACIO_HIST_NETEJA";

    private final EntornAppRepository entornAppRepository;
    private final EntornAppHistRepository entornAppHistRepository;
    private final AppInfoHelper appInfoHelper;
    private final ParametresHelper parametresHelper;
    private final TaskExecutor configuracioWorkerExecutor;
    private final SchedulerTaskRegistryService schedulerTaskRegistry;

    @Value("${" + BaseConfig.PROP_SCHEDULER_LEADER + ":#{true}}")
    private Boolean schedulerLeader;
    @Value("${" + BaseConfig.PROP_SCHEDULER_BACK + ":#{false}}")
    private Boolean schedulerBack;

    public ConfiguracioSchedulerService(
            EntornAppRepository entornAppRepository,
            EntornAppHistRepository entornAppHistRepository,
            AppInfoHelper appInfoHelper,
            ParametresHelper parametresHelper,
            @Qualifier("configuracioWorkerExecutor") TaskExecutor configuracioWorkerExecutor,
            SchedulerTaskRegistryService schedulerTaskRegistry) {
        this.entornAppRepository = entornAppRepository;
        this.entornAppHistRepository = entornAppHistRepository;
        this.appInfoHelper = appInfoHelper;
        this.parametresHelper = parametresHelper;
        this.configuracioWorkerExecutor = configuracioWorkerExecutor;
        this.schedulerTaskRegistry = schedulerTaskRegistry;
    }

    @PostConstruct
    public void registrarTasques() {
        schedulerTaskRegistry.registerCron(TASK_ID, "Refresc de configuració", "Actualitza la informació de configuració de les aplicacions", "30 0/15 * * * *", this::scheduledConfiguracioTasks);
        schedulerTaskRegistry.registerCron(TASK_ID_HIST, "Neteja d'historial de configuració", "Elimina els registres antics d'historial de l'entorn-app de configuració", "30 10 0 * * *", this::scheduledConfiguracioEntornAppHistTasks);
    }

    @Scheduled(cron = "30 0/15 * * * *")
    public void scheduledConfiguracioTasks() {
        schedulerTaskRegistry.recordStart(TASK_ID);
        if (!isLeader()) {
            log.debug("Refresc de configuració ignorada: aquesta instància no és leader per als schedulers");
            schedulerTaskRegistry.recordSuccess(TASK_ID);
            return;
        }

        try {
            List<EntornAppEntity> entornAppsActives = entornAppRepository.findByActivaTrueAndAppActivaTrue();
            if (entornAppsActives.isEmpty()) {
                log.debug("No hi ha cap entorn-app activa per a les tasques de configuració");
                schedulerTaskRegistry.recordSuccess(TASK_ID);
                return;
            }
            var entornAppProjections = entornAppsActives.stream()
                    .map(ea -> new AppInfoHelper.AppInfoEntornAppProjection(ea.getId(), ea.getInfoUrl(),
                            ea.isSalutAuth(), ea.getApp().getNom(), ea.getEntorn().getNom(),
                            ea.isParametreAuth(), ea.getNomUsuariAuth(), ea.getContrasenyaAuth()))
                    .collect(Collectors.toList());
            var entornAppIds = entornAppProjections.stream().map(ea -> ea.getId().toString()).collect(Collectors.joining(", "));
            log.debug("Es van a executar les tasques de configuració per {} entorn-apps: {}", entornAppsActives.size(), entornAppIds);
            entornAppProjections.forEach(this::executarProces);
            schedulerTaskRegistry.recordSuccess(TASK_ID);
        } catch (Exception e) {
            schedulerTaskRegistry.recordError(TASK_ID);
            throw e;
        }
    }

    private void executarProces(AppInfoHelper.AppInfoEntornAppProjection entornApp) {
        Long entornAppId = entornApp.getId();
        try {
            configuracioWorkerExecutor.execute(() -> {
                try {
                    log.debug("Executant procés de refresc de la informació per l'entornApp {}", entornAppId);
                    // Refrescar informació de entorn-app
                    appInfoHelper.refreshAppInfo(entornApp);
                } catch (Exception e) {
                    log.error("Error en l'execució del procés de refresc de la informació per l'entornApp {}", entornAppId, e);
                }
            });
        } catch (TaskRejectedException e) {
            log.error("Error en programar la tasca al worker de configuració per l'entornApp: {}.", entornAppId, e);
        }
    }

    private boolean isLeader() {
        // TODO: Implementar per microserveis
        return schedulerLeader && schedulerBack;
    }

    @Scheduled(cron = "30 10 0 * * *")
    public void scheduledConfiguracioEntornAppHistTasks() {
        schedulerTaskRegistry.recordStart(TASK_ID_HIST);
        try {
            if (!isLeader()) {
                log.debug("Metode diari per a entornAppHist ignorat: aquesta instància no és leader per als schedulers");
                schedulerTaskRegistry.recordSuccess(TASK_ID_HIST);
                return;
            }
            Integer retencioDiesHistoric = parametresHelper.getParametreEnter(BaseConfig.PROP_CONFIG_ENTORN_APP_HIST_RETENCIO_DIES, 30);
            if (retencioDiesHistoric != null && retencioDiesHistoric > 0) {
                LocalDateTime menysRetencioHistoric = LocalDateTime.now().minusDays(retencioDiesHistoric.longValue());
                long eliminats = entornAppHistRepository.deleteByDataBefore(menysRetencioHistoric);
                log.info("Neteja d'entornAppHist finalitzada. Eliminats {} registres anteriors a {}", eliminats, menysRetencioHistoric);
            }
            schedulerTaskRegistry.recordSuccess(TASK_ID_HIST);
        } catch (Exception e) {
            log.error("Error executant la neteja d'entornAppHist", e);
            schedulerTaskRegistry.recordError(TASK_ID_HIST);
        }
    }

}
