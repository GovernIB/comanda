package es.caib.comanda.ms.salut.helper;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MonitorHelperTest {

    @Test
    void testJvmMemory() {
        MonitorHelper.MemoryUsage jvmMemory = MonitorHelper.getJvmMemory();
        assertThat(jvmMemory).isNotNull();
        assertThat(jvmMemory.getTotalMemory()).isGreaterThan(0);
        assertThat(jvmMemory.getFreeMemory()).isGreaterThan(0);
        assertThat(jvmMemory.getUsedMemory()).isEqualTo(jvmMemory.getTotalMemory() - jvmMemory.getFreeMemory());
        assertThat(jvmMemory.getFormatedTotalMemory()).isNotBlank();
        assertThat(jvmMemory.getFormatedFreeMemory()).isNotBlank();
        assertThat(jvmMemory.getFormatedUsedMemory()).isNotBlank();
    }

    @Test
    void testPhysicalMemory() {
        MonitorHelper.MemoryUsage physicalMemory = MonitorHelper.getPhisicalMemory();
        if (physicalMemory != null) {
            assertThat(physicalMemory.getTotalMemory()).isGreaterThan(0);
            assertThat(physicalMemory.getFreeMemory()).isGreaterThan(0);
            assertThat(physicalMemory.getFormatedTotalMemory()).isNotBlank();
        }
    }

    @Test
    void testCpuUsage() {
        MonitorHelper.CpuUsage cpuUsage = MonitorHelper.getCpuUsage();
        assertThat(cpuUsage).isNotNull();
        assertThat(cpuUsage.getCores()).isGreaterThan(0);
        // loadAverage can be -1.0 on some systems (e.g. Windows)
        assertThat(cpuUsage.getFormatedLoadAverage()).isNotNull();
        
        if (cpuUsage.isValidSystemCpuLoad()) {
            assertThat(cpuUsage.getFormatedSystemCpuLoad()).contains("%");
        }
        if (cpuUsage.isValidProcessCpuLoad()) {
            assertThat(cpuUsage.getFormatedProcessCpuLoad()).contains("%");
        }
    }

    @Test
    void testDiskUsage() {
        MonitorHelper.DiskUsage rootDisk = MonitorHelper.getRootDiskUsage();
        assertThat(rootDisk).isNotNull();
        assertThat(rootDisk.getNom()).isEqualTo("/");
        assertThat(rootDisk.getTotalSpace()).isGreaterThanOrEqualTo(0);
        assertThat(rootDisk.getFormatedTotalSpace()).isNotBlank();

        var allDisks = MonitorHelper.getDisksUsage();
        assertThat(allDisks).isNotEmpty();
        assertThat(allDisks).filteredOn(d -> "/".equals(d.getNom())).isNotEmpty();
    }

    @Test
    void testSystemInfo() {
        MonitorHelper.SystemInfo systemInfo = MonitorHelper.getSystemInfo();
        assertThat(systemInfo).isNotNull();
        assertThat(systemInfo.getOs()).isNotBlank();
        assertThat(systemInfo.getJdkVersion()).isNotBlank();
        assertThat(systemInfo.getStartTime()).isNotNull();
        assertThat(systemInfo.getUpTime()).isGreaterThan(0);
        assertThat(systemInfo.getFormatedStartTime()).isNotBlank();
        assertThat(systemInfo.getFormatedUpTime()).contains("dies");
    }

    @Test
    void testJvmInfo() {
        MonitorHelper.JvmInfo jvmInfo = MonitorHelper.getJvmInfo();
        assertThat(jvmInfo).isNotNull();
        assertThat(jvmInfo.getThreadCount()).isGreaterThan(0);
        assertThat(jvmInfo.getPeakThreadCount()).isGreaterThan(0);
        assertThat(jvmInfo.getDaemonThreadCount()).isGreaterThanOrEqualTo(0);
        assertThat(jvmInfo.getGcCount()).isGreaterThanOrEqualTo(0);
        assertThat(jvmInfo.getGcTime()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testApplicationServerInfo() {
        String serverInfo = MonitorHelper.getApplicationServerInfo();
        assertThat(serverInfo).isNotNull();
        // Since we are running in a test environment, it's likely "Desconegut" or some property-based value
        assertThat(serverInfo).isNotBlank();
    }

    @Test
    void testHumanReadableByteCount() {
        var jvmMemory = MonitorHelper.getJvmMemory();
        // MemoryUsage uses humanReadableByteCount internally
        assertThat(jvmMemory.getFormatedTotalMemory()).containsAnyOf("B", "KB", "MB", "GB", "TB");
        
        // We can't call private humanReadableByteCount directly without reflection or making it package-private,
        // but it's already covered by testing the formated getters in MemoryUsage and DiskUsage.
    }

    @Test
    void testGetInfoSistemaMapping() {
        var info = MonitorHelper.getInfoSistema();
        assertThat(info).isNotNull();
        
        var disk = MonitorHelper.getRootDiskUsage();
        var jvm = MonitorHelper.getJvmMemory();
        
        assertThat(info.getEspaiDiscLliure()).isEqualTo(disk.getFormatedFreeSpace());
        
        // Use a more robust check as memory values change between calls
        assertThat(info.getMemoriaDisponible()).containsAnyOf("MB", "GB", "B", "KB");
        assertThat(info.getMemoriaDisponible()).isNotEqualTo(info.getEspaiDiscLliure());
    }
}
