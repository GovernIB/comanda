package es.caib.comanda.ms.salut.helper.components;

import es.caib.comanda.model.server.monitoring.EstatSalutEnum;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Utilitats per calcular estat global.
 */
public final class CalculSalutGlobal {

    private CalculSalutGlobal() {}

    public static <K extends Enum<K>> EstatSalutEnum calculaEstatGlobal(
            Map<K, EstatSalutEnum> estatsPerSubsistema,
            Function<K, Boolean> esCriticFunc
    ) {
        Objects.requireNonNull(estatsPerSubsistema);
        Objects.requireNonNull(esCriticFunc);

        boolean hiHaDown = false, hiHaError = false, hiHaDegraded = false, hiHaWarn = false, hiHaUp = false;

        for (Map.Entry<K, EstatSalutEnum> e : estatsPerSubsistema.entrySet()) {
            K subsistema = e.getKey();
            EstatSalutEnum estat = e.getValue() == null ? EstatSalutEnum.UNKNOWN : e.getValue();
            boolean esCritic = Boolean.TRUE.equals(esCriticFunc.apply(subsistema));

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
                    // UNKNOWN: no modifica
            }
        }

        // Igual que al teu codi: ERROR o DOWN crític => ERROR global (DOWN global el tenies comentat)
        if (hiHaError || hiHaDown) return EstatSalutEnum.ERROR;
        if (hiHaDegraded) return EstatSalutEnum.DEGRADED;
        if (hiHaWarn) return EstatSalutEnum.WARN;
        if (hiHaUp) return EstatSalutEnum.UP;
        return EstatSalutEnum.UNKNOWN;
    }

    /**
     * Helper per inicialitzar un EnumMap amb UNKNOWN (opcional).
     */
    public static <K extends Enum<K>> Map<K, EstatSalutEnum> mapaEstatsUnknown(Class<K> tipusEnum) {
        Map<K, EstatSalutEnum> m = new EnumMap<>(tipusEnum);
        for (K k : tipusEnum.getEnumConstants()) m.put(k, EstatSalutEnum.UNKNOWN);
        return m;
    }
}
