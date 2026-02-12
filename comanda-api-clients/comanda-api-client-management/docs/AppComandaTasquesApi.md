# AppComandaTasquesApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**consultarTasca**](AppComandaTasquesApi.md#consultarTasca) | **GET** /tasques/v1/{identificador} | Consulta d&#39;una tasca |
| [**crearMultiplesTasques**](AppComandaTasquesApi.md#crearMultiplesTasques) | **POST** /tasques/v1/multiple | Creació de múltiples tasques |
| [**crearTasca**](AppComandaTasquesApi.md#crearTasca) | **POST** /tasques/v1 | Creació s&#39;una tasca |
| [**eliminarTasca**](AppComandaTasquesApi.md#eliminarTasca) | **DELETE** /tasques/v1/{identificador} | Eliminació una tasca existent |
| [**modificarMultiplesTasques**](AppComandaTasquesApi.md#modificarMultiplesTasques) | **PUT** /tasques/v1/multiple | Modificació de múltiples tasques |
| [**modificarTasca**](AppComandaTasquesApi.md#modificarTasca) | **PUT** /tasques/v1/{identificador} | Modificació una tasca |
| [**obtenirLlistatTasques**](AppComandaTasquesApi.md#obtenirLlistatTasques) | **GET** /tasques/v1 | Consulta de tasques |



## consultarTasca

> Tasca consultarTasca(identificador, appCodi, entornCodi)

Consulta d&#39;una tasca

Obté les dades d&#39;una tasca identificada pel seu identificador, codi d&#39;aplicació i codi d&#39;entorn.

### Example

```java
// Import classes:
import es.caib.comanda.service.management.ApiClient;
import es.caib.comanda.service.management.ApiException;
import es.caib.comanda.service.management.Configuration;
import es.caib.comanda.service.management.models.*;
import es.caib.comanda.api.management.AppComandaTasquesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaTasquesApi apiInstance = new AppComandaTasquesApi(defaultClient);
        String identificador = "identificador_example"; // String | Identificador de la tasca
        String appCodi = "appCodi_example"; // String | Codi de l'aplicació
        String entornCodi = "entornCodi_example"; // String | Codi de l'entorn
        try {
            Tasca result = apiInstance.consultarTasca(identificador, appCodi, entornCodi);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaTasquesApi#consultarTasca");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **identificador** | **String**| Identificador de la tasca | |
| **appCodi** | **String**| Codi de l&#39;aplicació | |
| **entornCodi** | **String**| Codi de l&#39;entorn | |

### Return type

[**Tasca**](Tasca.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## crearMultiplesTasques

> String crearMultiplesTasques(tasca)

Creació de múltiples tasques

Afegeix múltiples missatges d&#39;alta de tasques a una cua de events per a que es creïn aquestes de forma asíncrona a Comanda.

### Example

```java
// Import classes:
import es.caib.comanda.service.management.ApiClient;
import es.caib.comanda.service.management.ApiException;
import es.caib.comanda.service.management.Configuration;
import es.caib.comanda.service.management.models.*;
import es.caib.comanda.api.management.AppComandaTasquesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaTasquesApi apiInstance = new AppComandaTasquesApi(defaultClient);
        List<Tasca> tasca = Arrays.asList(); // List<Tasca> | 
        try {
            String result = apiInstance.crearMultiplesTasques(tasca);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaTasquesApi#crearMultiplesTasques");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **tasca** | [**List&lt;Tasca&gt;**](Tasca.md)|  | [optional] |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: text/plain


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## crearTasca

> String crearTasca(tasca)

Creació s&#39;una tasca

Afegeix un missatge d&#39;alta de tasca a una cua de events per a que es crei aquesta de forma asíncrona a Comanda.

### Example

```java
// Import classes:
import es.caib.comanda.service.management.ApiClient;
import es.caib.comanda.service.management.ApiException;
import es.caib.comanda.service.management.Configuration;
import es.caib.comanda.service.management.models.*;
import es.caib.comanda.api.management.AppComandaTasquesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaTasquesApi apiInstance = new AppComandaTasquesApi(defaultClient);
        Tasca tasca = new Tasca(); // Tasca | 
        try {
            String result = apiInstance.crearTasca(tasca);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaTasquesApi#crearTasca");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **tasca** | [**Tasca**](Tasca.md)|  | [optional] |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: text/plain


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## eliminarTasca

> String eliminarTasca(identificador, appCodi, entornCodi)

Eliminació una tasca existent

Es comprova si la tasca existeix, i en cas afirmatiu, s&#39;afegeix un missatge d&#39;eliminació de tasca a una cua de events per a que s&#39;elimini aquesta de forma asíncrona a Comanda.

### Example

```java
// Import classes:
import es.caib.comanda.service.management.ApiClient;
import es.caib.comanda.service.management.ApiException;
import es.caib.comanda.service.management.Configuration;
import es.caib.comanda.service.management.models.*;
import es.caib.comanda.api.management.AppComandaTasquesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaTasquesApi apiInstance = new AppComandaTasquesApi(defaultClient);
        String identificador = "identificador_example"; // String | Identificador de la tasca
        String appCodi = "appCodi_example"; // String | Codi de l'aplicació
        String entornCodi = "entornCodi_example"; // String | Codi de l'entorn
        try {
            String result = apiInstance.eliminarTasca(identificador, appCodi, entornCodi);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaTasquesApi#eliminarTasca");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **identificador** | **String**| Identificador de la tasca | |
| **appCodi** | **String**| Codi de l&#39;aplicació | |
| **entornCodi** | **String**| Codi de l&#39;entorn | |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: text/plain


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## modificarMultiplesTasques

> String modificarMultiplesTasques(tasca)

Modificació de múltiples tasques

Es comprova si les tasques existeixen, i en cas afirmatiu, s&#39;afegeixen múltiples missatges de modificació de tasques a una cua de events per a que es modifiquin aquestes de forma asíncrona a Comanda. Les tasques no existents s&#39;ignoren.

### Example

```java
// Import classes:
import es.caib.comanda.service.management.ApiClient;
import es.caib.comanda.service.management.ApiException;
import es.caib.comanda.service.management.Configuration;
import es.caib.comanda.service.management.models.*;
import es.caib.comanda.api.management.AppComandaTasquesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaTasquesApi apiInstance = new AppComandaTasquesApi(defaultClient);
        List<Tasca> tasca = Arrays.asList(); // List<Tasca> | 
        try {
            String result = apiInstance.modificarMultiplesTasques(tasca);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaTasquesApi#modificarMultiplesTasques");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **tasca** | [**List&lt;Tasca&gt;**](Tasca.md)|  | [optional] |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: text/plain


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## modificarTasca

> String modificarTasca(identificador, tasca)

Modificació una tasca

Es comprova si la tasca existeix, i en cas afirmatiu, s&#39;afegeix un missatge de modificació de tasca a una cua de events per a que es modifiqui aquesta de forma asíncrona a Comanda.

### Example

```java
// Import classes:
import es.caib.comanda.service.management.ApiClient;
import es.caib.comanda.service.management.ApiException;
import es.caib.comanda.service.management.Configuration;
import es.caib.comanda.service.management.models.*;
import es.caib.comanda.api.management.AppComandaTasquesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaTasquesApi apiInstance = new AppComandaTasquesApi(defaultClient);
        String identificador = "identificador_example"; // String | Identificador de la tasca
        Tasca tasca = new Tasca(); // Tasca | 
        try {
            String result = apiInstance.modificarTasca(identificador, tasca);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaTasquesApi#modificarTasca");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **identificador** | **String**| Identificador de la tasca | |
| **tasca** | [**Tasca**](Tasca.md)|  | [optional] |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: text/plain


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## obtenirLlistatTasques

> TascaPage obtenirLlistatTasques(quickFilter, filter, namedQueries, perspectives, page, size)

Consulta de tasques

Obté un llistat paginat de tasques amb possibilitat d&#39;aplicar filtres ràpids, filtres avançats, consultes predefinides i perspectives.

### Example

```java
// Import classes:
import es.caib.comanda.service.management.ApiClient;
import es.caib.comanda.service.management.ApiException;
import es.caib.comanda.service.management.Configuration;
import es.caib.comanda.service.management.models.*;
import es.caib.comanda.api.management.AppComandaTasquesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaTasquesApi apiInstance = new AppComandaTasquesApi(defaultClient);
        String quickFilter = "quickFilter_example"; // String | Filtre ràpid
        String filter = "filter_example"; // String | Filtre avançat en format JSON o expressió del MS
        List<String> namedQueries = Arrays.asList(); // List<String> | Consultes predefinides
        List<String> perspectives = Arrays.asList(); // List<String> | Perspectives de camp
        String page = "page_example"; // String | Número de pàgina
        Integer size = 56; // Integer | Mida de pàgina
        try {
            TascaPage result = apiInstance.obtenirLlistatTasques(quickFilter, filter, namedQueries, perspectives, page, size);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaTasquesApi#obtenirLlistatTasques");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **quickFilter** | **String**| Filtre ràpid | [optional] |
| **filter** | **String**| Filtre avançat en format JSON o expressió del MS | [optional] |
| **namedQueries** | [**List&lt;String&gt;**](String.md)| Consultes predefinides | [optional] |
| **perspectives** | [**List&lt;String&gt;**](String.md)| Perspectives de camp | [optional] |
| **page** | **String**| Número de pàgina | [optional] |
| **size** | **Integer**| Mida de pàgina | [optional] |

### Return type

[**TascaPage**](TascaPage.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |

