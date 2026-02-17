package es.caib.comanda.ms.salut.helper.components;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Estadístiques d'un component (subsistema / integració / connector).
 * Període = des de la darrera consulta (reset-on-read).
 * Total = acumulatiu des de l'arrencada.
 */
@Getter
public final class EstadistiquesComponent {

    private final String componentId;
    private final String endpoint;

    // Període
    private final long okPeriode;
    private final long errorPeriode;
    private final double tempsMigMsPeriode;

    // Totals
    private final long okTotal;
    private final long errorTotal;
    private final double tempsMigMsTotal;

    private final Instant instantSnapshot;

    public EstadistiquesComponent(String componentId,
                                  String endpoint,
                                  long okPeriode, long errorPeriode, double tempsMigMsPeriode,
                                  long okTotal, long errorTotal, double tempsMigMsTotal,
                                  Instant instantSnapshot) {
        this.componentId = Objects.requireNonNull(componentId);
        this.endpoint = endpoint;
        this.okPeriode = okPeriode;
        this.errorPeriode = errorPeriode;
        this.tempsMigMsPeriode = tempsMigMsPeriode;
        this.okTotal = okTotal;
        this.errorTotal = errorTotal;
        this.tempsMigMsTotal = tempsMigMsTotal;
        this.instantSnapshot = Objects.requireNonNull(instantSnapshot);
    }

    public long getTotalPeriode() { return okPeriode + errorPeriode; }
}
