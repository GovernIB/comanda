package es.caib.comanda.salut.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import es.caib.comanda.ms.salut.helper.MonitorHelper;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SalutSchedulerService {

    private final SalutClientHelper salutClientHelper;
    private final SalutInfoHelper salutInfoHelper;
    private final ParametresHelper parametresHelper;
    private final TaskExecutor salutWorkerExecutor;

    @Value("${" + BaseConfig.PROP_SCHEDULER_LEADER + ":#{true}}")
    private Boolean schedulerLeader;
    @Value("${" + BaseConfig.PROP_SCHEDULER_BACK + ":#{false}}")
    private Boolean schedulerBack;

    private final Map<Long, AtomicBoolean> tasquesEnExecucio = new ConcurrentHashMap<>();

    public SalutSchedulerService(
            SalutClientHelper salutClientHelper,
            SalutInfoHelper salutInfoHelper,
            ParametresHelper parametresHelper,
            @Qualifier("salutWorkerExecutor") TaskExecutor salutWorkerExecutor) {
        this.salutClientHelper = salutClientHelper;
        this.salutInfoHelper = salutInfoHelper;
        this.parametresHelper = parametresHelper;
        this.salutWorkerExecutor = salutWorkerExecutor;
    }

    private void executarProces(EntornApp entornApp) {
        // Guard anti-solapament per entorn: si ja hi ha una execució pendent o en curs, saltam
        AtomicBoolean flag = tasquesEnExecucio.computeIfAbsent(entornApp.getId(), k -> new AtomicBoolean(false));
        if (!flag.compareAndSet(false, true)) {
            log.debug("S'ignora execució de salut solapada per l'entornApp {} (ja en curs)", entornApp.getId());
            return;
        }

        // Encuar el treball al worker executor per no bloquejar el scheduler i no perdre execucions
        try {
            salutWorkerExecutor.execute(() -> {
                try {
                    log.debug("Executant procés per l'entornApp {}", entornApp.getId());
                    salutInfoHelper.getSalutInfo(entornApp);
                } catch (Exception e) {
                    log.error("Error en l'execució del procés d'obtenció de informació de salut per l'entornApp {}", entornApp.getId(), e);
                } finally {
                    flag.set(false);
                }
            });
        } catch (RejectedExecutionException rex) {
            log.warn("Worker saturat, s'ignora execució de salut per l'entornApp {} aquest minut: {}", entornApp.getId(), rex.getMessage());
            flag.set(false);
        }
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

    // Informe periòdic cada 5 minuts de l'estat del sistema i del worker
    @Scheduled(cron = "20 */5 * * * *")
    public void informePeriodicSistema() {
        if (!isLeader()) {
            return;
        }
        Boolean generarLogReport = parametresHelper.getParametreBoolean(BaseConfig.PROP_SALUT_LOG_REPORT, false);
        if (!generarLogReport) {
            return;
        }

        try {
            // Informació del sistema
            MonitorHelper.CpuUsage cpu = MonitorHelper.getCpuUsage();
            MonitorHelper.MemoryUsage jvm = MonitorHelper.getJvmMemory();
            MonitorHelper.MemoryUsage phy = MonitorHelper.getPhisicalMemory();
            MonitorHelper.DiskUsage disk = MonitorHelper.getRootDiskUsage();
            MonitorHelper.SystemInfo sys = MonitorHelper.getSystemInfo();

            // Informació del worker executor
            Integer poolSize = null;
            Integer active = null;
            Integer core = null;
            Integer max = null;
            Integer queueSize = null;
            if (salutWorkerExecutor instanceof ThreadPoolTaskExecutor) {
                ThreadPoolTaskExecutor ex = (ThreadPoolTaskExecutor) salutWorkerExecutor;
                poolSize = ex.getPoolSize();
                active = ex.getActiveCount();
                core = ex.getCorePoolSize();
                max = ex.getMaxPoolSize();
                ThreadPoolExecutor tpe = ex.getThreadPoolExecutor();
                if (tpe != null && tpe.getQueue() != null) {
                    queueSize = tpe.getQueue().size();
                }
            }

            log.info("[SALUT REPORT] System: cores={}, loadAvg={}, sysCpu={}, procCpu={}, uptime={}, start={}, JVM: used={}, total={}, Phys: used={}, total={}, Disk '/': used={}, total={}, Worker: pool={}/{}, active={}, queue={}",
                    cpu != null ? cpu.getCores() : null,
                    cpu != null ? cpu.getFormatedLoadAverage() : null,
                    cpu != null ? cpu.getFormatedSystemCpuLoad() : null,
                    cpu != null ? cpu.getFormatedProcessCpuLoad() : null,
                    sys != null ? sys.getFormatedUpTime() : null,
                    sys != null ? sys.getFormatedStartTime() : null,
                    jvm != null ? jvm.getFormatedUsedMemory() : null,
                    jvm != null ? jvm.getFormatedTotalMemory() : null,
                    phy != null ? phy.getFormatedUsedMemory() : null,
                    phy != null ? phy.getFormatedTotalMemory() : null,
                    disk != null ? disk.getFormatedUsedSpace() : null,
                    disk != null ? disk.getFormatedTotalSpace() : null,
                    core != null && max != null ? (core + "/" + max) : null,
                    active,
                    queueSize);
        } catch (Throwable t) {
            log.warn("Error generant l'informe periòdic de sistema", t);
        }
    }

}
