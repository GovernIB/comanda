package es.caib.comanda.configuracio.logic.intf.exception;

import lombok.Getter;

/**
 * Excepció que es llança quan es produeixen errors en el mapeig recurs - entitat.
 * 
 * @author Limit Tecnologies
 */
@Getter
public class ObjectMappingException extends RuntimeException {

	private final Class<?> sourceClass;
	private final Class<?> targetClass;

	public ObjectMappingException(Class<?> sourceClass, Class<?> targetClass) {
		super("Coudn't map " + sourceClass.getName() + " to " + targetClass.getName());
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
	}

	public ObjectMappingException(Class<?> sourceClass, Class<?> targetClass, Throwable t) {
		super("Coudn't map " + sourceClass.getName() + " to " + targetClass.getName(), t);
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
	}

}
