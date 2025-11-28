package es.caib.comanda.ms.salut.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
                            .nom("/")
                            .totalSpace(totalSpace)
                            .freeSpace(freeSpace)
                            .usedSpace(usedSpace)
                            .build());
                }
            }

        } catch (Throwable ignored) { }
        return null;
    }

    // Identificació SO, JVM i entorn
    public static SystemInfo getSystemInfo() {
        try {
            String os = System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")";
            String jvm = System.getProperty("java.vm.name") + " " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")";
            long uptimeMs = runtimeMXBean.getUptime();
            long startTime = runtimeMXBean.getStartTime();

            return SystemInfo.builder()
                    .os(os)
                    .jvm(jvm)
                    .upTime(uptimeMs)
                    .startTime(startTime)
                    .build();
        } catch (Throwable ignored) { }
        return null;
    }

    public static JvmInfo getJvmInfo() {
        try {
            // Threads
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
        public String getPctFreeSpace() {
            try {
                return pctFormat(freeSpace);
            } catch (Throwable ignored) { }
            return null;
        }
        public String getPctUsedSpace() {
            try {
                return pctFormat(usedSpace);
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
                return String.format("%.2f", loadAverage);
            } catch (Throwable ignored) { }
            return null;
        }
        public boolean isValidSystemCpuLoad() {
            return systemCpuLoad != null && systemCpuLoad >= 0;
        }
        public String getFormatedSystemCpuLoad() {
            try {
                return pctFormat(systemCpuLoad);
            } catch (Throwable ignored) { }
            return null;
        }
        public boolean isValidProcessCpuLoad() {
            return processCpuLoad != null && processCpuLoad >= 0;
        }
        public String getFormatedProcessCpuLoad() {
            try {
                return pctFormat(processCpuLoad);
            } catch (Throwable ignored) { }
            return null;
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemInfo {

        private String os;
        private String jvm;
        private Long upTime;
        private Long startTime;

        public String getFormatedUpTime() {
            try {
                return humanReadableDuration(upTime);
            } catch (Throwable ignored) { }
            return null;
        }
        public String getFormatedStartTime() {
            try {
                return formatDateTime(new Date(startTime));
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

        public String getFormatedGcTime() {
            try {
                return humanReadableDuration(gcTime);
            } catch (Throwable ignored) { }
            return null;
        }
    }


    private static String humanReadableByteCount(long bytes) {

        long unit = 1000;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "kMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String pctFormat(double val) {
        String p = String.valueOf(val * 100.0);
        int ix = p.indexOf(".") + 1;
        String percent = p.substring(0, ix) + p.substring(ix, ix+1);
        return percent + "%";
    }

    // Helpers compatibles amb Java 7 per obtenir mètriques esteses sense dependre de com.sun.*
    private static Double reflectDouble(Object bean, String method) {
        try {
            java.lang.reflect.Method m = bean.getClass().getMethod(method);
            Object v = m.invoke(bean);
            if (v instanceof Double) return (Double) v;
            if (v != null) return Double.valueOf(v.toString());
        } catch (Throwable ignored) { }
        return null;
    }

    private static Long reflectLong(Object bean, String method) {
        try {
            java.lang.reflect.Method m = bean.getClass().getMethod(method);
            Object v = m.invoke(bean);
            if (v instanceof Long) return (Long) v;
            if (v != null) return Long.valueOf(v.toString());
        } catch (Throwable ignored) { }
        return null;
    }

    private static String humanReadableDuration(long millis) {
        if (millis < 0) return "N/D";
        long seconds = millis / 1000;
        long s = seconds % 60;
        long minutes = (seconds / 60) % 60;
        long hours = (seconds / 3600) % 24;
        long days = seconds / 86400;
        if (days > 0) {
            return String.format("%dd %02dh %02dm %02ds", days, hours, minutes, s);
        } else {
            return String.format("%02dh %02dm %02ds", hours, minutes, s);
        }
    }

    private static String formatDateTime(Date date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            return sdf.format(date);
        } catch (Throwable t) {
            return date.toString();
        }
    }

}
