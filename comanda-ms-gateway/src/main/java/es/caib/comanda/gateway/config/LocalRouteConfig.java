package es.caib.comanda.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.net.URI;
import java.net.URL;
import java.util.List;

@Slf4j
@Configuration
public class LocalRouteConfig {

	@Value("${es.caib.comanda.gateway.index.service.url}")
	private String indexServiceUrl;
	@Value("${es.caib.comanda.gateway.app.service.url}")
	private String appServiceUrl;
	@Value("${es.caib.comanda.gateway.salut.service.url}")
	private String salutServiceUrl;

	@Bean
	public RouteLocator localRoutes(RouteLocatorBuilder builder) {
		return builder.routes().
				route("index", r -> r.path(toRoutePattern(indexServiceUrl, false)).uri(indexServiceUrl)).
				route("app", r -> r.path(toRoutePattern(appServiceUrl, true)).uri(appServiceUrl)).
				route("salut", r -> r.path(toRoutePattern(salutServiceUrl, true)).uri(salutServiceUrl)).
				build();
	}

	@Bean
	public CorsWebFilter corsWebFilter() {
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOrigins(List.of("*"));
		corsConfig.setAllowedMethods(List.of("*"));
		corsConfig.setAllowedHeaders(List.of("*"));
		corsConfig.setAllowCredentials(/*true*/false);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig); // Aplica la configuració a totes les rutes
		return new CorsWebFilter(source);
	}

	private String toRoutePattern(String serviceUrl, boolean wildcards) {
		try {
			URL url = new URI(serviceUrl).toURL();
			return url.getFile() + (wildcards ? "/**" : "");
		} catch (Exception ex) {
			log.error("Couldn't get pattern from service URL " + serviceUrl);
			return null;
		}
	}

}
