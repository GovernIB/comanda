package es.caib.comanda.api.server.monitoring;

import es.caib.comanda.model.server.monitoring.AppInfo;
import es.caib.comanda.model.server.monitoring.SalutInfo;

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
@Api(description = "the ComandaAppSalut API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public interface ComandaAppSalutApi {

    /**
     * Retorna l'estat de salut funcional i integracions, amb metadades de versió.
     *
     * @return successful operation
     */
    @GET
    @Path("/salut/v1")
    @Produces({ "application/json" })
    @ApiOperation(value = "Obtenir informació de l'estat de salut de l'aplicació", notes = "Retorna l'estat de salut funcional i integracions, amb metadades de versió.", tags={ "COMANDA → APP / Salut" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = SalutInfo.class) })
    SalutInfo salut();


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
