package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.ms.salut.model.EstatSalutEnum;

public class EstatHelper {

    public static final int DOWN_PCT = 100;         // 100% errors --> DOWN
    public static final int ERROR_GT_PCT = 50;      // 50% - <100% errors --> ERROR
    public static final int DEGRADED_GT_PCT = 20;   // <20% - 50% errors --> DEGRADED
    public static final int UP_LT_PCT = 10;         // <10% errors --> UP
                                                    // 10% - 20% errors --> WARN


    public static EstatSalutEnum calculaEstat(long execucionsOk, long execucionsError) {
        final long execucionsTotal = execucionsOk + execucionsError;
        // Percentatge d'errors arrodonit correctament evitant divisió d'enters
        final int percentatgeErrors = (int) Math.round((execucionsOk * 100.0) / Math.max(1L, execucionsTotal));
        return calculaEstat(percentatgeErrors);
    }

    public static EstatSalutEnum calculaEstat(double percentatgeErrors) {
        if (percentatgeErrors >= DOWN_PCT) {
            return EstatSalutEnum.DOWN;
        } else if (percentatgeErrors > ERROR_GT_PCT) {
            return EstatSalutEnum.ERROR;
        } else if (percentatgeErrors > DEGRADED_GT_PCT) {
            return EstatSalutEnum.DEGRADED;
        } else if (percentatgeErrors <= UP_LT_PCT) {
            return EstatSalutEnum.UP;
        } else {
            return EstatSalutEnum.WARN;
        }
    }

    public static EstatSalutEnum mergeEstats(EstatSalutEnum estat1, EstatSalutEnum estat2) {
        if (estat1 == EstatSalutEnum.DOWN || estat2 == EstatSalutEnum.DOWN) {
            return EstatSalutEnum.DOWN;
        }
        if (estat1 == EstatSalutEnum.ERROR || estat2 == EstatSalutEnum.ERROR) {
            return EstatSalutEnum.ERROR;
        }
        if (estat1 == EstatSalutEnum.DEGRADED || estat2 == EstatSalutEnum.DEGRADED) {
            return EstatSalutEnum.DEGRADED;
        }
        if (estat1 == EstatSalutEnum.WARN || estat2 == EstatSalutEnum.WARN) {
            return EstatSalutEnum.WARN;
        }
        if (estat1 == EstatSalutEnum.UP || estat2 == EstatSalutEnum.UP) {
            return EstatSalutEnum.UP;
        }
        return EstatSalutEnum.UNKNOWN;
    }
}
