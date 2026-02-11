package es.caib.comanda.api.server.monitoring;

import es.caib.comanda.model.server.monitoring.AppInfo;
import es.caib.comanda.model.server.monitoring.SalutInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Api(description = "the ComandaAppSalut API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public interface ComandaAppSalutApi {

    /**
     * Retorna l'estat de salut funcional i integracions, amb metadades de versió.
     *
     * @param dataPeriode Data mínima de la que es demana informació per període
     * @param dataTotal Data mínima de la que demana informació per totals
     * @return successful operation
     */
    @GET
    @Path("/salut/v1")
    @Produces({ "application/json" })
    @ApiOperation(value = "Obtenir informació de l'estat de salut de l'aplicació", notes = "Retorna l'estat de salut funcional i integracions, amb metadades de versió.", tags={ "COMANDA → APP / Salut" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = SalutInfo.class) })
    SalutInfo salut(@QueryParam("dataPeriode")  @ApiParam("Data mínima de la que es demana informació per període")  java.time.OffsetDateTime dataPeriode,@QueryParam("dataTotal")  @ApiParam("Data mínima de la que demana informació per totals")  java.time.OffsetDateTime dataTotal);


    /**
     * Retorna dades bàsiques de l'aplicació (codi, nom, versió, data de build, etc.) i contextos exposats.
     *
     * @return successful operation
     */
    @GET
    @Path("/salut/v1/info")
    @Produces({ "application/json" })
    @ApiOperation(value = "Obtenir informació de l'aplicació", notes = "Retorna dades bàsiques de l'aplicació (codi, nom, versió, data de build, etc.) i contextos exposats.", tags={ "COMANDA → APP / Salut" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = AppInfo.class) })
    AppInfo salutInfo();

}
