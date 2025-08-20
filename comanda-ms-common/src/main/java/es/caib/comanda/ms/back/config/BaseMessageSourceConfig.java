package es.caib.comanda.ms.back.config;

import es.caib.comanda.base.config.BaseConfig;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Configuración del MessageSource de l'aplicació.
 *
 * @author Límit Tecnologies
 */
public abstract class BaseMessageSourceConfig {

	protected Locale getDefaultLocale() {
		return Locale.forLanguageTag(BaseConfig.DEFAULT_LOCALE);
	}

	protected String getBasename() {
		return BaseConfig.APP_NAME + "-messages";
	}

	protected String[] getBasenames() {
		return new String[] { getBasename() };
	}

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		configureBaseName(messageSource);
		messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
		messageSource.setFallbackToSystemLocale(false);
		messageSource.setDefaultLocale(getDefaultLocale());
		return messageSource;
	}

	private void configureBaseName(ReloadableResourceBundleMessageSource messageSource) {
		List<String> baseNamesWithClasspath = new ArrayList<>();
//		baseNamesWithClasspath.add("classpath:" + getDefaultLocale());
		String[] baseNames = getBasenames();
		if (baseNames != null) {
			baseNamesWithClasspath.addAll(Arrays.stream(baseNames).
					map(n -> "classpath:" + n).
					collect(Collectors.toList()));
		}
		messageSource.setBasenames(baseNamesWithClasspath.toArray(new String[0]));
	}

}
