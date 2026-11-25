package es.caib.comanda.monitor.logic.helper;

import es.caib.comanda.monitor.logic.helper.WorkerPoolMetricsHelper.RawWorkerPoolSample;
import es.caib.comanda.monitor.logic.intf.model.performance.WorkerPoolType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests per a WorkerPoolMetricsHelper")
class WorkerPoolMetricsHelperTest {

    @Test
    @DisplayName("Descobreix executors i schedulers, deduplica ThreadPoolTaskScheduler, evita Integer.MAX_VALUE i ordena")
    void collectAll_descobreixIDeduplicaCorrectament() throws Exception {
        ThreadPoolTaskExecutor salutExec = new ThreadPoolTaskExecutor();
        salutExec.setCorePoolSize(5);
        salutExec.setMaxPoolSize(5);
        salutExec.setQueueCapacity(1000);
        salutExec.setThreadNamePrefix("salut-worker-");
        salutExec.initialize();

        ThreadPoolTaskExecutor alarmesExec = new ThreadPoolTaskExecutor();
        alarmesExec.setCorePoolSize(2);
        alarmesExec.setMaxPoolSize(4);
        alarmesExec.setQueueCapacity(100);
        alarmesExec.setThreadNamePrefix("alarmes-mail-");
        alarmesExec.initialize();

        ThreadPoolTaskScheduler monitorScheduler = new ThreadPoolTaskScheduler();
        monitorScheduler.setPoolSize(10);
        monitorScheduler.setThreadNamePrefix("monitor-tasques-");
        monitorScheduler.initialize();

        ThreadPoolTaskExecutor uninitialized = new ThreadPoolTaskExecutor();
        uninitialized.setCorePoolSize(3);

        ThreadPoolExecutor customPool = new ThreadPoolExecutor(
                1, 2, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(50)
        );

        try {
            CountDownLatch ran = new CountDownLatch(1);
            salutExec.execute(ran::countDown);
            assertThat(ran.await(5, TimeUnit.SECONDS)).isTrue();

            Map<String, TaskScheduler> schedulers = new LinkedHashMap<>();
            schedulers.put("monitorTaskScheduler", monitorScheduler);

            Map<String, Executor> executors = new LinkedHashMap<>();
            // El mateix scheduler apareix al mapa d'executors (com fa Spring) -> s'ha de deduplicar!
            executors.put("monitorTaskScheduler", monitorScheduler);
            executors.put("salutWorkerExecutor", salutExec);
            executors.put("alarmesMailExecutor", alarmesExec);
            executors.put("customThreadPool", customPool);
            executors.put("uninitializedExecutor", uninitialized);
            executors.put("lambdaExecutor", Runnable::run);

            WorkerPoolMetricsHelper helper = new WorkerPoolMetricsHelper();
            ReflectionTestUtils.setField(helper, "schedulers", schedulers);
            ReflectionTestUtils.setField(helper, "executors", executors);

            List<RawWorkerPoolSample> samples = helper.collectAll();

            // EXECUTOR ordenats alfabèticament abans de SCHEDULER; lambda i uninitialized omesos
            assertThat(samples).extracting(RawWorkerPoolSample::getPoolName)
                    .containsExactly("alarmesMailExecutor", "customThreadPool", "salutWorkerExecutor", "monitorTaskScheduler");

            RawWorkerPoolSample alarmes = samples.get(0);
            assertThat(alarmes.getPoolType()).isEqualTo(WorkerPoolType.EXECUTOR);
            assertThat(alarmes.getThreadNamePrefix()).isEqualTo("alarmes-mail-");
            assertThat(alarmes.getCoreSize()).isEqualTo(2);
            assertThat(alarmes.getMaxSize()).isEqualTo(4);
            assertThat(alarmes.getQueueCapacity()).isEqualTo(100);

            RawWorkerPoolSample custom = samples.get(1);
            assertThat(custom.getPoolType()).isEqualTo(WorkerPoolType.EXECUTOR);
            assertThat(custom.getThreadNamePrefix()).isNull();
            assertThat(custom.getQueueCapacity()).isEqualTo(50);

            RawWorkerPoolSample salut = samples.get(2);
            assertThat(salut.getPoolType()).isEqualTo(WorkerPoolType.EXECUTOR);
            assertThat(salut.getMaxSize()).isEqualTo(5);
            assertThat(salut.getQueueCapacity()).isEqualTo(1000);
            assertThat(salut.getRawCompletedTaskCount()).isGreaterThanOrEqualTo(0L);

            RawWorkerPoolSample sched = samples.get(3);
            assertThat(sched.getPoolType()).isEqualTo(WorkerPoolType.SCHEDULER);
            assertThat(sched.getThreadNamePrefix()).isEqualTo("monitor-tasques-");
            assertThat(sched.getCoreSize()).isEqualTo(10);
            // ScheduledThreadPoolExecutor retorna Integer.MAX_VALUE -> normalitzat a coreSize (10)
            assertThat(sched.getMaxSize()).isEqualTo(10);
            // DelayedWorkQueue és no fitada -> queueCapacity null
            assertThat(sched.getQueueCapacity()).isNull();
        } finally {
            salutExec.shutdown();
            alarmesExec.shutdown();
            monitorScheduler.shutdown();
            customPool.shutdownNow();
        }
    }

