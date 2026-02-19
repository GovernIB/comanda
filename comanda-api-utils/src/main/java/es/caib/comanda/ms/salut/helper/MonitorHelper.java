package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.model.server.monitoring.InformacioSistema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

@Slf4j
public class MonitorHelper {

    private static Runtime rt;
    private static RuntimeMXBean runtimeMXBean;
    private static ThreadMXBean threadMXBean;
    private static OperatingSystemMXBean osBean;

    static {
        rt = Runtime.getRuntime();
        runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        threadMXBean = ManagementFactory.getThreadMXBean();
        osBean = ManagementFactory.getOperatingSystemMXBean();
    }


    // JVM Memory usage
    public static MemoryUsage getJvmMemory() {
        try {
            Long totalMemory = rt.totalMemory();
            Long freeMemory = rt.freeMemory();
            Long usedMemory = totalMemory - freeMemory;
            return MemoryUsage.builder()
                    .totalMemory(totalMemory)
                    .freeMemory(freeMemory)
                    .usedMemory(usedMemory)
                    .build();
        } catch (Throwable ignored) { }
        return null;
    }

    // Phisical Memory usage
    public static MemoryUsage getPhisicalMemory() {
        try {
            Long totalMemory = reflectLong(osBean, "getTotalPhysicalMemorySize");
            Long freeMemory = reflectLong(osBean, "getFreePhysicalMemorySize");
            Long usedMemory = totalMemory - freeMemory;
            return MemoryUsage.builder()
                    .totalMemory(totalMemory)
                    .freeMemory(freeMemory)
                    .usedMemory(usedMemory)
                    .build();
        } catch (Throwable ignored) { }
        return null;
    }

    // CPU usage
    public static CpuUsage getCpuUsage() {
        try {
            Integer cores = rt.availableProcessors();
            Double loadAverage = osBean.getSystemLoadAverage();
            Double systemCpuLoad = reflectDouble(osBean, "getSystemCpuLoad");
            Double processCpuLoad = reflectDouble(osBean, "getProcessCpuLoad");
            return CpuUsage.builder()
                    .cores(cores)
                    .loadAverage(loadAverage)
                    .systemCpuLoad(systemCpuLoad)
                    .processCpuLoad(processCpuLoad)
                    .build();
        } catch (Throwable ignored) { }
        return null;
    }

    // Disk usage
    public static DiskUsage getRootDiskUsage() {
        try {
            File f = new File("/");
            long totalSpace = f.getTotalSpace();
            long freeSpace = f.getUsableSpace();
            long usedSpace = totalSpace > 0 ? (totalSpace - freeSpace) : 0;

            return DiskUsage.builder()
                    .nom("/")
                    .totalSpace(totalSpace)
                    .freeSpace(freeSpace)
                    .usedSpace(usedSpace)
                    .build();
        } catch (Throwable ignored) { }
        return null;
    }

    // Disk usage
    public static List<DiskUsage> getDisksUsage() {
        List<DiskUsage> disks = new ArrayList<>();
        try {
            // Discs per cada arrel disponible
            File[] roots = File.listRoots();
            if (roots != null) {
                for (int i = 0; i < roots.length; i++) {
                    File r = roots[i];
                    String nom = r.getPath();
                    long totalSpace = r.getTotalSpace();
                    long freeSpace = r.getUsableSpace();
                    long usedSpace = totalSpace > 0 ? (totalSpace - freeSpace) : 0;

                    disks.add(DiskUsage.builder()
                            .nom(nom)
                            .totalSpace(totalSpace)
                            .freeSpace(freeSpace)
                            .usedSpace(usedSpace)
                            .build());
                }
            }
        } catch (Throwable ignored) { }
        return disks;
    }

    public static SystemInfo getSystemInfo() {
        try {
            String os = System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")";
            String jdkVersion = System.getProperty("java.version");
            String jvm = System.getProperty("java.vendor") + " - " + System.getProperty("java.version");
            long startTime = runtimeMXBean.getStartTime();
            long upTime = runtimeMXBean.getUptime();

            return SystemInfo.builder()
                    .os(os)
                    .jdkVersion(jdkVersion)
                    .jvm(jvm)
                    .startTime(new Date(startTime))
                    .upTime(upTime)
                    .build();
        } catch (Throwable ignored) { }
        return null;
    }

