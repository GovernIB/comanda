package es.caib.comanda.broker.metrics;

import es.caib.comanda.base.metrics.BrokerQueueMetricsSnapshot;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.postoffice.Binding;
import org.apache.activemq.artemis.core.postoffice.PostOffice;
import org.apache.activemq.artemis.core.postoffice.QueueBinding;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.Queue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a ArtemisBrokerQueueMetricsProvider")
class ArtemisBrokerQueueMetricsProviderTest {

    @Mock
    private ActiveMQServer activeMQServer;

    @InjectMocks
    private ArtemisBrokerQueueMetricsProvider provider;

    @Test
    @DisplayName("Retorna instantànies de cues normals i filtra cues internes, temporals i activemq.*")
    void collectQueueMetrics_retornaCuesNormalsIFiltraInternes() {
        PostOffice postOffice = mock(PostOffice.class);
        when(activeMQServer.isStarted()).thenReturn(true);
        when(activeMQServer.getPostOffice()).thenReturn(postOffice);

        QueueBinding tasques = mockQueueBinding("tasquesQueue", "tasquesQueue", false, false, 15L, 4, 5, 100L, 85L);
        QueueBinding internal = mockSkippedQueueBinding("internalQueue", true, false);
        QueueBinding temp = mockSkippedQueueBinding("tempQueue", false, true);
        QueueBinding sys = mockNamedSkippedQueueBinding("activemq.management");
        Binding nonQueueBinding = mock(Binding.class);

        when(postOffice.getAllBindings()).thenReturn(Stream.of(tasques, internal, temp, sys, nonQueueBinding));

        Map<String, BrokerQueueMetricsSnapshot> result = provider.collectQueueMetrics();

        assertThat(result).containsOnlyKeys("tasquesQueue");
        BrokerQueueMetricsSnapshot snap = result.get("tasquesQueue");
        assertThat(snap.getAddress()).isEqualTo("tasquesQueue");
        assertThat(snap.getMessageCount()).isEqualTo(15L);
        assertThat(snap.getDeliveringCount()).isEqualTo(4);
        assertThat(snap.getPendingCount()).isEqualTo(11L);
        assertThat(snap.getConsumerCount()).isEqualTo(5);
        assertThat(snap.getMessagesAdded()).isEqualTo(100L);
        assertThat(snap.getMessagesAcknowledged()).isEqualTo(85L);
    }

    @Test
    @DisplayName("Si messageCount < deliveringCount puntualment, getPendingCount() retorna 0")
    void snapshot_pendingCountMaiNegatiu() {
        BrokerQueueMetricsSnapshot snap = new BrokerQueueMetricsSnapshot(
                "avisosQueue", "avisosQueue", 1L, 3, 5, 10L, 9L
        );
        assertThat(snap.getPendingCount()).isZero();
    }

    @Test
    @DisplayName("Quan activeMQServer no ha arrencat o és nul, retorna mapa buit")
    void collectQueueMetrics_quanServidorAturatONul_retornaBuit() {
        when(activeMQServer.isStarted()).thenReturn(false);
        assertThat(provider.collectQueueMetrics()).isEmpty();

        ArtemisBrokerQueueMetricsProvider withoutServer = new ArtemisBrokerQueueMetricsProvider();
        assertThat(withoutServer.collectQueueMetrics()).isEmpty();
    }

    private QueueBinding mockQueueBinding(
            String name,
            String address,
            boolean internal,
            boolean temp,
            long messageCount,
            int delivering,
            int consumers,
            long added,
            long acked) {
        Queue queue = mock(Queue.class);
        when(queue.isInternalQueue()).thenReturn(internal);
        when(queue.isTemporary()).thenReturn(temp);
        when(queue.getName()).thenReturn(SimpleString.toSimpleString(name));
        when(queue.getAddress()).thenReturn(SimpleString.toSimpleString(address));
        when(queue.getMessageCount()).thenReturn(messageCount);
        when(queue.getDeliveringCount()).thenReturn(delivering);
        when(queue.getConsumerCount()).thenReturn(consumers);
        when(queue.getMessagesAdded()).thenReturn(added);
        when(queue.getMessagesAcknowledged()).thenReturn(acked);

        QueueBinding binding = mock(QueueBinding.class);
        when(binding.getQueue()).thenReturn(queue);
        return binding;
    }

    private QueueBinding mockSkippedQueueBinding(String name, boolean internal, boolean temp) {
        Queue queue = mock(Queue.class);
        when(queue.isInternalQueue()).thenReturn(internal);
        if (!internal) {
            when(queue.isTemporary()).thenReturn(temp);
        }
        QueueBinding binding = mock(QueueBinding.class);
        when(binding.getQueue()).thenReturn(queue);
        return binding;
    }

    private QueueBinding mockNamedSkippedQueueBinding(String name) {
        Queue queue = mock(Queue.class);
        when(queue.isInternalQueue()).thenReturn(false);
        when(queue.isTemporary()).thenReturn(false);
        when(queue.getName()).thenReturn(SimpleString.toSimpleString(name));
        QueueBinding binding = mock(QueueBinding.class);
        when(binding.getQueue()).thenReturn(queue);
        return binding;
    }
}
