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
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EstadisticaSchedulerService {

    private final EstadisticaHelper estadisticaHelper;
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final CompactacioHelper compactacioHelper;
    private final ParametresHelper parametresHelper;
    private final TaskExecutor estadisticaWorkerExecutor;

    @Value("${" + BaseConfig.PROP_SCHEDULER_LEADER + ":#{true}}")
    private Boolean schedulerLeader;
    @Value("${" + BaseConfig.PROP_SCHEDULER_BACK + ":#{false}}")
    private Boolean schedulerBack;

    public EstadisticaSchedulerService(
            EstadisticaHelper estadisticaHelper,
            EstadisticaClientHelper estadisticaClientHelper,
            CompactacioHelper compactacioHelper,
            ParametresHelper parametresHelper,
            @Qualifier("estadisticaWorkerExecutor") TaskExecutor estadisticaWorkerExecutor) {
        this.estadisticaHelper = estadisticaHelper;
        this.estadisticaClientHelper = estadisticaClientHelper;
        this.compactacioHelper = compactacioHelper;
        this.parametresHelper = parametresHelper;
        this.estadisticaWorkerExecutor = estadisticaWorkerExecutor;
    }

    private void executarProces(EntornApp entornApp) {
        // Encuar el treball al worker executor per no bloquejar el scheduler i no perdre execucions
        try {
            estadisticaWorkerExecutor.execute(() -> {
                try {
                    log.info("Executant procés d'obtenció de dades estadístiques per l'entornApp {}", entornApp.getId());
                    // Refrescar informació estadística de entorn-app
                    estadisticaHelper.getEstadisticaInfoDades(entornApp);
                } catch (Exception e) {
                    log.error("Error en l'execució del procés d'obtenció de dades estadístiques per l'entornApp {}", entornApp.getId(), e);
                }
            });
        } catch (TaskRejectedException e) {
            log.error("Error en programar la tasca al worker d'estadística per l'entornApp: {}.", entornApp.getId(), e);
        }
    }
    private void executarProcesCompactacio(EntornApp entornApp) {
        // Encuar el treball al worker executor per no bloquejar el scheduler i no perdre execucions
        try {
            estadisticaWorkerExecutor.execute(() -> {
                try {
                    log.info("Executant procés de compactació de dades estadístiques per l'entornApp {}", entornApp.getId());

                    // Compactació de dades estadístiques de entorn-app
                    compactacioHelper.compactar(entornApp);

                } catch (Exception e) {
                    log.error("Error en l'execució del procés de compactació de dades estadístiques per l'entornApp {}", entornApp.getId(), e);
                }
            });

        } catch (TaskRejectedException e) {
            log.error("Error en programar la tasca al worker d'estadística (compactació) per l'entornApp: {}.", entornApp.getId(), e);
        }
    }

    private boolean isLeader() {
        // TODO: Implementar per microserveis
        return schedulerLeader && schedulerBack;
    }

    private boolean comandaCronCheck(String cronText, LocalDateTime referenceDate) throws IllegalArgumentException {
        String[] parts = StringUtils.tokenizeToStringArray(cronText, " ");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Format de cron invàlid (no s'han proporcionat tots els segments necessaris)");
        }
        int minut;
        int hora;
        try {
            minut = Integer.parseInt(parts[1]);
            hora = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Segment de cron invàlid", e);
        }
        return referenceDate.getMinute() == minut && referenceDate.getHour() == hora;
    }

    @Scheduled(cron = "0 * * * * *")
    public void scheduledEstadisticaTasks() {
        if (!isLeader()) {
            log.info("Refresc d'estadístiques ignorada: aquesta instància no és leader per als schedulers");
            return;
        }
        LocalDateTime referenceDate = LocalDateTime.now();

        List<EntornApp> entornAppsActives = estadisticaClientHelper.entornAppFindByActivaTrue();
        if (entornAppsActives.isEmpty()) {
            log.debug("No hi ha cap entorn-app activa per a les tasques d'estadístiques");
            return;
        }
        var entornAppIds = entornAppsActives.stream().map(ea -> ea.getId().toString()).collect(Collectors.joining(", "));
        log.debug("Es van a executar les tasques d'estadístiques per {} entorn-apps: {}", entornAppsActives.size(), entornAppIds);
        for (EntornApp entornApp : entornAppsActives) {
            try {
                if (comandaCronCheck(entornApp.getEstadisticaCron(), referenceDate)) {
                    executarProces(entornApp);
                }
            } catch (IllegalArgumentException e) {
                log.warn("EntornApp {}:{} no és un cron vàlid.", entornApp.getId(), entornApp.getEstadisticaCron());
            }
        }

        // Compactat d'estadístiques
        Boolean compactarActiu = parametresHelper.getParametreBoolean(BaseConfig.PROP_STATS_COMPACTAR_ACTIU, false);
        if (compactarActiu) {
            String compactarCron = parametresHelper.getParametreText(BaseConfig.PROP_STATS_COMPACTAR_CRON, "0 0 3 * * *");
            try {
                if (comandaCronCheck(compactarCron, referenceDate)) {
                    entornAppsActives.stream()
                            .filter(EntornApp::getCompactable)
                            .forEach(this::executarProcesCompactacio);
                }
            } catch (IllegalArgumentException e) {
                log.warn("Compactació d'estadístiques: {} no és un cron vàlid.", compactarCron);
            }
        }
    }
}
