package es.caib.comanda.api.v1.management;

import es.caib.comanda.service.v1.management.ApiException;
import es.caib.comanda.service.v1.management.ApiClient;
import es.caib.comanda.service.v1.management.Configuration;
import es.caib.comanda.service.v1.management.Pair;

import javax.ws.rs.core.GenericType;

import es.caib.comanda.model.v1.management.Tasca;
import es.caib.comanda.model.v1.management.TascaPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.17.0")
public class AppComandaTasquesApi {
  private ApiClient apiClient;

  public AppComandaTasquesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AppComandaTasquesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Consulta d&#39;una tasca
   * Obté les dades d&#39;una tasca identificada pel seu identificador, codi d&#39;aplicació i codi d&#39;entorn.
   * @param identificador Identificador de la tasca (required)
   * @param appCodi Codi de l&#39;aplicació (required)
   * @param entornCodi Codi de l&#39;entorn (required)
   * @return a {@code Tasca}
   * @throws ApiException if fails to make API call
   */
  public Tasca consultarTasca(@javax.annotation.Nonnull String identificador, @javax.annotation.Nonnull String appCodi, @javax.annotation.Nonnull String entornCodi) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'identificador' is set
    if (identificador == null) {
      throw new ApiException(400, "Missing the required parameter 'identificador' when calling consultarTasca");
    }
    
    // verify the required parameter 'appCodi' is set
    if (appCodi == null) {
      throw new ApiException(400, "Missing the required parameter 'appCodi' when calling consultarTasca");
    }
    
    // verify the required parameter 'entornCodi' is set
    if (entornCodi == null) {
      throw new ApiException(400, "Missing the required parameter 'entornCodi' when calling consultarTasca");
    }
    
    // create path and map variables
    String localVarPath = "/tasques/v1/{identificador}".replaceAll("\\{format\\}","json")
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

    GenericType<Tasca> localVarReturnType = new GenericType<Tasca>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Creació de múltiples tasques
   * Afegeix múltiples missatges d&#39;alta de tasques a una cua de events per a que es creïn aquestes de forma asíncrona a Comanda.
   * @param tasca  (optional)
   * @return a {@code String}
   * @throws ApiException if fails to make API call
   */
  public String crearMultiplesTasques(@javax.annotation.Nullable List<Tasca> tasca) throws ApiException {
    Object localVarPostBody = tasca;
    
    // create path and map variables
    String localVarPath = "/tasques/v1/multiple".replaceAll("\\{format\\}","json");

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
   * Creació s&#39;una tasca
   * Afegeix un missatge d&#39;alta de tasca a una cua de events per a que es crei aquesta de forma asíncrona a Comanda.
   * @param tasca  (optional)
   * @return a {@code String}
   * @throws ApiException if fails to make API call
   */
  public String crearTasca(@javax.annotation.Nullable Tasca tasca) throws ApiException {
    Object localVarPostBody = tasca;
    
    // create path and map variables
    String localVarPath = "/tasques/v1".replaceAll("\\{format\\}","json");

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
   * Modificació de múltiples tasques
   * Es comprova si les tasques existeixen, i en cas afirmatiu, s&#39;afegeixen múltiples missatges de modificació de tasques a una cua de events per a que es modifiquin aquestes de forma asíncrona a Comanda. Les tasques no existents s&#39;ignoren.
   * @param tasca  (optional)
   * @return a {@code String}
   * @throws ApiException if fails to make API call
   */
  public String modificarMultiplesTasques(@javax.annotation.Nullable List<Tasca> tasca) throws ApiException {
    Object localVarPostBody = tasca;
    
    // create path and map variables
    String localVarPath = "/tasques/v1/multiple".replaceAll("\\{format\\}","json");

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
   * Modificació una tasca
   * Es comprova si la tasca existeix, i en cas afirmatiu, s&#39;afegeix un missatge de modificació de tasca a una cua de events per a que es modifiqui aquesta de forma asíncrona a Comanda.
   * @param identificador Identificador de la tasca (required)
   * @param tasca  (optional)
   * @return a {@code String}
   * @throws ApiException if fails to make API call
   */
  public String modificarTasca(@javax.annotation.Nonnull String identificador, @javax.annotation.Nullable Tasca tasca) throws ApiException {
    Object localVarPostBody = tasca;
    
    // verify the required parameter 'identificador' is set
    if (identificador == null) {
      throw new ApiException(400, "Missing the required parameter 'identificador' when calling modificarTasca");
    }
    
    // create path and map variables
    String localVarPath = "/tasques/v1/{identificador}".replaceAll("\\{format\\}","json")
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
   * Consulta de tasques
   * Obté un llistat paginat de tasques amb possibilitat d&#39;aplicar filtres ràpids, filtres avançats, consultes predefinides i perspectives.
   * @param quickFilter Filtre ràpid (optional)
   * @param filter Filtre avançat en format JSON o expressió del MS (optional)
   * @param namedQueries Consultes predefinides (optional)
   * @param perspectives Perspectives de camp (optional)
   * @param page Número de pàgina (optional)
   * @param size Mida de pàgina (optional)
   * @return a {@code TascaPage}
   * @throws ApiException if fails to make API call
   */
  public TascaPage obtenirLlistatTasques(@javax.annotation.Nullable String quickFilter, @javax.annotation.Nullable String filter, @javax.annotation.Nullable List<String> namedQueries, @javax.annotation.Nullable List<String> perspectives, @javax.annotation.Nullable String page, @javax.annotation.Nullable Integer size) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/tasques/v1".replaceAll("\\{format\\}","json");

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

    GenericType<TascaPage> localVarReturnType = new GenericType<TascaPage>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
