/**
 *
 */
package es.caib.comanda.ms.back.config;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Configuración del MessageSource de la aplicación.
 *
 * @author Límit Tecnologies
 */
@Configuration
public class MessageSourceConfig {

	protected Locale getDefaultLocale() {
		return Locale.forLanguageTag(BaseConfig.DEFAULT_LOCALE);
	}

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:" + BaseConfig.APP_NAME + "-messages");
		messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
		messageSource.setFallbackToSystemLocale(false);
		messageSource.setDefaultLocale(getDefaultLocale());
		return messageSource;
	}

}
