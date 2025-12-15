package es.caib.comanda.api.v1.estadistica.impl;

import es.caib.comanda.api.v1.estadistica.*;
import es.caib.comanda.model.v1.estadistica.*;


import es.caib.comanda.model.v1.estadistica.EstadistiquesInfo;
import es.caib.comanda.model.v1.estadistica.RegistresEstadistics;

import java.util.List;
import es.caib.comanda.api.v1.estadistica.NotFoundException;

import java.io.InputStream;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@RequestScoped
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaResteasyServerCodegen", comments = "Generator version: 7.17.0")
public class ComandaAppEstadstiquesApiServiceImpl implements ComandaAppEstadstiquesApiService {
      public Response estadistiques(SecurityContext securityContext)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
      public Response estadistiquesInfo(SecurityContext securityContext)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
      public Response estadistiquesPerData(String data,SecurityContext securityContext)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
      public Response estadistiquesPerRang(String dataInici,String dataFi,SecurityContext securityContext)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
}
