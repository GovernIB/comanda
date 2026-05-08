package es.caib.comanda.ms.salut.helper.components;

import es.caib.comanda.ms.salut.model.EstatSalutEnum;

import java.util.Map;

public final class CalculSalutGlobal {

    private CalculSalutGlobal() {
    }

    public interface CriticitatComponentResolver {
        Boolean esCritic(String componentId);
    }

    public static EstatSalutEnum calculaEstatGlobal(Map<String, EstatSalutEnum> estatsPerComponent,
                                                    CriticitatComponentResolver esCriticFunc) {
        if (estatsPerComponent == null || esCriticFunc == null) {
            throw new IllegalArgumentException("paràmetres obligatoris");
        }
        boolean hiHaDown = false;
        boolean hiHaError = false;
        boolean hiHaDegraded = false;
        boolean hiHaWarn = false;
        boolean hiHaUp = false;
        for (Map.Entry<String, EstatSalutEnum> entry : estatsPerComponent.entrySet()) {
            String id = entry.getKey();
            EstatSalutEnum estat = entry.getValue() != null ? entry.getValue() : EstatSalutEnum.UNKNOWN;
            boolean esCritic = Boolean.TRUE.equals(esCriticFunc.esCritic(id));
            switch (estat) {
                case UP: hiHaUp = true; break;
                case WARN: hiHaWarn = true; break;
                case DEGRADED: if (esCritic) { hiHaDegraded = true; } else { hiHaWarn = true; } break;
                case ERROR: if (esCritic) { hiHaError = true; } else { hiHaWarn = true; } break;
                case DOWN: if (esCritic) { hiHaDown = true; } else { hiHaWarn = true; } break;
                default: break;
            }
        }
        if (hiHaError || hiHaDown) { return EstatSalutEnum.ERROR; }
        if (hiHaDegraded) { return EstatSalutEnum.DEGRADED; }
        if (hiHaWarn) { return EstatSalutEnum.WARN; }
        if (hiHaUp) { return EstatSalutEnum.UP; }
        return EstatSalutEnum.UNKNOWN;
    }
}
