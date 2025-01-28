package es.caib.comanda.back.controller;

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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador per retornar la informació de les variables d'entorn.
 * 
 * @author Límit Tecnologies
 */
@Hidden
@RestController
public class SysenvController {

	@Autowired
	private Environment env;

	@GetMapping("/sysenv")
	public ResponseEntity<String> systemEnvironment(
			@RequestParam(required = false) String format) {
		Map<String, Object> systemEnv = getAllProperties(env);
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
					filter(e -> e.getKey().startsWith("REACT_APP")).
					map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\",").
					collect(Collectors.joining("\n"));
			envJson = "window.__RUNTIME_CONFIG__ = {" + json + "}";
			contentType = MediaType.valueOf("text/javascript");
		} else if ("vite".equalsIgnoreCase(format)) {
			String json = systemEnv.entrySet().stream().
					filter(e -> e.getKey().startsWith("VITE")).
					map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\",").
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
	public static Map<String, Object> getAllProperties(Environment env) {
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

}
