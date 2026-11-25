package es.caib.comanda.monitor.logic.helper;

import com.zaxxer.hikari.metrics.IMetricsTracker;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.metrics.PoolStats;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * {@link MetricsTrackerFactory} lleuger per a HikariCP que registra el temps màxim d'espera
 * d'adquisició ({@code maxWaitTimeMs}) sense envolcallar {@code DataSource} ni {@code Connection}.
 */
public class HikariPoolStatsTrackerFactory implements MetricsTrackerFactory {

    private final MetricsTrackerFactory delegate;
    private final LongSupplier connectionTimeoutMsSupplier;
    private final AtomicLong maxWaitTimeMs = new AtomicLong(0L);

    /**
     * Crea una fàbrica de mètriques que opcionalment delega en una altra existent i llegeix
     * dinàmicament el timeout configurat al pool.
     *
     * @param delegate                    fàbrica prèvia configurada al pool (pot ser {@code null})
     * @param connectionTimeoutMsSupplier proveïdor del timeout del pool en mil·lisegons
     */
    public HikariPoolStatsTrackerFactory(MetricsTrackerFactory delegate, LongSupplier connectionTimeoutMsSupplier) {
        this.delegate = delegate;
        this.connectionTimeoutMsSupplier = connectionTimeoutMsSupplier != null ? connectionTimeoutMsSupplier : () -> 0L;
    }

    /**
     * Crea una fàbrica de mètriques amb un valor fix de timeout en mil·lisegons.
     *
     * @param delegate            fàbrica prèvia configurada al pool (pot ser {@code null})
     * @param connectionTimeoutMs timeout configurat al pool en mil·lisegons
     */
    public HikariPoolStatsTrackerFactory(MetricsTrackerFactory delegate, long connectionTimeoutMs) {
        this(delegate, () -> connectionTimeoutMs);
    }

    /**
     * Retorna el temps màxim d'espera d'adquisició registrat en mil·lisegons.
     */
    public long getMaxWaitTimeMs() {
        return maxWaitTimeMs.get();
    }

    @Override
    public IMetricsTracker create(String poolName, PoolStats poolStats) {
        IMetricsTracker delegateTracker = delegate != null ? delegate.create(poolName, poolStats) : null;
        return new IMetricsTracker() {
            @Override
            public void recordConnectionCreatedMillis(long connectionCreatedMillis) {
                if (delegateTracker != null) {
                    delegateTracker.recordConnectionCreatedMillis(connectionCreatedMillis);
                }
            }

            @Override
            public void recordConnectionAcquiredNanos(long elapsedAcquiredNanos) {
                long elapsedMs = TimeUnit.NANOSECONDS.toMillis(Math.max(0L, elapsedAcquiredNanos));
                maxWaitTimeMs.accumulateAndGet(elapsedMs, Math::max);
                if (delegateTracker != null) {
                    delegateTracker.recordConnectionAcquiredNanos(elapsedAcquiredNanos);
                }
            }

            @Override
            public void recordConnectionUsageMillis(long elapsedBorrowedMillis) {
                if (delegateTracker != null) {
                    delegateTracker.recordConnectionUsageMillis(elapsedBorrowedMillis);
                }
            }

            @Override
            public void recordConnectionTimeout() {
                long timeoutMs = Math.max(0L, connectionTimeoutMsSupplier.getAsLong());
                if (timeoutMs > 0L) {
                    maxWaitTimeMs.accumulateAndGet(timeoutMs, Math::max);
                }
                if (delegateTracker != null) {
                    delegateTracker.recordConnectionTimeout();
                }
            }

            @Override
            public void close() {
                if (delegateTracker != null) {
                    delegateTracker.close();
                }
            }
        };
    }
}
