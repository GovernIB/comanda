package es.caib.comanda.api.v1.log;

import es.caib.comanda.model.v1.log.*;
import es.caib.comanda.api.v1.log.ComandaAppLogsApiService;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import es.caib.comanda.model.v1.log.FitxerContingut;
import es.caib.comanda.model.v1.log.FitxerInfo;

import java.util.Map;
import java.util.List;
import es.caib.comanda.api.v1.log.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;
import javax.inject.Inject;

import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/v1/logs")


@io.swagger.annotations.Api(description = "the ComandaAppLogs API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaResteasyServerCodegen", comments = "Generator version: 7.17.0")
public class ComandaAppLogsApi  {

    @Inject ComandaAppLogsApiService service;

    @GET
    @Path("/{nomFitxer}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Obtenir contingut complet d'un fitxer de log", notes = "Retorna el contingut i detalls del fitxer de log que es troba dins la carpeta de logs del servidor, i que té el nom indicat", response = FitxerContingut.class, tags={ "COMANDA → APP / Logs", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = FitxerContingut.class) })
    public Response getFitxerByNom( @PathParam("nomFitxer") String nomFitxer,@Context SecurityContext securityContext)
    throws NotFoundException {
        return service.getFitxerByNom(nomFitxer,securityContext);
    }
    @GET
    @Path("/{nomFitxer}/linies/{nLinies}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Obtenir les darreres línies d'un fitxer de log", notes = "Retorna les darreres linies del fitxer de log indicat per nom. Concretament es retorna el número de línies indicat al paràmetre nLinies.", response = String.class, responseContainer = "List", tags={ "COMANDA → APP / Logs", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = String.class, responseContainer = "List") })
    public Response llegitUltimesLinies( @PathParam("nomFitxer") String nomFitxer, @PathParam("nLinies") Long nLinies,@Context SecurityContext securityContext)
    throws NotFoundException {
        return service.llegitUltimesLinies(nomFitxer,nLinies,securityContext);
    }
    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Obtenir el llistat de fitxers de log disponibles", notes = "Retorna una llista amb tots els fitxers que es troben dins la carpeta de logs del servidor de l'aplicació", response = FitxerInfo.class, responseContainer = "List", tags={ "COMANDA → APP / Logs", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = FitxerInfo.class, responseContainer = "List") })
    public Response llistarFitxers(@Context SecurityContext securityContext)
    throws NotFoundException {
        return service.llistarFitxers(securityContext);
    }
}
