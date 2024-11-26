package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.configuracio.logic.intf.exception.ObjectMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

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
	 *            l'objecte de destí.
	 * @throws ObjectMappingException
	 *            quan es dona algun error en el mapeig.
	 */
	public void map(Object source, Object target) throws ObjectMappingException {
		ReflectionUtils.doWithFields(source.getClass(), sourceField -> {
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
			}/* else {
				log.warn(
						"Couldn't map {} to target class {}: target field with same name not found",
						sourceField,
						target.getClass().getName());
			}*/
		});
	}

	/**
	 * Crea un clon del recurs per a la lògica onChange.
	 *
	 * @param resource
	 *            el recurs d'orígen.
	 * @param resourceClass
	 *            la classe del recurs.
	 * @return el recurs clonat.
	 */
	public <RR> RR cloneResource(
			RR resource,
			Class<RR> resourceClass) {
		if (resource != null) {
			try {
				RR clonedResource = resourceClass.getConstructor().newInstance();
				ReflectionUtils.doWithFields(resourceClass, field -> {
					if (!isStaticFinal(field)) {
						ReflectionUtils.makeAccessible(field);
						Object value = getFieldValue(resource, field);
						ReflectionUtils.setField(
								field,
								clonedResource,
								value);
					}
				});
				return clonedResource;
			} catch (Exception ex) {
				log.error("No s'ha pogut crear la instància del recurs {}", resourceClass.getName(), ex);
				throw new RuntimeException("No s'ha pogut crear la instància del recurs " + resourceClass.getName(), ex);
			}
		} else {
			return null;
		}
	}

	private <T> T getNewInstance(Class<T> targetClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		return targetClass.getConstructor().newInstance();
	}

	private boolean isSimpleType(Class<?> type) {
		return type.isPrimitive() ||
				Boolean.class == type ||
				Character.class == type ||
				Serializable.class.isAssignableFrom(type) ||
				CharSequence.class.isAssignableFrom(type) ||
				Number.class.isAssignableFrom(type) ||
				Date.class.isAssignableFrom(type) ||
				LocalTime.class.isAssignableFrom(type) ||
				LocalDateTime.class.isAssignableFrom(type) ||
				Enum.class.isAssignableFrom(type);
	}

	private Object getFieldValue(
			Object entity,
			java.lang.reflect.Field entityField) {
		String getMethodName = methodNameFromField(entityField.getName(), "get");
		Method method = ReflectionUtils.findMethod(entity.getClass(), getMethodName);
		if (method != null) {
			return ReflectionUtils.invokeMethod(method, entity);
		} else {
			return ReflectionUtils.getField(entityField, entity);
		}
	}

	private boolean isStaticFinal(java.lang.reflect.Field field) {
		int modifiers = field.getModifiers();
		return Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
	}

	private String methodNameFromField(String fieldName, String prefix) {
		return prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	}

}
