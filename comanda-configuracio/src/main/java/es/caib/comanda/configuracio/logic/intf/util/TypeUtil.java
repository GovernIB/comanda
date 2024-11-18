package es.caib.comanda.configuracio.logic.intf.util;

import org.springframework.core.GenericTypeResolver;
import java.lang.reflect.Type;

/**
 * Utilitats per a obtenir informació dels tipus Java via reflection.
 * 
 * @author Limit Tecnologies
 */
public class TypeUtil {

	public static Type getArgumentTypeFromGenericSuperclass(
			Class<?> clazz,
			Class<?> superClass,
			int index) {
		return GenericTypeResolver.resolveTypeArguments(
				clazz,
				superClass != null ? superClass : clazz)[index];
	}
	public static Type getArgumentTypeFromGenericSuperclass(Class<?> clazz, int index) {
		return getArgumentTypeFromGenericSuperclass(clazz, null, index);
	}

}
