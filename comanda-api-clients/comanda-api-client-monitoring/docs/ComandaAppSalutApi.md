# ComandaAppSalutApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**salut**](ComandaAppSalutApi.md#salut) | **GET** /salut/v1 | Obtenir informació de l&#39;estat de salut de l&#39;aplicació |
| [**salutInfo**](ComandaAppSalutApi.md#salutInfo) | **GET** /salut/v1/info | Obtenir informació de l&#39;aplicació |



## salut

> SalutInfo salut()

Obtenir informació de l&#39;estat de salut de l&#39;aplicació

Retorna l&#39;estat de salut funcional i integracions, amb metadades de versió.

### Example

```java
// Import classes:
import es.caib.comanda.service.monitoring.ApiClient;
import es.caib.comanda.service.monitoring.ApiException;
import es.caib.comanda.service.monitoring.Configuration;
import es.caib.comanda.service.monitoring.models.*;
import es.caib.comanda.api.monitoring.ComandaAppSalutApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        ComandaAppSalutApi apiInstance = new ComandaAppSalutApi(defaultClient);
        try {
            SalutInfo result = apiInstance.salut();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ComandaAppSalutApi#salut");
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

[**SalutInfo**](SalutInfo.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |


## salutInfo

> AppInfo salutInfo()

Obtenir informació de l&#39;aplicació

Retorna dades bàsiques de l&#39;aplicació (codi, nom, versió, data de build, etc.) i contextos exposats.

### Example

```java
// Import classes:
import es.caib.comanda.service.monitoring.ApiClient;
import es.caib.comanda.service.monitoring.ApiException;
import es.caib.comanda.service.monitoring.Configuration;
import es.caib.comanda.service.monitoring.models.*;
import es.caib.comanda.api.monitoring.ComandaAppSalutApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        ComandaAppSalutApi apiInstance = new ComandaAppSalutApi(defaultClient);
        try {
            AppInfo result = apiInstance.salutInfo();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ComandaAppSalutApi#salutInfo");
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

[**AppInfo**](AppInfo.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | successful operation |  -  |

