package es.caib.comanda.ms.logic.helper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SchedulerTaskRegistryService {

    private final Map<String, SchedulerTaskEntry> registry = new ConcurrentHashMap<>();

    public void registerCron(String id, String nom, String descripcio, String cronExpressio, Runnable executor) {
        registry.put(id, new SchedulerTaskEntry(id, nom, descripcio, cronExpressio, null, executor));
    }

    public void registerFixedRate(String id, String nom, String descripcio, long fixedRateMinuts, Runnable executor) {
        registry.put(id, new SchedulerTaskEntry(id, nom, descripcio, null, fixedRateMinuts, executor));
    }

    public void recordStart(String id) {
        SchedulerTaskEntry entry = registry.get(id);
        if (entry != null) {
            entry.ultimaExecucio = LocalDateTime.now();
            entry.ultimaExecucioStartMs = System.currentTimeMillis();
            entry.ultimaEstat = null;
            entry.ultimaDuracio = null;
        }
    }

    public void recordSuccess(String id) {
        SchedulerTaskEntry entry = registry.get(id);
        if (entry != null) {
            entry.ultimaEstat = "OK";
            if (entry.ultimaExecucioStartMs != null) {
                entry.ultimaDuracio = System.currentTimeMillis() - entry.ultimaExecucioStartMs;
            }
        }
    }

    public void recordError(String id) {
        SchedulerTaskEntry entry = registry.get(id);
        if (entry != null) {
            entry.ultimaEstat = "ERROR";
            if (entry.ultimaExecucioStartMs != null) {
                entry.ultimaDuracio = System.currentTimeMillis() - entry.ultimaExecucioStartMs;
            }
        }
    }

    public void trigger(String id) {
        SchedulerTaskEntry entry = registry.get(id);
        if (entry != null && entry.executor != null) {
            log.info("Reiniciant tasca en segon pla: {}", id);
            CompletableFuture.runAsync(entry.executor);
        }
    }

    public List<SchedulerTaskEntry> getAll() {
        return new ArrayList<>(registry.values());
    }

    public Optional<SchedulerTaskEntry> getById(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    @Getter
    public static class SchedulerTaskEntry {
        private final String id;
        private final String nom;
        private final String descripcio;
        private final String cronExpressio;
        private final Long fixedRateMinuts;
        private final Runnable executor;
        private volatile LocalDateTime ultimaExecucio;
        private volatile Long ultimaExecucioStartMs;
        private volatile String ultimaEstat;
        private volatile Long ultimaDuracio;

        SchedulerTaskEntry(String id, String nom, String descripcio, String cronExpressio, Long fixedRateMinuts, Runnable executor) {
            this.id = id;
            this.nom = nom;
            this.descripcio = descripcio;
            this.cronExpressio = cronExpressio;
            this.fixedRateMinuts = fixedRateMinuts;
            this.executor = executor;
        }

        public LocalDateTime getProxExecucio() {
            if (cronExpressio != null) {
                try {
                    CronExpression cron = CronExpression.parse(cronExpressio);
                    ZonedDateTime next = cron.next(ZonedDateTime.now(ZoneId.systemDefault()));
                    return next != null ? next.toLocalDateTime() : null;
                } catch (Exception e) {
                    return null;
                }
            }
            if (fixedRateMinuts != null) {
                LocalDateTime base = ultimaExecucio != null ? ultimaExecucio : LocalDateTime.now();
                return base.plusMinutes(fixedRateMinuts);
            }
            return null;
        }

        public String getExpressioHuman() {
            if (cronExpressio != null) return "cron: " + cronExpressio;
            if (fixedRateMinuts != null) return "cada " + fixedRateMinuts + " minuts";
            return null;
        }
    }
}
