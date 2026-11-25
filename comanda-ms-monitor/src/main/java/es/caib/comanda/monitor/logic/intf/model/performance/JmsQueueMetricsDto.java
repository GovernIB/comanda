package es.caib.comanda.monitor.logic.intf.model.performance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Estat actual i històric recent d'una cua JMS i els seus listeners.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JmsQueueMetricsDto {

    private String queueName;
    private int listenerCount;
    private Integer minConsumers;
    private Integer maxConsumers;
    private Integer consumerCount;
    private Integer deliveringCount;
    private Long pendingCount;
    private Long messageCount;
    private Long completedMessages;
    private Long addedMessages;
    private List<JmsQueueSampleDto> history = Collections.emptyList();
    private List<JmsQueueSampleDto> preview;
}
