package es.caib.comanda.api.controller.v1;

import es.caib.comanda.model.v1.estadistica.EstadistiquesInfo;
import es.caib.comanda.model.v1.estadistica.RegistresEstadistics;
import io.swagger.v3.oas.annotations.Hidden;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static es.caib.comanda.ms.back.config.BaseOpenApiConfig.SECURITY_NAME;

/**
 * Contracte de l'API d'Estadístiques que COMANDA espera que implementin les APPs.
 * Defineix rutes i models retornats; cada APP ha d'aportar la implementació.
 */
@Deprecated
@Hidden
@RestController
@RequestMapping("/v1/estadistiques")
@Tag(name = "COMANDA → APP / Estadistiques", description = "Contracte d'API d'estadístiques que COMANDA pot consultar a les APPs")
@SecurityScheme(type = SecuritySchemeType.HTTP, name = SECURITY_NAME, scheme = "basic")
public class EstadistiquesApiController {

    @Hidden
    @GetMapping("/info")
    @SecurityRequirement(name = SECURITY_NAME)
    @Operation(operationId = "estadistiquesInfo",
            summary = "Obtenir informació de 'estructura de les estadístiques",
            description = "Retorna el codi de l'app i el catàleg de dimensions i indicadors disponibles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta",
                    content = @Content(schema = @Schema(implementation = EstadistiquesInfo.class))),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
    })
    public EstadistiquesInfo estadistiquesInfo() throws IOException {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

    @Hidden
    @GetMapping
    @SecurityRequirement(name = SECURITY_NAME)
    @Operation(operationId = "estadistiques",
            summary = "Obtenir darreres estadístiques diàries disponibles",
            description = "Retorna registres d'estadístiques més recents disponibles (estadístiques d'ahir).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta",
                    content = @Content(schema = @Schema(implementation = RegistresEstadistics.class))),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
    })
    public RegistresEstadistics estadistiques(HttpServletRequest request) throws Exception {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

    @Hidden
    @GetMapping("/of/{data}")
    @SecurityRequirement(name = SECURITY_NAME)
    @Operation(operationId = "estadistiquesPerData",
            summary = "Obtenir estadístiques d'una data concreta",
            description = "Retorna les estadístiques corresponents a la data indicada amb format dd-MM-yyyy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta",
                    content = @Content(schema = @Schema(implementation = RegistresEstadistics.class))),
            @ApiResponse(responseCode = "400", description = "Paràmetres invàlids (format de data incorrecte)"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
    })
    public RegistresEstadistics estadistiquesPerData(
            HttpServletRequest request,
            @Parameter(name = "data", description = "Data en format dd-MM-yyyy", required = true) @PathVariable("data") String data) throws Exception {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

//    @Hidden
//    @GetMapping
//    @SecurityRequirement(name = SECURITY_NAME)
//    @Operation(operationId = "estadistiquesPerData2",
//            summary = "Obtenir estadístiques d'una data concreta",
//            description = "Retorna les estadístiques corresponents a la data indicada amb format dd-MM-yyyy.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "Operació correcta",
//                    content = @Content(schema = @Schema(implementation = RegistresEstadistics.class))),
//            @ApiResponse(responseCode = "400", description = "Paràmetres invàlids (format de data incorrecte)"),
//            @ApiResponse(responseCode = "401", description = "No autenticat"),
//            @ApiResponse(responseCode = "403", description = "Prohibit"),
//            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
//    })
//    public RegistresEstadistics estadistiquesPerData2(
//            HttpServletRequest request,
//            @Parameter(name = "data", description = "Data concreta en format dd-MM-yyyy", required = true) @RequestParam("data") String data) throws Exception {
//        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
//                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
//    }

    @Hidden
    @GetMapping("/from/{dataInici}/to/{dataFi}")
    @SecurityRequirement(name = SECURITY_NAME)
    @Operation(operationId = "estadistiquesPerRang",
            summary = "Obtenir les estadístiques d'un interval donat",
            description = "Retorna llista d'estadístiques de tots els dies entre la dataInici i la dataFi (en format dd-MM-yyyy), ambdues incloses. " +
                    "La resposta contindrà un objecte de tipus RegistresEstadistics per a cada dia inclòs en l'intèrval.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta",
                    content = @Content(schema = @Schema(implementation = RegistresEstadistics.class))),
            @ApiResponse(responseCode = "400", description = "Paràmetre invàlid (format de data o rang)"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
    })
    public List<RegistresEstadistics> estadistiquesPerRang(
            HttpServletRequest request,
            @Parameter(name = "dataInici", description = "Data d'inici en format dd-MM-yyyy", required = true) @PathVariable("dataInici") String dataInici,
            @Parameter(name = "dataFi", description = "Data de fi en format dd-MM-yyyy", required = true) @PathVariable("dataFi") String dataFi) throws Exception {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

//    @Hidden
//    @GetMapping
//    @SecurityRequirement(name = SECURITY_NAME)
//    @Operation(operationId = "estadistiquesPerRang2",
//            summary = "Obtenir les estadístiques d'un interval donat",
//            description = "Retorna llista d'estadístiques de tots els dies entre la dataInici i la dataFi (en format dd-MM-yyyy), ambdues incloses. " +
//                    "La resposta contindrà un objecte de tipus RegistresEstadistics per a cada dia inclòs en l'intèrval.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "Operació correcta",
//                    content = @Content(schema = @Schema(implementation = RegistresEstadistics.class))),
//            @ApiResponse(responseCode = "400", description = "Paràmetre invàlid (format de data o rang)"),
//            @ApiResponse(responseCode = "401", description = "No autenticat"),
//            @ApiResponse(responseCode = "403", description = "Prohibit"),
//            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
//    })
//    public List<RegistresEstadistics> estadistiquesPerRang2(
//            HttpServletRequest request,
//            @Parameter(name = "dataInici", description = "Data d'inici del rang en format dd-MM-yyyy") @RequestParam("dataInici") String dataInici,
//            @Parameter(name = "dataFi", description = "Data de fi del rang en format dd-MM-yyyy") @RequestParam("dataFi") String dataFi) throws Exception {
//        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
//                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
//    }

//    // Endpoints de generació de dades (Hidden en contracte públic)
//    @Hidden
//    @RequestMapping(value = "/generarDadesExplotacio", method = RequestMethod.GET)
//    @ResponseBody
//    public String generarDadesExplotacio(HttpServletRequest request) throws Exception {
//        throw new UnsupportedOperationException("Contracte: implementació a càrrec de l'APP");
//    }
//
//    @Hidden
//    @RequestMapping(value = "/generarDadesExplotacio/{dies}", method = RequestMethod.GET)
//    @ResponseBody
//    public String generarDadesExplotacio(HttpServletRequest request, @PathVariable Integer dies) throws Exception {
//        throw new UnsupportedOperationException("Contracte: implementació a càrrec de l'APP");
//    }
//
//    @Hidden
//    @RequestMapping(value = "/generarDadesBasiquesExplotacio/from/{dataInici}/to/{dataFi}", method = RequestMethod.GET)
//    @ResponseBody
//    public String generarDadesBasiquesExplotacio(HttpServletRequest request, @PathVariable String dataInici, @PathVariable String dataFi) throws Exception {
//        throw new UnsupportedOperationException("Contracte: implementació a càrrec de l'APP");
//    }
}
