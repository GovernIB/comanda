package es.caib.comanda.ms.salut.model;

public class EstatByPercent {

    public static final int DOWN_PCT = 100;     // 100% errors
    public static final int ERROR_GT_PCT = 30;  // >30% errors
    public static final int DEGRADED_GT_PCT = 10; // >10% errors
    public static final int UP_LT_PCT = 5;      // <5% errors

    public static EstatSalutEnum calculaEstat(double percent) {
        if (percent >= DOWN_PCT) {
            return EstatSalutEnum.DOWN;
        } else if (percent > ERROR_GT_PCT) {
            return EstatSalutEnum.ERROR;
        } else if (percent > DEGRADED_GT_PCT) {
            return EstatSalutEnum.DEGRADED;
        } else if (percent <= UP_LT_PCT) {
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
