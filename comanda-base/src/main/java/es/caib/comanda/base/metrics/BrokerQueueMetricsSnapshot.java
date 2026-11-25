package es.caib.comanda.base.metrics;

/**
 * Instantània immutable de les mètriques natives d'una cua del broker.
 */
public class BrokerQueueMetricsSnapshot {

    private final String queueName;
    private final String address;
    private final long messageCount;
    private final int deliveringCount;
    private final int consumerCount;
    private final long messagesAdded;
    private final long messagesAcknowledged;

    public BrokerQueueMetricsSnapshot(
            String queueName,
            String address,
            long messageCount,
            int deliveringCount,
            int consumerCount,
            long messagesAdded,
            long messagesAcknowledged) {
        this.queueName = queueName;
        this.address = address;
        this.messageCount = messageCount;
        this.deliveringCount = deliveringCount;
        this.consumerCount = consumerCount;
        this.messagesAdded = messagesAdded;
        this.messagesAcknowledged = messagesAcknowledged;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getAddress() {
        return address;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public int getDeliveringCount() {
        return deliveringCount;
    }

    public int getConsumerCount() {
        return consumerCount;
    }

    public long getMessagesAdded() {
        return messagesAdded;
    }

    public long getMessagesAcknowledged() {
        return messagesAcknowledged;
    }

    /**
     * Nombre de missatges pendents en cua esperant ser assignats a un consumidor.
     */
    public long getPendingCount() {
        return Math.max(0L, messageCount - deliveringCount);
    }
}