    public static JvmInfo getJvmInfo() {
        try {
            Integer threadCount = threadMXBean.getThreadCount();
            Integer peakThreadCount = threadMXBean.getPeakThreadCount();
            Integer deamonThreadCount = threadMXBean.getDaemonThreadCount();

            // GC
            long gcCount = 0L;
            long gcTime = 0L;
            for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
                long c = gc.getCollectionCount();
                long t = gc.getCollectionTime();
                if (c > 0) gcCount += c;
                if (t > 0) gcTime += t;
            }

            return JvmInfo.builder()
                    .threadCount(threadCount)
                    .peakThreadCount(peakThreadCount)
                    .daemonThreadCount(deamonThreadCount)
                    .gcCount(gcCount)
                    .gcTime(gcTime)
                    .build();
        } catch (Throwable ignored) { }
        return null;
    }


    public static InformacioSistema getInfoSistema() {

        // CPU usage
        MonitorHelper.CpuUsage cpuUsage = MonitorHelper.getCpuUsage();
        Integer cpuCores = cpuUsage.getCores();
        String loadAverage = cpuUsage.getFormatedLoadAverage();
        String systemCpuLoad = cpuUsage.isValidSystemCpuLoad() ? cpuUsage.getFormatedSystemCpuLoad() : null;

        // Memory usage
        MonitorHelper.MemoryUsage jvmMemory = MonitorHelper.getJvmMemory();
        String totalMemory = jvmMemory.getFormatedTotalMemory();
        String freeMemory = jvmMemory.getFormatedFreeMemory();

        // Disk usage
        MonitorHelper.DiskUsage rootDiskUsage = MonitorHelper.getRootDiskUsage();
        String totalSpace = rootDiskUsage.getFormatedTotalSpace();
        String freeSpace = rootDiskUsage.getFormatedFreeSpace();

        // Identificació SO, JVM i entorn
        MonitorHelper.SystemInfo systemInfo = MonitorHelper.getSystemInfo();
        String os = systemInfo.getOs();
        String startTime = systemInfo.getFormatedStartTime();
        String upTime = systemInfo.getFormatedUpTime();

        return new InformacioSistema()
                .processadors(cpuCores)
                .carregaSistema(loadAverage)
                .cpuSistema(systemCpuLoad)
                .memoriaTotal(totalMemory)
                .memoriaDisponible(freeSpace)
                .espaiDiscTotal(totalSpace)
                .espaiDiscLliure(freeSpace)
                .sistemaOperatiu(os)
                .dataArrencada(startTime)
                .tempsFuncionant(upTime);
    }

    public static String getApplicationServerInfo() {
        try {
            String serverInfo = System.getProperty("server.info");
            if (serverInfo != null) return serverInfo;

            if (System.getProperty("jboss.home.dir") != null) {
                String version = getJBossVersion();
                if (version != null)
                    return version;
                version = System.getProperty("jboss.server.version", "");
                boolean isEap = System.getProperty("jboss.modules.system.pkgs", "").contains("com.redhat")
                        || System.getProperty("jboss.product.name", "").contains("EAP");
                String serverType = isEap ? "JBoss EAP" : "JBoss/WildFly";
                return serverType + (version.isEmpty() ? "" : " " + version);
            }
            if (System.getProperty("websphere.home.dir") != null) {
                String version = System.getProperty("websphere.version", "");
                return "WebSphere" + (version.isEmpty() ? "" : " " + version);
            }
            if (System.getProperty("weblogic.home.dir") != null) {
                String version = System.getProperty("weblogic.version", "");
                return "WebLogic" + (version.isEmpty() ? "" : " " + version);
            }
            if (System.getProperty("com.sun.aas.instanceRoot") != null) {
                String version = System.getProperty("glassfish.version", "");
                return "GlassFish" + (version.isEmpty() ? "" : " " + version);
            }
            if (System.getProperty("catalina.home") != null) {
                String version = System.getProperty("server.version", "");
                return "Apache Tomcat" + (version.isEmpty() ? "" : " " + version);
            }
            if (System.getProperty("spring.boot.version") != null) {
                String version = System.getProperty("spring.boot.version", "");
                return "Spring Boot" + (version.isEmpty() ? "" : " " + version);
            }

            return "Desconegut";
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static String jbossVersionCache = null;

    public static String getJBossVersion() {

        if (jbossVersionCache == null) {
            String jbossVersion = null;

            try {
                // MBean modern (WildFly / EAP 6+)
                ObjectName newRoot = new ObjectName("jboss.as:management-root=server");

                // MBean antic (JBoss EAP 5 / AS 5-6)
                ObjectName legacyRoot = new ObjectName("jboss.system:type=Server");

                for (MBeanServer server : MBeanServerFactory.findMBeanServer(null)) {

                    if (server == null) {
                        continue;
                    }

                    // --- WildFly / EAP 6+ ---
                    try {
                        if (server.isRegistered(newRoot)) {
                            String productName = safeGet(server, newRoot, "product-name");
                            String productVersion = safeGet(server, newRoot, "product-version");

                            if (productVersion != null) {
                                jbossVersion = (productName != null ? productName + " " : "") + productVersion;
                                break;
                            }
                        }
                    } catch (Exception ignore) {
                        // Continua buscant
                    }

                    // --- JBoss EAP 5 / AS 5-6 ---
                    try {
                        if (server.isRegistered(legacyRoot)) {
                            String version = safeGet(server, legacyRoot, "Version");
                            if (version != null) {
                                jbossVersion = "JBoss " + version;
                                break;
                            }
                        }
                    } catch (Exception ignore) {
                        // Continua buscant
                    }
                }

            } catch (Exception e) {
                log.error("JBOSS VERSION: error no controlat " + e.getMessage(), e);
            }

            if (jbossVersion != null) {
                jbossVersionCache = jbossVersion;
            }
        }

        return jbossVersionCache;
    }

    /**
     * Helper per evitar NullPointer i ClassCast.
     */
    private static String safeGet(MBeanServer server, ObjectName name, String attribute) {
        try {
            Object value = server.getAttribute(name, attribute);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryUsage {

        private Long totalMemory;
        private Long freeMemory;
        private Long usedMemory;

        public String getFormatedTotalMemory() {
            try {
                return humanReadableByteCount(totalMemory);
            } catch (Throwable ignored) { }
            return null;
        }
        public String getFormatedFreeMemory() {
            try {
                return humanReadableByteCount(freeMemory);
            } catch (Throwable ignored) { }
            return null;
        }
        public String getFormatedUsedMemory() {
            try {
                return humanReadableByteCount(usedMemory);
            } catch (Throwable ignored) { }
            return null;
        }
        public String getPctFreeMemory() {
            try {
                return pctFormat(freeMemory);
            } catch (Throwable ignored) { }
            return null;
        }
        public String getPctUsedMemory() {
            try {
                return pctFormat(usedMemory);
            } catch (Throwable ignored) { }
            return null;
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiskUsage {

        private String nom;
        private Long totalSpace;
        private Long freeSpace;
        private Long usedSpace;

        public String getFormatedTotalSpace() {
            try {
                return humanReadableByteCount(totalSpace);
            } catch (Throwable ignored) { }
            return null;
        }

        public String getFormatedFreeSpace() {
            try {
                return humanReadableByteCount(freeSpace);
            } catch (Throwable ignored) { }
            return null;
        }

        public String getFormatedUsedSpace() {
            try {
                return humanReadableByteCount(usedSpace);
            } catch (Throwable ignored) { }
            return null;
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CpuUsage {

        private Integer cores;
        private Double loadAverage;
        private Double systemCpuLoad;
        private Double processCpuLoad;

        public String getFormatedLoadAverage() {
            try {
                if (loadAverage == null) return null;
                return String.format("%.2f", loadAverage);
            } catch (Throwable ignored) { }
            return null;
        }
        public String getFormatedSystemCpuLoad() {
            try {
                if (systemCpuLoad == null) return null;
                return String.format("%.2f %%", systemCpuLoad * 100);
            } catch (Throwable ignored) { }
            return null;
        }
        public String getFormatedProcessCpuLoad() {
            try {
                if (processCpuLoad == null) return null;
                return String.format("%.2f %%", processCpuLoad * 100);
            } catch (Throwable ignored) { }
            return null;
        }
        public boolean isValidSystemCpuLoad() {
            try {
                return systemCpuLoad != null && systemCpuLoad >= 0 && systemCpuLoad <= 1;
            } catch (Throwable ignored) { }
            return false;
        }
        public boolean isValidProcessCpuLoad() {
            try {
                return processCpuLoad != null && processCpuLoad >= 0 && processCpuLoad <= 1;
            } catch (Throwable ignored) { }
            return false;
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemInfo {

        private String os;
        private String jdkVersion;
        private String jvm;
        private Date startTime;
        private Long upTime;

        public String getFormatedStartTime() {
            try {
                return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(startTime);
            } catch (Throwable ignored) { }
            return null;
        }
        public String getFormatedUpTime() {
            try {
                long uptimeMillis = upTime;
                long seconds = uptimeMillis / 1000 % 60;
                long minutes = uptimeMillis / (1000 * 60) % 60;
                long hours = uptimeMillis / (1000 * 60 * 60) % 24;
                long days = uptimeMillis / (1000 * 60 * 60 * 24);
                return String.format("%d dies, %02d:%02d:%02d", days, hours, minutes, seconds);
            } catch (Throwable ignored) { }
            return null;
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JvmInfo {
        private Integer threadCount;
        private Integer peakThreadCount;
        private Integer daemonThreadCount;
        private Long gcCount;
        private Long gcTime;
    }

    private static String humanReadableByteCount(long bytes) {
        try {
            int unit = 1024;
            if (bytes < unit) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            String pre = ("KMGTPE").charAt(exp-1) + "";
            return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
        } catch (Throwable ignored) { }
        return null;
    }

    private static String pctFormat(Long value) {
        try {
            Long total = getPhisicalMemory().getTotalMemory();
            if (total == null || total == 0) return null;
            double pct = (value * 100.0) / total;
            return String.format("%.2f %%", pct);
        } catch (Throwable ignored) { }
        return null;
    }

    private static Long reflectLong(Object obj, String methodName) {
        try {
            return (Long) obj.getClass().getMethod(methodName).invoke(obj);
        } catch (Throwable ignored) { }
        return null;
    }
    private static Double reflectDouble(Object obj, String methodName) {
        try {
            return (Double) obj.getClass().getMethod(methodName).invoke(obj);
        } catch (Throwable ignored) { }
        return null;
    }
}
