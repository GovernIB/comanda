package es.caib.comanda.api.controller.v1.salut;

import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.model.v1.salut.SalutInfo;
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
@RestController
@RequestMapping("/salut/v1")
@Tag(name = "COMANDA → APP / Salut", description = "Contracte d'API de salut i metadades de l'aplicació que COMANDA pot consultar")
public class SalutApiV1Controller {

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

    @GetMapping
    @Operation(operationId = "salut",
            summary = "Obtenir informació de l'estat de salut de l'aplicació",
            description = "Retorna l'estat de salut funcional i integracions, amb metadades de versió.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta",
                    content = @Content(schema = @Schema(implementation = SalutInfo.class))),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor"),
    })
    public SalutInfo salut(HttpServletRequest request) throws java.io.IOException {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

}
