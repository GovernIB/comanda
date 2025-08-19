package es.caib.comanda.ms.logic.intf.exception;

import es.caib.comanda.client.model.ParamTipus;
import es.caib.comanda.client.model.Parametre;
import lombok.Getter;

/**
 * Excepció que es llença quan no es troba un element que suposadament existeix.
 * 
 * @author Límit Tecnologies
 */
@Getter
public class ParametreTipusException extends RuntimeException {

	private final String what;

    private static String getMaskedValue(Parametre what) {
        return ParamTipus.PASSWORD.equals(what.getTipus()) ? "********" : what.getValor();
    }

    public ParametreTipusException(Parametre what, ParamTipus paramTipus) {
        super(what + " type mismatch: " + paramTipus + " (expected) != " + what.getTipus() + ". Value: " + getMaskedValue(what));
        this.what = what.getCodi();
    }

    public ParametreTipusException(Parametre what, ParamTipus paramTipus, Throwable t) {
        super(what + " type mismatch: " + paramTipus + " (expected) != " + what.getTipus() + ". Value: " + getMaskedValue(what));
        this.what = what.getCodi();
	}

}