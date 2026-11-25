package es.caib.comanda.monitor.logic.helper;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import es.caib.comanda.monitor.logic.intf.model.performance.WaitCountType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Descobreix i recull mètriques natives de tots els pools de datasource disponibles:
 * <ul>
 *     <li>En JBoss / WildFly: tots els MBeans habilitats sota {@code jboss.as:subsystem=datasources}
 *         ({@code data-source} i {@code xa-data-source}), llegint {@code statistics=pool} quan
 *         {@code statistics-enabled=true}.</li>
 *     <li>En Spring Boot standalone: tots els beans {@link HikariDataSource} del context, combinant
 *         {@link HikariPoolMXBean} i {@link HikariPoolStatsTrackerFactory}.</li>
 * </ul>
 */
@Component
@Slf4j
public class DatasourcePoolMetricsHelper {

    private static final String[] JBOSS_DS_TYPES = { "data-source", "xa-data-source" };
    private static final long JBOSS_DISCOVERY_CACHE_TTL_MS = 60_000L;

    @Autowired(required = false)
    private Map<String, DataSource> dataSources;

    @Value("${spring.datasource.jndi-name:}")
    private String configuredJndiName = "";

    private volatile List<JbossDataSourceRef> cachedJbossRefs;
    private volatile long jbossCacheExpiresAtMs;

    /**
     * Recull una lectura actual de tots els pools detectats (JBoss JMX i/o HikariCP).
     */
    public List<RawDatasourcePoolSample> collectAll() {
        List<RawDatasourcePoolSample> samples = new ArrayList<>();
        Set<String> seenJbossPools = new HashSet<>();

        for (JbossDataSourceRef ref : getJbossDataSourceRefs()) {
            RawDatasourcePoolSample sample = readJbossSample(ref);
            samples.add(sample);
            if (sample.getPoolName() != null) {
                seenJbossPools.add(sample.getPoolName());
            }
        }

        collectHikariSamples(samples, seenJbossPools);
        return samples;
    }

    private static final String[] JBOSS_DS_CONFIG_ATTRS = { "min-pool-size", "max-pool-size", "statistics-enabled" };
    private static final String[] JBOSS_DS_STATS_ATTRS = { "InUseCount", "ActiveCount", "WaitCount", "MaxWaitTime" };

    private RawDatasourcePoolSample readJbossSample(JbossDataSourceRef ref) {
        RawDatasourcePoolSample sample = new RawDatasourcePoolSample();
        sample.setPoolKey("jboss:" + ref.objectName.getCanonicalName());
        sample.setPoolName(ref.poolName);
        sample.setJndiName(ref.jndiName);
        sample.setWaitCountType(WaitCountType.INTERVAL_DELTA);

        Map<String, Object> configAttrs = readAttributesMap(ref.server, ref.objectName, JBOSS_DS_CONFIG_ATTRS);
        sample.setMinSize(toInt(configAttrs.get("min-pool-size")));
        sample.setMaxSize(toInt(configAttrs.get("max-pool-size")));

        boolean statsFlag = Boolean.TRUE.equals(toBoolean(configAttrs.get("statistics-enabled")));
        if (!statsFlag) {
            sample.setStatisticsEnabled(false);
            return sample;
        }

        try {
            ObjectName statsName = new ObjectName(
                    ref.objectName.getDomain() + ":" + ref.objectName.getKeyPropertyListString() + ",statistics=pool"
            );
            if (!ref.server.isRegistered(statsName)) {
                log.debug("MBean {} té statistics-enabled=true però {} no està registrat", ref.objectName, statsName);
                sample.setStatisticsEnabled(false);
                return sample;
            }
            Map<String, Object> statsAttrs = readAttributesMap(ref.server, statsName, JBOSS_DS_STATS_ATTRS);
            sample.setStatisticsEnabled(true);
            sample.setInUseCount(toInt(statsAttrs.get("InUseCount")));
            sample.setActiveCount(toInt(statsAttrs.get("ActiveCount")));
            sample.setRawWaitCount(toLong(statsAttrs.get("WaitCount")));
            sample.setMaxWaitTimeMs(toLong(statsAttrs.get("MaxWaitTime")));
        } catch (Exception e) {
            log.debug("No s'han pogut llegir les estadístiques JMX de {}: {}", ref.objectName, e.getMessage());
            sample.setStatisticsEnabled(false);
        }
        return sample;
    }

