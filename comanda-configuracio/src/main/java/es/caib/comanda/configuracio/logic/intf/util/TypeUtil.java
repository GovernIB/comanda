package es.caib.comanda.configuracio.logic.intf.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

	public static boolean isNotNullField(Field field) {
		return field.getAnnotation(NotNull.class) != null;
	}

	public static boolean isCollectionFieldType(Field field) {
		return Collection.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType;
	}

	public static <T> Set<Class<T>> findAssignableClasses(Class<T> assignableType, String... packagesToScan) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AssignableTypeFilter(assignableType));
		Set<Class<T>> ret = new HashSet<>();
		for (String pkg: packagesToScan) {
			provider.findCandidateComponents(pkg).stream().
					map(BeanDefinition::getBeanClassName).
					map(n -> {
						try {
							return ClassUtils.forName(n, null);
						} catch (ClassNotFoundException ex) {
							return null;
						}
					}).
					filter(Objects::nonNull).
					forEach(c -> ret.add(((Class<T>)c)));
		}
		return ret;
	}

}
