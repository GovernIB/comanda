package es.caib.comanda.api.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("openapi")
@Configuration
@ComponentScan(
        basePackages = {
                "es.caib.comanda.api"
                // afegeix aquí altres paquets estrictament necessaris per arrencar /v3/api-docs
        }
)
public class OpenApiComponentScanConfig {
}
