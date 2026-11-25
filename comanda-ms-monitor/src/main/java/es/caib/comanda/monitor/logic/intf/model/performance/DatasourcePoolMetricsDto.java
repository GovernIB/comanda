package es.caib.comanda.monitor.logic.intf.model.performance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Estat actual i històric recent d'un pool de datasource detectat.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasourcePoolMetricsDto {

    private String poolName;
    private String jndiName;
    private boolean statisticsEnabled;
    private WaitCountType waitCountType;
    private Integer minSize;
    private Integer maxSize;
    private Integer inUseCount;
    private Integer activeCount;
    private Long waitCount;
    private Long maxWaitTimeMs;
    private List<DatasourcePoolSampleDto> history = Collections.emptyList();
    private List<DatasourcePoolSampleDto> preview;
}
