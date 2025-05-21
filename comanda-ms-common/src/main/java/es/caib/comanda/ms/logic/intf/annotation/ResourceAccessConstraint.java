package es.caib.comanda.ms.logic.intf.annotation;

import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotació per a configurar les restriccions d'accés a un recurs.
 * 
 * @author Limit Tecnologies
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceAccessConstraint {

	ResourceAccessConstraintType type();
	String[] roles() default {};
	PermissionEnum[] grantedPermissions() default {};

	public enum ResourceAccessConstraintType {
		PERMIT_ALL,
		AUTHENTICATED,
		ROLE,
		CUSTOM
	}

}
