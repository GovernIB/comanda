package es.caib.comanda.broker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for queue information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueInfoDTO {
    private String name;
    private String address;
    private String routingType;
    private boolean durable;
    private long messageCount;
    private long consumerCount;
    private long deliveringCount;
    private long messagesAdded;
    private long messagesAcknowledged;
    private String filter;
    private boolean temporary;
    private boolean autoCreated;
    private boolean purgeOnNoConsumers;
    private int maxConsumers;
}