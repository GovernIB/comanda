package es.caib.comanda.api.monitoring;

import es.caib.comanda.service.monitoring.ApiException;
import es.caib.comanda.service.monitoring.ApiClient;
import es.caib.comanda.service.monitoring.Configuration;
import es.caib.comanda.service.monitoring.Pair;

import javax.ws.rs.core.GenericType;

import java.io.File;
import es.caib.comanda.model.monitoring.FitxerContingut;
import es.caib.comanda.model.monitoring.FitxerInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.17.0")
public class ComandaAppLogsApi {
  private ApiClient apiClient;

  public ComandaAppLogsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ComandaAppLogsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Descarregar fitxer de log complet
   * Descarrega el fitxer de log complet que es troba dins la carpeta de logs del servidor, i que té el nom indicat
   * @param nomFitxer Nom del firxer (required)
   * @return a {@code java.io.File}
   * @throws ApiException if fails to make API call
   */
  public java.io.File descarregarFitxerDirecte(@javax.annotation.Nonnull String nomFitxer) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nomFitxer' is set
    if (nomFitxer == null) {
      throw new ApiException(400, "Missing the required parameter 'nomFitxer' when calling descarregarFitxerDirecte");
    }
    
    // create path and map variables
    String localVarPath = "/logs/v1/{nomFitxer}/directe".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "nomFitxer" + "\\}", apiClient.escapeString(nomFitxer.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, String> localVarCookieParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    
    final String[] localVarAccepts = {
      "application/octet-stream"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<java.io.File> localVarReturnType = new GenericType<java.io.File>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Obtenir contingut complet d&#39;un fitxer de log
   * Retorna el contingut i detalls del fitxer de log que es troba dins la carpeta de logs del servidor, i que té el nom indicat
   * @param nomFitxer Nom del firxer (required)
   * @return a {@code FitxerContingut}
   * @throws ApiException if fails to make API call
   */
  public FitxerContingut getFitxerByNom(@javax.annotation.Nonnull String nomFitxer) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nomFitxer' is set
    if (nomFitxer == null) {
      throw new ApiException(400, "Missing the required parameter 'nomFitxer' when calling getFitxerByNom");
    }
    
    // create path and map variables
    String localVarPath = "/logs/v1/{nomFitxer}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "nomFitxer" + "\\}", apiClient.escapeString(nomFitxer.toString()));

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

    GenericType<FitxerContingut> localVarReturnType = new GenericType<FitxerContingut>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Obtenir les darreres línies d&#39;un fitxer de log
   * Retorna les darreres linies del fitxer de log indicat per nom. Concretament es retorna el número de línies indicat al paràmetre nLinies.
   * @param nomFitxer Nom del firxer (required)
   * @param nLinies Número de línies a recuperar del firxer (required)
   * @return a {@code List<String>}
   * @throws ApiException if fails to make API call
   */
  public List<String> llegitUltimesLinies(@javax.annotation.Nonnull String nomFitxer, @javax.annotation.Nonnull Long nLinies) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'nomFitxer' is set
    if (nomFitxer == null) {
      throw new ApiException(400, "Missing the required parameter 'nomFitxer' when calling llegitUltimesLinies");
    }
    
    // verify the required parameter 'nLinies' is set
    if (nLinies == null) {
      throw new ApiException(400, "Missing the required parameter 'nLinies' when calling llegitUltimesLinies");
    }
    
    // create path and map variables
    String localVarPath = "/logs/v1/{nomFitxer}/linies/{nLinies}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "nomFitxer" + "\\}", apiClient.escapeString(nomFitxer.toString()))
      .replaceAll("\\{" + "nLinies" + "\\}", apiClient.escapeString(nLinies.toString()));

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

    GenericType<List<String>> localVarReturnType = new GenericType<List<String>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Obtenir el llistat de fitxers de log disponibles
   * Retorna una llista amb tots els fitxers que es troben dins la carpeta de logs del servidor de l&#39;aplicació
   * @return a {@code List<FitxerInfo>}
   * @throws ApiException if fails to make API call
   */
  public List<FitxerInfo> llistarFitxers() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/logs/v1".replaceAll("\\{format\\}","json");

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

    GenericType<List<FitxerInfo>> localVarReturnType = new GenericType<List<FitxerInfo>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
