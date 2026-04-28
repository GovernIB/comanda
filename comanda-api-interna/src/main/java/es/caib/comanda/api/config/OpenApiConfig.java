package es.caib.comanda.api.config;

import es.caib.comanda.ms.back.config.BaseOpenApiConfig;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
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

    static {
        ModelConverters.getInstance().addConverter(new ModelConverter() {
            @Override
            public Schema resolve(AnnotatedType type, io.swagger.v3.core.converter.ModelConverterContext context, Iterator<ModelConverter> chain) {
                java.lang.reflect.Type javaType = type.getType();

                // Comprovem si la classe és byte[]
                if (javaType instanceof Class<?> && ((Class<?>) javaType) == byte[].class) {
                    // Retornem un StringSchema amb format byte.
                    // Swagger agafarà la descripció de l'anotació @Schema del camp automàticament.
                    return new StringSchema().format("byte");
                }

                // Continuar amb la cadena de convertidors si no és un byte[]
                if (chain.hasNext()) {
                    return chain.next().resolve(type, context, chain);
                }
                return null;
            }
        });
    }

    // Limitar la documentació OpenAPI als controladors dels paquets indicats i agrupar-la sota un títol clar
    @Bean
    public GroupedOpenApi apiTasquesGroup() {
        return GroupedOpenApi.builder()
                .group("tasques-v1")
                .displayName("Tasques v1 | APP → COMANDA")
                .packagesToScan("es.caib.comanda.api.controller.v1")
                .pathsToMatch("/tasques/v1/**")
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
                .group("avisos-v1")
                .displayName("Avisos v1 | APP → COMANDA")
                .packagesToScan("es.caib.comanda.api.controller.v1")
                .pathsToMatch("/avisos/v1/**")
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
                .group("permisos-v1")
                .displayName("Permisos v1 | APP → COMANDA")
                .packagesToScan("es.caib.comanda.api.controller.v1")
                .pathsToMatch("/permisos/v1/**")
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
                .group("salut-v1")
                .displayName("Salut v1 | COMANDA → APP")
                .packagesToScan("es.caib.comanda.api.client.controller.v1")
                .pathsToMatch("/salut/v1/**")
                .addOpenApiCustomiser(openApi -> {
                    if (openApi.getInfo() != null) {
                        openApi.getInfo()
                                .title("API Interna de COMANDA utilitzada per obtenir informació de Salut de les aplicacions")
                                .description("Conjunt de serveis REST que les aplicacions han d'implementar per tal que Comanda pugui accedir a la informació de salut de l'aplicació.");
                    }
                    ExternalDocumentation externalDocs = new ExternalDocumentation();
                    externalDocs.setDescription("Exemple el servei generat automàticament a partir d'aquest contracte OpenAPI (Codi font Java)");
                    externalDocs.setUrl("https://github.com/GovernIB/comanda/tree/comanda-dev/comanda-api-servers/comanda-api-server-salut-v1");
                    openApi.setExternalDocs(externalDocs);
                    openApi.getPaths().values().forEach(pathItem ->
                            pathItem.readOperations().forEach(op -> op.setSecurity(List.of())));
                })
                .build();
    }
    @Bean
    public GroupedOpenApi apiEstadistiquesGroup() {
        return GroupedOpenApi.builder()
                .group("estadistiques-v1")
                .displayName("Estadistiques v1 | COMANDA → APP")
                .packagesToScan("es.caib.comanda.api.client.controller.v1")
                .pathsToMatch("/estadistiques/v1/**")
                .addOpenApiCustomiser(openApi -> {
                    if (openApi.getInfo() != null) {
                        openApi.getInfo()
                                .title("API Interna de COMANDA utilitzada per obtenir informació estadística de les aplicacions")
                                .description("Conjunt de serveis REST que les aplicacions han d'implementar per tal que Comanda pugui accedir a les dades estadístiques de l'aplicació.");
                    }
                    ExternalDocumentation externalDocs = new ExternalDocumentation();
                    externalDocs.setDescription("Exemple el servei generat automàticament a partir d'aquest contracte OpenAPI (Codi font Java)");
                    externalDocs.setUrl("https://github.com/GovernIB/comanda/tree/comanda-dev/comanda-api-servers/comanda-api-server-estadistica-v1");
                    openApi.setExternalDocs(externalDocs);
                })
                .build();
    }
    @Bean
    public GroupedOpenApi apiLogGroup() {
        return GroupedOpenApi.builder()
                .group("logs-v1")
                .displayName("Logs v1 | COMANDA → APP")
                .packagesToScan("es.caib.comanda.api.client.controller.v1")
                .pathsToMatch("/logs/v1/**")
                .addOpenApiCustomiser(openApi -> {
                    if (openApi.getInfo() != null) {
                        openApi.getInfo()
                                .title("API Interna de COMANDA utilitzada per obtenir els logs de les aplicacions")
                                .description("Conjunt de serveis REST que les aplicacions han d'implementar per tal que Comanda pugui accedir als fitxers de log de l'aplicació.");
                    }
                    ExternalDocumentation externalDocs = new ExternalDocumentation();
                    externalDocs.setDescription("Exemple el servei generat automàticament a partir d'aquest contracte OpenAPI (Codi font Java)");
                    externalDocs.setUrl("https://github.com/GovernIB/comanda/tree/comanda-dev/comanda-api-servers/comanda-api-server-log-v1");
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
