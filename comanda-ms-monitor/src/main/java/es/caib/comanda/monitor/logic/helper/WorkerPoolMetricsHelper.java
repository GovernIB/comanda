package es.caib.comanda.monitor.logic.helper;

import es.caib.comanda.monitor.logic.intf.model.performance.WorkerPoolType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Descobreix i recull mètriques natives de tots els pools de workers Spring
 * ({@link ThreadPoolTaskExecutor}, {@link ThreadPoolTaskScheduler} i {@link ThreadPoolExecutor}).
 */
@Component
@Slf4j
public class WorkerPoolMetricsHelper {

    @Autowired(required = false)
    private Map<String, Executor> executors;

    @Autowired(required = false)
    private Map<String, TaskScheduler> schedulers;

    /**
     * Recull una instantània actual de tots els pools de workers detectats al context.
     */
    public List<RawWorkerPoolSample> collectAll() {
        List<RawWorkerPoolSample> samples = new ArrayList<>();
        Set<ThreadPoolExecutor> seenExecutors = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<String> seenNames = new HashSet<>();

        // Escanejar schedulers primer perquè ThreadPoolTaskScheduler implementa també Executor
        if (schedulers != null && !schedulers.isEmpty()) {
            for (Map.Entry<String, TaskScheduler> entry : schedulers.entrySet()) {
                collectCandidate(entry.getKey(), entry.getValue(), seenExecutors, seenNames, samples);
            }
        }

        if (executors != null && !executors.isEmpty()) {
            for (Map.Entry<String, Executor> entry : executors.entrySet()) {
                collectCandidate(entry.getKey(), entry.getValue(), seenExecutors, seenNames, samples);
            }
        }

        samples.sort(Comparator
                .<RawWorkerPoolSample, Integer>comparing(s -> s.getPoolType() == WorkerPoolType.EXECUTOR ? 0 : 1)
                .thenComparing(s -> s.getPoolName() != null ? s.getPoolName() : "", String.CASE_INSENSITIVE_ORDER));
        return samples;
    }

    private void collectCandidate(
            String beanName,
            Object candidate,
            Set<ThreadPoolExecutor> seenExecutors,
            Set<String> seenNames,
            List<RawWorkerPoolSample> out) {
        if (beanName == null || candidate == null || seenNames.contains(beanName)) {
            return;
        }

        ThreadPoolExecutor tpe;
        WorkerPoolType poolType;
        String threadNamePrefix = null;

        try {
            if (candidate instanceof ThreadPoolTaskScheduler) {
                ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) candidate;
                tpe = scheduler.getScheduledThreadPoolExecutor();
                poolType = WorkerPoolType.SCHEDULER;
                threadNamePrefix = scheduler.getThreadNamePrefix();
            } else if (candidate instanceof ThreadPoolTaskExecutor) {
                ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) candidate;
                tpe = taskExecutor.getThreadPoolExecutor();
                poolType = WorkerPoolType.EXECUTOR;
                threadNamePrefix = taskExecutor.getThreadNamePrefix();
            } else if (candidate instanceof ScheduledThreadPoolExecutor) {
                tpe = (ScheduledThreadPoolExecutor) candidate;
                poolType = WorkerPoolType.SCHEDULER;
            } else if (candidate instanceof ThreadPoolExecutor) {
                tpe = (ThreadPoolExecutor) candidate;
                poolType = WorkerPoolType.EXECUTOR;
            } else {
                return;
            }
        } catch (IllegalStateException e) {
            log.debug("El pool de workers {} encara no està inicialitzat: {}", beanName, e.getMessage());
            return;
        } catch (Exception e) {
            log.debug("Error accedint al pool de workers {}: {}", beanName, e.getMessage());
            return;
        }

        if (tpe == null || !seenExecutors.add(tpe) || !seenNames.add(beanName)) {
            return;
        }

        int coreSize = tpe.getCorePoolSize();
        // ScheduledThreadPoolExecutor retorna Integer.MAX_VALUE per getMaximumPoolSize()
        int rawMax = tpe.getMaximumPoolSize();
        int maxSize = (poolType == WorkerPoolType.SCHEDULER || rawMax == Integer.MAX_VALUE)
                ? coreSize
                : rawMax;

        RawWorkerPoolSample sample = new RawWorkerPoolSample();
        sample.setPoolKey("worker:" + beanName);
        sample.setPoolName(beanName);
        sample.setThreadNamePrefix(threadNamePrefix);
        sample.setPoolType(poolType);
        sample.setCoreSize(coreSize);
        sample.setMaxSize(maxSize);
        sample.setPoolSize(tpe.getPoolSize());
        sample.setActiveCount(tpe.getActiveCount());

        BlockingQueue<?> queue = tpe.getQueue();
        sample.setQueueSize(resolveQueueSize(queue, poolType));
        sample.setQueueCapacity(resolveQueueCapacity(queue));
        sample.setRawCompletedTaskCount(tpe.getCompletedTaskCount());
        out.add(sample);
    }

    /**
     * Per a executors retorna la mida total de la cua.
     * Per a schedulers, la DelayedWorkQueue conté també les execucions futures programades;
     * només comptem les tasques no cancel·lades ja vençudes (delay <= 0) que realment esperen fil.
     */
    static int resolveQueueSize(BlockingQueue<?> queue, WorkerPoolType poolType) {
        if (queue == null) {
            return 0;
        }
        if (poolType != WorkerPoolType.SCHEDULER) {
            return queue.size();
        }
        int overdue = 0;
        for (Object item : queue) {
            // Ignorem tasques cancel·lades pendents de purgar de la DelayedWorkQueue
            if (item instanceof Future<?> && ((Future<?>) item).isCancelled()) {
                continue;
            }
            // Només comptem com a cua real les tasques que ja haurien d'estar executant-se
            if (item instanceof Delayed) {
                if (((Delayed) item).getDelay(TimeUnit.NANOSECONDS) <= 0L) {
                    overdue++;
                }
            } else {
                overdue++;
            }
        }
        return overdue;
    }

    static Integer resolveQueueCapacity(BlockingQueue<?> queue) {
        if (queue == null) {
            return null;
        }
        int remaining = queue.remainingCapacity();
        if (remaining == Integer.MAX_VALUE) {
            return null;
        }
        long total = (long) queue.size() + (long) remaining;
        return total >= Integer.MAX_VALUE ? null : (int) total;
    }

    @Data
    public static class RawWorkerPoolSample {
        private String poolKey;
        private String poolName;
        private String threadNamePrefix;
        private WorkerPoolType poolType;
        private Integer coreSize;
        private Integer maxSize;
        private Integer poolSize;
        private Integer activeCount;
        private Integer queueSize;
        private Integer queueCapacity;
        private Long rawCompletedTaskCount;
    }
}
