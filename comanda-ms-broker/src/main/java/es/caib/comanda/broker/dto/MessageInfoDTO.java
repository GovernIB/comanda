package es.caib.comanda.broker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * DTO for message information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageInfoDTO {
    private String messageID;
    private String queueName;
    private Date timestamp;
    private String type;
    private boolean durable;
    private int priority;
    private long size;
    private Map<String, Object> properties;
    private String content;
    private boolean redelivered;
    private long deliveryCount;
    private Date expirationTime;
}