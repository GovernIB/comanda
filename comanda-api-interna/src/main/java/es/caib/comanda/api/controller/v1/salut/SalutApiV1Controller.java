package es.caib.comanda.api.controller.v1.salut;

import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.model.v1.salut.SalutInfo;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;

import static es.caib.comanda.ms.back.config.BaseOpenApiConfig.BASIC_SECURITY_SCHEME;
import static es.caib.comanda.ms.back.config.BaseOpenApiConfig.SECURITY_NAME;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

/**
 * Contracte de l'API de Salut que COMANDA espera que implementin les APPs.
 * Aquesta classe defineix les rutes i els models retornats per generar el contracte OpenAPI.
 * La implementació real ha de ser aportada per cada APP.
 */
@RestController
@RequestMapping("/salut/v1")
@Tag(name = "COMANDA → APP / Salut", description = "Contracte d'API de salut i metadades de l'aplicació que COMANDA pot consultar")
@SecurityScheme(type = SecuritySchemeType.HTTP, name = SECURITY_NAME, scheme = BASIC_SECURITY_SCHEME)
public class SalutApiV1Controller {

    @GetMapping("/info")
    @PreAuthorize("hasRole(T(es.caib.comanda.base.config.BaseConfig).ROLE_WEBSERVICE)")
    @SecurityRequirement(name = SECURITY_NAME)
    @Operation(operationId = "salutInfo",
            summary = "Obtenir informació de l'aplicació",
            description = "Retorna dades bàsiques de l'aplicació (codi, nom, versió, data de build, etc.) i contextos exposats.",
            tags = {"COMANDA → APP / Salut"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta", content = @Content(schema = @Schema(implementation = AppInfo.class))),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
    })
    public AppInfo salutInfo(HttpServletRequest request) throws java.io.IOException {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

    @GetMapping
    @Operation(operationId = "salut",
            summary = "Obtenir informació de l'estat de salut de l'aplicació",
            description = "Retorna l'estat de salut funcional i integracions, amb metadades de versió.",
            tags = {"COMANDA → APP / Salut"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta",
                    content = @Content(schema = @Schema(implementation = SalutInfo.class))),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor"),
    })
    public SalutInfo salut(
            HttpServletRequest request,
            @DateTimeFormat(iso = DATE_TIME) @Parameter(name = "dataPeriode", description = "Data mínima de la que es demana informació per període", required = false) @RequestParam(required = false) OffsetDateTime dataPeriode,
            @DateTimeFormat(iso = DATE_TIME) @Parameter(name = "dataTotal", description = "Data mínima de la que demana informació per totals", required = false) @RequestParam(required = false) OffsetDateTime dataTotal) throws java.io.IOException {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

}
