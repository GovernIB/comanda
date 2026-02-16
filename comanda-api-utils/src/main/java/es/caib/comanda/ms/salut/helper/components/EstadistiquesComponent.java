package es.caib.comanda.ms.salut.helper.components;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Estadístiques d'un subsistema.
 * - període: des de la darrera consulta (reset-on-read)
 * - total: acumulat des de l'arrencada
 */
@Getter
public final class EstadistiquesComponent {

    private final String codi;

    // Període (darrer interval consultat)
    private final long execucionsOkPeriode;
    private final long execucionsErrorPeriode;
    private final double tempsMigMsPeriode;

    // Totals (des de l'arrencada)
    private final long execucionsOkTotal;
    private final long execucionsErrorTotal;
    private final double tempsMigMsTotal;

    // Marca temporal de quan s'ha generat l'snapshot (informatiu)
    private final Instant instantSnapshot;

    public EstadistiquesComponent(
            String codi,
            long execucionsOkPeriode,
            long execucionsErrorPeriode,
            double tempsMigMsPeriode,
            long execucionsOkTotal,
            long execucionsErrorTotal,
            double tempsMigMsTotal,
            Instant instantSnapshot
    ) {
        this.codi = Objects.requireNonNull(codi);
        this.execucionsOkPeriode = execucionsOkPeriode;
        this.execucionsErrorPeriode = execucionsErrorPeriode;
        this.tempsMigMsPeriode = tempsMigMsPeriode;
        this.execucionsOkTotal = execucionsOkTotal;
        this.execucionsErrorTotal = execucionsErrorTotal;
        this.tempsMigMsTotal = tempsMigMsTotal;
        this.instantSnapshot = Objects.requireNonNull(instantSnapshot);
    }

    public long getTotalPeriode() { return execucionsOkPeriode + execucionsErrorPeriode; }
    public long getTotalTotal() { return execucionsOkTotal + execucionsErrorTotal; }
}
