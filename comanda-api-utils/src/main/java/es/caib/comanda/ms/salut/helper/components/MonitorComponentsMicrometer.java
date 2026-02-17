package es.caib.comanda.ms.salut.helper.components;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * “Decorador” micrometer:
 * - Exporta totals a micrometer (Timer/Counter) amb tag component=componentId
 * - Manté període/fallback via MonitorComponentsMemoria (core)
 *
 * Important: amb ~50 components, la cardinalitat de tags és baixa i acceptable.
 */
public final class MonitorComponentsMicrometer {

    private final MeterRegistry registry;
    private final String prefixMetrica;

    private final MonitorComponentsMemoria monitorCore;

    // Cache de meters per component (per no recrear-los)
    private final Map<String, Timer> timersOk = new ConcurrentHashMap<>();
    private final Map<String, Counter> countersError = new ConcurrentHashMap<>();

    public MonitorComponentsMicrometer(MeterRegistry registry,
                                       String prefixMetrica,
                                       MonitorComponentsMemoria monitorCore) {
        this.registry = Objects.requireNonNull(registry);
        this.prefixMetrica = (prefixMetrica == null || prefixMetrica.isBlank()) ? "component" : prefixMetrica;
        this.monitorCore = Objects.requireNonNull(monitorCore);
    }

    public void registraExit(String componentId, long duracioMs) {
        monitorCore.registraExit(componentId, duracioMs);

        // Timer amb tag component
        Timer t = timersOk.computeIfAbsent(componentId, id ->
                Timer.builder(prefixMetrica + ".ok")
                        .description("Duració operacions OK")
                        .tag("component", id)
                        .publishPercentileHistogram()
                        .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                        .register(registry)
        );
        t.record(Math.max(0, duracioMs), TimeUnit.MILLISECONDS);
    }

    public void registraError(String componentId) {
        monitorCore.registraError(componentId);

        // Counter amb tag component
        Counter c = countersError.computeIfAbsent(componentId, id ->
                Counter.builder(prefixMetrica + ".errors")
                        .description("Nombre d'errors")
                        .tag("component", id)
                        .register(registry)
        );
        c.increment();
    }

    // Exposam el core per obtenir snapshot/fallback
    public MonitorComponentsMemoria getMonitorCore() {
        return monitorCore;
    }
}
