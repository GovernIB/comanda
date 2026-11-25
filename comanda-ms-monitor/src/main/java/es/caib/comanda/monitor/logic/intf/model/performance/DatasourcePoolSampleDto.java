package es.caib.comanda.monitor.logic.intf.model.performance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Punt històric sincronitzat d'un pool de connexions JDBC.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasourcePoolSampleDto {

    private LocalDateTime timestamp;
    private Integer inUseCount;
    private Integer activeCount;
    private Long waitCount;
    private Long maxWaitTimeMs;
}
