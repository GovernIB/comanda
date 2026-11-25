package es.caib.comanda.monitor.logic.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.IMetricsTracker;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.metrics.PoolStats;
import es.caib.comanda.monitor.logic.helper.DatasourcePoolMetricsHelper;
import es.caib.comanda.monitor.logic.helper.DatasourcePoolMetricsHelper.RawDatasourcePoolSample;
import es.caib.comanda.monitor.logic.helper.HikariPoolStatsTrackerFactory;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Tests per a HikariPoolStatsPostProcessor")
class HikariPoolStatsPostProcessorTest {

    private final HikariPoolStatsPostProcessor postProcessor = new HikariPoolStatsPostProcessor();

    @Test
    @DisplayName("getOrder retorna Ordered.LOWEST_PRECEDENCE")
    void getOrder_retornaLowestPrecedence() {
        assertThat(postProcessor.getOrder()).isEqualTo(Ordered.LOWEST_PRECEDENCE);
    }

    @Test
    @DisplayName("instrumenta tots els HikariDataSource del context Spring (primari lazy i secundari ja iniciat)")
    void springContext_instrumentaPrimariISecundari() {
        new ApplicationContextRunner()
                .withUserConfiguration(MultiHikariTestConfig.class, HikariPoolStatsPostProcessor.class, DatasourcePoolMetricsHelper.class)
                .run(context -> {
                    HikariDataSource main = context.getBean("mainDataSource", HikariDataSource.class);
                    HikariDataSource secondary = context.getBean("secondaryDataSource", HikariDataSource.class);

                    assertThat(main.getMetricsTrackerFactory()).isInstanceOf(HikariPoolStatsTrackerFactory.class);
                    assertThat(secondary.getMetricsTrackerFactory()).isInstanceOf(HikariPoolStatsTrackerFactory.class);

                    try (Connection c1 = main.getConnection();
                         Connection c2 = secondary.getConnection()) {
                        assertThat(c1.isClosed()).isFalse();
                        assertThat(c2.isClosed()).isFalse();

                        DatasourcePoolMetricsHelper helper = context.getBean(DatasourcePoolMetricsHelper.class);
                        List<RawDatasourcePoolSample> samples = helper.collectAll();

                        assertThat(samples).extracting(RawDatasourcePoolSample::getPoolName)
                                .contains("PostProcMainPool", "PostProcSecondaryPool");
                        assertThat(samples).filteredOn(s -> s.getPoolName().startsWith("PostProc"))
                                .allSatisfy(s -> assertThat(s.getMaxWaitTimeMs()).isNotNull());
                    }
                });
    }

    @Test
    @DisplayName("delega en una MetricsTrackerFactory prèvia si el pool encara no s'ha iniciat i llegeix connectionTimeout actualitzat")
    void postProcessAfterInitialization_delegaILlegeixConnectionTimeoutActualitzat() {
        try (HikariDataSource hikari = new HikariDataSource()) {
            MetricsTrackerFactory priorFactory = mock(MetricsTrackerFactory.class);
            IMetricsTracker priorTracker = mock(IMetricsTracker.class);
            when(priorFactory.create(anyString(), any(PoolStats.class))).thenReturn(priorTracker);

            hikari.setMetricsTrackerFactory(priorFactory);
            postProcessor.postProcessAfterInitialization(hikari, "mainDataSource");
            // Crida repetida per comprovar idempotència (no doble embolcall)
            postProcessor.postProcessAfterInitialization(hikari, "mainDataSource");

            assertThat(hikari.getMetricsTrackerFactory()).isInstanceOf(HikariPoolStatsTrackerFactory.class);
            HikariPoolStatsTrackerFactory trackerFactory = (HikariPoolStatsTrackerFactory) hikari.getMetricsTrackerFactory();

            // Actualització posterior de connectionTimeout reflectida dinàmicament
            hikari.setConnectionTimeout(4_321L);
            IMetricsTracker tracker = trackerFactory.create("pool", mock(PoolStats.class));
            tracker.recordConnectionTimeout();

            assertThat(trackerFactory.getMaxWaitTimeMs()).isEqualTo(4_321L);
            verify(priorTracker).recordConnectionTimeout();
        }
    }

