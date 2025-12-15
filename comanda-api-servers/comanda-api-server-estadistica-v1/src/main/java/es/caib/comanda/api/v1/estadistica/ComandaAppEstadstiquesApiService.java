package es.caib.comanda.api.v1.estadistica;

import es.caib.comanda.api.v1.estadistica.*;
import es.caib.comanda.model.v1.estadistica.*;


import es.caib.comanda.model.v1.estadistica.EstadistiquesInfo;
import es.caib.comanda.model.v1.estadistica.RegistresEstadistics;

import java.util.List;
import es.caib.comanda.api.v1.estadistica.NotFoundException;

import java.io.InputStream;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaResteasyServerCodegen", comments = "Generator version: 7.17.0")
public interface ComandaAppEstadstiquesApiService {
      Response estadistiques(SecurityContext securityContext)
      throws NotFoundException;
      Response estadistiquesInfo(SecurityContext securityContext)
      throws NotFoundException;
      Response estadistiquesPerData(String data,SecurityContext securityContext)
      throws NotFoundException;
      Response estadistiquesPerRang(String dataInici,String dataFi,SecurityContext securityContext)
      throws NotFoundException;
}
