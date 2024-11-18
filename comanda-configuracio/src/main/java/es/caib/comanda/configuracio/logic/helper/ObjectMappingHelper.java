package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.configuracio.logic.intf.exception.ObjectMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Mètodes per al mapeig d'objectes.
 *
 * @author Limit Tecnologies
 */
@Slf4j
@Component
public class ObjectMappingHelper {

	/**
	 * Retorna una instància de la classe especificada amb els camps amb el mateix nom mapejats.
	 *
	 * @param source
	 *            l'objecte d'orígen.
	 * @param targetClass
	 *            la classe que es vol obtenir.
	 * @return una nova instància de la classe especificada amb els camps mapejats.
	 * @param <T>
	 *            el tipus que es vol obtenir.
	 * @throws ObjectMappingException
	 *            quan es dona algun error en el mapeig.
	 */
	public <T> T newInstanceMap(Object source, Class<T> targetClass) throws ObjectMappingException {
		try {
			T target = getNewInstance(targetClass);
			map(source, target);
			return target;
		} catch (Exception ex) {
			throw new ObjectMappingException(source.getClass(), targetClass, ex);
		}
	}

	/**
	 * Mapeja els camps entre l'objecte d'orígen i l'objecte destí que tenguin els mateixos noms.
	 *
	 * @param source
	 *            l'objecte d'orígen.
	 * @param target
	 *            l'objecte destí.
	 * @throws ObjectMappingException
	 *            quan es dona algun error en el mapeig.
	 */
	public void map(Object source, Object target) throws ObjectMappingException {
		ReflectionUtils.doWithLocalFields(source.getClass(), sourceField -> {
			ReflectionUtils.makeAccessible(sourceField);
			Object value;
			if (isSimpleType(sourceField.getType())) {
				value = sourceField.get(source);
			} else {
				value = newInstanceMap(sourceField.get(source), sourceField.getType());
			}
			Field targetField = ReflectionUtils.findField(target.getClass(), sourceField.getName());
			if (targetField != null) {
				ReflectionUtils.makeAccessible(targetField);
				ReflectionUtils.setField(
						targetField,
						target,
						value);
			} else {
				log.warn(
						"Couldn't map {} to target class {}: target field with same name not found",
						sourceField,
						target.getClass().getName());
			}
		});
	}

	private <T> T getNewInstance(Class<T> targetClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		return targetClass.getConstructor().newInstance();
	}

	private boolean isSimpleType(Class<?> type) {
		return type.isPrimitive() ||
				Boolean.class == type ||
				Character.class == type ||
				CharSequence.class.isAssignableFrom(type) ||
				Number.class.isAssignableFrom(type) ||
				Enum.class.isAssignableFrom(type);
	}

}
