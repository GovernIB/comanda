package es.caib.comanda.api.v1.monitoring;

import es.caib.comanda.service.v1.monitoring.ApiException;
import es.caib.comanda.service.v1.monitoring.ApiClient;
import es.caib.comanda.service.v1.monitoring.Configuration;
import es.caib.comanda.service.v1.monitoring.Pair;

import javax.ws.rs.core.GenericType;

import es.caib.comanda.model.v1.monitoring.EstadistiquesInfo;
import es.caib.comanda.model.v1.monitoring.RegistresEstadistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.17.0")
public class ComandaAppEstadistiquesApi {
  private ApiClient apiClient;

  public ComandaAppEstadistiquesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ComandaAppEstadistiquesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Obtenir darreres estadístiques diàries disponibles
   * Retorna registres d&#39;estadístiques més recents disponibles (estadístiques d&#39;ahir).
   * @return a {@code RegistresEstadistics}
   * @throws ApiException if fails to make API call
   */
  public RegistresEstadistics estadistiques() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/estadistiques/v1".replaceAll("\\{format\\}","json");

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

    GenericType<RegistresEstadistics> localVarReturnType = new GenericType<RegistresEstadistics>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Obtenir informació de &#39;estructura de les estadístiques
   * Retorna el codi de l&#39;app i el catàleg de dimensions i indicadors disponibles.
   * @return a {@code EstadistiquesInfo}
   * @throws ApiException if fails to make API call
   */
  public EstadistiquesInfo estadistiquesInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/estadistiques/v1/info".replaceAll("\\{format\\}","json");

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

    GenericType<EstadistiquesInfo> localVarReturnType = new GenericType<EstadistiquesInfo>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Obtenir estadístiques d&#39;una data concreta
   * Retorna les estadístiques corresponents a la data indicada amb format dd-MM-yyyy.
   * @param data Data en format dd-MM-yyyy (required)
   * @return a {@code RegistresEstadistics}
   * @throws ApiException if fails to make API call
   */
  public RegistresEstadistics estadistiquesPerData(@javax.annotation.Nonnull String data) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'data' is set
    if (data == null) {
      throw new ApiException(400, "Missing the required parameter 'data' when calling estadistiquesPerData");
    }
    
    // create path and map variables
    String localVarPath = "/estadistiques/v1/of/{data}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "data" + "\\}", apiClient.escapeString(data.toString()));

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

    GenericType<RegistresEstadistics> localVarReturnType = new GenericType<RegistresEstadistics>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Obtenir les estadístiques d&#39;un interval donat
   * Retorna llista d&#39;estadístiques de tots els dies entre la dataInici i la dataFi (en format dd-MM-yyyy), ambdues incloses. La resposta contindrà un objecte de tipus RegistresEstadistics per a cada dia inclòs en l&#39;intèrval.
   * @param dataInici Data d&#39;inici en format dd-MM-yyyy (required)
   * @param dataFi Data de fi en format dd-MM-yyyy (required)
   * @return a {@code List<RegistresEstadistics>}
   * @throws ApiException if fails to make API call
   */
  public List<RegistresEstadistics> estadistiquesPerRang(@javax.annotation.Nonnull String dataInici, @javax.annotation.Nonnull String dataFi) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'dataInici' is set
    if (dataInici == null) {
      throw new ApiException(400, "Missing the required parameter 'dataInici' when calling estadistiquesPerRang");
    }
    
    // verify the required parameter 'dataFi' is set
    if (dataFi == null) {
      throw new ApiException(400, "Missing the required parameter 'dataFi' when calling estadistiquesPerRang");
    }
    
    // create path and map variables
    String localVarPath = "/estadistiques/v1/from/{dataInici}/to/{dataFi}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "dataInici" + "\\}", apiClient.escapeString(dataInici.toString()))
      .replaceAll("\\{" + "dataFi" + "\\}", apiClient.escapeString(dataFi.toString()));

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

    GenericType<List<RegistresEstadistics>> localVarReturnType = new GenericType<List<RegistresEstadistics>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
