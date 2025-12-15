package es.caib.comanda.api.v1.avis;

import es.caib.comanda.service.v1.avis.ApiException;
import es.caib.comanda.service.v1.avis.ApiClient;
import es.caib.comanda.service.v1.avis.Configuration;
import es.caib.comanda.service.v1.avis.Pair;

import javax.ws.rs.core.GenericType;

import es.caib.comanda.model.v1.avis.Avis;
import es.caib.comanda.model.v1.avis.AvisPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.17.0")
public class AppComandaAvisosApi {
  private ApiClient apiClient;

  public AppComandaAvisosApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AppComandaAvisosApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Consulta d&#39;un avís
   * Obté les dades d&#39;un avís a partir del seu identificador, codi d&#39;aplicació i codi d&#39;entorn.
   * @param identificador Identificador de l&#39;avís (required)
   * @param appCodi Codi de l&#39;aplicació (required)
   * @param entornCodi Codi de l&#39;entorn (required)
   * @return a {@code Avis}
   * @throws ApiException if fails to make API call
   */
  public Avis consultarAvis(@javax.annotation.Nonnull String identificador, @javax.annotation.Nonnull String appCodi, @javax.annotation.Nonnull String entornCodi) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'identificador' is set
    if (identificador == null) {
      throw new ApiException(400, "Missing the required parameter 'identificador' when calling consultarAvis");
    }
    
    // verify the required parameter 'appCodi' is set
    if (appCodi == null) {
      throw new ApiException(400, "Missing the required parameter 'appCodi' when calling consultarAvis");
    }
    
    // verify the required parameter 'entornCodi' is set
    if (entornCodi == null) {
      throw new ApiException(400, "Missing the required parameter 'entornCodi' when calling consultarAvis");
    }
    
    // create path and map variables
    String localVarPath = "/v1/avisos/{identificador}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "identificador" + "\\}", apiClient.escapeString(identificador.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, String> localVarCookieParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "appCodi", appCodi));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "entornCodi", entornCodi));

    
    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Avis> localVarReturnType = new GenericType<Avis>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Creació d&#39;un avís
   * Afegeix un missatge d&#39;alta d&#39;avís a una cua de events per a que es crei aquest de forma asíncrona a Comanda.
   * @param avis  (optional)
   * @return a {@code String}
   * @throws ApiException if fails to make API call
   */
  public String crearAvis(@javax.annotation.Nullable Avis avis) throws ApiException {
    Object localVarPostBody = avis;
    
    // create path and map variables
    String localVarPath = "/v1/avisos".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, String> localVarCookieParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    
    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Creació de múltiples avisos
   * Afegeix múltiples missatges d&#39;alta d&#39;avisos a una cua de events per a que es creïn aquests de forma asíncrona a Comanda.
   * @param avis  (optional)
   * @return a {@code String}
   * @throws ApiException if fails to make API call
   */
  public String crearMultiplesAvisos(@javax.annotation.Nullable List<Avis> avis) throws ApiException {
    Object localVarPostBody = avis;
    
    // create path and map variables
    String localVarPath = "/v1/avisos/multiple".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, String> localVarCookieParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    
    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Modificació d&#39;un avís existent
   * Es comprova si l&#39;avís existeix, i en cas afirmatiu, s&#39;afegeix un missatge de modificació d&#39;avís a una cua de events per a que es modifiqui aquest de forma asíncrona a Comanda.
   * @param identificador Identificador de l&#39;avís (required)
   * @param avis  (optional)
   * @return a {@code String}
   * @throws ApiException if fails to make API call
   */
  public String modificarAvis(@javax.annotation.Nonnull String identificador, @javax.annotation.Nullable Avis avis) throws ApiException {
    Object localVarPostBody = avis;
    
    // verify the required parameter 'identificador' is set
    if (identificador == null) {
      throw new ApiException(400, "Missing the required parameter 'identificador' when calling modificarAvis");
    }
    
    // create path and map variables
    String localVarPath = "/v1/avisos/{identificador}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "identificador" + "\\}", apiClient.escapeString(identificador.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, String> localVarCookieParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    
    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Modificació de múltiples avisos
   * Es comprova si els avisos existeixen, i en cas afirmatiu, s&#39;afegeixen missatges de modificació d&#39;avisos a una cua de events per a que es modifiquin aquests de forma asíncrona a Comanda.
   * @param avis  (optional)
   * @return a {@code String}
   * @throws ApiException if fails to make API call
   */
  public String modificarMultiplesAvisos(@javax.annotation.Nullable List<Avis> avis) throws ApiException {
    Object localVarPostBody = avis;
    
    // create path and map variables
    String localVarPath = "/v1/avisos/multiple".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, String> localVarCookieParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    
    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Llistat d&#39;avisos
   * Obté un llistat paginat d&#39;avisos amb la possibilitat d&#39;aplicar filtres de cerca.
   * @param quickFilter Filtre ràpid (optional)
   * @param filter Filtre avançat en format JSON o expressió del MS (optional)
   * @param namedQueries Consultes predefinides (optional)
   * @param perspectives Perspectives de camp (optional)
   * @param page Número de pàgina (optional)
   * @param size Mida de pàgina (optional)
   * @return a {@code AvisPage}
   * @throws ApiException if fails to make API call
   */
  public AvisPage obtenirLlistatAvisos(@javax.annotation.Nullable String quickFilter, @javax.annotation.Nullable String filter, @javax.annotation.Nullable List<String> namedQueries, @javax.annotation.Nullable List<String> perspectives, @javax.annotation.Nullable String page, @javax.annotation.Nullable Integer size) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v1/avisos".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, String> localVarCookieParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "quickFilter", quickFilter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "namedQueries", namedQueries));
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "perspectives", perspectives));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "page", page));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "size", size));

    
    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<AvisPage> localVarReturnType = new GenericType<AvisPage>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
