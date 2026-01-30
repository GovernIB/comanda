# AppComandaAvisosApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**consultarAvis**](AppComandaAvisosApi.md#consultarAvis) | **GET** /avisos/v1/{identificador} | Consulta d&#39;un avís |
| [**crearAvis**](AppComandaAvisosApi.md#crearAvis) | **POST** /avisos/v1 | Creació d&#39;un avís |
| [**crearMultiplesAvisos**](AppComandaAvisosApi.md#crearMultiplesAvisos) | **POST** /avisos/v1/multiple | Creació de múltiples avisos |
| [**modificarAvis**](AppComandaAvisosApi.md#modificarAvis) | **PUT** /avisos/v1/{identificador} | Modificació d&#39;un avís existent |
| [**modificarMultiplesAvisos**](AppComandaAvisosApi.md#modificarMultiplesAvisos) | **PUT** /avisos/v1/multiple | Modificació de múltiples avisos |
| [**obtenirLlistatAvisos**](AppComandaAvisosApi.md#obtenirLlistatAvisos) | **GET** /avisos/v1 | Llistat d&#39;avisos |



## consultarAvis

> Avis consultarAvis(identificador, appCodi, entornCodi)

Consulta d&#39;un avís

Obté les dades d&#39;un avís a partir del seu identificador, codi d&#39;aplicació i codi d&#39;entorn.

### Example

```java
// Import classes:
import es.caib.comanda.service.v1.management.ApiClient;
import es.caib.comanda.service.v1.management.ApiException;
import es.caib.comanda.service.v1.management.Configuration;
import es.caib.comanda.service.v1.management.models.*;
import es.caib.comanda.api.v1.management.AppComandaAvisosApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaAvisosApi apiInstance = new AppComandaAvisosApi(defaultClient);
        String identificador = "identificador_example"; // String | Identificador de l'avís
        String appCodi = "appCodi_example"; // String | Codi de l'aplicació
        String entornCodi = "entornCodi_example"; // String | Codi de l'entorn
        try {
            Avis result = apiInstance.consultarAvis(identificador, appCodi, entornCodi);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaAvisosApi#consultarAvis");
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
| **identificador** | **String**| Identificador de l&#39;avís | |
| **appCodi** | **String**| Codi de l&#39;aplicació | |
| **entornCodi** | **String**| Codi de l&#39;entorn | |

### Return type

[**Avis**](Avis.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## crearAvis

> String crearAvis(avis)

Creació d&#39;un avís

Afegeix un missatge d&#39;alta d&#39;avís a una cua de events per a que es crei aquest de forma asíncrona a Comanda.

### Example

```java
// Import classes:
import es.caib.comanda.service.v1.management.ApiClient;
import es.caib.comanda.service.v1.management.ApiException;
import es.caib.comanda.service.v1.management.Configuration;
import es.caib.comanda.service.v1.management.models.*;
import es.caib.comanda.api.v1.management.AppComandaAvisosApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaAvisosApi apiInstance = new AppComandaAvisosApi(defaultClient);
        Avis avis = new Avis(); // Avis | 
        try {
            String result = apiInstance.crearAvis(avis);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaAvisosApi#crearAvis");
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
| **avis** | [**Avis**](Avis.md)|  | [optional] |

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


## crearMultiplesAvisos

> String crearMultiplesAvisos(avis)

Creació de múltiples avisos

Afegeix múltiples missatges d&#39;alta d&#39;avisos a una cua de events per a que es creïn aquests de forma asíncrona a Comanda.

### Example

```java
// Import classes:
import es.caib.comanda.service.v1.management.ApiClient;
import es.caib.comanda.service.v1.management.ApiException;
import es.caib.comanda.service.v1.management.Configuration;
import es.caib.comanda.service.v1.management.models.*;
import es.caib.comanda.api.v1.management.AppComandaAvisosApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaAvisosApi apiInstance = new AppComandaAvisosApi(defaultClient);
        List<Avis> avis = Arrays.asList(); // List<Avis> | 
        try {
            String result = apiInstance.crearMultiplesAvisos(avis);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaAvisosApi#crearMultiplesAvisos");
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
| **avis** | [**List&lt;Avis&gt;**](Avis.md)|  | [optional] |

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


## modificarAvis

> String modificarAvis(identificador, avis)

Modificació d&#39;un avís existent

Es comprova si l&#39;avís existeix, i en cas afirmatiu, s&#39;afegeix un missatge de modificació d&#39;avís a una cua de events per a que es modifiqui aquest de forma asíncrona a Comanda.

### Example

```java
// Import classes:
import es.caib.comanda.service.v1.management.ApiClient;
import es.caib.comanda.service.v1.management.ApiException;
import es.caib.comanda.service.v1.management.Configuration;
import es.caib.comanda.service.v1.management.models.*;
import es.caib.comanda.api.v1.management.AppComandaAvisosApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaAvisosApi apiInstance = new AppComandaAvisosApi(defaultClient);
        String identificador = "identificador_example"; // String | Identificador de l'avís
        Avis avis = new Avis(); // Avis | 
        try {
            String result = apiInstance.modificarAvis(identificador, avis);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaAvisosApi#modificarAvis");
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
| **identificador** | **String**| Identificador de l&#39;avís | |
| **avis** | [**Avis**](Avis.md)|  | [optional] |

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


## modificarMultiplesAvisos

> String modificarMultiplesAvisos(avis)

Modificació de múltiples avisos

Es comprova si els avisos existeixen, i en cas afirmatiu, s&#39;afegeixen missatges de modificació d&#39;avisos a una cua de events per a que es modifiquin aquests de forma asíncrona a Comanda.

### Example

```java
// Import classes:
import es.caib.comanda.service.v1.management.ApiClient;
import es.caib.comanda.service.v1.management.ApiException;
import es.caib.comanda.service.v1.management.Configuration;
import es.caib.comanda.service.v1.management.models.*;
import es.caib.comanda.api.v1.management.AppComandaAvisosApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaAvisosApi apiInstance = new AppComandaAvisosApi(defaultClient);
        List<Avis> avis = Arrays.asList(); // List<Avis> | 
        try {
            String result = apiInstance.modificarMultiplesAvisos(avis);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaAvisosApi#modificarMultiplesAvisos");
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
| **avis** | [**List&lt;Avis&gt;**](Avis.md)|  | [optional] |

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


## obtenirLlistatAvisos

> AvisPage obtenirLlistatAvisos(quickFilter, filter, namedQueries, perspectives, page, size)

Llistat d&#39;avisos

Obté un llistat paginat d&#39;avisos amb la possibilitat d&#39;aplicar filtres de cerca.

### Example

```java
// Import classes:
import es.caib.comanda.service.v1.management.ApiClient;
import es.caib.comanda.service.v1.management.ApiException;
import es.caib.comanda.service.v1.management.Configuration;
import es.caib.comanda.service.v1.management.models.*;
import es.caib.comanda.api.v1.management.AppComandaAvisosApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaAvisosApi apiInstance = new AppComandaAvisosApi(defaultClient);
        String quickFilter = "quickFilter_example"; // String | Filtre ràpid
        String filter = "filter_example"; // String | Filtre avançat en format JSON o expressió del MS
        List<String> namedQueries = Arrays.asList(); // List<String> | Consultes predefinides
        List<String> perspectives = Arrays.asList(); // List<String> | Perspectives de camp
        String page = "page_example"; // String | Número de pàgina
        Integer size = 56; // Integer | Mida de pàgina
        try {
            AvisPage result = apiInstance.obtenirLlistatAvisos(quickFilter, filter, namedQueries, perspectives, page, size);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaAvisosApi#obtenirLlistatAvisos");
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

[**AvisPage**](AvisPage.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |

