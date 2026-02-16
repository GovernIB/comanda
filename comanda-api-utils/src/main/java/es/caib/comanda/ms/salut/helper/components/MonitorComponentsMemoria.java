package es.caib.comanda.ms.salut.helper.components;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Monitor genèric en memòria (sense micrometer).
 * - Període = des de darrera consulta (reset-on-read)
 * - Total = des d'arrencada (acumulatiu)
 * - Fallback per subsistema amb finestra circular de N peticions
 */
public final class MonitorComponentsMemoria<K extends Enum<K>> {

    private final Class<K> tipusEnum;
    private final Clock rellotge;
    private final Map<K, Entrada> entrades;

    private final int midaFinestraFallback;

    public MonitorComponentsMemoria(Class<K> tipusEnum, int midaFinestraFallback) {
        this(tipusEnum, midaFinestraFallback, Clock.systemUTC());
    }

    public MonitorComponentsMemoria(Class<K> tipusEnum, int midaFinestraFallback, Clock rellotge) {
        this.tipusEnum = Objects.requireNonNull(tipusEnum);
        this.rellotge = Objects.requireNonNull(rellotge);

        if (midaFinestraFallback <= 0) throw new IllegalArgumentException("midaFinestraFallback ha de ser > 0");
        this.midaFinestraFallback = midaFinestraFallback;

        this.entrades = new ConcurrentHashMap<>();
        for (K k : tipusEnum.getEnumConstants()) {
            entrades.put(k, new Entrada(midaFinestraFallback));
        }
    }

    /**
     * Registra una operació OK amb duració (ms).
     */
    public void registraExit(K subsistema, long duracioMs) {
        if (duracioMs < 0) duracioMs = 0;
        Entrada e = entrades.get(subsistema);
        if (e == null) return;

        // totals
        e.okTotal.increment();
        e.sumaLatenciaMsTotal.add(duracioMs);

        // període
        e.okPeriode.increment();
        e.sumaLatenciaMsPeriode.add(duracioMs);

        // finestra fallback
        e.finestra.afegeix(true);
    }

    /**
     * Registra una operació KO.
     */
    public void registraError(K subsistema) {
        Entrada e = entrades.get(subsistema);
        if (e == null) return;

        // totals
        e.errorTotal.increment();

        // període
        e.errorPeriode.increment();

        // finestra fallback
        e.finestra.afegeix(false);
    }

    /**
     * Retorna estadístiques per subsistema:
     * - període: fins ara (des de darrera consulta) i reseteja el període
     * - total: acumulatiu
     */
    public Map<K, EstadistiquesComponent> obtenSnapshot() {
        Instant ara = Instant.now(rellotge);

        Map<K, EstadistiquesComponent> out = new EnumMap<>(tipusEnum);

        for (K k : tipusEnum.getEnumConstants()) {
            Entrada e = entrades.get(k);
            out.put(k, e.creaSnapshot(k.name(), ara));
        }

        // Reset del període (IMPORTANT: no resetejam la finestra)
        for (Entrada e : entrades.values()) {
            e.resetejaPeriode();
        }

        return out;
    }

    /**
     * Permet obtenir el fallback d'un subsistema (ok/ko de la finestra).
     */
    public DadesFallback obtenFallback(K subsistema) {
        Entrada e = entrades.get(subsistema);
        if (e == null) return new DadesFallback(0,0,false);
        boolean te = !e.finestra.esBuida();
        return new DadesFallback(e.finestra.getOk(), e.finestra.getKo(), te);
    }

    public int getMidaFinestraFallback() { return midaFinestraFallback; }

    // --- Tipus interns ---

    public static final class DadesFallback {
        public final long ok;
        public final long ko;
        public final boolean te;

        public DadesFallback(long ok, long ko, boolean te) {
            this.ok = ok;
            this.ko = ko;
            this.te = te;
        }
    }

    private static final class Entrada {
        // Totals
        final LongAdder okTotal = new LongAdder();
        final LongAdder errorTotal = new LongAdder();
        final LongAdder sumaLatenciaMsTotal = new LongAdder();

        // Període
        final LongAdder okPeriode = new LongAdder();
        final LongAdder errorPeriode = new LongAdder();
        final LongAdder sumaLatenciaMsPeriode = new LongAdder();

        // Fallback per subsistema
        final FinestraBooleanaCircular finestra;

        Entrada(int midaFinestra) {
            this.finestra = new FinestraBooleanaCircular(midaFinestra);
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
