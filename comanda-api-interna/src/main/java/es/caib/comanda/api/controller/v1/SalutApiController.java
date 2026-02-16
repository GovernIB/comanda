package es.caib.comanda.api.controller.v1;

import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.model.v1.salut.SalutInfo;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

/**
 * Contracte de l'API de Salut que COMANDA espera que implementin les APPs.
 * Aquesta classe defineix les rutes i els models retornats per generar el contracte OpenAPI.
 * La implementació real ha de ser aportada per cada APP.
 */
@Deprecated
@Hidden
@RestController
@RequestMapping("/v1/salut")
@Tag(name = "COMANDA → APP / Salut", description = "Contracte d'API de salut i metadades de l'aplicació que COMANDA pot consultar")
public class SalutApiController {

    @Hidden
    @GetMapping("/info")
    @Operation(operationId = "salutInfo",
            summary = "Obtenir informació de l'aplicació",
            description = "Retorna dades bàsiques de l'aplicació (codi, nom, versió, data de build, etc.) i contextos exposats.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta",
                    content = @Content(schema = @Schema(implementation = AppInfo.class))),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
    })
    public AppInfo salutInfo(HttpServletRequest request) throws java.io.IOException {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

    @Hidden
    @GetMapping
    @Operation(operationId = "salut",
            summary = "Obtenir informació de l'estat de salut de l'aplicació",
            description = "Retorna l'estat de salut funcional i integracions, amb metadades de versió.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta",
                    content = @Content(schema = @Schema(implementation = SalutInfo.class))),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
    })
    public SalutInfo salut(HttpServletRequest request) throws java.io.IOException {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

//    @ResponseBody
//    @GetMapping("/salutPerformance")
//    @Operation(summary = "Healthcheck lleuger",
//            description = "Punt lleuger per comprovar disponibilitat (UP/DOWN)")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "UP",
//                    content = @Content(schema = @Schema(implementation = Health.class))),
//            @ApiResponse(responseCode = "500", description = "DOWN")
//    })
//    public abstract Health healthCheck();
//
//    @GetMapping("/metriques")
//    @Operation(summary = "Mètriques",
//            description = "Mètriques de l'aplicació en format text (p. ex. Prometheus)")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "Operació correcta"),
//        @ApiResponse(responseCode = "401", description = "No autenticat"),
//        @ApiResponse(responseCode = "403", description = "Prohibit"),
//        @ApiResponse(responseCode = "500", description = "Error intern del servidor")
//    })
//    public abstract String metriques(HttpServletRequest request) throws Exception;
}
