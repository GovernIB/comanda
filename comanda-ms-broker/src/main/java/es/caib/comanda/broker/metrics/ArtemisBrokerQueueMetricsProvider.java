package es.caib.comanda.broker.metrics;

import es.caib.comanda.base.metrics.BrokerQueueMetricsProvider;
import es.caib.comanda.base.metrics.BrokerQueueMetricsSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.core.postoffice.Binding;
import org.apache.activemq.artemis.core.postoffice.PostOffice;
import org.apache.activemq.artemis.core.postoffice.QueueBinding;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Implementació de {@link BrokerQueueMetricsProvider} sobre el servidor Artemis incrustat.
 */
@Component
@Slf4j
public class ArtemisBrokerQueueMetricsProvider implements BrokerQueueMetricsProvider {

    @Autowired(required = false)
    private ActiveMQServer activeMQServer;

    @Override
    public Map<String, BrokerQueueMetricsSnapshot> collectQueueMetrics() {
        if (activeMQServer == null || !activeMQServer.isStarted()) {
            return Collections.emptyMap();
        }
        PostOffice postOffice = activeMQServer.getPostOffice();
        if (postOffice == null) {
            return Collections.emptyMap();
        }

        Map<String, BrokerQueueMetricsSnapshot> result = new LinkedHashMap<>();
        try (Stream<Binding> bindings = postOffice.getAllBindings()) {
            if (bindings == null) {
                return result;
            }
            bindings.forEach(binding -> collectBinding(binding, result));
        } catch (Exception e) {
            log.debug("No s'han pogut recollir les mètriques de les cues d'Artemis: {}", e.getMessage());
        }
        return result;
    }

    private void collectBinding(Binding binding, Map<String, BrokerQueueMetricsSnapshot> out) {
        if (!(binding instanceof QueueBinding)) {
            return;
        }
        try {
            Queue queue = ((QueueBinding) binding).getQueue();
            if (queue == null || queue.isInternalQueue() || queue.isTemporary()) {
                return;
            }
            String name = queue.getName() != null ? queue.getName().toString() : null;
            if (name == null || name.isBlank() || name.startsWith("activemq.")) {
                return;
            }
            String address = queue.getAddress() != null ? queue.getAddress().toString() : name;
            out.put(name, new BrokerQueueMetricsSnapshot(
                    name,
                    address,
                    queue.getMessageCount(),
                    queue.getDeliveringCount(),
                    queue.getConsumerCount(),
                    queue.getMessagesAdded(),
                    queue.getMessagesAcknowledged()
            ));
        } catch (Exception e) {
            log.debug("Error llegint una cua d'Artemis: {}", e.getMessage());
        }
    }
}
