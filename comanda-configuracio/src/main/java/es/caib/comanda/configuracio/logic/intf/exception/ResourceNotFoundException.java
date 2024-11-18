package es.caib.comanda.configuracio.logic.intf.exception;

import lombok.Getter;

/**
 * Excepció que es llença quan no es troba el recurs.
 * 
 * @author Limit Tecnologies
 */
@Getter
public class ResourceNotFoundException extends NotFoundException {

	private final Class<?> clazz;
	private final String id;

	public ResourceNotFoundException(Class<?> clazz, String id) {
		super("Resource " + getResourceId(clazz, id));
		this.clazz = clazz;
		this.id = id;
	}

	private static String getResourceId(Class<?> clazz, String pk) {
		return "(class=" + clazz.getName() + ", pk=" + pk + ")";
	}

}
