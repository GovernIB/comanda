package es.caib.comanda.broker.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests per als DTOs del Broker")
class BrokerDTOTest {

    @Test
    @DisplayName("BrokerInfoDTO builder i accessors")
    void brokerInfoDTO_builderAndAccessors() {
        BrokerInfoDTO dto = BrokerInfoDTO.builder()
                .version("2.25.0")
                .name("Artemis-01")
                .status("STARTED")
                .uptime("Running")
                .memoryUsage(1024L)
                .diskUsage(2048L)
                .totalConnections(10)
                .totalQueues(5)
                .totalMessages(100)
                .build();

        assertThat(dto.getVersion()).isEqualTo("2.25.0");
        assertThat(dto.getName()).isEqualTo("Artemis-01");
        assertThat(dto.getStatus()).isEqualTo("STARTED");
        assertThat(dto.getUptime()).isEqualTo("Running");
        assertThat(dto.getMemoryUsage()).isEqualTo(1024L);
        assertThat(dto.getDiskUsage()).isEqualTo(2048L);
        assertThat(dto.getTotalConnections()).isEqualTo(10);
        assertThat(dto.getTotalQueues()).isEqualTo(5);
        assertThat(dto.getTotalMessages()).isEqualTo(100);
    }

    @Test
    @DisplayName("QueueInfoDTO builder i accessors")
    void queueInfoDTO_builderAndAccessors() {
        QueueInfoDTO dto = QueueInfoDTO.builder()
                .name("test-queue")
                .address("test-address")
                .routingType("ANYCAST")
                .durable(true)
                .messageCount(10L)
                .consumerCount(2L)
                .deliveringCount(1L)
                .messagesAdded(50L)
                .messagesAcknowledged(40L)
                .filter("type='SMS'")
                .temporary(false)
                .autoCreated(true)
                .purgeOnNoConsumers(false)
                .maxConsumers(100)
                .build();

        assertThat(dto.getName()).isEqualTo("test-queue");
        assertThat(dto.getAddress()).isEqualTo("test-address");
        assertThat(dto.getRoutingType()).isEqualTo("ANYCAST");
        assertThat(dto.isDurable()).isTrue();
        assertThat(dto.getMessageCount()).isEqualTo(10L);
        assertThat(dto.getConsumerCount()).isEqualTo(2L);
        assertThat(dto.getDeliveringCount()).isEqualTo(1L);
        assertThat(dto.getMessagesAdded()).isEqualTo(50L);
        assertThat(dto.getMessagesAcknowledged()).isEqualTo(40L);
        assertThat(dto.getFilter()).isEqualTo("type='SMS'");
        assertThat(dto.isTemporary()).isFalse();
        assertThat(dto.isAutoCreated()).isTrue();
        assertThat(dto.isPurgeOnNoConsumers()).isFalse();
        assertThat(dto.getMaxConsumers()).isEqualTo(100);
    }

    @Test
    @DisplayName("MessageInfoDTO builder i accessors")
    void messageInfoDTO_builderAndAccessors() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 3600000);
        MessageInfoDTO dto = MessageInfoDTO.builder()
                .messageID("ID:12345")
                .queueName("test-queue")
                .timestamp(now)
                .type("TEXT")
                .durable(true)
                .priority(4)
                .size(512L)
                .properties(Collections.singletonMap("prop", "value"))
                .content("Hello World")
                .redelivered(false)
                .deliveryCount(1L)
                .expirationTime(expiry)
                .build();

        assertThat(dto.getMessageID()).isEqualTo("ID:12345");
        assertThat(dto.getQueueName()).isEqualTo("test-queue");
        assertThat(dto.getTimestamp()).isEqualTo(now);
        assertThat(dto.getType()).isEqualTo("TEXT");
        assertThat(dto.isDurable()).isTrue();
        assertThat(dto.getPriority()).isEqualTo(4);
        assertThat(dto.getSize()).isEqualTo(512L);
        assertThat(dto.getProperties()).containsEntry("prop", "value");
        assertThat(dto.getContent()).isEqualTo("Hello World");
        assertThat(dto.isRedelivered()).isFalse();
        assertThat(dto.getDeliveryCount()).isEqualTo(1L);
        assertThat(dto.getExpirationTime()).isEqualTo(expiry);
    }
}
