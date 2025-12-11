package es.caib.comanda.salut.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SalutSchedulerService {

    private final SalutClientHelper salutClientHelper;
    private final SalutInfoHelper salutInfoHelper;
    private final TaskExecutor salutWorkerExecutor;

    @Value("${" + BaseConfig.PROP_SCHEDULER_LEADER + ":#{true}}")
    private Boolean schedulerLeader;
    @Value("${" + BaseConfig.PROP_SCHEDULER_BACK + ":#{false}}")
    private Boolean schedulerBack;

    public SalutSchedulerService(
            SalutClientHelper salutClientHelper,
            SalutInfoHelper salutInfoHelper,
            @Qualifier("salutWorkerExecutor") TaskExecutor salutWorkerExecutor) {
        this.salutClientHelper = salutClientHelper;
        this.salutInfoHelper = salutInfoHelper;
        this.salutWorkerExecutor = salutWorkerExecutor;
    }

    private void executarProces(EntornApp entornApp) {
        // Encuar el treball al worker executor per no bloquejar el scheduler i no perdre execucions
        salutWorkerExecutor.execute(() -> {
            try {
                log.debug("Executant procés per l'entornApp {}", entornApp.getId());
                salutInfoHelper.getSalutInfo(entornApp);
            } catch (Exception e) {
                log.error("Error en l'execució del procés d'obtenció de informació de salut per l'entornApp {}", entornApp.getId(), e);
            }
        });
    }

    private boolean isLeader() {
        // TODO: Implementar per microserveis
        return schedulerLeader && schedulerBack;
    }

    @Scheduled(cron = "0 * * * * *")
    public void scheduledSalutTasks() {
        if (!isLeader()) {
            log.debug("Refresc de salut ignorat: aquesta instància no és leader per als schedulers");
            return;
        }

        List<EntornApp> entornAppsActives = salutClientHelper.entornAppFindByActivaTrue();
        if (entornAppsActives.isEmpty()) {
            log.debug("No hi ha cap entorn-app activa per a les tasques de salut");
            return;
        }
        var entornAppIds = entornAppsActives.stream().map(ea -> ea.getId().toString()).collect(Collectors.joining(", "));
        log.debug("Es van a executar les tasques de salut per {} entorn-apps: {}", entornAppsActives.size(), entornAppIds);
        entornAppsActives.forEach(this::executarProces);
    }

}
