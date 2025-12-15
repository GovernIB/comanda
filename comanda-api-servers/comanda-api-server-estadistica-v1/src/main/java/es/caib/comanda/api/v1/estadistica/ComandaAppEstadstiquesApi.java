package es.caib.comanda.api.v1.estadistica;

import es.caib.comanda.model.v1.estadistica.*;
import es.caib.comanda.api.v1.estadistica.ComandaAppEstadstiquesApiService;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import es.caib.comanda.model.v1.estadistica.EstadistiquesInfo;
import es.caib.comanda.model.v1.estadistica.RegistresEstadistics;

import java.util.Map;
import java.util.List;
import es.caib.comanda.api.v1.estadistica.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;
import javax.inject.Inject;

import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/v1/estadistiques")


@io.swagger.annotations.Api(description = "the ComandaAppEstadstiques API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaResteasyServerCodegen", comments = "Generator version: 7.17.0")
public class ComandaAppEstadstiquesApi  {

    @Inject ComandaAppEstadstiquesApiService service;

    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Obtenir darreres estadístiques diàries disponibles", notes = "Retorna registres d'estadístiques més recents disponibles (estadístiques d'ahir).", response = RegistresEstadistics.class, tags={ "COMANDA → APP / Estadístiques", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = RegistresEstadistics.class) })
    public Response estadistiques(@Context SecurityContext securityContext)
    throws NotFoundException {
        return service.estadistiques(securityContext);
    }
    @GET
    @Path("/info")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Obtenir informació de 'estructura de les estadístiques", notes = "Retorna el codi de l'app i el catàleg de dimensions i indicadors disponibles.", response = EstadistiquesInfo.class, tags={ "COMANDA → APP / Estadístiques", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = EstadistiquesInfo.class) })
    public Response estadistiquesInfo(@Context SecurityContext securityContext)
    throws NotFoundException {
        return service.estadistiquesInfo(securityContext);
    }
    @GET
    @Path("/of/{data}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Obtenir estadístiques d'una data concreta", notes = "Retorna les estadístiques corresponents a la data indicada amb format dd-MM-yyyy.", response = RegistresEstadistics.class, tags={ "COMANDA → APP / Estadístiques", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = RegistresEstadistics.class) })
    public Response estadistiquesPerData( @PathParam("data") String data,@Context SecurityContext securityContext)
    throws NotFoundException {
        return service.estadistiquesPerData(data,securityContext);
    }
    @GET
    @Path("/from/{dataInici}/to/{dataFi}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Obtenir les estadístiques d'un interval donat", notes = "Retorna llista d'estadístiques de tots els dies entre la dataInici i la dataFi (en format dd-MM-yyyy), ambdues incloses. La resposta contindrà un objecte de tipus RegistresEstadistics per a cada dia inclòs en l'intèrval.", response = RegistresEstadistics.class, responseContainer = "List", tags={ "COMANDA → APP / Estadístiques", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = RegistresEstadistics.class, responseContainer = "List") })
    public Response estadistiquesPerRang( @PathParam("dataInici") String dataInici, @PathParam("dataFi") String dataFi,@Context SecurityContext securityContext)
    throws NotFoundException {
        return service.estadistiquesPerRang(dataInici,dataFi,securityContext);
    }
}
