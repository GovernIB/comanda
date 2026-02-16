# ComandaAppLogsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getFitxerByNom**](ComandaAppLogsApi.md#getFitxerByNom) | **GET** /logs/v1/{nomFitxer} | Obtenir contingut complet d&#39;un fitxer de log |
| [**llegitUltimesLinies**](ComandaAppLogsApi.md#llegitUltimesLinies) | **GET** /logs/v1/{nomFitxer}/linies/{nLinies} | Obtenir les darreres línies d&#39;un fitxer de log |
| [**llistarFitxers**](ComandaAppLogsApi.md#llistarFitxers) | **GET** /logs/v1 | Obtenir el llistat de fitxers de log disponibles |



## getFitxerByNom

> FitxerContingut getFitxerByNom(nomFitxer)

Obtenir contingut complet d&#39;un fitxer de log

Retorna el contingut i detalls del fitxer de log que es troba dins la carpeta de logs del servidor, i que té el nom indicat

### Example

```java
// Import classes:
import es.caib.comanda.service.monitoring.ApiClient;
import es.caib.comanda.service.monitoring.ApiException;
import es.caib.comanda.service.monitoring.Configuration;
import es.caib.comanda.service.monitoring.models.*;
import es.caib.comanda.api.monitoring.ComandaAppLogsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        ComandaAppLogsApi apiInstance = new ComandaAppLogsApi(defaultClient);
        String nomFitxer = "nomFitxer_example"; // String | Nom del firxer
        try {
            FitxerContingut result = apiInstance.getFitxerByNom(nomFitxer);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ComandaAppLogsApi#getFitxerByNom");
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
| **nomFitxer** | **String**| Nom del firxer | |

### Return type

[**FitxerContingut**](FitxerContingut.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## llegitUltimesLinies

> List&lt;String&gt; llegitUltimesLinies(nomFitxer, nLinies)

Obtenir les darreres línies d&#39;un fitxer de log

Retorna les darreres linies del fitxer de log indicat per nom. Concretament es retorna el número de línies indicat al paràmetre nLinies.

### Example

```java
// Import classes:
import es.caib.comanda.service.monitoring.ApiClient;
import es.caib.comanda.service.monitoring.ApiException;
import es.caib.comanda.service.monitoring.Configuration;
import es.caib.comanda.service.monitoring.models.*;
import es.caib.comanda.api.monitoring.ComandaAppLogsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        ComandaAppLogsApi apiInstance = new ComandaAppLogsApi(defaultClient);
        String nomFitxer = "nomFitxer_example"; // String | Nom del firxer
        Long nLinies = 56L; // Long | Número de línies a recuperar del firxer
        try {
            List<String> result = apiInstance.llegitUltimesLinies(nomFitxer, nLinies);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ComandaAppLogsApi#llegitUltimesLinies");
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
| **nomFitxer** | **String**| Nom del firxer | |
| **nLinies** | **Long**| Número de línies a recuperar del firxer | |

### Return type

**List&lt;String&gt;**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## llistarFitxers

> List&lt;FitxerInfo&gt; llistarFitxers()

Obtenir el llistat de fitxers de log disponibles

Retorna una llista amb tots els fitxers que es troben dins la carpeta de logs del servidor de l&#39;aplicació

### Example

```java
// Import classes:
import es.caib.comanda.service.monitoring.ApiClient;
import es.caib.comanda.service.monitoring.ApiException;
import es.caib.comanda.service.monitoring.Configuration;
import es.caib.comanda.service.monitoring.models.*;
import es.caib.comanda.api.monitoring.ComandaAppLogsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        ComandaAppLogsApi apiInstance = new ComandaAppLogsApi(defaultClient);
        try {
            List<FitxerInfo> result = apiInstance.llistarFitxers();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ComandaAppLogsApi#llistarFitxers");
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

[**List&lt;FitxerInfo&gt;**](FitxerInfo.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |

