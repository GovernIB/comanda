package es.caib.comanda.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Profile("!spring-cloud && !compose")
@Configuration
@EnableWebFluxSecurity
public class LocalRouteConfig {

    @Bean
    public RouteLocator localRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
//                .route("comanda-salut-service",
//                        r -> r.path("/api/v1/salut*", "/api/v1/salut/*")
//                                .filters(f -> f.circuitBreaker(c -> c.setName("salutCB")
//                                        .setFallbackUri("forward:/salut-failover")
//                                        .setRouteId("slt-failover")))
//                                .uri("http://localhost:8081"))
//                .route("comanda-salut-failover-service",
//                        r -> r.path("/api/salut-failover/**")
//                                .uri("http://localhost:8181"))
//                .build();
                .route("comanda-configuracio-service", r -> r.path("/api/v1/configuracio/**").uri("lb://comanda-configuracio-service"))
                .route("comanda-salut-service", r -> r.path("/api/v1/salut/**").uri("lb://comanda-salut-service"))
                .route("comanda-recoleccio-service", r -> r.path("/api/v1/recoleccio/**").uri("lb://comanda-recoleccio-service"))
                .route("comanda-alarmes-service", r -> r.path("/api/v1/alarmes/**").uri("lb://comanda-alarmes-service"))
                .route("comanda-visualitzacio-service", r -> r.path("/api/v1/visualitzacio/**").uri("lb://comanda-visualitzacio-service"))
                .build();

    }

    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange()
//                .pathMatchers("/eureka-server/**").permitAll()
//                .pathMatchers("/config-server/**").authenticated()
                .anyExchange().permitAll()
                .and()
                .oauth2Login();

        return http.build();
    }
}
