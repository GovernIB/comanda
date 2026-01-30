package es.caib.comanda.api.server.v1.monitoring;

import es.caib.comanda.model.server.v1.monitoring.FitxerContingut;
import es.caib.comanda.model.server.v1.monitoring.FitxerInfo;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Api(description = "the ComandaAppLogs API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public interface ComandaAppLogsApi {

    /**
     * Retorna el contingut i detalls del fitxer de log que es troba dins la carpeta de logs del servidor, i que té el nom indicat
     *
     * @param nomFitxer Nom del firxer
     * @return successful operation
     */
    @GET
    @Path("/logs/v1/{nomFitxer}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Obtenir contingut complet d'un fitxer de log", notes = "Retorna el contingut i detalls del fitxer de log que es troba dins la carpeta de logs del servidor, i que té el nom indicat", tags={ "COMANDA → APP / Logs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = FitxerContingut.class) })
    FitxerContingut getFitxerByNom(@PathParam("nomFitxer") @ApiParam("Nom del firxer") String nomFitxer);


    /**
     * Retorna les darreres linies del fitxer de log indicat per nom. Concretament es retorna el número de línies indicat al paràmetre nLinies.
     *
     * @param nomFitxer Nom del firxer
     * @param nLinies Número de línies a recuperar del firxer
     * @return successful operation
     */
    @GET
    @Path("/logs/v1/{nomFitxer}/linies/{nLinies}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Obtenir les darreres línies d'un fitxer de log", notes = "Retorna les darreres linies del fitxer de log indicat per nom. Concretament es retorna el número de línies indicat al paràmetre nLinies.", tags={ "COMANDA → APP / Logs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = String.class, responseContainer = "List") })
    List<String> llegitUltimesLinies(@PathParam("nomFitxer") @ApiParam("Nom del firxer") String nomFitxer,@PathParam("nLinies") @ApiParam("Número de línies a recuperar del firxer") Long nLinies);


    /**
     * Retorna una llista amb tots els fitxers que es troben dins la carpeta de logs del servidor de l'aplicació
     *
     * @return successful operation
     */
    @GET
    @Path("/logs/v1")
    @Produces({ "application/json" })
    @ApiOperation(value = "Obtenir el llistat de fitxers de log disponibles", notes = "Retorna una llista amb tots els fitxers que es troben dins la carpeta de logs del servidor de l'aplicació", tags={ "COMANDA → APP / Logs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = FitxerInfo.class, responseContainer = "List") })
    List<FitxerInfo> llistarFitxers();

}
