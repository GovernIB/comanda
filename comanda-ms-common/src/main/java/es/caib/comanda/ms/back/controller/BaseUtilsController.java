package es.caib.comanda.ms.back.controller;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * Controlador per a verificar si l'aplicació està funcionant
 *
 * @author Limit Tecnologies
 */
@Hidden
@RestController
public abstract class BaseUtilsController {

	@Autowired
	private Environment env;
	@Autowired
	private ServletContext servletContext;

	@GetMapping(BaseConfig.PING_PATH)
	public ResponseEntity<Void> ping() {
		return ResponseEntity.ok().build();
	}

	@GetMapping(value = { BaseConfig.AUTH_TOKEN_PATH, BaseConfig.AUTH_TOKEN_PATH + ".js" }, produces = "text/javascript")
	public ResponseEntity<String> authToken() {
		String authToken = getAuthToken();
		String response = null;
		if (authToken != null) {
			response = "window.__AUTH_TOKEN__ = '" + authToken + "'";
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping(value = { BaseConfig.MANIFEST_PATH, BaseConfig.MANIFEST_PATH + ".js" }, produces = "text/javascript")
	public ResponseEntity<String> manifest() throws IOException {
		Map<String, Object> manifestProps = getManifestProperties();
		MediaType contentType = MediaType.valueOf("text/javascript"); // MediaType.TEXT_PLAIN;
		String json = manifestProps.entrySet().stream().
				filter(e -> !e.getKey().equalsIgnoreCase("Class-Path")).
				map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\",").
				collect(Collectors.joining("\n"));
		String response = "window.__MANIFEST__ = {\n" + json + "\n}";
		return ResponseEntity.
				ok().
				contentType(contentType).
				body(response);
	}

	@GetMapping(value = { BaseConfig.SYSENV_PATH, BaseConfig.SYSENV_PATH + ".js" }, produces = "text/javascript")
	public ResponseEntity<String> systemEnvironment(
			@RequestParam(required = false) String format) {
		Map<String, Object> systemEnv = getAllProperties(env); // System.getenv();
		MediaType contentType = MediaType.TEXT_PLAIN;
		String envJson;
		if ("jsall".equalsIgnoreCase(format)) {
			String json = systemEnv.entrySet().stream().
					map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\",").
					collect(Collectors.joining("\n"));
			envJson = "window.__RUNTIME_CONFIG__ = {" + json + "}";
			contentType = MediaType.valueOf("text/javascript");
		} else if ("reactapp".equalsIgnoreCase(format)) {
			String json = systemEnv.entrySet().stream().
					filter(e -> e.getKey().startsWith("REACT_APP") || isReactAppMappedFrontProperty(e.getKey())).
					map(e -> {
						if (isReactAppMappedFrontProperty(e.getKey())) {
							return "\"" + getReactAppMappedFrontProperty(e.getKey()) + "\":\"" + e.getValue() + "\",";
						} else {
							return "\"" + e.getKey() + "\":\"" + e.getValue() + "\",";
						}
					}).
					collect(Collectors.joining("\n"));
			envJson = "window.__RUNTIME_CONFIG__ = {" + json + "}";
			contentType = MediaType.valueOf("text/javascript");
		} else if ("vite".equalsIgnoreCase(format)) {
			String json = systemEnv.entrySet().stream().
					filter(e -> e.getKey().startsWith("VITE") || isViteMappedFrontProperty(e.getKey())).
					map(e -> {
						if (isViteMappedFrontProperty(e.getKey())) {
							return "\"" + getViteMappedFrontProperty(e.getKey()) + "\":\"" + e.getValue() + "\",";
						} else {
							return "\"" + e.getKey() + "\":\"" + e.getValue() + "\",";
						}
					}).
					collect(Collectors.joining("\n"));
			envJson = "window.__RUNTIME_CONFIG__ = {" + json + "}";
			contentType = MediaType.valueOf("text/javascript");
		} else if ("showall".equalsIgnoreCase(format)) {
			envJson = systemEnv.entrySet().stream().
					map(e -> e.getKey() + "=" + e.getValue()).
					collect(Collectors.joining("\n"));
		} else {
			envJson = "";
		}
		return ResponseEntity.
				ok().
				contentType(contentType).
				body(envJson);
	}

	@SuppressWarnings("rawtypes")
	private Map<String, Object> getAllProperties(Environment env) {
		Map<String, Object> props = new HashMap<>();
		if (env instanceof ConfigurableEnvironment) {
			for (PropertySource<?> propertySource : ((ConfigurableEnvironment) env).getPropertySources()) {
				if (propertySource instanceof EnumerablePropertySource) {
					for (String key: ((EnumerablePropertySource)propertySource).getPropertyNames()) {
						props.put(key, propertySource.getProperty(key));
					}
				}
			}
		}
		return props;
	}

	protected String getAuthToken() {
		return null;
	}

	protected abstract boolean isReactAppMappedFrontProperty(String propertyName);
	protected abstract String getReactAppMappedFrontProperty(String propertyName);
	protected abstract boolean isViteMappedFrontProperty(String propertyName);
	protected abstract String getViteMappedFrontProperty(String propertyName);

	private Map<String, Object> getManifestProperties() throws IOException {
		InputStream manifestIs = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
		if (manifestIs != null) {
			Manifest manifest = new Manifest(manifestIs);
			Attributes attributes = manifest.getMainAttributes();
			Map<String, Object> props = attributes.keySet().stream().collect(Collectors.toMap(
					k -> k.toString(),
					k -> attributes.get(k)));
			return props;
		} else {
			return Collections.emptyMap();
		}
	}

}
