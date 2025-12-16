package es.caib.comanda.api.v1.log.impl;

import es.caib.comanda.api.v1.log.*;
import es.caib.comanda.model.v1.log.*;


import es.caib.comanda.model.v1.log.FitxerContingut;
import es.caib.comanda.model.v1.log.FitxerInfo;

import java.util.List;
import es.caib.comanda.api.v1.log.NotFoundException;

import java.io.InputStream;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@RequestScoped
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaResteasyServerCodegen", comments = "Generator version: 7.17.0")
public class ComandaAppLogsApiServiceImpl implements ComandaAppLogsApiService {
      public Response getFitxerByNom(String nomFitxer,SecurityContext securityContext)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
      public Response llegitUltimesLinies(String nomFitxer,Long nLinies,SecurityContext securityContext)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
      public Response llistarFitxers(SecurityContext securityContext)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
}
