package es.caib.comanda.ms.logic.intf.exception;

import lombok.Getter;

/**
 * Excepció que es llança quan s'intenta crear un recurs que ja existeix.
 * 
 * @author Limit Tecnologies
 */
@Getter
public class ResourceAlreadyExistsException extends RuntimeException {

	private final Class<?> clazz;
	private final String pk;

	public ResourceAlreadyExistsException(Class<?> clazz) {
		super("Resource " + getResourceId(clazz, null) + " already exists");
		this.clazz = clazz;
		this.pk = null;
	}

	public ResourceAlreadyExistsException(Class<?> clazz, String pk) {
		super("Resource " + getResourceId(clazz, pk) + " already exists");
		this.clazz = clazz;
		this.pk = pk;
	}

	public static String getResourceId(Class<?> clazz, String pk) {
		if (pk != null) {
			return "(class=" + clazz.getName() + ", pk=" + pk + ")";
		} else {
			return "(class=" + clazz.getName() + ")";
		}
	}

}
