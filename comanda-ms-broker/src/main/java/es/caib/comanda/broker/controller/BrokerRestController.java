package es.caib.comanda.broker.controller;

import es.caib.comanda.broker.dto.BrokerInfoDTO;
import es.caib.comanda.broker.dto.MessageInfoDTO;
import es.caib.comanda.broker.dto.QueueInfoDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.Queue;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.QueueBrowser;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static es.caib.comanda.ms.broker.model.Cues.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/broker")
@Tag(name = "23. Broker", description = "Servei de gestió del broker")
@PreAuthorize("hasRole(T(es.caib.comanda.base.config.BaseConfig).ROLE_ADMIN)")
public class BrokerRestController {

    private final ActiveMQServer activeMQServer;
    private final JmsTemplate jmsTemplate;

    private static final String[] QUEUE_NAMES = { CUA_TASQUES, CUA_AVISOS, CUA_PERMISOS };

    /**
     * Get broker information
     * @return Broker information
     */
    @GetMapping
    public ResponseEntity<BrokerInfoDTO> getBrokerInfo() {
        try {
            String version = activeMQServer.getVersion().getFullVersion();
            String name = activeMQServer.getConfiguration().getName();
            String status = activeMQServer.isStarted() ? "STARTED" : "STOPPED";

            // Calculate uptime - using a fixed value for demo
            String uptime = "Running";

            // Get memory usage
            Runtime runtime = Runtime.getRuntime();
            long memoryUsage = runtime.totalMemory() - runtime.freeMemory();

            // Get disk usage - using a fixed value for demo
            long diskUsage = 0;

            // Get connection count
            int totalConnections = activeMQServer.getConnectionCount();

            // Get queue count and message count
            Set<Queue> queues = new HashSet<>();
            try {
                // Get predefined queues from ArtemisEmbeddedConfig
                for (String queueName : QUEUE_NAMES) {
                    Queue queue = activeMQServer.locateQueue(SimpleString.toSimpleString(queueName));
                    if (queue != null) {
                        queues.add(queue);
                    }
                }
            } catch (Exception e) {
                log.error("Error getting queues", e);
            }

            int totalQueues = queues.size();
            int totalMessages = 0;
            for (Queue queue : queues) {
                totalMessages += queue.getMessageCount();
            }

            BrokerInfoDTO brokerInfo = BrokerInfoDTO.builder()
                    .version(version)
                    .name(name)
                    .status(status)
                    .uptime(uptime)
                    .memoryUsage(memoryUsage)
                    .diskUsage(diskUsage)
                    .totalConnections(totalConnections)
                    .totalQueues(totalQueues)
                    .totalMessages(totalMessages)
                    .build();

            return ResponseEntity.ok(brokerInfo);
        } catch (Exception e) {
            log.error("Error getting broker info", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all queues
     * @return List of queues
     */
    @GetMapping("/queues")
    public ResponseEntity<List<QueueInfoDTO>> getQueues() {
        try {
            List<QueueInfoDTO> queueInfos = new ArrayList<>();

            // Get predefined queues from ArtemisEmbeddedConfig
            for (String queueName : QUEUE_NAMES) {
                Queue queue = activeMQServer.locateQueue(SimpleString.toSimpleString(queueName));
                if (queue != null) {
                    queueInfos.add(mapQueueToDTO(queue));
                }
            }

            return ResponseEntity.ok(queueInfos);
        } catch (Exception e) {
            log.error("Error getting queues", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get queue by name
     * @param queueName Queue name
     * @return Queue information
     */
    @GetMapping("/queues/{queueName}")
    public ResponseEntity<QueueInfoDTO> getQueue(@PathVariable String queueName) {
        try {
            Queue queue = activeMQServer.locateQueue(SimpleString.toSimpleString(queueName));
            if (queue == null) {
                return ResponseEntity.notFound().build();
            }

            QueueInfoDTO queueInfo = mapQueueToDTO(queue);
            return ResponseEntity.ok(queueInfo);
        } catch (Exception e) {
            log.error("Error getting queue: " + queueName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create a new queue
     * @param queueName Queue name
     * @param durable Whether the queue is durable
     * @return Created queue information
     */
    @PostMapping("/queues/{queueName}")
    public ResponseEntity<QueueInfoDTO> createQueue(
            @PathVariable String queueName,
            @RequestParam(defaultValue = "true") boolean durable) {
        try {
            // Check if queue already exists
            Queue existingQueue = activeMQServer.locateQueue(SimpleString.toSimpleString(queueName));
            if (existingQueue != null) {
                return ResponseEntity.badRequest().build();
            }

            // Create queue
            SimpleString queueNameSS = SimpleString.toSimpleString(queueName);
            QueueConfiguration queueConfig = new QueueConfiguration(queueNameSS)
                    .setAddress(queueNameSS)
                    .setRoutingType(RoutingType.ANYCAST)
                    .setDurable(durable);

            activeMQServer.createQueue(queueConfig);

            // Get created queue
            Queue createdQueue = activeMQServer.locateQueue(queueNameSS);
            QueueInfoDTO queueInfo = mapQueueToDTO(createdQueue);

            return ResponseEntity.ok(queueInfo);
        } catch (Exception e) {
            log.error("Error creating queue: " + queueName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete a queue
     * @param queueName Queue name
     * @return Success status
     */
    @DeleteMapping("/queues/{queueName}")
    public ResponseEntity<Void> deleteQueue(@PathVariable String queueName) {
        try {
            // Check if queue exists
            SimpleString queueNameSS = SimpleString.toSimpleString(queueName);
            Queue existingQueue = activeMQServer.locateQueue(queueNameSS);
            if (existingQueue == null) {
                return ResponseEntity.notFound().build();
            }

            // Delete queue
            activeMQServer.destroyQueue(queueNameSS);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting queue: " + queueName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get messages from a queue using JMS
     * @param queueName Queue name
     * @return List of messages
     */
    @GetMapping("/queues/{queueName}/messages")
    public ResponseEntity<List<MessageInfoDTO>> getMessages(@PathVariable String queueName) {
        try {
            // Check if queue exists
            Queue queue = activeMQServer.locateQueue(SimpleString.toSimpleString(queueName));
            if (queue == null) {
                return ResponseEntity.notFound().build();
            }

            // Use JmsTemplate to browse messages
            List<MessageInfoDTO> messages = new ArrayList<>();

            jmsTemplate.execute(session -> {
                try {
                    QueueBrowser browser = session.createBrowser((javax.jms.Queue) session.createQueue(queueName));
                    Enumeration<?> enumeration = browser.getEnumeration();

                    while (enumeration.hasMoreElements()) {
                        javax.jms.Message message = (javax.jms.Message) enumeration.nextElement();
                        MessageInfoDTO messageInfo = convertJmsMessageToDTO(message, queueName);
                        messages.add(messageInfo);
                    }

                    browser.close();
                } catch (Exception e) {
                    log.error("Error browsing messages", e);
                }
                return null;
            });

            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error getting messages from queue: " + queueName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete a message from a queue
     * @param queueName Queue name
     * @param messageID Message ID
     * @return Success status
     */
    @DeleteMapping("/queues/{queueName}/messages/{messageID}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable String queueName, 
            @PathVariable String messageID) {
        try {
            // Check if queue exists
            Queue queue = activeMQServer.locateQueue(SimpleString.toSimpleString(queueName));
            if (queue == null) {
                return ResponseEntity.notFound().build();
            }

            // Use JMS to remove the message
            boolean[] removed = {false};

            jmsTemplate.execute(session -> {
                try {
                    // Create a consumer with a selector for the specific message ID
                    MessageConsumer consumer = session.createConsumer(
                            session.createQueue(queueName),
                            "JMSMessageID='" + messageID + "'");

                    // Receive the message with a short timeout
                    javax.jms.Message message = consumer.receive(1000);
                    if (message != null) {
                        removed[0] = true;
                    }

                    consumer.close();
                } catch (Exception e) {
                    log.error("Error removing message", e);
                }
                return null;
            });

            if (removed[0]) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting message: " + messageID + " from queue: " + queueName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Purge all messages from a queue
     * @param queueName Queue name
     * @return Success status
     */
    @DeleteMapping("/queues/{queueName}/messages")
    public ResponseEntity<Void> purgeQueue(@PathVariable String queueName) {
        try {
            // Check if queue exists
            Queue queue = activeMQServer.locateQueue(SimpleString.toSimpleString(queueName));
            if (queue == null) {
                return ResponseEntity.notFound().build();
            }

            // Use JMS to consume and discard all messages
            jmsTemplate.execute(session -> {
                try {
                    // Create a consumer for the queue
                    MessageConsumer consumer = session.createConsumer(session.createQueue(queueName));

                    // Receive and discard messages until there are no more
                    while (consumer.receive(100) != null) {
                        // Just discard the message
                    }

                    consumer.close();
                } catch (Exception e) {
                    log.error("Error consuming messages", e);
                }
                return null;
            });

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error purging queue: " + queueName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Map a Queue to QueueInfoDTO
     * @param queue Queue
     * @return QueueInfoDTO
     */
    private QueueInfoDTO mapQueueToDTO(Queue queue) {
        return QueueInfoDTO.builder()
                .name(queue.getName().toString())
                .address(queue.getAddress().toString())
                .routingType(queue.getRoutingType().toString())
                .durable(queue.isDurable())
                .messageCount(queue.getMessageCount())
                .consumerCount(queue.getConsumerCount())
                .deliveringCount(queue.getDeliveringCount())
                .messagesAdded(queue.getMessagesAdded())
                .messagesAcknowledged(queue.getMessagesAcknowledged())
                .filter(queue.getFilter() != null ? queue.getFilter().getFilterString().toString() : null)
                .temporary(queue.isTemporary())
                .autoCreated(queue.isAutoCreated())
                .purgeOnNoConsumers(queue.isPurgeOnNoConsumers())
                .maxConsumers(queue.getMaxConsumers())
                .build();
    }

    /**
     * Convert a JMS Message to MessageInfoDTO
     * @param message JMS Message
     * @param queueName Queue name
     * @return MessageInfoDTO
     */
    private MessageInfoDTO convertJmsMessageToDTO(javax.jms.Message message, String queueName) throws Exception {
        Map<String, Object> properties = new HashMap<>();
        Enumeration<?> propertyNames = message.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String name = (String) propertyNames.nextElement();
            properties.put(name, message.getObjectProperty(name));
        }

        String content = "";
        if (message instanceof TextMessage) {
            content = ((TextMessage) message).getText();
        } else if (message instanceof ObjectMessage) {
            Object obj = ((ObjectMessage) message).getObject();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            content = "Binary content (serialized object): " + obj.getClass().getName();
        } else if (message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            bytesMessage.reset();
            byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytes);
            content = "Binary content (bytes): " + bytes.length + " bytes";
        } else if (message instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) message;
            StringBuilder sb = new StringBuilder();
            Enumeration<?> mapNames = mapMessage.getMapNames();
            while (mapNames.hasMoreElements()) {
                String name = (String) mapNames.nextElement();
                sb.append(name).append(": ").append(mapMessage.getObject(name)).append(", ");
            }
            content = "Map content: {" + sb.toString() + "}";
        } else if (message instanceof StreamMessage) {
            content = "Stream content (not displayed)";
        }

        return MessageInfoDTO.builder()
                .messageID(message.getJMSMessageID())
                .queueName(queueName)
                .timestamp(new Date(message.getJMSTimestamp()))
                .type(message.getJMSType())
                .durable(true) // JMS messages are durable by default
                .priority(message.getJMSPriority())
                .size(0) // Not available in JMS API
                .properties(properties)
                .content(content)
                .redelivered(message.getJMSRedelivered())
                .deliveryCount(0) // Not available in JMS API
                .expirationTime(message.getJMSExpiration() > 0 ? new Date(message.getJMSExpiration()) : null)
                .build();
    }

}
