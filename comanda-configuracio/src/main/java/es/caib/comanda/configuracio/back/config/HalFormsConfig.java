package es.caib.comanda.configuracio.back.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;

/**
 * Configuració de HAL FORMS per Spring HATEOAS.
 * 
 * @author Límit Tecnologies
 */
@Configuration
public class HalFormsConfig {

	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	HalFormsConfiguration halFormsConfiguration() {
		return new HalFormsConfiguration();
	}

	/*@PostConstruct
	void postConstruct() {
		Object halFormsTemplatePropertyWriter = applicationContext.getBean("halFormsTemplatePropertyWriter");
		Field builderField = ReflectionUtils.findField(halFormsTemplatePropertyWriter.getClass(), "builder");
		ReflectionUtils.makeAccessible(builderField);
		Object halFormsTemplateBuilder = ReflectionUtils.getField(builderField, halFormsTemplatePropertyWriter); // HalFormsTemplateBuilder
		Field factoryField = ReflectionUtils.findField(halFormsTemplateBuilder.getClass(), "factory");
		ReflectionUtils.makeAccessible(factoryField);
		Object halFormsPropertyFactory = ReflectionUtils.getField(factoryField, halFormsTemplateBuilder); // HalFormsPropertyFactory
		Method createPropertiesMethod = ReflectionUtils.findMethod(halFormsPropertyFactory.getClass(), "createProperties", halFormsModel.getClass());
		ReflectionUtils.makeAccessible(createPropertiesMethod);
		return (List<?>)ReflectionUtils.invokeMethod(createPropertiesMethod, halFormsPropertyFactory, halFormsModel);
	}*/

}
