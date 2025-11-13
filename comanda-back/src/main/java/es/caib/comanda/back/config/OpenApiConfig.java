package es.caib.comanda.back.config;

import es.caib.comanda.ms.back.config.BaseOpenApiConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig extends BaseOpenApiConfig {

    @Override
    protected String getTitle() {
        return "API Back";
    }

    @Override
    protected boolean enableAuthComponent() {
        return true;
    }
}
