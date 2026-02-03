package es.caib.comanda.api.controller.v1.log;

import es.caib.comanda.model.v1.log.FitxerContingut;
import es.caib.comanda.model.v1.log.FitxerInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static es.caib.comanda.ms.back.config.BaseOpenApiConfig.BASIC_SECURITY_SCHEME;
import static es.caib.comanda.ms.back.config.BaseOpenApiConfig.SECURITY_NAME;


/**
 * Contracte de l'API de Salut que COMANDA espera que implementin les APPs.
 * Aquesta classe defineix les rutes i els models retornats per generar el contracte OpenAPI.
 * La implementació real ha de ser aportada per cada APP.
 */
@RestController
@RequestMapping("/logs/v1")
@Tag(name = "COMANDA → APP / Logs", description = "Contracte d'API de salut i metadades de l'aplicació que COMANDA pot consultar")
@SecurityScheme(type = SecuritySchemeType.HTTP, name = SECURITY_NAME, scheme = BASIC_SECURITY_SCHEME)
@PreAuthorize("hasRole(T(es.caib.comanda.base.config.BaseConfig).ROLE_WEBSERVICE)")
public class LogApiV1Controller {

    @Operation(operationId = "llistarFitxers",
            summary = "Obtenir el llistat de fitxers de log disponibles",
            description = "Retorna una llista amb tots els fitxers que es troben dins la carpeta de logs del servidor de l'aplicació")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Llista de fitxers obtinguda correctament",
                    content = @Content(schema = @Schema(implementation = FitxerInfo.class))),
            @ApiResponse(responseCode = "501", description = "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.")
    })
    @GetMapping()
    @SecurityRequirement(name = SECURITY_NAME)
    public List<FitxerInfo> getFitxers() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

    @Operation(operationId = "getFitxerByNom",
            summary = "Obtenir contingut complet d'un fitxer de log",
            description = "Retorna el contingut i detalls del fitxer de log que es troba dins la carpeta de logs del servidor, i que té el nom indicat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contingut del fitxer obtingut correctament",
                    content = @Content(schema = @Schema(implementation = FitxerContingut.class))),
            @ApiResponse(responseCode = "404", description = "Fitxer no trobat"),
            @ApiResponse(responseCode = "501", description = "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.")
    })
    @GetMapping("/{nomFitxer}")
    @SecurityRequirement(name = SECURITY_NAME)
    public FitxerContingut getFitxerByNom(@Parameter(name = "nomFitxer", description = "Nom del firxer", required = true) @PathVariable("nomFitxer") String nomFitxer) {
//        return logService.getFitxerByNom(nom);
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

    @Operation(operationId = "llegitUltimesLinies",
            summary = "Obtenir les darreres línies d'un fitxer de log",
            description = "Retorna les darreres linies del fitxer de log indicat per nom. Concretament es retorna el número de línies indicat al paràmetre nLinies.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Línies del fitxer obtingudes correctament",
                    content = @Content(schema = @Schema(implementation = String.class, type = "array", example = "[\n" +
                            "  \"2024-01-15 10:23:45.123 ERROR [http-nio-8080-exec-1] c.e.c.a.c.GlobalExceptionHandler - Error processing request: Connection timeout\",\n" +
                            "  \"2024-01-15 10:23:46.456 WARN  [http-nio-8080-exec-2] c.e.c.a.s.UserService - User authentication failed for user: admin\",\n" +
                            "  \"2024-01-15 10:23:47.789 INFO  [http-nio-8080-exec-3] c.e.c.a.c.LogController - Fetching log file: application.log\",\n" +
                            "  \"2024-01-15 10:23:48.012 DEBUG [http-nio-8080-exec-4] c.e.c.a.r.DatabaseRepository - Executing query: SELECT * FROM users WHERE active=true\",\n" +
                            "  \"2024-01-15 10:23:49.345 INFO  [http-nio-8080-exec-5] c.e.c.a.c.HealthController - Health check completed successfully\"\n" +
                            "]"))),
            @ApiResponse(responseCode = "404", description = "Fitxer no trobat"),
            @ApiResponse(responseCode = "501", description = "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.")
    })
    @GetMapping("/{nomFitxer}/linies/{nLinies}")
    @SecurityRequirement(name = SECURITY_NAME)
    public List<String> getFitxerLinies(
            @Parameter(name = "nomFitxer", description = "Nom del firxer", required = true) @PathVariable("nomFitxer") String nomFitxer,
            @Parameter(name = "nLinies", description = "Número de línies a recuperar del firxer", required = true) @PathVariable("nLinies") Long nLinies) {
//        return logService.readLastNLines(nomFitxer, nLinies);
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

}
