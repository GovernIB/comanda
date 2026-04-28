package es.caib.comanda.api.management;

import es.caib.comanda.service.management.ApiException;
import es.caib.comanda.service.management.ApiClient;
import es.caib.comanda.service.management.Configuration;
import es.caib.comanda.service.management.Pair;

import javax.ws.rs.core.GenericType;

import es.caib.comanda.model.management.Permis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.17.0")
public class AppComandaPermisosApi {
  private ApiClient apiClient;

  public AppComandaPermisosApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AppComandaPermisosApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Consulta d&#39;un permís
   * Obté les dades d&#39;un permís identificat pel seu identificador, codi d&#39;aplicació i codi d&#39;entorn.
   * @param identificador Identificador del permís (required)
   * @param appCodi Codi de l&#39;aplicació (required)
   * @param entornCodi Codi de l&#39;entorn (required)
   * @return a {@code Permis}
   * @throws ApiException if fails to make API call
   */
  public Permis consultarPermis(@javax.annotation.Nonnull String identificador, @javax.annotation.Nonnull String appCodi, @javax.annotation.Nonnull String entornCodi) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'identificador' is set
    if (identificador == null) {
      throw new ApiException(400, "Missing the required parameter 'identificador' when calling consultarPermis");
    }
    
    // verify the required parameter 'appCodi' is set
    if (appCodi == null) {
      throw new ApiException(400, "Missing the required parameter 'appCodi' when calling consultarPermis");
    }
    
    // verify the required parameter 'entornCodi' is set
    if (entornCodi == null) {
      throw new ApiException(400, "Missing the required parameter 'entornCodi' when calling consultarPermis");
    }
    
    // create path and map variables
    String localVarPath = "/permisos/v1/{identificador}".replaceAll("\\{format\\}","json")
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

    GenericType<Permis> localVarReturnType = new GenericType<Permis>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Creació de múltiples permisos
   * Afegeix múltiples missatges d&#39;alta de permisos a una cua de events per a que es creïn aquests de forma asíncrona a Comanda.
   * @param permis  (optional)
   * @return a {@code String}
   * @throws ApiException if fails to make API call
   */
  public String crearMultiplesPermisos(@javax.annotation.Nullable List<Permis> permis) throws ApiException {
    Object localVarPostBody = permis;
    
    // create path and map variables
    String localVarPath = "/permisos/v1/multiple".replaceAll("\\{format\\}","json");

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
   * Creació d&#39;un permís
   * Afegeix un missatge d&#39;alta de permís a una cua de events per a que es crei aquest de forma asíncrona a Comanda.
   * @param permis  (optional)
   * @return a {@code String}
   * @throws ApiException if fails to make API call
   */
  public String crearPermis(@javax.annotation.Nullable Permis permis) throws ApiException {
    Object localVarPostBody = permis;
    
    // create path and map variables
    String localVarPath = "/permisos/v1".replaceAll("\\{format\\}","json");

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
   * Eliminació de permisos
   * Afegeix múltiples missatges d&#39;eliminació de permisos a una cua de events per a que s&#39;eliminin aquests de forma asíncrona a Comanda.
   * @param permis  (optional)
   * @return a {@code String}
   * @throws ApiException if fails to make API call
   */
  public String eliminarPermisos(@javax.annotation.Nullable List<Permis> permis) throws ApiException {
    Object localVarPostBody = permis;
    
    // create path and map variables
    String localVarPath = "/permisos/v1".replaceAll("\\{format\\}","json");

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
    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Modificació de múltiples permisos
   * Es comprova si els permisos existeixen, i en cas afirmatiu, s&#39;afegeixen múltiples missatges de modificació de permisos a una cua de events per a que es modifiquin aquests de forma asíncrona a Comanda. Els permisos no existents s&#39;ignoren.
   * @param permis  (optional)
   * @return a {@code String}
   * @throws ApiException if fails to make API call
   */
  public String modificarMultiplesPermisos(@javax.annotation.Nullable List<Permis> permis) throws ApiException {
    Object localVarPostBody = permis;
    
    // create path and map variables
    String localVarPath = "/permisos/v1/multiple".replaceAll("\\{format\\}","json");

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
   * Modificació d&#39;un permís existent
   * Es comprova si el permís existeix, i en cas afirmatiu, s&#39;afegeix un missatge de modificació de permís a una cua de events per a que es modifiqui aquest de forma asíncrona a Comanda.
   * @param identificador Identificador funcional (objecte.identificador) (required)
   * @param permis  (optional)
   * @return a {@code String}
   * @throws ApiException if fails to make API call
   */
  public String modificarPermis(@javax.annotation.Nonnull String identificador, @javax.annotation.Nullable Permis permis) throws ApiException {
    Object localVarPostBody = permis;
    
    // verify the required parameter 'identificador' is set
    if (identificador == null) {
      throw new ApiException(400, "Missing the required parameter 'identificador' when calling modificarPermis");
    }
    
    // create path and map variables
    String localVarPath = "/permisos/v1/{identificador}".replaceAll("\\{format\\}","json")
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
}
