

# IntegracioPeticions

Mètriques de peticions d'una integració: totals i darrer període

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**totalOk** | **Long** | Nombre total de peticions amb resultat correcte |  [optional] |
|**totalError** | **Long** | Nombre total de peticions amb error |  [optional] |
|**totalTempsMig** | **Integer** | Temps mig total de resposta (ms) |  [optional] |
|**peticionsOkUltimPeriode** | **Long** | Peticions OK en el darrer període |  [optional] |
|**peticionsErrorUltimPeriode** | **Long** | Peticions en error en el darrer període |  [optional] |
|**tempsMigUltimPeriode** | **Integer** | Temps mig de resposta en el darrer període (ms) |  [optional] |
|**endpoint** | **String** | Endpoint concret associat a aquestes mètriques |  [optional] |
|**peticionsPerEntorn** | [**Map&lt;String, IntegracioPeticions&gt;**](IntegracioPeticions.md) | Mètriques per entorn (clau &#x3D; codi d&#39;entorn) |  [optional] |



