package es.caib.comanda.ms.salut.helper.components;

import es.caib.comanda.ms.salut.helper.EstatHelper;
import es.caib.comanda.ms.salut.model.EstatSalutEnum;

public final class PoliticaSalutPerDefecte {
    private final long minimMostresPerPercentatge;

    public PoliticaSalutPerDefecte(long minimMostresPerPercentatge) {
        if (minimMostresPerPercentatge <= 0) {
            throw new IllegalArgumentException("minimMostresPerPercentatge ha de ser > 0");
        }
        this.minimMostresPerPercentatge = minimMostresPerPercentatge;
    }

    public EstatSalutEnum calculaEstat(EstadistiquesComponent est, long okFallback, long koFallback, boolean teFallback) {
        if (est == null) {
            throw new IllegalArgumentException("est no pot ser null");
        }
        long totalPeriode = est.getTotalPeriode();
        if (totalPeriode == 0) {
            if (teFallback && (okFallback + koFallback) > 0) {
                return EstatHelper.calculaEstat(okFallback, koFallback);
            }
            return EstatSalutEnum.UNKNOWN;
        }
        if (totalPeriode < minimMostresPerPercentatge && teFallback && (okFallback + koFallback) > 0) {
            return EstatHelper.calculaEstat(okFallback, koFallback);
        }
        return EstatHelper.calculaEstat(est.getOkPeriode(), est.getErrorPeriode());
    }
}
