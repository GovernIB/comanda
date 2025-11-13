package es.caib.comanda.api.config;

import es.caib.comanda.ms.back.config.BaseOpenApiConfig;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig extends BaseOpenApiConfig {

    @Override
    protected String getTitle() {
        return "API Interna";
    }

    @Override
    protected boolean enableAuthComponent() {
        return true;
    }

    @Override
    protected AuthType getAuthType() {
        return AuthType.BASIC;
    }

    // Limitar la documentació OpenAPI als controladors del paquet indicat
    @Bean
    public GroupedOpenApi apiInternaGroup() {
        return GroupedOpenApi.builder()
                .group("api-interna")
                .packagesToScan("es.caib.comanda.api.controller")
                .build();
    }
}
