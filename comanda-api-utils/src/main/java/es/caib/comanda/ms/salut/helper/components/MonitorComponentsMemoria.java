package es.caib.comanda.ms.salut.helper.components;

import lombok.Getter;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Monitor en memòria per components dinàmics (String componentId).
 *
 * - registraExit(componentId, duracioMs)
 * - registraError(componentId)
 * - obtenSnapshot(): retorna estadístiques per component i reseteja el període (reset-on-read)
 * - obtenFallback(componentId): ok/ko de la finestra del component
 */
public final class MonitorComponentsMemoria {

    private final Clock rellotge;
    @Getter
    private final int midaFinestraFallback;

    // Entrades per component (creació lazy)
    private final ConcurrentHashMap<String, Entrada> entrades = new ConcurrentHashMap<>();

    public MonitorComponentsMemoria(int midaFinestraFallback) {
        this(midaFinestraFallback, Clock.systemUTC());
    }

    public MonitorComponentsMemoria(int midaFinestraFallback, Clock rellotge) {
        if (midaFinestraFallback <= 0) throw new IllegalArgumentException("midaFinestraFallback ha de ser > 0");
        this.midaFinestraFallback = midaFinestraFallback;
        this.rellotge = Objects.requireNonNull(rellotge);
    }

    public void registraExit(String componentId, long duracioMs) {
        if (componentId == null || componentId.isBlank()) return;
        if (duracioMs < 0) duracioMs = 0;

        Entrada e = entrades.get(componentId);
        if (e == null) {
            e = entrades.computeIfAbsent(componentId, id -> new Entrada(midaFinestraFallback));
        }

        // Totals
        e.okTotal.increment();
        e.sumaLatenciaMsTotal.add(duracioMs);

        // Període
        e.okPeriode.increment();
        e.sumaLatenciaMsPeriode.add(duracioMs);

        // Fallback per component
        e.finestra.afegeix(true);
    }

    public void registraError(String componentId) {
        if (componentId == null || componentId.isBlank()) return;

        Entrada e = entrades.get(componentId);
        if (e == null) {
            e = entrades.computeIfAbsent(componentId, id -> new Entrada(midaFinestraFallback));
        }

        // Totals
        e.errorTotal.increment();

        // Període
        e.errorPeriode.increment();

        // Fallback per component
        e.finestra.afegeix(false);
    }

    /**
     * Inicialitza un component si no existeix.
     * Útil per pre-registrar components que s'han de monitoritzar.
     */
    public void inicialitzaComponent(String componentId) {
        inicialitzaComponent(componentId, null);
    }

    /**
     * Inicialitza un component si no existeix, opcionalment indicant un endpoint.
     */
    public void inicialitzaComponent(String componentId, String endpoint) {
        if (componentId == null || componentId.isBlank()) return;
        entrades.compute(componentId, (id, existent) -> {
            if (existent == null) {
                Entrada n = new Entrada(midaFinestraFallback);
                n.endpoint = endpoint;
                return n;
            }
            if (endpoint != null) {
                existent.endpoint = endpoint;
            }
            return existent;
        });
    }

    /**
     * Snapshot del període i totals.
     * IMPORTANT: reseteja el període de tots els components després de retornar.
     */
    public Map<String, EstadistiquesComponent> obtenSnapshot() {
        Instant ara = Instant.now(rellotge);

        // Copiam a un HashMap per no exposar la concurrència interna
        Map<String, EstadistiquesComponent> out = new HashMap<>();

        for (Map.Entry<String, Entrada> ent : entrades.entrySet()) {
            out.put(ent.getKey(), ent.getValue().creaSnapshot(ent.getKey(), ara));
        }

        // Reset del període (sense tocar la finestra)
        for (Entrada e : entrades.values()) {
            e.resetejaPeriode();
        }

        return Collections.unmodifiableMap(out);
    }

    public DadesFallback obtenFallback(String componentId) {
        Entrada e = entrades.get(componentId);
        if (e == null) return new DadesFallback(0, 0, false, null);
        boolean te = !e.finestra.esBuida();
        return new DadesFallback(e.finestra.getOk(), e.finestra.getKo(), te, e.endpoint);
    }

    public static final class DadesFallback {
        public final long ok;
        public final long ko;
        public final boolean te;
        public final String endpoint;

        public DadesFallback(long ok, long ko, boolean te, String endpoint) {
            this.ok = ok;
            this.ko = ko;
            this.te = te;
            this.endpoint = endpoint;
        }
    }

    private static final class Entrada {
        String endpoint;
        // Totals
        final LongAdder okTotal = new LongAdder();
        final LongAdder errorTotal = new LongAdder();
        final LongAdder sumaLatenciaMsTotal = new LongAdder();

        // Període
        final LongAdder okPeriode = new LongAdder();
        final LongAdder errorPeriode = new LongAdder();
        final LongAdder sumaLatenciaMsPeriode = new LongAdder();

        // Fallback per component
        final FinestraBooleanaCircular finestra;

        Entrada(int midaFinestraFallback) {
            this.finestra = new FinestraBooleanaCircular(midaFinestraFallback);
        }

        EstadistiquesComponent creaSnapshot(String componentId, Instant ara) {
            long okP = okPeriode.sum();
            long koP = errorPeriode.sum();
            double migP = okP > 0 ? (sumaLatenciaMsPeriode.sum() * 1.0) / okP : 0.0;

            long okT = okTotal.sum();
            long koT = errorTotal.sum();
            double migT = okT > 0 ? (sumaLatenciaMsTotal.sum() * 1.0) / okT : 0.0;

            return new EstadistiquesComponent(
                    componentId,
                    endpoint,
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
