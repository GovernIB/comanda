package es.caib.comanda.api.server.monitoring;

import es.caib.comanda.model.server.monitoring.EstadistiquesInfo;
import es.caib.comanda.model.server.monitoring.RegistresEstadistics;

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
@Path("/estadistiques/v1")
@Api(description = "the ComandaAppEstadistiques API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public interface ComandaAppEstadistiquesApi {

    /**
     * Retorna registres d'estadístiques més recents disponibles (estadístiques d'ahir).
     *
     * @return successful operation
     */
    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Obtenir darreres estadístiques diàries disponibles", notes = "Retorna registres d'estadístiques més recents disponibles (estadístiques d'ahir).", tags={ "COMANDA → APP / Estadistiques" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = RegistresEstadistics.class) })
    RegistresEstadistics estadistiques();

    /**
     * Retorna el codi de l'app i el catàleg de dimensions i indicadors disponibles.
     *
     * @return successful operation
     */
    @GET
    @Path("/info")
    @Produces({ "application/json" })
    @ApiOperation(value = "Obtenir informació de 'estructura de les estadístiques", notes = "Retorna el codi de l'app i el catàleg de dimensions i indicadors disponibles.", tags={ "COMANDA → APP / Estadistiques" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = EstadistiquesInfo.class) })
    EstadistiquesInfo estadistiquesInfo();

    /**
     * Retorna les estadístiques corresponents a la data indicada amb format dd-MM-yyyy.
     *
     * @param data Data en format dd-MM-yyyy
     * @return successful operation
     */
    @GET
    @Path("/of/{data}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Obtenir estadístiques d'una data concreta", notes = "Retorna les estadístiques corresponents a la data indicada amb format dd-MM-yyyy.", tags={ "COMANDA → APP / Estadistiques" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = RegistresEstadistics.class) })
    RegistresEstadistics estadistiquesPerData(@PathParam("data") @ApiParam("Data en format dd-MM-yyyy") String data);

    /**
     * Retorna llista d'estadístiques de tots els dies entre la dataInici i la dataFi (en format dd-MM-yyyy), ambdues incloses. La resposta contindrà un objecte de tipus RegistresEstadistics per a cada dia inclòs en l'intèrval.
     *
     * @param dataInici Data d&#39;inici en format dd-MM-yyyy
     * @param dataFi Data de fi en format dd-MM-yyyy
     * @return successful operation
     */
    @GET
    @Path("/from/{dataInici}/to/{dataFi}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Obtenir les estadístiques d'un interval donat", notes = "Retorna llista d'estadístiques de tots els dies entre la dataInici i la dataFi (en format dd-MM-yyyy), ambdues incloses. La resposta contindrà un objecte de tipus RegistresEstadistics per a cada dia inclòs en l'intèrval.", tags={ "COMANDA → APP / Estadistiques" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = RegistresEstadistics.class, responseContainer = "List") })
    List<RegistresEstadistics> estadistiquesPerRang(@PathParam("dataInici") @ApiParam("Data d&#39;inici en format dd-MM-yyyy") String dataInici,@PathParam("dataFi") @ApiParam("Data de fi en format dd-MM-yyyy") String dataFi);
}