    @Test
    @DisplayName("Si no hi ha beans injectats retorna llista buida")
    void collectAll_senseBeans_retornaBuit() {
        WorkerPoolMetricsHelper helper = new WorkerPoolMetricsHelper();
        assertThat(helper.collectAll()).isEmpty();
    }

    @Test
    @DisplayName("resolveQueueSize compta tota la cua per a executors i només tasques vençudes no cancel·lades per a schedulers")
    void resolveQueueSize_filtraCorrectamentSegonsTipus() {
        assertThat(WorkerPoolMetricsHelper.resolveQueueSize(null, WorkerPoolType.EXECUTOR)).isZero();
        assertThat(WorkerPoolMetricsHelper.resolveQueueSize(null, WorkerPoolType.SCHEDULER)).isZero();

        BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        queue.add(new FakeScheduledItem(0L, false));       // vençuda
        queue.add(new FakeScheduledItem(-50L, false));     // vençuda
        queue.add(new FakeScheduledItem(10_000L, false));  // futura
        queue.add(new FakeScheduledItem(-10L, true));      // vençuda però cancel·lada
        queue.add("non-delayed-item");                     // element genèric

        assertThat(WorkerPoolMetricsHelper.resolveQueueSize(queue, WorkerPoolType.EXECUTOR)).isEqualTo(5);
        assertThat(WorkerPoolMetricsHelper.resolveQueueSize(queue, WorkerPoolType.SCHEDULER)).isEqualTo(3);
    }

    @Test
    @DisplayName("Un scheduler amb tasques futures té queueSize 0 i només augmenta quan hi ha tasques vençudes esperant fil")
    void collectAll_schedulerNomesComptaTasquesVencudes() throws Exception {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("test-sched-");
        scheduler.initialize();

        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        try {
            WorkerPoolMetricsHelper helper = new WorkerPoolMetricsHelper();
            ReflectionTestUtils.setField(helper, "schedulers", Collections.singletonMap("testSched", scheduler));

            // 1. Tasca futura -> present a la DelayedWorkQueue, però queueSize ha de ser 0
            ScheduledFuture<?> futureOnly = scheduler.schedule(() -> {}, Instant.now().plusSeconds(3600));
            assertThat(scheduler.getScheduledThreadPoolExecutor().getQueue()).hasSize(1);
            List<RawWorkerPoolSample> beforeSaturation = helper.collectAll();
            assertThat(beforeSaturation).hasSize(1);
            assertThat(beforeSaturation.get(0).getQueueSize()).isZero();
            futureOnly.cancel(false);

            // 2. Bloquegem l'únic fil del scheduler
            scheduler.schedule(() -> {
                started.countDown();
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, Instant.now());
            assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();

            // 3. Encuem 2 tasques immediates, 1 immediata cancel·lada i 1 futura
            scheduler.schedule(() -> {}, Instant.now());
            scheduler.schedule(() -> {}, Instant.now());
            ScheduledFuture<?> cancelledImmediate = scheduler.schedule(() -> {}, Instant.now());
            cancelledImmediate.cancel(false);
            scheduler.schedule(() -> {}, Instant.now().plusSeconds(3600));

            List<RawWorkerPoolSample> saturated = helper.collectAll();
            assertThat(saturated).hasSize(1);
            assertThat(saturated.get(0).getActiveCount()).isEqualTo(1);
            assertThat(saturated.get(0).getQueueSize()).isEqualTo(2);
        } finally {
            release.countDown();
            scheduler.shutdown();
        }
    }

    private static final class FakeScheduledItem implements Delayed, Future<Object> {
        private final long delayNanos;
        private final boolean cancelled;

        private FakeScheduledItem(long delayNanos, boolean cancelled) {
            this.delayNanos = delayNanos;
            this.cancelled = cancelled;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(delayNanos, TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return Long.compare(getDelay(TimeUnit.NANOSECONDS), o.getDelay(TimeUnit.NANOSECONDS));
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Object get() {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            return null;
        }
    }
}
