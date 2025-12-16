# AppComandaPermisosApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**consultarPermis**](AppComandaPermisosApi.md#consultarPermis) | **GET** /v1/permisos/{identificador} | Consulta d&#39;un permís |
| [**crearMultiplesPermisos**](AppComandaPermisosApi.md#crearMultiplesPermisos) | **POST** /v1/permisos/multiple | Creació de múltiples permisos |
| [**crearPermis**](AppComandaPermisosApi.md#crearPermis) | **POST** /v1/permisos | Creació d&#39;un permís |
| [**eliminarPermisos**](AppComandaPermisosApi.md#eliminarPermisos) | **DELETE** /v1/permisos | Eliminació de permisos |
| [**modificarMultiplesPermisos**](AppComandaPermisosApi.md#modificarMultiplesPermisos) | **PUT** /v1/permisos/multiple | Modificació de múltiples permisos |
| [**modificarPermis**](AppComandaPermisosApi.md#modificarPermis) | **PUT** /v1/permisos/{identificador} | Modificació d&#39;un permís existent |



## consultarPermis

> Permis consultarPermis(identificador, appCodi, entornCodi)

Consulta d&#39;un permís

Obté les dades d&#39;un permís identificat pel seu identificador, codi d&#39;aplicació i codi d&#39;entorn.

### Example

```java
// Import classes:
import es.caib.comanda.service.v1.permis.ApiClient;
import es.caib.comanda.service.v1.permis.ApiException;
import es.caib.comanda.service.v1.permis.Configuration;
import es.caib.comanda.service.v1.permis.models.*;
import es.caib.comanda.api.v1.permis.AppComandaPermisosApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaPermisosApi apiInstance = new AppComandaPermisosApi(defaultClient);
        String identificador = "identificador_example"; // String | Identificador del permís
        String appCodi = "appCodi_example"; // String | Codi de l'aplicació
        String entornCodi = "entornCodi_example"; // String | Codi de l'entorn
        try {
            Permis result = apiInstance.consultarPermis(identificador, appCodi, entornCodi);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaPermisosApi#consultarPermis");
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
| **identificador** | **String**| Identificador del permís | |
| **appCodi** | **String**| Codi de l&#39;aplicació | |
| **entornCodi** | **String**| Codi de l&#39;entorn | |

### Return type

[**Permis**](Permis.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## crearMultiplesPermisos

> String crearMultiplesPermisos(permis)

Creació de múltiples permisos

Afegeix múltiples missatges d&#39;alta de permisos a una cua de events per a que es creïn aquests de forma asíncrona a Comanda.

### Example

```java
// Import classes:
import es.caib.comanda.service.v1.permis.ApiClient;
import es.caib.comanda.service.v1.permis.ApiException;
import es.caib.comanda.service.v1.permis.Configuration;
import es.caib.comanda.service.v1.permis.models.*;
import es.caib.comanda.api.v1.permis.AppComandaPermisosApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaPermisosApi apiInstance = new AppComandaPermisosApi(defaultClient);
        List<Permis> permis = Arrays.asList(); // List<Permis> | 
        try {
            String result = apiInstance.crearMultiplesPermisos(permis);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaPermisosApi#crearMultiplesPermisos");
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
| **permis** | [**List&lt;Permis&gt;**](Permis.md)|  | [optional] |

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


## crearPermis

> String crearPermis(permis)

Creació d&#39;un permís

Afegeix un missatge d&#39;alta de permís a una cua de events per a que es crei aquest de forma asíncrona a Comanda.

### Example

```java
// Import classes:
import es.caib.comanda.service.v1.permis.ApiClient;
import es.caib.comanda.service.v1.permis.ApiException;
import es.caib.comanda.service.v1.permis.Configuration;
import es.caib.comanda.service.v1.permis.models.*;
import es.caib.comanda.api.v1.permis.AppComandaPermisosApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaPermisosApi apiInstance = new AppComandaPermisosApi(defaultClient);
        Permis permis = new Permis(); // Permis | 
        try {
            String result = apiInstance.crearPermis(permis);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaPermisosApi#crearPermis");
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
| **permis** | [**Permis**](Permis.md)|  | [optional] |

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


## eliminarPermisos

> String eliminarPermisos(permis)

Eliminació de permisos

Afegeix múltiples missatges d&#39;eliminació de permisos a una cua de events per a que s&#39;eliminin aquests de forma asíncrona a Comanda.

### Example

```java
// Import classes:
import es.caib.comanda.service.v1.permis.ApiClient;
import es.caib.comanda.service.v1.permis.ApiException;
import es.caib.comanda.service.v1.permis.Configuration;
import es.caib.comanda.service.v1.permis.models.*;
import es.caib.comanda.api.v1.permis.AppComandaPermisosApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaPermisosApi apiInstance = new AppComandaPermisosApi(defaultClient);
        List<Permis> permis = Arrays.asList(); // List<Permis> | 
        try {
            String result = apiInstance.eliminarPermisos(permis);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaPermisosApi#eliminarPermisos");
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
| **permis** | [**List&lt;Permis&gt;**](Permis.md)|  | [optional] |

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


## modificarMultiplesPermisos

> String modificarMultiplesPermisos(permis)

Modificació de múltiples permisos

Es comprova si els permisos existeixen, i en cas afirmatiu, s&#39;afegeixen múltiples missatges de modificació de permisos a una cua de events per a que es modifiquin aquests de forma asíncrona a Comanda. Els permisos no existents s&#39;ignoren.

### Example

```java
// Import classes:
import es.caib.comanda.service.v1.permis.ApiClient;
import es.caib.comanda.service.v1.permis.ApiException;
import es.caib.comanda.service.v1.permis.Configuration;
import es.caib.comanda.service.v1.permis.models.*;
import es.caib.comanda.api.v1.permis.AppComandaPermisosApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaPermisosApi apiInstance = new AppComandaPermisosApi(defaultClient);
        List<Permis> permis = Arrays.asList(); // List<Permis> | 
        try {
            String result = apiInstance.modificarMultiplesPermisos(permis);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaPermisosApi#modificarMultiplesPermisos");
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
| **permis** | [**List&lt;Permis&gt;**](Permis.md)|  | [optional] |

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


## modificarPermis

> String modificarPermis(identificador, permis)

Modificació d&#39;un permís existent

Es comprova si el permís existeix, i en cas afirmatiu, s&#39;afegeix un missatge de modificació de permís a una cua de events per a que es modifiqui aquest de forma asíncrona a Comanda.

### Example

```java
// Import classes:
import es.caib.comanda.service.v1.permis.ApiClient;
import es.caib.comanda.service.v1.permis.ApiException;
import es.caib.comanda.service.v1.permis.Configuration;
import es.caib.comanda.service.v1.permis.models.*;
import es.caib.comanda.api.v1.permis.AppComandaPermisosApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        AppComandaPermisosApi apiInstance = new AppComandaPermisosApi(defaultClient);
        String identificador = "identificador_example"; // String | Identificador funcional (objecte.identificador)
        Permis permis = new Permis(); // Permis | 
        try {
            String result = apiInstance.modificarPermis(identificador, permis);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppComandaPermisosApi#modificarPermis");
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
| **identificador** | **String**| Identificador funcional (objecte.identificador) | |
| **permis** | [**Permis**](Permis.md)|  | [optional] |

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

