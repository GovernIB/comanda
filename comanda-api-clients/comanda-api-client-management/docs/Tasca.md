

# Tasca

Representa una tasca publicada a COMANDA perquè sigui processada asíncronament

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**appCodi** | **String** | Codi de l&#39;aplicació que publica la tasca |  |
|**entornCodi** | **String** | Codi de l&#39;entorn de l&#39;aplicació |  |
|**identificador** | **String** | Identificador únic de la tasca en l&#39;àmbit de l&#39;APP |  |
|**tipus** | **String** | Tipus funcional de la tasca |  |
|**nom** | **String** | Nom curt de la tasca |  |
|**descripcio** | **String** | Descripció detallada de la tasca |  [optional] |
|**estat** | **TascaEstat** |  |  |
|**estatDescripcio** | **String** | Descripció de l&#39;estat actual |  [optional] |
|**numeroExpedient** | **String** | Número d&#39;expedient relacionat (si aplica) |  [optional] |
|**prioritat** | **Prioritat** |  |  [optional] |
|**dataInici** | [**OffsetDateTime**](OffsetDateTime.md) |  |  [optional] |
|**dataFi** | [**OffsetDateTime**](OffsetDateTime.md) |  |  [optional] |
|**dataCaducitat** | [**OffsetDateTime**](OffsetDateTime.md) |  |  [optional] |
|**redireccio** | [**URL**](URL.md) |  |  |
|**responsable** | **String** | Usuari responsable |  [optional] |
|**grup** | **String** | Grup responsable |  [optional] |
|**usuarisAmbPermis** | **List&lt;String&gt;** | Llista d&#39;usuaris amb permís |  [optional] |
|**grupsAmbPermis** | **List&lt;String&gt;** | Llista de grups amb permís |  [optional] |



