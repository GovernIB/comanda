package es.caib.comanda.ms.salut.helper.components;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Monitor amb micrometer:
 * - Totals: Timer/Counter a micrometer
 * - Període: en memòria (reset-on-read)
 * - Fallback: finestra per subsistema (en memòria)
 */
public final class MonitorComponentsMicrometer<K extends Enum<K>> {

    private final Class<K> tipusEnum;
    private final Clock rellotge;
    private final MeterRegistry registry;
    private final String prefixMetrica;

    private final Map<K, Entrada> entrades;
    private final int midaFinestraFallback;

    public MonitorComponentsMicrometer(Class<K> tipusEnum,
                                       MeterRegistry registry,
                                       String prefixMetrica,
                                       int midaFinestraFallback) {
        this(tipusEnum, registry, prefixMetrica, midaFinestraFallback, Clock.systemUTC());
    }

    public MonitorComponentsMicrometer(Class<K> tipusEnum,
                                       MeterRegistry registry,
                                       String prefixMetrica,
                                       int midaFinestraFallback,
                                       Clock rellotge) {
        this.tipusEnum = Objects.requireNonNull(tipusEnum);
        this.registry = Objects.requireNonNull(registry);
        this.prefixMetrica = (prefixMetrica == null || prefixMetrica.isBlank()) ? "subsistema" : prefixMetrica;
        this.rellotge = Objects.requireNonNull(rellotge);

        if (midaFinestraFallback <= 0) throw new IllegalArgumentException("midaFinestraFallback ha de ser > 0");
        this.midaFinestraFallback = midaFinestraFallback;

        this.entrades = new ConcurrentHashMap<>();
        for (K k : tipusEnum.getEnumConstants()) {
            entrades.put(k, new Entrada(registry, this.prefixMetrica, k.name(), midaFinestraFallback));
        }
    }

    public void registraExit(K subsistema, long duracioMs) {
        if (duracioMs < 0) duracioMs = 0;
        Entrada e = entrades.get(subsistema);
        if (e == null) return;

        // micrometer (totals)
        e.timerOk.record(duracioMs, TimeUnit.MILLISECONDS);

        // totals en memòria (per poder calcular mitjanes al DTO)
        e.okTotal.increment();
        e.sumaLatenciaMsTotal.add(duracioMs);

        // període en memòria
        e.okPeriode.increment();
        e.sumaLatenciaMsPeriode.add(duracioMs);

        // fallback per subsistema
        e.finestra.afegeix(true);
    }

    public void registraError(K subsistema) {
        Entrada e = entrades.get(subsistema);
        if (e == null) return;

        // micrometer (totals)
        e.counterError.increment();

        // totals en memòria
        e.errorTotal.increment();

        // període en memòria
        e.errorPeriode.increment();

        // fallback per subsistema
        e.finestra.afegeix(false);
    }

    public Map<K, EstadistiquesComponent> obtenSnapshot() {
        Instant ara = Instant.now(rellotge);

        Map<K, EstadistiquesComponent> out = new EnumMap<>(tipusEnum);
        for (K k : tipusEnum.getEnumConstants()) {
            Entrada e = entrades.get(k);
            out.put(k, e.creaSnapshot(k.name(), ara));
        }

        // Reset del període (sense tocar finestra)
        for (Entrada e : entrades.values()) {
            e.resetejaPeriode();
        }

        return out;
    }

    public MonitorComponentsMemoria.DadesFallback obtenFallback(K subsistema) {
        Entrada e = entrades.get(subsistema);
        if (e == null) return new MonitorComponentsMemoria.DadesFallback(0,0,false);
        boolean te = !e.finestra.esBuida();
        return new MonitorComponentsMemoria.DadesFallback(e.finestra.getOk(), e.finestra.getKo(), te);
    }

    public int getMidaFinestraFallback() { return midaFinestraFallback; }

    private static final class Entrada {
        // micrometer totals
        final Timer timerOk;
        final Counter counterError;

        // totals en memòria (per estadístiques)
        final LongAdder okTotal = new LongAdder();
        final LongAdder errorTotal = new LongAdder();
        final LongAdder sumaLatenciaMsTotal = new LongAdder();

        // període en memòria
        final LongAdder okPeriode = new LongAdder();
        final LongAdder errorPeriode = new LongAdder();
        final LongAdder sumaLatenciaMsPeriode = new LongAdder();

        // fallback per subsistema
        final FinestraBooleanaCircular finestra;

        Entrada(MeterRegistry registry, String prefix, String codi, int midaFinestra) {
            this.finestra = new FinestraBooleanaCircular(midaFinestra);

            this.timerOk = Timer.builder(prefix + "." + codi.toLowerCase() + ".ok")
                    .description("Duració operacions OK")
                    .publishPercentileHistogram()
                    .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                    .register(registry);

            this.counterError = Counter.builder(prefix + "." + codi.toLowerCase() + ".errors")
                    .description("Nombre d'errors")
                    .register(registry);
        }

        EstadistiquesComponent creaSnapshot(String codi, Instant ara) {
            long okP = okPeriode.sum();
            long koP = errorPeriode.sum();
            double migP = okP > 0 ? (sumaLatenciaMsPeriode.sum() * 1.0) / okP : 0.0;

            long okT = okTotal.sum();
            long koT = errorTotal.sum();
            double migT = okT > 0 ? (sumaLatenciaMsTotal.sum() * 1.0) / okT : 0.0;

            return new EstadistiquesComponent(
                    codi,
                    okP, koP, migP,
                    okT, koT, migT,
                    ara
            );
        }

        void resetejaPeriode() {
            okPeriode.reset();
            errorPeriode.reset();
            sumaLatenciaMsPeriode.reset();
        }
    }
}
