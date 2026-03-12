package es.caib.comanda.ms.salut.helper.components;

import es.caib.comanda.model.server.monitoring.EstatSalutEnum;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Càlcul d'estat global amb regla de crítics/no crítics (com el vostre codi).
 */
public final class CalculSalutGlobal {

    private CalculSalutGlobal() {}

    public static EstatSalutEnum calculaEstatGlobal(
            Map<String, EstatSalutEnum> estatsPerComponent,
            Function<String, Boolean> esCriticFunc
    ) {
        Objects.requireNonNull(estatsPerComponent);
        Objects.requireNonNull(esCriticFunc);

        boolean hiHaDown = false, hiHaError = false, hiHaDegraded = false, hiHaWarn = false, hiHaUp = false;

        for (Map.Entry<String, EstatSalutEnum> e : estatsPerComponent.entrySet()) {
            String id = e.getKey();
            EstatSalutEnum estat = e.getValue() == null ? EstatSalutEnum.UNKNOWN : e.getValue();
            boolean esCritic = Boolean.TRUE.equals(esCriticFunc.apply(id));

            switch (estat) {
                case UP:
                    hiHaUp = true;
                    break;
                case WARN:
                    hiHaWarn = true;
                    break;
                case DEGRADED:
                    if (esCritic) hiHaDegraded = true;
                    else hiHaWarn = true;
                    break;
                case ERROR:
                    if (esCritic) hiHaError = true;
                    else hiHaWarn = true;
                    break;
                case DOWN:
                    if (esCritic) hiHaDown = true;
                    else hiHaWarn = true;
                    break;
                default:
                    // UNKNOWN: ignoram
            }
        }

        // Com al vostre codi: DOWN/ERROR crític => ERROR global
        if (hiHaError || hiHaDown) return EstatSalutEnum.ERROR;
        if (hiHaDegraded) return EstatSalutEnum.DEGRADED;
        if (hiHaWarn) return EstatSalutEnum.WARN;
        if (hiHaUp) return EstatSalutEnum.UP;
        return EstatSalutEnum.UNKNOWN;
    }
}
