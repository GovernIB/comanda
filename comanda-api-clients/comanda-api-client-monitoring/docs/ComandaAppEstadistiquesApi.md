# ComandaAppEstadistiquesApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**estadistiques**](ComandaAppEstadistiquesApi.md#estadistiques) | **GET** /estadistiques/v1 | Obtenir darreres estadístiques diàries disponibles |
| [**estadistiquesInfo**](ComandaAppEstadistiquesApi.md#estadistiquesInfo) | **GET** /estadistiques/v1/info | Obtenir informació de &#39;estructura de les estadístiques |
| [**estadistiquesPerData**](ComandaAppEstadistiquesApi.md#estadistiquesPerData) | **GET** /estadistiques/v1/of/{data} | Obtenir estadístiques d&#39;una data concreta |
| [**estadistiquesPerRang**](ComandaAppEstadistiquesApi.md#estadistiquesPerRang) | **GET** /estadistiques/v1/from/{dataInici}/to/{dataFi} | Obtenir les estadístiques d&#39;un interval donat |



## estadistiques

> RegistresEstadistics estadistiques()

Obtenir darreres estadístiques diàries disponibles

Retorna registres d&#39;estadístiques més recents disponibles (estadístiques d&#39;ahir).

### Example

```java
// Import classes:
import es.caib.comanda.service.monitoring.ApiClient;
import es.caib.comanda.service.monitoring.ApiException;
import es.caib.comanda.service.monitoring.Configuration;
import es.caib.comanda.service.monitoring.models.*;
import es.caib.comanda.api.monitoring.ComandaAppEstadistiquesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        ComandaAppEstadistiquesApi apiInstance = new ComandaAppEstadistiquesApi(defaultClient);
        try {
            RegistresEstadistics result = apiInstance.estadistiques();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ComandaAppEstadistiquesApi#estadistiques");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**RegistresEstadistics**](RegistresEstadistics.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## estadistiquesInfo

> EstadistiquesInfo estadistiquesInfo()

Obtenir informació de &#39;estructura de les estadístiques

Retorna el codi de l&#39;app i el catàleg de dimensions i indicadors disponibles.

### Example

```java
// Import classes:
import es.caib.comanda.service.monitoring.ApiClient;
import es.caib.comanda.service.monitoring.ApiException;
import es.caib.comanda.service.monitoring.Configuration;
import es.caib.comanda.service.monitoring.models.*;
import es.caib.comanda.api.monitoring.ComandaAppEstadistiquesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        ComandaAppEstadistiquesApi apiInstance = new ComandaAppEstadistiquesApi(defaultClient);
        try {
            EstadistiquesInfo result = apiInstance.estadistiquesInfo();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ComandaAppEstadistiquesApi#estadistiquesInfo");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**EstadistiquesInfo**](EstadistiquesInfo.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## estadistiquesPerData

> RegistresEstadistics estadistiquesPerData(data)

Obtenir estadístiques d&#39;una data concreta

Retorna les estadístiques corresponents a la data indicada amb format dd-MM-yyyy.

### Example

```java
// Import classes:
import es.caib.comanda.service.monitoring.ApiClient;
import es.caib.comanda.service.monitoring.ApiException;
import es.caib.comanda.service.monitoring.Configuration;
import es.caib.comanda.service.monitoring.models.*;
import es.caib.comanda.api.monitoring.ComandaAppEstadistiquesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        ComandaAppEstadistiquesApi apiInstance = new ComandaAppEstadistiquesApi(defaultClient);
        String data = "data_example"; // String | Data en format dd-MM-yyyy
        try {
            RegistresEstadistics result = apiInstance.estadistiquesPerData(data);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ComandaAppEstadistiquesApi#estadistiquesPerData");
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
| **data** | **String**| Data en format dd-MM-yyyy | |

### Return type

[**RegistresEstadistics**](RegistresEstadistics.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## estadistiquesPerRang

> List&lt;RegistresEstadistics&gt; estadistiquesPerRang(dataInici, dataFi)

Obtenir les estadístiques d&#39;un interval donat

Retorna llista d&#39;estadístiques de tots els dies entre la dataInici i la dataFi (en format dd-MM-yyyy), ambdues incloses. La resposta contindrà un objecte de tipus RegistresEstadistics per a cada dia inclòs en l&#39;intèrval.

### Example

```java
// Import classes:
import es.caib.comanda.service.monitoring.ApiClient;
import es.caib.comanda.service.monitoring.ApiException;
import es.caib.comanda.service.monitoring.Configuration;
import es.caib.comanda.service.monitoring.models.*;
import es.caib.comanda.api.monitoring.ComandaAppEstadistiquesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        ComandaAppEstadistiquesApi apiInstance = new ComandaAppEstadistiquesApi(defaultClient);
        String dataInici = "dataInici_example"; // String | Data d'inici en format dd-MM-yyyy
        String dataFi = "dataFi_example"; // String | Data de fi en format dd-MM-yyyy
        try {
            List<RegistresEstadistics> result = apiInstance.estadistiquesPerRang(dataInici, dataFi);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ComandaAppEstadistiquesApi#estadistiquesPerRang");
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
| **dataInici** | **String**| Data d&#39;inici en format dd-MM-yyyy | |
| **dataFi** | **String**| Data de fi en format dd-MM-yyyy | |

### Return type

[**List&lt;RegistresEstadistics&gt;**](RegistresEstadistics.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |

