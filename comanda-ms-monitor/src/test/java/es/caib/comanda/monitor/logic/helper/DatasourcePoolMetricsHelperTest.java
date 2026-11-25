package es.caib.comanda.monitor.logic.helper;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import es.caib.comanda.monitor.logic.helper.DatasourcePoolMetricsHelper.RawDatasourcePoolSample;
import es.caib.comanda.monitor.logic.intf.model.performance.WaitCountType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests per a DatasourcePoolMetricsHelper")
class DatasourcePoolMetricsHelperTest {

    private final List<ObjectName> registeredMBeans = new ArrayList<>();
    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    @AfterEach
    void tearDown() throws Exception {
        for (ObjectName name : registeredMBeans) {
            if (mBeanServer.isRegistered(name)) {
                mBeanServer.unregisterMBean(name);
            }
        }
        registeredMBeans.clear();
    }

    private void registerMapMBean(String objectNameStr, Map<String, Object> attrs) throws Exception {
        ObjectName name = new ObjectName(objectNameStr);
        mBeanServer.registerMBean(new MapDynamicMBean(attrs), name);
        registeredMBeans.add(name);
    }

    @Test
    @DisplayName("JBoss JMX multi-DS: descobreix tots els datasources habilitats (incloent ExampleDS i XA), omet enabled=false i ordena JNDI configurat primer")
    void collectAll_jbossMultiDs_descobreixIOrdenaCorrectament() throws Exception {
        registerMapMBean("jboss.as:subsystem=datasources,data-source=ExampleDS", Map.of(
                "jndi-name", "java:jboss/datasources/ExampleDS",
                "enabled", true,
                "min-pool-size", 1,
                "max-pool-size", 20,
                "statistics-enabled", false
        ));
        registerMapMBean("jboss.as:subsystem=datasources,data-source=disabledDS", Map.of(
                "jndi-name", "java:jboss/datasources/DisabledDS",
                "enabled", false,
                "min-pool-size", 1,
                "max-pool-size", 10,
                "statistics-enabled", true
        ));
        registerMapMBean("jboss.as:subsystem=datasources,xa-data-source=alphaXA", Map.of(
                "jndi-name", "java:jboss/datasources/alphaXA",
                "enabled", true,
                "min-pool-size", 2,
                "max-pool-size", 15,
                "statistics-enabled", true
        ));
        registerMapMBean("jboss.as:subsystem=datasources,xa-data-source=alphaXA,statistics=pool", Map.of(
                "ActiveCount", 3,
                "InUseCount", 1,
                "WaitCount", 4L,
                "MaxWaitTime", 22L
        ));
        registerMapMBean("jboss.as:subsystem=datasources,data-source=comandaDB", Map.of(
                "jndi-name", "java:jboss/datasources/comandaDS",
                "enabled", true,
                "min-pool-size", 5,
                "max-pool-size", 50,
                "statistics-enabled", true
        ));
        registerMapMBean("jboss.as:subsystem=datasources,data-source=comandaDB,statistics=pool", Map.of(
                "ActiveCount", 12,
                "InUseCount", 4,
                "WaitCount", 9L,
                "MaxWaitTime", 35L
        ));

        DatasourcePoolMetricsHelper helper = new DatasourcePoolMetricsHelper();
        ReflectionTestUtils.setField(helper, "configuredJndiName", "java:jboss/datasources/comandaDS");

        List<RawDatasourcePoolSample> samples = helper.collectAll();

        assertThat(samples).hasSize(3);

        // El JNDI configurat apareix primer
        RawDatasourcePoolSample comanda = samples.get(0);
        assertThat(comanda.getPoolName()).isEqualTo("comandaDB");
        assertThat(comanda.getJndiName()).isEqualTo("java:jboss/datasources/comandaDS");
        assertThat(comanda.isStatisticsEnabled()).isTrue();
        assertThat(comanda.getWaitCountType()).isEqualTo(WaitCountType.INTERVAL_DELTA);
        assertThat(comanda.getMinSize()).isEqualTo(5);
        assertThat(comanda.getMaxSize()).isEqualTo(50);
        assertThat(comanda.getInUseCount()).isEqualTo(4);
        assertThat(comanda.getActiveCount()).isEqualTo(12);
        assertThat(comanda.getRawWaitCount()).isEqualTo(9L);
        assertThat(comanda.getMaxWaitTimeMs()).isEqualTo(35L);

        // Després ordre alfabètic: alphaXA (habilitat amb stats) i ExampleDS (habilitat però sense stats)
        RawDatasourcePoolSample alpha = samples.get(1);
        assertThat(alpha.getPoolName()).isEqualTo("alphaXA");
        assertThat(alpha.isStatisticsEnabled()).isTrue();
        assertThat(alpha.getInUseCount()).isEqualTo(1);
        assertThat(alpha.getRawWaitCount()).isEqualTo(4L);

        RawDatasourcePoolSample example = samples.get(2);
        assertThat(example.getPoolName()).isEqualTo("ExampleDS");
        assertThat(example.isStatisticsEnabled()).isFalse();
        assertThat(example.getWaitCountType()).isEqualTo(WaitCountType.INTERVAL_DELTA);
        assertThat(example.getMinSize()).isEqualTo(1);
        assertThat(example.getMaxSize()).isEqualTo(20);
        assertThat(example.getInUseCount()).isNull();
        assertThat(example.getRawWaitCount()).isNull();
    }

    @Test
    @DisplayName("HikariCP amb HikariPoolStatsTrackerFactory i múltiples beans: recull INSTANTANEOUS i maxWaitTimeMs")
    void collectAll_hikariMultiBean_recullMetriquesNativesITracker() throws Exception {
        try (HikariDataSource dsMain = new HikariDataSource();
             HikariDataSource dsSecondary = createStartedBareHikari("SecondaryHikari", "helper_hikari_sec")) {

            dsMain.setPoolName("MainHikari");
            dsMain.setJdbcUrl("jdbc:h2:mem:helper_hikari_main;DB_CLOSE_DELAY=-1");
            dsMain.setDriverClassName("org.h2.Driver");
            dsMain.setUsername("sa");
            dsMain.setPassword("");
            dsMain.setMinimumIdle(2);
            dsMain.setMaximumPoolSize(8);
            dsMain.setMetricsTrackerFactory(new HikariPoolStatsTrackerFactory(null, dsMain.getConnectionTimeout()));

            Map<String, DataSource> dsMap = new LinkedHashMap<>();
            dsMap.put("secondaryDataSource", dsSecondary);
            dsMap.put("mainDataSource", dsMain);

            DatasourcePoolMetricsHelper helper = new DatasourcePoolMetricsHelper();
            ReflectionTestUtils.setField(helper, "dataSources", dsMap);

            try (Connection c1 = dsMain.getConnection();
                 Connection c2 = dsSecondary.getConnection()) {
                assertThat(c1.isClosed()).isFalse();
                assertThat(c2.isClosed()).isFalse();

                List<RawDatasourcePoolSample> samples = helper.collectAll();
                assertThat(samples).hasSize(2);

                RawDatasourcePoolSample mainSample = samples.get(0);
                assertThat(mainSample.getPoolName()).isEqualTo("MainHikari");
                assertThat(mainSample.isStatisticsEnabled()).isTrue();
                assertThat(mainSample.getWaitCountType()).isEqualTo(WaitCountType.INSTANTANEOUS);
                assertThat(mainSample.getMinSize()).isEqualTo(2);
                assertThat(mainSample.getMaxSize()).isEqualTo(8);
                assertThat(mainSample.getInUseCount()).isEqualTo(1);
                assertThat(mainSample.getActiveCount()).isGreaterThanOrEqualTo(1);
                assertThat(mainSample.getRawWaitCount()).isZero();
                assertThat(mainSample.getMaxWaitTimeMs()).isNotNull();

                RawDatasourcePoolSample secSample = samples.get(1);
                assertThat(secSample.getPoolName()).isEqualTo("SecondaryHikari");
                assertThat(secSample.isStatisticsEnabled()).isTrue();
                assertThat(secSample.getWaitCountType()).isEqualTo(WaitCountType.INSTANTANEOUS);
                assertThat(secSample.getMaxWaitTimeMs()).isNull();
            }
        }
    }

    @Test
    @DisplayName("Quan no hi ha MBeans JBoss ni DataSources Spring retorna llista buida")
    void collectAll_senseFonts_retornaLlistaBuida() {
        DatasourcePoolMetricsHelper helper = new DatasourcePoolMetricsHelper();
        assertThat(helper.collectAll()).isEmpty();
    }

    private HikariDataSource createStartedBareHikari(String poolName, String dbName) {
        HikariConfig config = new HikariConfig();
        config.setPoolName(poolName);
        config.setJdbcUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("sa");
        config.setPassword("");
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(4);
        return new HikariDataSource(config);
    }

    private static final class MapDynamicMBean implements DynamicMBean {
        private final Map<String, Object> attributes;

        private MapDynamicMBean(Map<String, Object> attributes) {
            this.attributes = new HashMap<>(attributes);
        }

        @Override
        public Object getAttribute(String attribute) throws AttributeNotFoundException {
            if (!attributes.containsKey(attribute)) {
                throw new AttributeNotFoundException(attribute);
            }
            return attributes.get(attribute);
        }

        @Override
        public void setAttribute(Attribute attribute) {
            attributes.put(attribute.getName(), attribute.getValue());
        }

        @Override
        public AttributeList getAttributes(String[] names) {
            AttributeList list = new AttributeList();
            for (String name : names) {
                if (attributes.containsKey(name)) {
                    list.add(new Attribute(name, attributes.get(name)));
                }
            }
            return list;
        }

        @Override
        public AttributeList setAttributes(AttributeList list) {
            return list;
        }

        @Override
        public Object invoke(String actionName, Object[] params, String[] signature) {
            return null;
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            return new MBeanInfo(
                    MapDynamicMBean.class.getName(),
                    "Test Dynamic MBean",
                    null, null, null, null
            );
        }
    }
}
