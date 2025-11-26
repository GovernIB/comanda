package es.caib.comanda.api.client.controller.v1;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.estadistica.model.EstadistiquesInfo;
import es.caib.comanda.ms.estadistica.model.RegistresEstadistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * Contracte de l'API d'Estadístiques que COMANDA espera que implementin les APPs.
 * Defineix rutes i models retornats; cada APP ha d'aportar la implementació.
 */
@RestController
@RequestMapping(BaseConfig.API_PATH + "/v1/estadistiques")
@Tag(name = "COMANDA → APP / Estadístiques", description = "Contracte d'API d'estadístiques que COMANDA pot consultar a les APPs")
public class EstadistiquesApiController {

    @GetMapping("/info")
    @Operation(summary = "Informació d'estadístiques",
            description = "Retorna el codi de l'app i el catàleg de dimensions i indicadors disponibles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta",
                    content = @Content(schema = @Schema(implementation = EstadistiquesInfo.class))),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
    })
    public EstadistiquesInfo statsInfo() throws IOException {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

    @GetMapping
    @Operation(summary = "Darrera captura d'estadístiques",
            description = "Retorna registres d'estadístiques més recents disponibles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta",
                    content = @Content(schema = @Schema(implementation = RegistresEstadistics.class))),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
    })
    public RegistresEstadistics estadistiques(HttpServletRequest request) throws IOException {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

//    @GetMapping("/estadistiques/{dies}")
//    @Operation(summary = "Històric d'estadístiques (N dies)",
//            description = "Retorna llista de registres per als darrers N dies (sense incloure el dia en curs).")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "Operació correcta",
//                    content = @Content(schema = @Schema(implementation = RegistresEstadistics.class))),
//            @ApiResponse(responseCode = "400", description = "Paràmetre invàlid (dies)"),
//            @ApiResponse(responseCode = "401", description = "No autenticat"),
//            @ApiResponse(responseCode = "403", description = "Prohibit"),
//            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
//    })
//    public abstract List<RegistresEstadistics> estadistiques(HttpServletRequest request, @PathVariable Integer dies) throws IOException;

    @GetMapping("/of/{data}")
    @Operation(summary = "Estadístiques d'una data",
            description = "Retorna estadístiques corresponents a la data indicada amb format dd-MM-yyyy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta",
                    content = @Content(schema = @Schema(implementation = RegistresEstadistics.class))),
            @ApiResponse(responseCode = "400", description = "Paràmetre invàlid (data no compleix el format dd-MM-yyyy)"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
    })
    public RegistresEstadistics estadistiques(HttpServletRequest request, @PathVariable String data) throws Exception {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

    @GetMapping("/from/{dataInici}/to/{dataFi}")
    @Operation(summary = "Estadístiques per interval",
            description = "Retorna llista d'estadístiques des de dataInici fins a dataFi (ambdues dd-MM-yyyy).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta",
                    content = @Content(schema = @Schema(implementation = RegistresEstadistics.class))),
            @ApiResponse(responseCode = "400", description = "Paràmetre invàlid (format de data o rang)"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
    })
    public List<RegistresEstadistics> estadistiques(HttpServletRequest request, @PathVariable String dataInici, @PathVariable String dataFi) throws Exception {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "No implementat a COMANDA. Aquest endpoint l'ha d'exposar l'APP.");
    }

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
