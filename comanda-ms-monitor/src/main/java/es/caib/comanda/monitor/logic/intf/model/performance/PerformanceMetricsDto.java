package es.caib.comanda.monitor.logic.intf.model.performance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Resposta unificada de mètriques internes de rendiment.
 * <p>
 * Organitzada per categories independents al nivell arrel ({@code datasources},
 * {@code workerPools} i {@code jmsQueues}) sincronitzades sobre el mateix rellotge de mostreig.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceMetricsDto {

    private LocalDateTime collectedAt;
    private int samplingIntervalSeconds;
    private int maxHistoryPoints;
    private int windowSeconds;
    private int bucketSeconds;
    private Integer previewBucketSeconds;
    private boolean collectionEnabled = true;
    private long estimatedMemoryBytes;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private List<DatasourcePoolMetricsDto> datasources = Collections.emptyList();
    private List<WorkerPoolMetricsDto> workerPools = Collections.emptyList();
    private List<JmsQueueMetricsDto> jmsQueues = Collections.emptyList();
}
