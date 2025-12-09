package es.caib.comanda.ms.salut.model;

import es.caib.comanda.ms.salut.helper.MonitorHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@AllArgsConstructor
@Schema(name = "SalutInfo", description = "Estat de salut funcional de l'aplicació i metadades associades")
public class SalutInfo {
    @Schema(description = "Codi identificador de l'aplicació", example = "NOTIB")
    @NotNull @Size(min = 1, max = 16)
    private String codi;
    @Schema(description = "Instant de generació de l'estat de salut", type = "string", format = "date-time")
    @NotNull
    private Date data;
    @Schema(description = "Estat global de l'aplicació")
    @NotNull @Valid
    private EstatSalut estat;
    @Schema(description = "Estat de la base de dades")
    @NotNull @Valid
    private EstatSalut bd;
    @Schema(description = "Integracions amb el seu estat")
    @Valid
    private List<IntegracioSalut> integracions;
    @Schema(description = "Altres detalls rellevants de salut")
    @Valid
    private List<DetallSalut> altres;
    @Schema(description = "Missatges informatius o d'alerta")
    @Valid
    private List<MissatgeSalut> missatges;
    @Schema(description = "Versió de l'aplicació", example = "1.4.3")
    @Size(max = 10)
    private String versio;
    @Schema(description = "Subsistemes interns amb el seu estat")
    @Valid
    private List<SubsistemaSalut> subsistemes;

    SalutInfo() {
        this.integracions = new ArrayList<>();
        this.missatges = new ArrayList<>();
        // Afegix informació del sistema a l'apartat altres
        this.altres = getInfoSistema();
    }

    // Custom builder to validate bean constraints on build()
    public static class SalutInfoBuilder {
        public SalutInfo build() {
            SalutInfo instance = new SalutInfo(codi, data, estat, bd, integracions, altres, missatges, versio, subsistemes);
            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            Set<ConstraintViolation<SalutInfo>> violations = validator.validate(instance);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            // Afegix informació del sistema a l'apartat altres
            if (altres == null || altres.isEmpty()) {
                altres = getInfoSistema();
            }

            return instance;
        }
    }

    public static List<DetallSalut> getInfoSistema() {
        List<DetallSalut> detallSaluts = new ArrayList<>();

        // CPU usage
        MonitorHelper.CpuUsage cpuUsage = MonitorHelper.getCpuUsage();
        detallSaluts.add(DetallSalut.builder().codi("PRC").nom("Processadors").valor(String.valueOf(cpuUsage.getCores())).build());
        detallSaluts.add(DetallSalut.builder().codi("LAVG").nom("Càrrega del sistema (LoadAvg)").valor(cpuUsage.getFormatedLoadAverage()).build());
        if (cpuUsage.isValidSystemCpuLoad()) {
            detallSaluts.add(DetallSalut.builder().codi("SCPU").nom("CPU sistema").valor(cpuUsage.getFormatedSystemCpuLoad()).build());
        }
//        if (cpuUsage.isValidProcessCpuLoad()) {
//            detallSaluts.add(DetallSalut.builder().codi("PCPU").nom("CPU procés").valor(cpuUsage.getFormatedProcessCpuLoad()).build());
//        }

        // Memory usage
        MonitorHelper.MemoryUsage jvmMemory = MonitorHelper.getJvmMemory();
        detallSaluts.add(DetallSalut.builder().codi("MET").nom("Memòria total").valor(jvmMemory.getFormatedTotalMemory()).build());
        detallSaluts.add(DetallSalut.builder().codi("MED").nom("Memòria disponible").valor(jvmMemory.getFormatedFreeMemory()).build());

        // Disk usage
        MonitorHelper.DiskUsage rootDiskUsage = MonitorHelper.getRootDiskUsage();
        detallSaluts.add(DetallSalut.builder().codi("EDT").nom("Espai de disc total").valor(rootDiskUsage.getFormatedTotalSpace()).build());
        detallSaluts.add(DetallSalut.builder().codi("EDL").nom("Espai de disc lliure").valor(rootDiskUsage.getFormatedFreeSpace()).build());

        // Identificació SO, JVM i entorn
        MonitorHelper.SystemInfo systemInfo = MonitorHelper.getSystemInfo();
        detallSaluts.add(DetallSalut.builder().codi("SO").nom("Sistema operatiu").valor(systemInfo.getOs()).build());
//        detallSaluts.add(DetallSalut.builder().codi("JVM").nom("JVM").valor(systemInfo.getJvm()).build());
        detallSaluts.add(DetallSalut.builder().codi("ST").nom("Data arrancada").valor(systemInfo.getFormatedStartTime()).build());
        detallSaluts.add(DetallSalut.builder().codi("UT").nom("Temps funcionant").valor(systemInfo.getFormatedUpTime()).build());

//        // Threads
//        MonitorHelper.JvmInfo jvmInfo = MonitorHelper.getJvmInfo();
//        detallSaluts.add(DetallSalut.builder().codi("THC").nom("Threads actius").valor(String.valueOf(jvmInfo.getThreadCount())).build());
//        detallSaluts.add(DetallSalut.builder().codi("THP").nom("Màxim de threads").valor(String.valueOf(jvmInfo.getPeakThreadCount())).build());
//        detallSaluts.add(DetallSalut.builder().codi("THD").nom("Threads dimoni").valor(String.valueOf(jvmInfo.getDaemonThreadCount())).build());

        return detallSaluts;
    }

}
