package es.caib.comanda.api.v1.salut;

import es.caib.comanda.model.v1.salut.*;
import es.caib.comanda.api.v1.salut.ComandaAppSalutApiService;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.model.v1.salut.SalutInfo;

import java.util.Map;
import java.util.List;
import es.caib.comanda.api.v1.salut.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;
import javax.inject.Inject;

import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/v1/salut")


@io.swagger.annotations.Api(description = "the ComandaAppSalut API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaResteasyServerCodegen", comments = "Generator version: 7.17.0")
public class ComandaAppSalutApi  {

    @Inject ComandaAppSalutApiService service;

    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Obtenir informació de l'estat de salut de l'aplicació", notes = "Retorna l'estat de salut funcional i integracions, amb metadades de versió.", response = SalutInfo.class, tags={ "COMANDA → APP / Salut", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = SalutInfo.class) })
    public Response salut(@Context SecurityContext securityContext)
    throws NotFoundException {
        return service.salut(securityContext);
    }
    @GET
    @Path("/info")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Obtenir informació de l'aplicació", notes = "Retorna dades bàsiques de l'aplicació (codi, nom, versió, data de build, etc.) i contextos exposats.", response = AppInfo.class, tags={ "COMANDA → APP / Salut", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = AppInfo.class) })
    public Response salutInfo(@Context SecurityContext securityContext)
    throws NotFoundException {
        return service.salutInfo(securityContext);
    }
}
