package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.ms.salut.model.InformacioSistema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MonitorHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorHelper.class);
    private static final Runtime RT = Runtime.getRuntime();
    private static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();
    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
    private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getOperatingSystemMXBean();
    public static String jbossVersionCache = null;

    public static MemoryUsage getJvmMemory() {
        try {
            Long totalMemory = Long.valueOf(RT.totalMemory());
            Long freeMemory = Long.valueOf(RT.freeMemory());
            Long usedMemory = Long.valueOf(totalMemory.longValue() - freeMemory.longValue());
            return new MemoryUsage(totalMemory, freeMemory, usedMemory);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static MemoryUsage getPhisicalMemory() {
        try {
            Long totalMemory = reflectLong(OS_BEAN, "getTotalPhysicalMemorySize");
            Long freeMemory = reflectLong(OS_BEAN, "getFreePhysicalMemorySize");
            if (totalMemory == null || freeMemory == null) {
                return null;
            }
            Long usedMemory = Long.valueOf(totalMemory.longValue() - freeMemory.longValue());
            return new MemoryUsage(totalMemory, freeMemory, usedMemory);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static CpuUsage getCpuUsage() {
        try {
            Integer cores = Integer.valueOf(RT.availableProcessors());
            Double loadAverage = Double.valueOf(OS_BEAN.getSystemLoadAverage());
            Double systemCpuLoad = reflectDouble(OS_BEAN, "getSystemCpuLoad");
            Double processCpuLoad = reflectDouble(OS_BEAN, "getProcessCpuLoad");
            return new CpuUsage(cores, loadAverage, systemCpuLoad, processCpuLoad);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static DiskUsage getRootDiskUsage() {
        try {
            File f = new File("/");
            long totalSpace = f.getTotalSpace();
            long freeSpace = f.getUsableSpace();
            long usedSpace = totalSpace > 0 ? totalSpace - freeSpace : 0;
            return new DiskUsage("/", Long.valueOf(totalSpace), Long.valueOf(freeSpace), Long.valueOf(usedSpace));
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static SystemInfo getSystemInfo() {
        try {
            String os = System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")";
            String jdkVersion = System.getProperty("java.version");
            String jvm = System.getProperty("java.vendor") + " - " + System.getProperty("java.version");
            return new SystemInfo(os, jdkVersion, jvm, new Date(RUNTIME_MX_BEAN.getStartTime()), Long.valueOf(RUNTIME_MX_BEAN.getUptime()));
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static JvmInfo getJvmInfo() {
        try {
            long gcCount = 0L;
            long gcTime = 0L;
            List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
            for (GarbageCollectorMXBean gc : beans) {
                long currentCount = gc.getCollectionCount();
                long currentTime = gc.getCollectionTime();
                if (currentCount > 0) {
                    gcCount += currentCount;
                }
                if (currentTime > 0) {
                    gcTime += currentTime;
                }
            }
            return new JvmInfo(Integer.valueOf(THREAD_MX_BEAN.getThreadCount()),
                    Integer.valueOf(THREAD_MX_BEAN.getPeakThreadCount()),
                    Integer.valueOf(THREAD_MX_BEAN.getDaemonThreadCount()),
                    Long.valueOf(gcCount),
                    Long.valueOf(gcTime));
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static InformacioSistema getInfoSistema() {
        CpuUsage cpuUsage = getCpuUsage();
        MemoryUsage jvmMemory = getJvmMemory();
        DiskUsage rootDiskUsage = getRootDiskUsage();
        SystemInfo systemInfo = getSystemInfo();

        InformacioSistema info = new InformacioSistema();
        if (cpuUsage != null) {
            info.setProcessadors(cpuUsage.getCores())
                    .setCarregaSistema(cpuUsage.getFormatedLoadAverage())
                    .setCpuSistema(cpuUsage.isValidSystemCpuLoad() ? cpuUsage.getFormatedSystemCpuLoad() : null);
        }
        if (jvmMemory != null) {
            info.setMemoriaTotal(jvmMemory.getFormatedTotalMemory())
                    .setMemoriaDisponible(jvmMemory.getFormatedFreeMemory());
        }
        if (rootDiskUsage != null) {
            info.setEspaiDiscTotal(rootDiskUsage.getFormatedTotalSpace())
                    .setEspaiDiscLliure(rootDiskUsage.getFormatedFreeSpace());
        }
        if (systemInfo != null) {
            info.setSistemaOperatiu(systemInfo.getOs())
                    .setDataArrencada(systemInfo.getFormatedStartTime())
                    .setTempsFuncionant(systemInfo.getFormatedUpTime());
        }
        return info;
    }

    public static String getApplicationServerInfo() {
        try {
            String serverInfo = System.getProperty("server.info");
            if (serverInfo != null) {
                return serverInfo;
            }
            if (System.getProperty("jboss.home.dir") != null) {
                String version = getJBossVersion();
                if (version != null) {
                    return version;
                }
                version = System.getProperty("jboss.server.version", "");
                boolean isEap = System.getProperty("jboss.modules.system.pkgs", "").contains("com.redhat")
                        || System.getProperty("jboss.product.name", "").contains("EAP");
                return (isEap ? "JBoss EAP" : "JBoss/WildFly") + (version.length() == 0 ? "" : " " + version);
            }
            if (System.getProperty("websphere.home.dir") != null) {
                return "WebSphere" + appendVersion(System.getProperty("websphere.version", ""));
            }
            if (System.getProperty("weblogic.home.dir") != null) {
                return "WebLogic" + appendVersion(System.getProperty("weblogic.version", ""));
            }
            if (System.getProperty("com.sun.aas.instanceRoot") != null) {
                return "GlassFish" + appendVersion(System.getProperty("glassfish.version", ""));
            }
            if (System.getProperty("catalina.home") != null) {
                return "Apache Tomcat" + appendVersion(System.getProperty("server.version", ""));
            }
            if (System.getProperty("spring.boot.version") != null) {
                return "Spring Boot" + appendVersion(System.getProperty("spring.boot.version", ""));
            }
            return "Desconegut";
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static String getJBossVersion() {
        if (jbossVersionCache == null) {
            String jbossVersion = null;
            try {
                ObjectName newRoot = new ObjectName("jboss.as:management-root=server");
                ObjectName legacyRoot = new ObjectName("jboss.system:type=Server");
                List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
                for (MBeanServer server : servers) {
                    if (server == null) {
                        continue;
                    }
                    try {
                        if (server.isRegistered(newRoot)) {
                            String productName = safeGet(server, newRoot, "product-name");
                            String productVersion = safeGet(server, newRoot, "product-version");
                            if (productVersion != null) {
                                jbossVersion = (productName != null ? productName + " " : "") + productVersion;
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    try {
                        if (server.isRegistered(legacyRoot)) {
                            String version = safeGet(server, legacyRoot, "Version");
                            if (version != null) {
                                jbossVersion = version;
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Throwable ignored) {
            }
            jbossVersionCache = jbossVersion;
        }
        return jbossVersionCache;
    }

    private static String appendVersion(String version) {
        return version != null && version.length() > 0 ? " " + version : "";
    }

    private static String safeGet(MBeanServer server, ObjectName objectName, String attribute) {
        try {
            Object value = server.getAttribute(objectName, attribute);
            return value != null ? value.toString() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Long reflectLong(Object target, String methodName) {
        try {
            Object value = target.getClass().getMethod(methodName).invoke(target);
            return value instanceof Number ? Long.valueOf(((Number) value).longValue()) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Double reflectDouble(Object target, String methodName) {
        try {
            Object value = target.getClass().getMethod(methodName).invoke(target);
            return value instanceof Number ? Double.valueOf(((Number) value).doubleValue()) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static class MemoryUsage {
        private final Long totalMemory;
        private final Long freeMemory;
        private final Long usedMemory;

        public MemoryUsage(Long totalMemory, Long freeMemory, Long usedMemory) {
            this.totalMemory = totalMemory;
            this.freeMemory = freeMemory;
            this.usedMemory = usedMemory;
        }
        public Long getTotalMemory() { return totalMemory; }
        public Long getFreeMemory() { return freeMemory; }
        public Long getUsedMemory() { return usedMemory; }
        public String getFormatedTotalMemory() { return humanReadableByteCount(totalMemory.longValue()); }
        public String getFormatedFreeMemory() { return humanReadableByteCount(freeMemory.longValue()); }
    }

    public static class CpuUsage {
        private final Integer cores;
        private final Double loadAverage;
        private final Double systemCpuLoad;
        private final Double processCpuLoad;

        public CpuUsage(Integer cores, Double loadAverage, Double systemCpuLoad, Double processCpuLoad) {
            this.cores = cores;
            this.loadAverage = loadAverage;
            this.systemCpuLoad = systemCpuLoad;
            this.processCpuLoad = processCpuLoad;
        }
        public Integer getCores() { return cores; }
        public String getFormatedLoadAverage() { return loadAverage != null && loadAverage.doubleValue() >= 0 ? String.format("%.2f", loadAverage) : null; }
        public boolean isValidSystemCpuLoad() { return systemCpuLoad != null && systemCpuLoad.doubleValue() >= 0.0d; }
        public String getFormatedSystemCpuLoad() { return isValidSystemCpuLoad() ? String.format("%.2f%%", systemCpuLoad.doubleValue() * 100.0d) : null; }
    }

    public static class DiskUsage {
        private final String nom;
        private final Long totalSpace;
        private final Long freeSpace;
        private final Long usedSpace;

        public DiskUsage(String nom, Long totalSpace, Long freeSpace, Long usedSpace) {
            this.nom = nom;
            this.totalSpace = totalSpace;
            this.freeSpace = freeSpace;
            this.usedSpace = usedSpace;
        }
        public String getFormatedTotalSpace() { return humanReadableByteCount(totalSpace.longValue()); }
        public String getFormatedFreeSpace() { return humanReadableByteCount(freeSpace.longValue()); }
    }

    public static class SystemInfo {
        private final String os;
        private final String jdkVersion;
        private final String jvm;
        private final Date startTime;
        private final Long upTime;

        public SystemInfo(String os, String jdkVersion, String jvm, Date startTime, Long upTime) {
            this.os = os;
            this.jdkVersion = jdkVersion;
            this.jvm = jvm;
            this.startTime = startTime;
            this.upTime = upTime;
        }
        public String getOs() { return os; }
        public String getJdkVersion() { return jdkVersion; }
        public String getFormatedStartTime() { return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(startTime); }
        public String getFormatedUpTime() { return formatDuration(upTime.longValue()); }
    }

    public static class JvmInfo {
        private final Integer threadCount;
        private final Integer peakThreadCount;
        private final Integer daemonThreadCount;
        private final Long gcCount;
        private final Long gcTime;

        public JvmInfo(Integer threadCount, Integer peakThreadCount, Integer daemonThreadCount, Long gcCount, Long gcTime) {
            this.threadCount = threadCount;
            this.peakThreadCount = peakThreadCount;
            this.daemonThreadCount = daemonThreadCount;
            this.gcCount = gcCount;
            this.gcTime = gcTime;
        }
    }

    public static String humanReadableByteCount(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) { sb.append(days).append("d "); }
        if (hours > 0 || sb.length() > 0) { sb.append(hours).append("h "); }
        if (minutes > 0 || sb.length() > 0) { sb.append(minutes).append("m "); }
        sb.append(secs).append("s");
        return sb.toString().trim();
    }
}
