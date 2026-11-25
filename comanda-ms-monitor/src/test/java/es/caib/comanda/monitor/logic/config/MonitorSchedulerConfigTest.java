package es.caib.comanda.monitor.logic.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests per a MonitorSchedulerConfig")
class MonitorSchedulerConfigTest {

    @Test
    @DisplayName("monitorTaskScheduler configura poolSize 10, prefix i removeOnCancelPolicy=true")
    void monitorTaskScheduler_configuraRemoveOnCancelPolicy() {
        MonitorSchedulerConfig config = new MonitorSchedulerConfig();
        TaskScheduler taskScheduler = config.monitorTaskScheduler();

        assertThat(taskScheduler).isInstanceOf(ThreadPoolTaskScheduler.class);
        ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) taskScheduler;
        try {
            assertThat(scheduler.getScheduledThreadPoolExecutor().getCorePoolSize()).isEqualTo(10);
            assertThat(scheduler.getThreadNamePrefix()).isEqualTo("monitor-tasques-");
            assertThat(scheduler.getScheduledThreadPoolExecutor().getRemoveOnCancelPolicy()).isTrue();
        } finally {
            scheduler.shutdown();
        }
    }
}
