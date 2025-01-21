package es.caib.comanda.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalRouteConfig {

	@Value("${es.caib.comanda.app.service.url}")
	private String appServiceUrl;
	@Value("${es.caib.comanda.salut.service.url}")
	private String salutServiceUrl;

	@Bean
	public RouteLocator localRoutes(RouteLocatorBuilder builder) {
		return builder.routes().
				route("app", r -> r.path("/api/apps/**").uri(appServiceUrl)).
				route("salut", r -> r.path("/api/saluts/**").uri(salutServiceUrl)).
				build();
	}

}
