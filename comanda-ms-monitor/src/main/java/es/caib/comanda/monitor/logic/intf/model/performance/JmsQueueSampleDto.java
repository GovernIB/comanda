package es.caib.comanda.monitor.logic.intf.model.performance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Punt històric sincronitzat d'una cua JMS i els seus consumidors.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JmsQueueSampleDto {

    private LocalDateTime timestamp;
    private Integer consumerCount;
    private Integer deliveringCount;
    private Long pendingCount;
    private Long completedMessages;
    private Long addedMessages;
}
