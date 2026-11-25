package es.caib.comanda.monitor.logic.helper;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.IMetricsTracker;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.metrics.PoolStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Tests per a HikariPoolStatsTrackerFactory")
class HikariPoolStatsTrackerFactoryTest {

    @Test
    @DisplayName("recordConnectionAcquiredNanos actualitza maxWaitTimeMs de forma monòtona")
    void recordConnectionAcquiredNanos_actualitzaMaxWaitTimeMs() {
        HikariPoolStatsTrackerFactory factory = new HikariPoolStatsTrackerFactory(null, 30_000L);
        IMetricsTracker tracker = factory.create("testPool", mock(PoolStats.class));

        tracker.recordConnectionAcquiredNanos(-500L);
        assertThat(factory.getMaxWaitTimeMs()).isZero();

        tracker.recordConnectionAcquiredNanos(TimeUnit.MILLISECONDS.toNanos(12));
        assertThat(factory.getMaxWaitTimeMs()).isEqualTo(12L);

        tracker.recordConnectionAcquiredNanos(TimeUnit.MILLISECONDS.toNanos(45));
        assertThat(factory.getMaxWaitTimeMs()).isEqualTo(45L);

        tracker.recordConnectionAcquiredNanos(TimeUnit.MILLISECONDS.toNanos(8));
        assertThat(factory.getMaxWaitTimeMs()).isEqualTo(45L);
    }

    @Test
    @DisplayName("recordConnectionTimeout registra connectionTimeoutMs dinàmic com a màxim sense reduir pics previs")
    void recordConnectionTimeout_registraTimeoutMaximDinamic() {
        AtomicLong dynamicTimeout = new AtomicLong(5_000L);
        HikariPoolStatsTrackerFactory factory = new HikariPoolStatsTrackerFactory(null, dynamicTimeout::get);
        IMetricsTracker tracker = factory.create("testPool", mock(PoolStats.class));

        tracker.recordConnectionTimeout();
        assertThat(factory.getMaxWaitTimeMs()).isEqualTo(5_000L);

        dynamicTimeout.set(9_000L);
        tracker.recordConnectionTimeout();
        assertThat(factory.getMaxWaitTimeMs()).isEqualTo(9_000L);

        tracker.recordConnectionAcquiredNanos(TimeUnit.MILLISECONDS.toNanos(12_500));
        tracker.recordConnectionTimeout();
        assertThat(factory.getMaxWaitTimeMs()).isEqualTo(12_500L);
    }

    @Test
    @DisplayName("delega tots els esdeveniments de mètriques a la fàbrica configurada prèviament")
    void create_delegaTotsElsEsdevenimentsADelegate() {
        MetricsTrackerFactory delegateFactory = mock(MetricsTrackerFactory.class);
        IMetricsTracker delegateTracker = mock(IMetricsTracker.class);
        PoolStats poolStats = mock(PoolStats.class);
        when(delegateFactory.create("testPool", poolStats)).thenReturn(delegateTracker);

        HikariPoolStatsTrackerFactory factory = new HikariPoolStatsTrackerFactory(delegateFactory, 10_000L);
        IMetricsTracker tracker = factory.create("testPool", poolStats);

        tracker.recordConnectionCreatedMillis(15L);
        tracker.recordConnectionAcquiredNanos(2_000_000L);
        tracker.recordConnectionUsageMillis(25L);
        tracker.recordConnectionTimeout();
        tracker.close();

        verify(delegateTracker).recordConnectionCreatedMillis(15L);
        verify(delegateTracker).recordConnectionAcquiredNanos(2_000_000L);
        verify(delegateTracker).recordConnectionUsageMillis(25L);
        verify(delegateTracker).recordConnectionTimeout();
        verify(delegateTracker).close();
    }

    @Test
    @DisplayName("integració amb HikariDataSource real en memòria H2")
    void hikariDataSourceReal_registraAdquisicionsSenseWrapper() throws Exception {
        try (HikariDataSource hikari = new HikariDataSource()) {
            hikari.setPoolName("H2TrackerTestPool");
            hikari.setJdbcUrl("jdbc:h2:mem:tracker_factory_test;DB_CLOSE_DELAY=-1");
            hikari.setDriverClassName("org.h2.Driver");
            hikari.setUsername("sa");
            hikari.setPassword("");
            hikari.setMinimumIdle(1);
            hikari.setMaximumPoolSize(4);

            HikariPoolStatsTrackerFactory factory = new HikariPoolStatsTrackerFactory(
                    hikari.getMetricsTrackerFactory(),
                    hikari::getConnectionTimeout
            );
            hikari.setMetricsTrackerFactory(factory);

            try (Connection conn = hikari.getConnection()) {
                assertThat(conn.isClosed()).isFalse();
            }

            assertThat(hikari.getMetricsTrackerFactory()).isSameAs(factory);
            assertThat(factory.getMaxWaitTimeMs()).isGreaterThanOrEqualTo(0L);
        }
    }
}
