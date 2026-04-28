package es.caib.comanda.ms.salut.helper.components;

import es.caib.comanda.model.server.monitoring.EstatSalutEnum;
import es.caib.comanda.ms.salut.helper.EstatHelper;

import java.util.Objects;

/**
 * Política de salut basada en EstatHelper, amb fallback per subsistema.
 */
public final class PoliticaSalutPerDefecte {

    private final long minimMostresPerPercentatge;

    public PoliticaSalutPerDefecte(long minimMostresPerPercentatge) {
        if (minimMostresPerPercentatge <= 0) throw new IllegalArgumentException("minimMostresPerPercentatge ha de ser > 0");
        this.minimMostresPerPercentatge = minimMostresPerPercentatge;
    }

    /**
     * Calcula l'estat d'un subsistema a partir de les estadístiques del període
     * i, si cal, del fallback (finestra de darreres N peticions del mateix subsistema).
     */
    public EstatSalutEnum calculaEstat(EstadistiquesComponent est,
                                       long okFallback,
                                       long koFallback,
                                       boolean teFallback) {
        Objects.requireNonNull(est);

        long totalPeriode = est.getTotalPeriode();

        // Sense mostres al període: si hi ha finestra, usam finestra; si no, UNKNOWN
        if (totalPeriode == 0) {
            if (teFallback && (okFallback + koFallback) > 0) {
                return EstatHelper.calculaEstat(okFallback, koFallback);
            }
            return EstatSalutEnum.UNKNOWN;
        }

        // Si hi ha poca mostra, usam fallback del component (si n'hi ha)
        if (totalPeriode < minimMostresPerPercentatge && teFallback && (okFallback + koFallback) > 0) {
            return EstatHelper.calculaEstat(okFallback, koFallback);
        }

        // En cas contrari, usam el període (encara que sigui petit i no hi hagi fallback)
        return EstatHelper.calculaEstat(est.getOkPeriode(), est.getErrorPeriode());
    }
}

