package es.caib.comanda.monitor.logic.intf.model.performance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Punt històric sincronitzat d'un pool de workers ({@code ThreadPoolExecutor}).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkerPoolSampleDto {

    private LocalDateTime timestamp;
    private Integer poolSize;
    private Integer activeCount;
    private Integer queueSize;
    private Long completedTasks;
}