    private void collectHikariSamples(List<RawDatasourcePoolSample> out, Set<String> skipPoolNames) {
        if (dataSources == null || dataSources.isEmpty()) {
            return;
        }

        List<Map.Entry<String, DataSource>> entries = new ArrayList<>(dataSources.entrySet());
        entries.sort(Comparator
                .<Map.Entry<String, DataSource>, Integer>comparing(e -> "mainDataSource".equals(e.getKey()) ? 0 : 1)
                .thenComparing(Map.Entry::getKey, String.CASE_INSENSITIVE_ORDER));

        Set<String> seenHikariPools = new HashSet<>(skipPoolNames);
        for (Map.Entry<String, DataSource> entry : entries) {
            HikariDataSource hikari = unwrapHikari(entry.getValue());
            if (hikari == null) {
                continue;
            }
            String poolName = hikari.getPoolName();
            if (poolName == null || poolName.isBlank()) {
                poolName = entry.getKey();
            }
            if (!seenHikariPools.add(poolName)) {
                continue;
            }

            RawDatasourcePoolSample sample = new RawDatasourcePoolSample();
            sample.setPoolKey("hikari:" + poolName);
            sample.setPoolName(poolName);
            sample.setWaitCountType(WaitCountType.INSTANTANEOUS);
            sample.setMinSize(hikari.getMinimumIdle());
            sample.setMaxSize(hikari.getMaximumPoolSize());

            try {
                HikariPoolMXBean poolBean = hikari.getHikariPoolMXBean();
                if (poolBean != null) {
                    sample.setStatisticsEnabled(true);
                    sample.setInUseCount(poolBean.getActiveConnections());
                    sample.setActiveCount(poolBean.getTotalConnections());
                    sample.setRawWaitCount((long) poolBean.getThreadsAwaitingConnection());

                    MetricsTrackerFactory trackerFactory = hikari.getMetricsTrackerFactory();
                    if (trackerFactory instanceof HikariPoolStatsTrackerFactory) {
                        sample.setMaxWaitTimeMs(((HikariPoolStatsTrackerFactory) trackerFactory).getMaxWaitTimeMs());
                    }
                } else {
                    sample.setStatisticsEnabled(false);
                }
            } catch (Exception e) {
                log.debug("No s'han pogut llegir mètriques de HikariDataSource {}: {}", poolName, e.getMessage());
                sample.setStatisticsEnabled(false);
            }
            out.add(sample);
        }
    }

    private HikariDataSource unwrapHikari(DataSource ds) {
        if (ds == null) {
            return null;
        }
        try {
            if (ds instanceof HikariDataSource) {
                return (HikariDataSource) ds;
            }
            if (ds.isWrapperFor(HikariDataSource.class)) {
                return ds.unwrap(HikariDataSource.class);
            }
        } catch (Exception e) {
            log.debug("No s'ha pogut desenbolcallar DataSource com a HikariDataSource: {}", e.getMessage());
        }
        return null;
    }

    private synchronized List<JbossDataSourceRef> getJbossDataSourceRefs() {
        long now = System.currentTimeMillis();
        if (cachedJbossRefs != null && now < jbossCacheExpiresAtMs && allRegistered(cachedJbossRefs)) {
            return cachedJbossRefs;
        }
        List<JbossDataSourceRef> discovered = discoverJbossDataSources();
        cachedJbossRefs = discovered;
        jbossCacheExpiresAtMs = now + JBOSS_DISCOVERY_CACHE_TTL_MS;
        return discovered;
    }

