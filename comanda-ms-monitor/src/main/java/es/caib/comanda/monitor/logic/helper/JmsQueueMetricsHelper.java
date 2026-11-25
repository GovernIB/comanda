package es.caib.comanda.monitor.logic.helper;

import es.caib.comanda.base.metrics.BrokerQueueMetricsProvider;
import es.caib.comanda.base.metrics.BrokerQueueMetricsSnapshot;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Descobreix i recull mètriques dels listeners JMS registrats i de les cues del broker Artemis.
 */
@Component
@Slf4j
public class JmsQueueMetricsHelper {

    @Autowired(required = false)
    private JmsListenerEndpointRegistry jmsListenerEndpointRegistry;

    @Autowired(required = false)
    private BrokerQueueMetricsProvider brokerQueueMetricsProvider;

    /**
     * Recull una instantània unificada de totes les cues JMS amb listeners i/o cua al broker.
     */
    public List<RawJmsQueueSample> collectAll() {
        Map<String, ListenerAggregate> listenersByQueue = collectListenerAggregates();
        Map<String, BrokerQueueMetricsSnapshot> brokerSnapshots = collectBrokerSnapshots();

        Set<String> allQueueNames = new LinkedHashSet<>(listenersByQueue.keySet());
        allQueueNames.addAll(brokerSnapshots.keySet());

        List<RawJmsQueueSample> samples = new ArrayList<>(allQueueNames.size());
        for (String queueName : allQueueNames) {
            ListenerAggregate listener = listenersByQueue.get(queueName);
            BrokerQueueMetricsSnapshot broker = brokerSnapshots.get(queueName);

            RawJmsQueueSample sample = new RawJmsQueueSample();
            sample.setPoolKey("jms:" + queueName);
            sample.setQueueName(queueName);
            sample.setListenerCount(listener != null ? listener.listenerCount : 0);

            if (listener != null && listener.hasDmlc) {
                sample.setMinConsumers(listener.minConsumers);
                sample.setMaxConsumers(listener.maxConsumers);
            }

            if (broker != null) {
                sample.setConsumerCount(broker.getConsumerCount());
                sample.setDeliveringCount(broker.getDeliveringCount());
                sample.setPendingCount(broker.getPendingCount());
                sample.setMessageCount(broker.getMessageCount());
                sample.setRawMessagesAdded(broker.getMessagesAdded());
                sample.setRawMessagesAcknowledged(broker.getMessagesAcknowledged());
            } else if (listener != null && listener.hasDmlc) {
                sample.setConsumerCount(listener.scheduledConsumers);
            }

            samples.add(sample);
        }

        samples.sort(Comparator
                .<RawJmsQueueSample, Integer>comparing(s -> s.getListenerCount() > 0 ? 0 : 1)
                .thenComparing(s -> s.getQueueName() != null ? s.getQueueName() : "", String.CASE_INSENSITIVE_ORDER));
        return samples;
    }

    private Map<String, ListenerAggregate> collectListenerAggregates() {
        if (jmsListenerEndpointRegistry == null) {
            return Collections.emptyMap();
        }
        Collection<MessageListenerContainer> containers = jmsListenerEndpointRegistry.getListenerContainers();
        if (containers == null || containers.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, ListenerAggregate> byQueue = new LinkedHashMap<>();
        for (MessageListenerContainer container : containers) {
            if (!(container instanceof AbstractMessageListenerContainer)) {
                continue;
            }
            AbstractMessageListenerContainer amlc = (AbstractMessageListenerContainer) container;
            String destination = amlc.getDestinationName();
            if (destination == null || destination.isBlank()) {
                continue;
            }
            ListenerAggregate agg = byQueue.computeIfAbsent(destination, k -> new ListenerAggregate());
            agg.listenerCount += 1;
            if (container instanceof DefaultMessageListenerContainer) {
                DefaultMessageListenerContainer dmlc = (DefaultMessageListenerContainer) container;
                agg.hasDmlc = true;
                agg.minConsumers += Math.max(0, dmlc.getConcurrentConsumers());
                agg.maxConsumers += Math.max(0, dmlc.getMaxConcurrentConsumers());
                agg.scheduledConsumers += Math.max(0, dmlc.getScheduledConsumerCount());
            }
        }
        return byQueue;
    }

    private Map<String, BrokerQueueMetricsSnapshot> collectBrokerSnapshots() {
        if (brokerQueueMetricsProvider == null) {
            return Collections.emptyMap();
        }
        try {
            Map<String, BrokerQueueMetricsSnapshot> snapshots = brokerQueueMetricsProvider.collectQueueMetrics();
            return snapshots != null ? snapshots : Collections.emptyMap();
        } catch (Exception e) {
            log.debug("Error recollint mètriques del broker JMS: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private static final class ListenerAggregate {
        private int listenerCount;
        private boolean hasDmlc;
        private int minConsumers;
        private int maxConsumers;
        private int scheduledConsumers;
    }

    @Data
    public static class RawJmsQueueSample {
        private String poolKey;
        private String queueName;
        private int listenerCount;
        private Integer minConsumers;
        private Integer maxConsumers;
        private Integer consumerCount;
        private Integer deliveringCount;
        private Long pendingCount;
        private Long messageCount;
        private Long rawMessagesAdded;
        private Long rawMessagesAcknowledged;
    }
}
