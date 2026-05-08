package es.caib.comanda.ms.salut.helper.components;

import java.util.Date;

public final class EstadistiquesComponent {
    private final String componentId;
    private final String endpoint;
    private final long okPeriode;
    private final long errorPeriode;
    private final double tempsMigMsPeriode;
    private final long okTotal;
    private final long errorTotal;
    private final double tempsMigMsTotal;
    private final Date instantSnapshot;

    public EstadistiquesComponent(String componentId, String endpoint, long okPeriode, long errorPeriode,
                                  double tempsMigMsPeriode, long okTotal, long errorTotal,
                                  double tempsMigMsTotal, Date instantSnapshot) {
        this.componentId = componentId;
        this.endpoint = endpoint;
        this.okPeriode = okPeriode;
        this.errorPeriode = errorPeriode;
        this.tempsMigMsPeriode = tempsMigMsPeriode;
        this.okTotal = okTotal;
        this.errorTotal = errorTotal;
        this.tempsMigMsTotal = tempsMigMsTotal;
        this.instantSnapshot = instantSnapshot;
    }

    public String getComponentId() { return componentId; }
    public String getEndpoint() { return endpoint; }
    public long getOkPeriode() { return okPeriode; }
    public long getErrorPeriode() { return errorPeriode; }
    public double getTempsMigMsPeriode() { return tempsMigMsPeriode; }
    public long getOkTotal() { return okTotal; }
    public long getErrorTotal() { return errorTotal; }
    public double getTempsMigMsTotal() { return tempsMigMsTotal; }
    public Date getInstantSnapshot() { return instantSnapshot; }
    public long getTotalPeriode() { return okPeriode + errorPeriode; }
}
