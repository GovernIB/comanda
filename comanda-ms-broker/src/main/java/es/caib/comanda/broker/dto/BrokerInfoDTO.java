package es.caib.comanda.broker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for broker information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerInfoDTO {
    private String version;
    private String name;
    private String status;
    private String uptime;
    private long memoryUsage;
    private long diskUsage;
    private int totalConnections;
    private int totalQueues;
    private int totalMessages;
}