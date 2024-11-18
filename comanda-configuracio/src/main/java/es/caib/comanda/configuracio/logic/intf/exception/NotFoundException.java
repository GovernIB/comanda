package es.caib.comanda.configuracio.logic.intf.exception;

import lombok.Getter;

/**
 * Excepció que es llença quan no es troba un element que suposadament existeix.
 * 
 * @author Limit Tecnologies
 */
@Getter
public abstract class NotFoundException extends RuntimeException {

	private final String what;

	public NotFoundException(String what) {
		super(what + " not found");
		this.what = what;
	}

	public NotFoundException(String what, Throwable t) {
		super(what + " not found", t);
		this.what = what;
	}

}