    private boolean allRegistered(List<JbossDataSourceRef> refs) {
        for (JbossDataSourceRef ref : refs) {
            try {
                if (!ref.server.isRegistered(ref.objectName)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private List<JbossDataSourceRef> discoverJbossDataSources() {
        Map<String, JbossDataSourceRef> byCanonicalName = new LinkedHashMap<>();
        for (MBeanServer server : getMBeanServers()) {
            for (String typeKey : JBOSS_DS_TYPES) {
                collectMBeanCandidates(server, typeKey, byCanonicalName);
            }
        }
        List<JbossDataSourceRef> refs = new ArrayList<>(byCanonicalName.values());
        refs.sort(Comparator
                .<JbossDataSourceRef, Integer>comparing(ref -> isConfiguredJndi(ref.jndiName) ? 0 : 1)
                .thenComparing(ref -> ref.poolName != null ? ref.poolName : "", String.CASE_INSENSITIVE_ORDER)
                .thenComparing(ref -> ref.objectName.getCanonicalName()));
        return refs;
    }

    private void collectMBeanCandidates(
            MBeanServer server,
            String typeKey,
            Map<String, JbossDataSourceRef> out) {
        try {
            ObjectName pattern = new ObjectName("jboss.as:subsystem=datasources," + typeKey + "=*");
            Set<ObjectName> names = server.queryNames(pattern, null);
            if (names == null) {
                return;
            }
            for (ObjectName name : names) {
                if (name.getKeyProperty("statistics") != null
                        || name.getKeyProperty("connection-properties") != null
                        || name.getKeyProperty("xa-datasource-properties") != null) {
                    continue;
                }
                Boolean enabled = getBooleanAttr(server, name, "enabled");
                if (Boolean.FALSE.equals(enabled)) {
                    continue;
                }
                String poolName = stripQuotes(name.getKeyProperty(typeKey));
                String jndiName = getStringAttr(server, name, "jndi-name");
                out.putIfAbsent(name.getCanonicalName(), new JbossDataSourceRef(server, name, poolName, jndiName));
            }
        } catch (Exception e) {
            log.debug("Error cercant MBeans JBoss ({}): {}", typeKey, e.getMessage());
        }
    }

    private boolean isConfiguredJndi(String jndiName) {
        return configuredJndiName != null
                && !configuredJndiName.isBlank()
                && jndiName != null
                && configuredJndiName.trim().equals(jndiName.trim());
    }

    List<MBeanServer> getMBeanServers() {
        List<MBeanServer> servers = new ArrayList<>();
        ArrayList<MBeanServer> found = MBeanServerFactory.findMBeanServer(null);
        if (found != null) {
            servers.addAll(found);
        }
        MBeanServer platform = ManagementFactory.getPlatformMBeanServer();
        if (platform != null && !servers.contains(platform)) {
            servers.add(platform);
        }
        return servers;
    }

    private static String stripQuotes(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static Map<String, Object> readAttributesMap(MBeanServer server, ObjectName name, String[] attrNames) {
        Map<String, Object> result = new HashMap<>(attrNames.length);
        try {
            AttributeList list = server.getAttributes(name, attrNames);
            if (list != null) {
                for (Attribute attr : list.asList()) {
                    result.put(attr.getName(), attr.getValue());
                }
                return result;
            }
        } catch (Exception ignored) {
            // Fallback individual si algun MBean no suporta getAttributes en bloc
        }
        for (String attrName : attrNames) {
            Object val = safeGetAttribute(server, name, attrName);
            if (val != null) {
                result.put(attrName, val);
            }
        }
        return result;
    }

    private static Object safeGetAttribute(MBeanServer server, ObjectName name, String attr) {
        try {
            return server.getAttribute(name, attr);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getStringAttr(MBeanServer server, ObjectName name, String attr) {
        Object val = safeGetAttribute(server, name, attr);
        return val != null ? val.toString() : null;
    }

    private static Boolean getBooleanAttr(MBeanServer server, ObjectName name, String attr) {
        return toBoolean(safeGetAttribute(server, name, attr));
    }

    private static Boolean toBoolean(Object val) {
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        if (val != null) {
            return Boolean.parseBoolean(val.toString());
        }
        return null;
    }

    private static Integer toInt(Object val) {
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val != null) {
            try {
                return Integer.parseInt(val.toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Long toLong(Object val) {
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        if (val != null) {
            try {
                return Long.parseLong(val.toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static final class JbossDataSourceRef {
        private final MBeanServer server;
        private final ObjectName objectName;
        private final String poolName;
        private final String jndiName;

        private JbossDataSourceRef(MBeanServer server, ObjectName objectName, String poolName, String jndiName) {
            this.server = server;
            this.objectName = objectName;
            this.poolName = poolName;
            this.jndiName = jndiName;
        }
    }

    @Data
    public static class RawDatasourcePoolSample {
        private String poolKey;
        private String poolName;
        private String jndiName;
        private boolean statisticsEnabled;
        private WaitCountType waitCountType;
        private Integer minSize;
        private Integer maxSize;
        private Integer inUseCount;
        private Integer activeCount;
        private Long rawWaitCount;
        private Long maxWaitTimeMs;
    }
}
