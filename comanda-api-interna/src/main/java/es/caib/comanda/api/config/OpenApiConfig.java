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
    public GroupedOpenApi apiTasquesGroup() {
        return GroupedOpenApi.builder()
                .group("Tasques-v1")
                .displayName("Tasques v1 | APP → COMANDA")
                .packagesToScan("es.caib.comanda.api.controller.v1")
                .pathsToMatch("/v1/tasques/**")
                .addOpenApiCustomiser(openApi -> {
                    if (openApi.getInfo() != null) {
                        openApi.getInfo()
                                .title("API Interna de comanda que les aplicacions poden utilitzar per enviar informació de tasques a COMANDA")
                                .description("Conjunt de serveis REST de Comanda per poder donar d'alta o modificar tasques.");
                    }
                })
                .build();
    }
    @Bean
    public GroupedOpenApi apiAvisosGroup() {
        return GroupedOpenApi.builder()
                .group("Avisos-v1")
                .displayName("Avisos v1 | APP → COMANDA")
                .packagesToScan("es.caib.comanda.api.controller.v1")
                .pathsToMatch("/v1/avisos/**")
                .addOpenApiCustomiser(openApi -> {
                    if (openApi.getInfo() != null) {
                        openApi.getInfo()
                                .title("API Interna de comanda que les aplicacions poden utilitzar per enviar informació d'avisos a COMANDA")
                                .description("Conjunt de serveis REST de Comanda per poder donar d'alta o modificar avisos.");
                    }
                })
                .build();
    }
    @Bean
    public GroupedOpenApi apiPermisosGroup() {
        return GroupedOpenApi.builder()
                .group("Permisos-v1")
                .displayName("Permisos v1 | APP → COMANDA")
                .packagesToScan("es.caib.comanda.api.controller.v1")
                .pathsToMatch("/v1/permisos/**")
                .addOpenApiCustomiser(openApi -> {
                    if (openApi.getInfo() != null) {
                        openApi.getInfo()
                                .title("API Interna de comanda que les aplicacions poden utilitzar per enviar informació de permisos a COMANDA")
                                .description("Conjunt de serveis REST de Comanda per poder donar d'alta o modificar informació dels permisos que els usuaris tenen assignats a les diferents aplicacions.");
                    }
                })
                .build();
    }

    @Bean
    public GroupedOpenApi apiSalutGroup() {
        return GroupedOpenApi.builder()
                .group("Salut-v1")
                .displayName("Salut v1 | COMANDA → APP")
                .packagesToScan("es.caib.comanda.api.client.controller.v1")
                .pathsToMatch("/v1/salut/**")
                .addOpenApiCustomiser(openApi -> {
                    if (openApi.getInfo() != null) {
                        openApi.getInfo()
                                .title("API Interna de COMANDA utilitzada per obtenir informació de Salut de les aplicacions")
                                .description("Conjunt de serveis REST que les aplicacions han d'implementar per tal que Comanda pugui accedir a la informació de salut de l'aplicació.");
                    }
                    ExternalDocumentation externalDocs = new ExternalDocumentation();
                    externalDocs.setDescription("Exemple el servei generat automàticament a partir d'aquest contracte OpenAPI (Codi font Java)");
                    externalDocs.setUrl("https://github.com/GovernIB/comanda/tree/comanda-dev/comanda-api-server-salut-v1");
                    openApi.setExternalDocs(externalDocs);
                    openApi.getPaths().values().forEach(pathItem ->
                            pathItem.readOperations().forEach(op -> op.setSecurity(List.of())));
                })
                .build();
    }
    @Bean
    public GroupedOpenApi apiEstadistiquesGroup() {
        return GroupedOpenApi.builder()
                .group("Estadístiques-v1")
                .displayName("Estadístiques v1 | COMANDA → APP")
                .packagesToScan("es.caib.comanda.api.client.controller.v1")
                .pathsToMatch("/v1/estadistiques/**")
                .addOpenApiCustomiser(openApi -> {
                    if (openApi.getInfo() != null) {
                        openApi.getInfo()
                                .title("API Interna de COMANDA utilitzada per obtenir informació estadística de les aplicacions")
                                .description("Conjunt de serveis REST que les aplicacions han d'implementar per tal que Comanda pugui accedir a les dades estadístiques de l'aplicació.");
                    }
                    ExternalDocumentation externalDocs = new ExternalDocumentation();
                    externalDocs.setDescription("Exemple el servei generat automàticament a partir d'aquest contracte OpenAPI (Codi font Java)");
                    externalDocs.setUrl("https://github.com/GovernIB/comanda/tree/comanda-dev/comanda-api-server-estadistica-v1");
                    openApi.setExternalDocs(externalDocs);
                })
                .build();
    }
    @Bean
    public GroupedOpenApi apiLogGroup() {
        return GroupedOpenApi.builder()
                .group("Logs-v1")
                .displayName("Logs v1 | COMANDA → APP")
                .packagesToScan("es.caib.comanda.api.client.controller.v1")
                .pathsToMatch("/v1/logs/**")
                .addOpenApiCustomiser(openApi -> {
                    if (openApi.getInfo() != null) {
                        openApi.getInfo()
                                .title("API Interna de COMANDA utilitzada per obtenir els logs de les aplicacions")
                                .description("Conjunt de serveis REST que les aplicacions han d'implementar per tal que Comanda pugui accedir als fitxers de log de l'aplicació.");
                    }
                    ExternalDocumentation externalDocs = new ExternalDocumentation();
                    externalDocs.setDescription("Exemple el servei generat automàticament a partir d'aquest contracte OpenAPI (Codi font Java)");
                    externalDocs.setUrl("https://github.com/GovernIB/comanda/tree/comanda-dev/comanda-api-server-log-v1");
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
