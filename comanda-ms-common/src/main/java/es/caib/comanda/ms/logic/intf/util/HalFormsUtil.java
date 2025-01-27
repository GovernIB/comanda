package es.caib.comanda.ms.logic.intf.util;

import es.caib.comanda.ms.logic.intf.exception.ComponentNotFoundException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;
import es.caib.comanda.ms.logic.intf.service.ResourceServiceLocator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitats per a HAL-FORMS.
 * 
 * @author Limit Tecnologies
 */
public class HalFormsUtil {

	public static Map<String, Object> getNewResourceValues(Class<?> resourceClass) throws ResourceNotCreatedException {
		Map<String, Object> values = new HashMap<>();
		ResourceServiceLocator resourceServiceLocator = ResourceServiceLocator.getInstance();
		if (resourceServiceLocator != null) {
			try {
				MutableResourceService<?, ?> mutableResourceService = ResourceServiceLocator.getInstance().
						getMutableEntityResourceServiceForResourceClass(resourceClass);
				Object newInstance = mutableResourceService.newResourceInstance();
				if (newInstance != null) {
					values.putAll(toMap(newInstance));
				}
			} catch (ComponentNotFoundException ignored) {}
		}
		return values;
	}

	private static Map<String, Object> toMap(Object object) {
		Map<String, Object> map = new HashMap<>();
		Field[] fields = object.getClass().getDeclaredFields();
		for (Field field: fields) {
			field.setAccessible(true);
			try {
				Object value = field.get(object);
				if (value != null) {
					map.put(field.getName(), value);
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return map;
	}

}
