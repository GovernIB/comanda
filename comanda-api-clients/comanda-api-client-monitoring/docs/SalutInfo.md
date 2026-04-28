

# SalutInfo

Estat de salut funcional de l'aplicació i metadades associades

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**codi** | **String** | Codi identificador de l&#39;aplicació |  |
|**data** | [**OffsetDateTime**](OffsetDateTime.md) |  |  |
|**estatGlobal** | [**EstatSalut**](EstatSalut.md) |  |  |
|**estatBaseDeDades** | [**EstatSalut**](EstatSalut.md) |  |  |
|**integracions** | [**List&lt;IntegracioSalut&gt;**](IntegracioSalut.md) | Integracions amb el seu estat |  [optional] |
|**informacioSistema** | [**InformacioSistema**](InformacioSistema.md) |  |  [optional] |
|**missatges** | [**List&lt;MissatgeSalut&gt;**](MissatgeSalut.md) | Missatges informatius o d&#39;alerta |  [optional] |
|**versio** | **String** | Versió de l&#39;aplicació |  [optional] |
|**subsistemes** | [**List&lt;SubsistemaSalut&gt;**](SubsistemaSalut.md) | Subsistemes interns amb el seu estat |  [optional] |



