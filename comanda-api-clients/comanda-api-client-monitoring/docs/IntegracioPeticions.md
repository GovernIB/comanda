

# IntegracioPeticions

Mètriques de peticions d'una integració: totals i darrer període

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**totalOk** | **Long** | Nombre total de peticions amb resultat correcte |  |
|**totalError** | **Long** | Nombre total de peticions amb error |  |
|**totalTempsMig** | **Integer** | Temps mig total de resposta (ms) |  |
|**peticionsOkUltimPeriode** | **Long** | Peticions OK en el darrer període |  |
|**peticionsErrorUltimPeriode** | **Long** | Peticions en error en el darrer període |  |
|**tempsMigUltimPeriode** | **Integer** | Temps mig de resposta en el darrer període (ms) |  |
|**endpoint** | **String** | Endpoint concret associat a aquestes mètriques |  [optional] |
|**peticionsPerEntorn** | [**Map&lt;String, IntegracioPeticions&gt;**](IntegracioPeticions.md) | Mètriques per entorn (clau &#x3D; codi d&#39;entorn) |  [optional] |



