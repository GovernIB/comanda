package es.caib.comanda.api.v1.salut;

import es.caib.comanda.api.v1.salut.*;
import es.caib.comanda.model.v1.salut.*;


import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.model.v1.salut.SalutInfo;

import java.util.List;
import es.caib.comanda.api.v1.salut.NotFoundException;

import java.io.InputStream;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaResteasyServerCodegen", comments = "Generator version: 7.17.0")
public interface ComandaAppSalutApiService {
      Response salut(SecurityContext securityContext)
      throws NotFoundException;
      Response salutInfo(SecurityContext securityContext)
      throws NotFoundException;
}
