package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

import java.util.EnumSet;
import java.util.Set;

public enum TipusDimensioEnum {
    CONSELLERIA,
    ORGAN_GESTOR,
    ENTITAT,
//    PROCEDIMENT,
//    USUARI,
    ;

    public static final Set<TipusDimensioEnum> TIPUS_AMB_UNITAT_ORG =
        EnumSet.of(TipusDimensioEnum.ORGAN_GESTOR, TipusDimensioEnum.CONSELLERIA);
}
