

# AppInfo

Informació bàsica de l'aplicació consultada per COMANDA

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**codi** | **String** | Codi identificador de l&#39;aplicació |  |
|**nom** | **String** | Nom complet de l&#39;aplicació |  |
|**versio** | **String** | Versió desplegada de l&#39;aplicació |  |
|**data** | [**OffsetDateTime**](OffsetDateTime.md) |  |  |
|**revisio** | **String** | Revisió o identificador de commit de la build |  [optional] |
|**jdkVersion** | **String** | Versió de JDK amb la qual s&#39;executa l&#39;aplicació |  [optional] |
|**versioJboss** | **String** | Versió de JBoss/WildFly amb la qual s&#39;executa l&#39;aplicació |  [optional] |
|**integracions** | [**List&lt;IntegracioInfo&gt;**](IntegracioInfo.md) | Llista d&#39;integracions exposades per l&#39;aplicació |  [optional] |
|**subsistemes** | [**List&lt;SubsistemaInfo&gt;**](SubsistemaInfo.md) | Llista de subsistemes interns amb el seu estat |  [optional] |
|**contexts** | [**List&lt;ContextInfo&gt;**](ContextInfo.md) | Contextos o endpoints base exposats per l&#39;aplicació |  [optional] |



