package es.caib.comanda.api.monitoring;

import es.caib.comanda.service.monitoring.ApiException;
import es.caib.comanda.service.monitoring.ApiClient;
import es.caib.comanda.service.monitoring.Configuration;
import es.caib.comanda.service.monitoring.Pair;

import javax.ws.rs.core.GenericType;

import es.caib.comanda.model.monitoring.AppInfo;
import es.caib.comanda.model.monitoring.SalutInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.17.0")
public class ComandaAppSalutApi {
  private ApiClient apiClient;

  public ComandaAppSalutApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ComandaAppSalutApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Obtenir informació de l&#39;estat de salut de l&#39;aplicació
   * Retorna l&#39;estat de salut funcional i integracions, amb metadades de versió.
   * @return a {@code SalutInfo}
   * @throws ApiException if fails to make API call
   */
  public SalutInfo salut() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/salut/v1".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, String> localVarCookieParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<SalutInfo> localVarReturnType = new GenericType<SalutInfo>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Obtenir informació de l&#39;aplicació
   * Retorna dades bàsiques de l&#39;aplicació (codi, nom, versió, data de build, etc.) i contextos exposats.
   * @return a {@code AppInfo}
   * @throws ApiException if fails to make API call
   */
  public AppInfo salutInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/salut/v1/info".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, String> localVarCookieParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<AppInfo> localVarReturnType = new GenericType<AppInfo>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
