package es.caib.comanda.monitor.logic.helper;

import es.caib.comanda.base.metrics.BrokerQueueMetricsProvider;
import es.caib.comanda.base.metrics.BrokerQueueMetricsSnapshot;
import es.caib.comanda.monitor.logic.helper.JmsQueueMetricsHelper.RawJmsQueueSample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a JmsQueueMetricsHelper")
class JmsQueueMetricsHelperTest {

    @Mock
    private JmsListenerEndpointRegistry registry;

    @Mock
    private BrokerQueueMetricsProvider brokerProvider;

    @InjectMocks
    private JmsQueueMetricsHelper helper;

    @Test
    @DisplayName("Combina listeners JMS (incloent múltiples selectors per cua) amb instantànies del broker")
    void collectAll_combinaListenersIBroker() {
        DefaultMessageListenerContainer estApp = mockDmlc("netejaEntornAppEstadisticaQueue", 5, 50, 5);
        DefaultMessageListenerContainer estEntorn = mockDmlc("netejaEntornAppEstadisticaQueue", 5, 50, 5);
        DefaultMessageListenerContainer tasques = mockDmlc("tasquesQueue", 5, 50, 6);

        when(registry.getListenerContainers()).thenReturn(List.of(estApp, estEntorn, tasques));

        Map<String, BrokerQueueMetricsSnapshot> snapshots = new LinkedHashMap<>();
        snapshots.put("tasquesQueue", new BrokerQueueMetricsSnapshot("tasquesQueue", "tasquesQueue", 14L, 6, 8, 250L, 236L));
        snapshots.put("netejaEntornAppEstadisticaQueue", new BrokerQueueMetricsSnapshot(
                "netejaEntornAppEstadisticaQueue", "netejaEntornAppEstadisticaQueue", 0L, 0, 10, 40L, 40L
        ));
        snapshots.put("extraBrokerQueue", new BrokerQueueMetricsSnapshot("extraBrokerQueue", "extraBrokerQueue", 2L, 0, 0, 5L, 3L));
        when(brokerProvider.collectQueueMetrics()).thenReturn(snapshots);

        List<RawJmsQueueSample> samples = helper.collectAll();

        // Ordenació: cues amb listener (alfabèticament) abans que cues sense listener
        assertThat(samples).extracting(RawJmsQueueSample::getQueueName)
                .containsExactly("netejaEntornAppEstadisticaQueue", "tasquesQueue", "extraBrokerQueue");

        RawJmsQueueSample est = samples.get(0);
        assertThat(est.getListenerCount()).isEqualTo(2);
        assertThat(est.getMinConsumers()).isEqualTo(10);
        assertThat(est.getMaxConsumers()).isEqualTo(100);
        assertThat(est.getConsumerCount()).isEqualTo(10);
        assertThat(est.getRawMessagesAcknowledged()).isEqualTo(40L);

        RawJmsQueueSample tasquesSample = samples.get(1);
        assertThat(tasquesSample.getListenerCount()).isEqualTo(1);
        assertThat(tasquesSample.getMinConsumers()).isEqualTo(5);
        assertThat(tasquesSample.getMaxConsumers()).isEqualTo(50);
        assertThat(tasquesSample.getConsumerCount()).isEqualTo(8);
        assertThat(tasquesSample.getDeliveringCount()).isEqualTo(6);
        assertThat(tasquesSample.getPendingCount()).isEqualTo(8L);
        assertThat(tasquesSample.getMessageCount()).isEqualTo(14L);
        assertThat(tasquesSample.getRawMessagesAdded()).isEqualTo(250L);
        assertThat(tasquesSample.getRawMessagesAcknowledged()).isEqualTo(236L);

        RawJmsQueueSample extra = samples.get(2);
        assertThat(extra.getListenerCount()).isZero();
        assertThat(extra.getMinConsumers()).isNull();
        assertThat(extra.getPendingCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Si no hi ha broker provider disponible, usa scheduledConsumerCount dels listeners")
    void collectAll_senseBrokerProvider_retornaInfoDelsListeners() {
        DefaultMessageListenerContainer avisos = mockDmlc("avisosQueue", 5, 50, 5);
        when(registry.getListenerContainers()).thenReturn(List.of(avisos));

        JmsQueueMetricsHelper listenersOnly = new JmsQueueMetricsHelper();
        org.springframework.test.util.ReflectionTestUtils.setField(listenersOnly, "jmsListenerEndpointRegistry", registry);

        List<RawJmsQueueSample> samples = listenersOnly.collectAll();
        assertThat(samples).hasSize(1);
        assertThat(samples.get(0).getQueueName()).isEqualTo("avisosQueue");
        assertThat(samples.get(0).getConsumerCount()).isEqualTo(5);
        assertThat(samples.get(0).getPendingCount()).isNull();
        assertThat(samples.get(0).getRawMessagesAcknowledged()).isNull();
    }

    private DefaultMessageListenerContainer mockDmlc(String dest, int min, int max, int scheduled) {
        DefaultMessageListenerContainer dmlc = mock(DefaultMessageListenerContainer.class);
        when(dmlc.getDestinationName()).thenReturn(dest);
        when(dmlc.getConcurrentConsumers()).thenReturn(min);
        when(dmlc.getMaxConcurrentConsumers()).thenReturn(max);
        when(dmlc.getScheduledConsumerCount()).thenReturn(scheduled);
        return dmlc;
    }
}
