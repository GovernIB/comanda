package es.caib.comanda.base.metrics;

import java.util.Map;

/**
 * Proveïdor SPI de mètriques de cues del broker per evitar acoblament directe
 * entre {@code comanda-ms-monitor} i {@code comanda-ms-broker}.
 */
public interface BrokerQueueMetricsProvider {

    /**
     * Recull les instantànies actuals de les cues disponibles indexades per nom de cua.
     */
    Map<String, BrokerQueueMetricsSnapshot> collectQueueMetrics();
}