    @Test
    @DisplayName("desembolcalla DelegatingDataSource per instrumentar el HikariDataSource subjacent")
    void postProcessAfterInitialization_desembolcallaDelegatingDataSource() {
        try (HikariDataSource hikari = new HikariDataSource()) {
            DataSource wrapped = new DelegatingDataSource(hikari);
            Object result = postProcessor.postProcessAfterInitialization(wrapped, "wrappedDataSource");

            assertThat(result).isSameAs(wrapped);
            assertThat(hikari.getMetricsTrackerFactory()).isInstanceOf(HikariPoolStatsTrackerFactory.class);
        }
    }

    @Test
    @DisplayName("omet instrumentació quan metricRegistry ja està configurat")
    void postProcessAfterInitialization_ometQuanMetricRegistryConfigurat() {
        try (HikariDataSource hikari = new HikariDataSource()) {
            SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
            hikari.setMetricRegistry(meterRegistry);

            postProcessor.postProcessAfterInitialization(hikari, "mainDataSource");

            assertThat(hikari.getMetricRegistry()).isSameAs(meterRegistry);
            assertThat(hikari.getMetricsTrackerFactory()).isNull();
        }
    }

    @Test
    @DisplayName("omet instrumentació sense error quan el pool ja està iniciat amb una altra MetricsTrackerFactory")
    void postProcessAfterInitialization_ometQuanPoolIniciatAmbAltraFactory() {
        MetricsTrackerFactory customFactory = (poolName, poolStats) -> new IMetricsTracker() {};
        HikariConfig config = new HikariConfig();
        config.setPoolName("StartedWithFactoryPool");
        config.setJdbcUrl("jdbc:h2:mem:postproc_started_with_factory;DB_CLOSE_DELAY=-1");
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("sa");
        config.setPassword("");
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(2);
        config.setMetricsTrackerFactory(customFactory);

        try (HikariDataSource started = new HikariDataSource(config)) {
            postProcessor.postProcessAfterInitialization(started, "startedDataSource");

            assertThat(started.getMetricsTrackerFactory()).isSameAs(customFactory);
        }
    }

    @Test
    @DisplayName("ignora beans que no són DataSource o DataSources no-Hikari")
    void postProcessAfterInitialization_ignoraNoHikari() {
        String otherBean = "notADataSource";
        assertThat(postProcessor.postProcessAfterInitialization(otherBean, "strBean")).isSameAs(otherBean);

        DataSource nonHikari = mock(DataSource.class);
        assertThat(postProcessor.postProcessAfterInitialization(nonHikari, "jndiDs")).isSameAs(nonHikari);
    }

    @Configuration
    static class MultiHikariTestConfig {

        @Bean(destroyMethod = "close")
        @Primary
        HikariDataSource mainDataSource() {
            HikariDataSource ds = new HikariDataSource();
            ds.setPoolName("PostProcMainPool");
            ds.setJdbcUrl("jdbc:h2:mem:postproc_main;DB_CLOSE_DELAY=-1");
            ds.setDriverClassName("org.h2.Driver");
            ds.setUsername("sa");
            ds.setPassword("");
            ds.setMinimumIdle(1);
            ds.setMaximumPoolSize(4);
            return ds;
        }

        @Bean(destroyMethod = "close")
        HikariDataSource secondaryDataSource() {
            HikariConfig cfg = new HikariConfig();
            cfg.setPoolName("PostProcSecondaryPool");
            cfg.setJdbcUrl("jdbc:h2:mem:postproc_secondary;DB_CLOSE_DELAY=-1");
            cfg.setDriverClassName("org.h2.Driver");
            cfg.setUsername("sa");
            cfg.setPassword("");
            cfg.setMinimumIdle(1);
            cfg.setMaximumPoolSize(2);
            return new HikariDataSource(cfg);
        }
    }
}
