package es.caib.comanda.configuracio.back.config;

import es.caib.comanda.configuracio.back.controller.MutableResourceController;
import es.caib.comanda.configuracio.back.controller.ReadonlyResourceController;
import es.caib.comanda.configuracio.logic.intf.util.TypeUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Configuració de HAL-FORMS.
 * 
 * @author Josep Gayà
 */
@Slf4j
public abstract class BaseHalFormsConfig {

	@Bean
	HalFormsConfiguration halFormsConfiguration() {
		return createHalFormsConfiguration(
				TypeUtil.findAssignableClasses(
						MutableResourceController.class,
						getControllerPackages()));
	}

	protected abstract String[] getControllerPackages();

	private HalFormsConfiguration createHalFormsConfiguration(Set<Class<MutableResourceController>> resourceControllerClasses) {
		HalFormsConfiguration halFormsConfiguration = new HalFormsConfiguration();
		if (resourceControllerClasses != null) {
			for (Class<MutableResourceController> r: resourceControllerClasses) {
				halFormsConfiguration = withResourceController(halFormsConfiguration, r);
			}
		}
		return halFormsConfiguration;
	}

	private HalFormsConfiguration withResourceController(
			HalFormsConfiguration halFormsConfiguration,
			Class<MutableResourceController> resourceControllerClass) {
		Class<?> resourceClass = (Class<?>)TypeUtil.getArgumentTypeFromGenericSuperclass(
				resourceControllerClass,
				ReadonlyResourceController.class,
				0);
		MutableHolder<HalFormsConfiguration> halFormsConfigurationHolder = new MutableHolder<>(halFormsConfiguration);
		ReflectionUtils.doWithFields(
				resourceClass,
				field -> {
					configurationWithEnumOptions(
							halFormsConfigurationHolder,
							resourceClass,
							field);
				},
				this::isEnumField);
		return halFormsConfigurationHolder.getValue();
	}

	private void configurationWithEnumOptions(
			MutableHolder<HalFormsConfiguration> halFormsConfigurationHolder,
			Class<?> resourceClass,
			Field resourceField) {
		log.info("New HAL-FORMS enum options (class={}, field={})", resourceClass, resourceField.getName());
		halFormsConfigurationHolder.setValue(
				halFormsConfigurationHolder.getValue().withOptions(
						resourceClass,
						resourceField.getName(),
						metadata -> HalFormsOptions.
								inline(getInlineOptionsEnumConstants(resourceField)).
								withMinItems(TypeUtil.isNotNullField(resourceField) ? 1L : null).
								withMaxItems(TypeUtil.isCollectionFieldType(resourceField) ? null : 1L)));
	}

	private boolean isEnumField(Field field) {
		if (field.getType().isArray()) {
			return field.getType().getComponentType().isEnum();
		} else {
			return field.getType().isEnum();
		}
	}

	private Object[] getInlineOptionsEnumConstants(Field field) {
		if (field.getType().isArray()) {
			return field.getType().getComponentType().getEnumConstants();
		} else {
			return field.getType().getEnumConstants();
		}
	}
	@Getter
	@Setter
	@AllArgsConstructor
	public static class MutableHolder<T> {
		private T value;
	}

}
