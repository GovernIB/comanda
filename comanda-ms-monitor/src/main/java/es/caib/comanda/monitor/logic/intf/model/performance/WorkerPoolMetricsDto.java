package es.caib.comanda.monitor.logic.intf.model.performance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Estat actual i històric recent d'un pool de workers.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkerPoolMetricsDto {

    private String poolName;
    private String threadNamePrefix;
    private WorkerPoolType poolType;
    private Integer coreSize;
    private Integer maxSize;
    private Integer poolSize;
    private Integer activeCount;
    private Integer queueSize;
    private Integer queueCapacity;
    private Long completedTasks;
    private List<WorkerPoolSampleDto> history = Collections.emptyList();
    private List<WorkerPoolSampleDto> preview;
}
