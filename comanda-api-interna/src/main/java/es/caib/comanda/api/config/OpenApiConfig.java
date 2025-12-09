package es.caib.comanda.api.config;

import es.caib.comanda.ms.back.config.BaseOpenApiConfig;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig extends BaseOpenApiConfig {

    @Override
    protected String getTitle() {
        return "API Interna de COMANDA que ofereix serveis que les APPs poden utilitzar per interactuar amb COMANDA";
    }

    @Override
    protected boolean enableAuthComponent() {
        return true;
    }

    @Override
    protected AuthType getAuthType() {
        return AuthType.BASIC;
    }

    // Limitar la documentació OpenAPI als controladors dels paquets indicats i agrupar-la sota un títol clar
    @Bean
    public GroupedOpenApi apiAppComandaGroup() {
        return GroupedOpenApi.builder()
                .group("APP → COMANDA v1")
                .packagesToScan("es.caib.comanda.api.controller.v1")
                .pathsToMatch("/api/v1/jms/**")
                .build();
    }

    @Bean
    public GroupedOpenApi apiComandaAppGroup() {
        return GroupedOpenApi.builder()
                .group("COMANDA → APP v1")
                .packagesToScan("es.caib.comanda.api.client.controller.v1")
                // Incloure els endpoints del contracte COMANDA → APP (salut, appInfo, estadístiques)
                // NOTA: Amb Spring Boot 2.7 + springdoc 1.x, el matching pot variar segons l'estratègia;
                // fer servir un patró més ampli evita falsos negatius.
                .pathsToMatch("/api/v1/**")
                // Excloure qualsevol altre endpoint que es pugui colar (monitoratge, JMS, etc.)
                .pathsToExclude(
                        "/api/**/monitor/**",
                        "/api/v1/jms/**"
                )
                .addOpenApiCustomiser(openApi -> {
                    if (openApi.getInfo() != null) {
                        openApi.getInfo()
                                .description("API que COMANDA utilitza per cridar a les aplicacions integrades. ");
                    }
                    ExternalDocumentation externalDocs = new ExternalDocumentation();
                    externalDocs.setDescription("Exemple el servei generat automàticament a partir d'aquest contracte OpenAPI (Codi font Java)");
                    externalDocs.setUrl("https://github.com/GovernIB/comanda/tree/comanda-dev/api-server-comanda-app-v1");
                    openApi.setExternalDocs(externalDocs);
                })
                .build();
    }

    // Afegim informació general i la llista de servidors a l'esquema OpenAPI
    @Bean
    @Override
    public OpenAPI customOpenAPI() {
        OpenAPI openapi = super.customOpenAPI();
        // Enriquir Info amb descripció, contacte i llicència
        if (openapi.getInfo() != null) {
            openapi.getInfo()
                    .description("API interna de COMANDA. Inclou els serveis que les APPs poden utilitzar per interactuar amb COMANDA. " +
                            "Es recomana usar el client Java generat automàticament a partir d'aquest contracte OpenAPI.")
                    .contact(new Contact()
                            .name("Govern de les Illes Balears - Suport")
                            .email("suport@caib.es"))
                    .license(new License()
                            .name("EUPL v1.2")
                            .url("https://joinup.ec.europa.eu/collection/eupl/eupl-text-11-12"));
        }

        // Definició dels servidors habituals
        List<Server> servers = new java.util.ArrayList<>();
        servers.add(new Server().url("/comandaapi/interna"));
        servers.add(new Server().url("http://localhost:8080/comandaapi/interna").description("Local"));
        servers.add(new Server().url("https://dev.caib.es/comandaapi/interna").description("DEV"));
        servers.add(new Server().url("https://proves.caib.es/comandaapi/interna").description("PRE"));
        servers.add(new Server().url("https://se.caib.es/comandaapi/interna").description("SE"));
        servers.add(new Server().url("https://www.caib.es/comandaapi/interna").description("PRO"));
        openapi.setServers(servers);

        return openapi;
    }
}
