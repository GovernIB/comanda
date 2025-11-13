  
![][image1]

**COMANDA: Manual d’integració**

octubre de 2025

**Serveis d’Administració Electrònica en el Govern de les Illes Balears**

Lot 2\. Serveis d’administració electrònica

# **Control de versions del document** {#control-de-versions-del-document}

| Control de Canvis |  |  |  |
| :---- | :---- | :---- | :---- |
| **Data** | **Autor** | **Versió** | **Canvis** |
| 06/10/25 | Límit Tecnologies | 1.0 | Versió inicial |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |

| Revisat per |  |  |
| :---- | :---- | :---- |
| **Nom** | **Data** | **Àrea, departament o empresa** |
|  |  |  |
|  |  |  |
|  |  |  |

| Aprovat per |  |  |
| :---- | :---- | :---- |
| **Nom** | **Data** | **Àrea, departament o empresa** |
|  |  |  |
|  |  |  |
|  |  |  |

| Llista de distribució |  |  |
| :---- | :---- | :---- |
| **Nom** | **Àrea, departament o empresa** | **Correu electrònic** |
|  |  |  |
|  |  |  |
|  |  |  |

**Índex**

**[Control de versions del document	2](#control-de-versions-del-document)**

[**1\. Objecte	4**](#objecte)

[**2\. Requisits previs	4**](#requisits-previs)

[**3\. Salut	5**](#salut)

[3.1. Obtenció de informació de l’aplicació	5](#obtenció-de-informació-de-l’aplicació)

[Model de dades	6](#model-de-dades)

[3.2. Obtenció de informació de salut	8](#obtenció-de-informació-de-salut)

[Model de dades	11](#model-de-dades-1)

[**4\. Estadístiques	16**](#estadístiques)

[4.1. Obtenció de informació estadística de l’aplicació	16](#obtenció-de-informació-estadística-de-l’aplicació)

[Model de dades	17](#model-de-dades-2)

[4.2. Obtenció de dades estadístiques	18](#obtenció-de-dades-estadístiques)

[Model de dades	20](#model-de-dades-3)

[**5\. Tasques	24**](#tasques)

[Model de dades	25](#model-de-dades-4)

[**6\. Avisos	26**](#avisos)

[Model de dades	27](#model-de-dades-5)

[**7\. Exemples	28**](#exemples)

[Enviar una tasca	28](#enviar-una-tasca)

[Enviar un avís	28](#enviar-un-avís)

# 

1. # **Objecte** {#objecte}

En aquest manual es detalla com integrar una aplicació amb Comanda per generar i enviar a Comanda la informació de Salut i Estadístiques de l’aplicació, així com com Tasques i Avisos. 

2. # **Requisits previs** {#requisits-previs}

És aconsellable disposar de la llibreria de models de Comanda al classpath (**comanda-lib**).  
Si s’utilitza Maven es pot afegir la següent dependència (utilitzant la darrera versió disponible):

|     `<dependency>         <groupId>es.caib.comanda</groupId>         <artifactId>comanda-lib</artifactId>         <version>0.1.1</version>     </dependency>` |
| :---- |

Aquesta llibreria inclou el model de dades que Comanda espera rebre per a les integracions:

* Salut:   
  * es.caib.comanda.ms.salut.model.AppInfo  
  * es.caib.comanda.ms.salut.model.SalutInfo  
* Estadístiques:   
  * es.caib.comanda.ms.estadistica.model.DimensioDesc  
  * es.caib.comanda.ms.estadistica.model.IndicadorDesc  
  * es.caib.comanda.ms.estadistica.model.RegistresEstadistics  
  * es.caib.comanda.ms.estadistica.model.EstadistiquesInfo  
* Tasques:   
  * es.caib.comanda.ms.broker.model.Tasca  
* Avisos:  
  *  es.caib.comanda.ms.broker.model.Avis  
  * es.caib.comanda.ms.broker.model.AvisTipus

Tambés serà necessari disposar d’un **client HTTP o JMS** per a l’enviament de la informació de Tasques i Avisos (per exemple RestTemplate i JmsTemplate).  
Comanda disposa d’un broker de missatges JMS per rebre la informació de tasques i avisos. Al mateix temps s’ha implementat un servei REST amb la mateixa funcionalitat per si no es disposa de client JMS o si el proxy bloqueja els missatges enviats al broker.

3. # **Salut** {#salut}

Cada aplicació que es vulgui integrar amb el mòdul de Salut de comanda haurà d’implementar 2 endpoints de tipus GET. Els noms dels endpoinds son lliures, tot i que en aquest document utilitzarem com a exemple /appInfo i /salut

* GET /appInfo: retorna informació d’aplicació (AppInfo) per a la pantalla de Salut de Comanda.  
* GET /salut: retorna informació de salut de l’aplicació (SalutInfo).

  1. ## **Obtenció de informació de l’aplicació** {#obtenció-de-informació-de-l’aplicació}

| Obtenció de informació de l’aplicació |  |
| ----- | :---- |
| **Adreça API** | Adreça lliure a decidir pels desenvolupadors de l’aplicació.   Ex. https://dev.caib.es/notibapi/interna/appInfo |
| **Descripció** | Aquest servei retorna informació de l’aplicació (AppInfo) |
| **Mètode** | GET |
| **Autenticació** | Actualment es requereix que el mètode no tingui autenticació. S’espera que en un futur s’utilitzi autenticació BASIC |
| **Paràmetres** | No accepta paràmetres |
| **Resposta** | AppInfo |
| **Ex. de petició** | curl \--location 'https://dev.caib.es/notibapi/interna/appInfo' |
| **Ex. de resposta** | {     "codi": "NOT",     "nom": "Notib",     "versio": "2.0.11",     "data": 1759492373000,     "revisio": "3b536d2d9b6057bc3c984329de295dc5eb6d3e84",     "jdkVersion": "11",     "integracions": \[         {             "codi": "ARX",             "nom": "Arxiu"         },         {             "codi": "USR",             "nom": "Usuaris"         }     \],     "subsistemes": \[         {             "codi": "AWE",             "nom": "Alta web"         },         {             "codi": "ARE",             "nom": "Alta REST"         }     \],     "contexts": \[         {             "codi": "BACK",             "nom": "Backoffice",             "path": "https://dev.caib.es/notibback",             "manuals": \[                 {                     "nom": "Manual d'usuari",                     "path": "https://github.com/GovernIB/notib/…/pdf/NOTIB\_usuari.pdf"                 },                 {                     "nom": "Manual d'administració",                     "path": "https://github.com/GovernIB/notib/…/pdf/NOTIB\_administracio.pdf"                 }             \]         },         {             "codi": "INT",             "nom": "API interna",             "path": "https://dev.caib.es/notibapi/interna",             "manuals": \[                 {                     "nom": "Manual d'integració",                     "path": "https://github.com/GovernIB/notib/…/pdf/NOTIB\_integracio.pdf"                 }             \],             "api": "https://dev.caib.es/notibapi/interna/rest"         },         {             "codi": "EXT",             "nom": "API externa",             "path": "https://dev.caib.es/notibapi/externa",             "api": "https://dev.caib.es/notibapi/externa/rest"         }     \] } |

1. 

### **Model de dades** {#model-de-dades}

| AppInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena(16) | 1 | Codi (normalment de 3 lletres) assignat a l’aplicació. |
| nom | cadena(100) | 1 | Nom de l’aplicació |
| versio | cadena(10) | 1 | Número de versió actual de l’aplicació |
| data | Date | 0..1 | Data de la versió (es podria utilitzar la data en que s’ha pujat la versió al repositori, o la data en que ha estat compilada) |
| revisio | cadena(64) | 0..1 | Número de la revisió en el repositori (Número o codi del commit de la versió desplegada) Ex.![][image2] |
| jdkVersion | cadena(10) | 0..1 | Versió de Java amb la que està compilada l'aplicació |
| integracions | List\<IntegracioInfo\> | 0..N | Informació de les integracions de l’aplicació (aplicacions externes amb les que es comunica) |
| subsistemes | List\<SubsistemaInfo\> | 0..N | Informació dels subsitemes de l’aplicació.Entenem com a subsistema qualsevol part de l’aplicació amb prou entitat o importància com per controlar si està funcionant correctament o no.Ex. En una aplicació de gestió d’expedients: Alta d’expedients Tramitació de tasca Adjunció de documents … \*\* Només s’han d’emplenar els camps **codi** i **nom** en la informació de subsistemes |
| contexts | List\<ContextInfo\> | 0..N | Informació dels diferents contexts que es publiquen al desplegar l’aplicació.Habitualment ens trobarem 3 contexts: backoffice, api interna i api externa. |

| IntegracioInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena(16) | 1 | Codi de la integració. |
| nom | cadena(100) | 1 | Nom de la integració |

Per emplenar la informació de la integració es pot utilitzar l’enumerat es.caib.comanda.ms.salut.model.IntegracioApp, que és un llistat de integracions, amb el codi i el nom.

Si s’utilitza aquest enumerat per informar d’una integració es pot utilitzar el builder:

| `IntegracioInfo integracioInfo = IntegracioInfo.builder().integracioApp(IntegracioApp.COD).build();` |
| :---- |

| SubsistemaInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena(16) | 1 | Codi del subsistema. |
| nom | cadena(100) | 1 | Nom del subsistema |

| ContextInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena(64) | 1 | Codi del context. Valor lliure a decidir pels desenvolupadors de l’aplicació. |
| nom | cadena(100) | 1 | Nom del context. Valor lliure a decidir pels desenvolupadors de l’aplicació. |
| path | cadena(255) | 1 | Url on es pot accedir al context |
| manuals | List\<Manual\> | 0..1 | Informació dels manuals de l’aplicació |
| api | cadena(255) | 0..1 | Url on es pot accedir a la informació swagger de la API del context |

En cas de utilitzar els contexts de backoffice, api interna i api externa es recomana utilitzar com a codi i nom els següents valors (per manetenir la coherència entre les deferents aplicacions):

| Codi | Nom |
| :---- | :---- |
| BACK | Backoffice |
| INT | API interna |
| EXT | API externa |

| Manual |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| nom | cadena(128) | 1 | Nom del manual |
| path | cadena(255) | 1 | URL on es pot accedir al manual |

2. ## **Obtenció de informació de salut** {#obtenció-de-informació-de-salut}

| Obtenció de informació de salut |  |
| ----- | :---- |
| **Adreça API** | Adreça lliure a decidir pels desenvolupadors de l’aplicació.   Ex. https://dev.caib.es/notibapi/interna/salut |
| **Descripció** | Aquest servei retorna informació de salut de l’aplicació |
| **Mètode** | GET |
| **Autenticació** | Actualment es requereix que el mètode no tingui autenticació. S’espera que en un futur s’utilitzi autenticació BASIC |
| **Paràmetres** | No accepta paràmetres |
| **Resposta** | SalutInfo |
| **Ex. de petició** | curl \--location 'https://dev.caib.es/notibapi/interna/salut' |
| **Ex. de resposta** | {     "codi": "NOT",     "data": 1759754016720,     "estat": {         "estat": "UP",         "latencia": 29     },     "bd": {         "estat": "UP",         "latencia": 0     },     "integracions": \[         {             "codi": "ARX",             "peticions": {                 "totalOk": 0,                 "totalError": 0,                 "totalTempsMig": 0,                 "peticionsOkUltimPeriode": 0,                 "peticionsErrorUltimPeriode": 0,                 "tempsMigUltimPeriode": 0,                 "peticionsPerEntorn": {}             }         },         {             "estat": "UP",             "latencia": 0,             "codi": "USR",             "peticions": {                 "totalOk": 34,                 "totalError": 0,                 "totalTempsMig": 303,                 "peticionsOkUltimPeriode": 0,                 "peticionsErrorUltimPeriode": 0,                 "tempsMigUltimPeriode": 0,                 "peticionsPerEntorn": {}             }         }     \],     "altres": \[         {             "codi": "PRC",             "nom": "Processadors",             "valor": "4"         },         {             "codi": "SCPU",             "nom": "Càrrega del sistema",             "valor": "No disponible"         },         {             "codi": "PCPU",             "nom": "Càrrega del procés",             "valor": "0.0%"         },         {             "codi": "MED",             "nom": "Memòria disponible",             "valor": "382,5 MB"         },         {             "codi": "MET",             "nom": "Memòria total",             "valor": "2,1 GB"         },         {             "codi": "EDT",             "nom": "Espai de disc total",             "valor": "56,6 GB"         },         {             "codi": "EDL",             "nom": "Espai de disc lliure",             "valor": "4,6 GB"         },         {             "codi": "SO",             "nom": "Sistema operatiu",             "valor": "Linux 3.10.0-957.10.1.el7.x86\_64 (amd64)"         }     \],     "versio": "2.0.11",     "subsistemes": \[         {             "estat": "UP",             "latencia": 0,             "codi": "AWE",             "totalOk": 5,             "totalError": 0,             "totalTempsMig": 116,             "peticionsOkUltimPeriode": 0,             "peticionsErrorUltimPeriode": 0,             "tempsMigUltimPeriode": 0         },         {             "estat": "UP",             "latencia": 0,             "codi": "ARE",             "totalOk": 19,             "totalError": 0,             "totalTempsMig": 252,             "peticionsOkUltimPeriode": 0,             "peticionsErrorUltimPeriode": 0,             "tempsMigUltimPeriode": 0         }     \] } |

2. \*\* Aquest mètode ha de contestar amb el mínim temps possible per no afectar al rendiment de comanda. Per aquest motiu es desaconsella que al consultar les dades de les integracions es facin peticions a les aplicacions integrades. La informació de salut s’hauria d’obtenir únicament amb dades de la pròpia aplicació.

3. 

### **Model de dades** {#model-de-dades-1}

| SalutInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena(16) | 1 | Codi (normalment de 3 lletres) assignat a l’aplicació. |
| versio | cadena(10) | 1 | Número de versió actual de l’aplicació |
| data | Date | 0..1 | Data de la versió (es podria utilitzar la data en que s’ha pujat la versió al repositori, o la data en que ha estat compilada) |
| estat | EstatSalut | 1 | Estat en que es troba l’aplicació i el temps que tarda a contestar (latència) |
| bd | EstatSalut | 0..1 | Estat en que es troba la BBDD i el temps que tarda a contestar (latència) |
| integracions | List\<IntegracioSalut\> | 0..N | Estat en que es troba cada integració, i Informació de les peticions realitzades mitjançant la integració. |
| subsistemes | List\<SubsistemaSalut\> | 0..N | Estat en que es troba cada subsistema, i Informació de les peticions realitzades mitjançant la integració. |
| missatges | List\<MissatgeSalut\> | 0..N | Missatges rellevants que es mostrin a l’aplicació. *Exemples de missatges poden ser: Es realitzarà un manteniment de l’aplicació el proper dilluns entre les 9:00 i les 12:00 Entorn únicament per a proves dels desenvolupadors* En molts casos aquests missatges seran avisos dins l’aplicació, tot i que decidir quins missatges informar en en mòdul de salut i en el mòdul d’avisos queda en mans dels desenvolupadors de cada aplicació. |
| altres | List\<DetallSalut\> | 0..N | Altres informació d’interés del sistema (CPU, Memòria, …) |

| EstatSalut |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| estat | EstatSalutEnum | 1 | Estat del sistema. Possibles valors: \[UP, WARN, DEGRADED, DOWN, MAINTENANCE, UNKNOWN, ERROR\] |
| latencia | sencer | 1 | Latència en milisegons |

Els possibles valors de l’estat son:

| EstatSalutEnum |  |
| ----- | :---- |
| Valor | Descripció |
| UP | El sistema es troba funcionant correctament o amb una mínima tassa d’errors (\< 10% errors) |
| WARN | El sistema està funcionant, però amb una tassa d’errors entre el 10 \- 20%, que convé anar controlant |
| DEGRADED | Funcionant, però amb una tassa d’errors entre el 20-50% que ens indica que el sistema sembla estar degradat |
| ERROR | El sistema no està funcionant, o està funcionant amb una tassa d’errors molt elevat (\> 50%) |
| MAINTENANCE | El sistema es troba actualment en manteniment |
| UNKNOWN | Es desconeix l’estat del sistema. Aquest cas es pot donar, per exemple, quan encara no s’ha realitzat cap petició, i per tant no es té informació del sistema. |
| DOWN | El sistema no està funcionant. Tassa d’errors del 100%. |

Per calcular l’estat d’un sistema (aplicació, BBDD, integració o subsistema), si es disposa del percentatge de fallades, per tal que totes les aplicacions apliquin els mateixos percentatges per calcular l’estat, es recomana utilitzar els mètodes de la classe es.caib.comanda.ms.salut.model.EstatByPercent:

* calculaEstat: calcula d’estat a partir d’un valor amb el percentatge d’errades.  
* mergeEstats: calcula l’estat a partir de dos estats. Retorna l’estat pitjor dels dos.

| `EstatSalutEnum estat = EstatByPercent.calculaEstat(percentatgeDeErrors); EstatSalutEnum estatAplicacio = EstatByPercent.mergeEstats(estatApp, estatSubsistemes);` |
| :---- |

| IntegracioSalut |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena(16) | 1 | Codi de la integració. Serà el mateix codi informat en AppInfo. |
| estat | EstatSalutEnum | 1 | Estat de la integració. |
| latencia | sencer | 0..1 | Latència en milisegons. |
| peticions | IntegracioPeticions | 0..1 | Informació de les peticions realitzades a la integració |

| IntegracioPeticions |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| totalOk | long | 1 | Nombre total de peticions realitzades correctament des de l’última arrancada de l’aplicació |
| totalError | long | 1 | Nombre total de peticions realitzades amb error des de l’última arrancada de l’aplicació |
| totalTempsMig | sencer | 1 | Temps mig en milisegons que ha costat realitzar les peticions que no han donat error des de l’última arrancada de l’aplicació |
| peticionsOkUltimPeriode | long | 1 | Nombre total de peticions realitzades correctament des de la anterior consulta de salut |
| peticionsErrorUltimPeriode | long | 1 | Nombre total de peticions realitzades amb error des de la anterior consulta de salut |
| tempsMigUltimPeriode | sencer | 1 | Temps mig en milisegons que ha costat realitzar les peticions que no han donat error des de la anterior consulta de salut |
| endpoint | cadena(255) | 0..1 | Adreça on es realitzen les peticions |
| peticionsPerEntorn | Map\<cadena, IntegracioPeticions | 0..N | Detall de les peticions en el cas en que hi hagi múltiples instàncies d’aquesta |

En el cas en que hi hagi múltiples instàncies d’una mateixa integració (per exemple, diferents configuracions de la integració per diferents entitats o òrgans), en el camp peticionsPerEntorn es detallaran les peticions realitzades a cada una de les instàncies de la integració. En el cas en que no hi hagi múltiples instàncies, el camp es deixarà buid.

En el cas de múltiples instàncies, la clau del mapa serà un codi identificatiu de la instància (per exemple, el codi de l’entitat, si hi ha diferents configuracions per entitat), i la IntegracioPeticios serà el detall de les peticions únicament per a la instància concreta.

Exemple:  
*Suposem que tenim una integració amb el Registre, configurada de manera diferent per dues entitats: GOIB i SALUT, podriem tenir com a IntegracioPeticions:*

|         `{             "estat": "UP",             "latencia": 0,             "codi": "REG",             "peticions": {                 "totalOk": 34,                 "totalError": 3,                 "totalTempsMig": 267,                 "peticionsOkUltimPeriode": 7,                 "peticionsErrorUltimPeriode": 1,                 "tempsMigUltimPeriode": 275,                 "peticionsPerEntorn":                 {                      “GOIB” :                      {                         "codi": "REG",                         "peticions": {                             "totalOk": 28,                             "totalError": 0,                             "totalTempsMig": 242,                             "peticionsOkUltimPeriode": 5,                             "peticionsErrorUltimPeriode": 0,                             "tempsMigUltimPeriode": 234,                             "endpoint": "https://dev.caib.es/regweb3"                        }                     },                     “SALUT”:                     {                         "codi": "REG",                         "peticions": {                             "totalOk": 6,                             "totalError": 3,                             "totalTempsMig": 385,                             "peticionsOkUltimPeriode": 2,                             "peticionsErrorUltimPeriode": 1,                             "tempsMigUltimPeriode": 377,                             "endpoint": "https://dev.salut.es/registre"                        }                     }                 }             }         }` |
| :---- |

*El bloc GOIB conté les mètriques de les peticions dirigides cap al registre de l’entitat GOIB, incloent també l’adreça on es troba el registre de l’entitat GOIB.*  
*El bloc SALUT conté les mètriques de les peticions dirigides cap al registre de l’entitat SALUT, incloent també l’adreça on es troba el registre de l’entitat SALUT.*  
*El bloc del principi inclou les mètriques totals de la integració de registre. Així. per exemple, si es sumen el número de peticions de cada entitat, ens donarà el valor informat en aquest bloc inicial de totals.*

| SubsistemaSalut |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena(16) | 1 | Codi del subsistema. Serà el mateix codi informat en AppInfo. |
| estat | EstatSalutEnum | 1 | Estat del subsistema. |
| latencia | sencer | 0..1 | Latència en milisegons. |
| totalOk | long | 1 | Nombre total de peticions realitzades correctament des de l’última arrancada de l’aplicació |
| totalError | long | 1 | Nombre total de peticions realitzades amb error des de l’última arrancada de l’aplicació |
| totalTempsMig | sencer | 1 | Temps mig en milisegons que ha costat realitzar les peticions que no han donat error des de l’última arrancada de l’aplicació |
| peticionsOkUltimPeriode | long | 1 | Nombre total de peticions realitzades correctament des de la anterior consulta de salut |
| peticionsErrorUltimPeriode | long | 1 | Nombre total de peticions realitzades amb error des de la anterior consulta de salut |
| tempsMigUltimPeriode | sencer | 1 | Temps mig en milisegons que ha costat realitzar les peticions que no han donat error des de la anterior consulta de salut |

| MissatgeSalut |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| data | Date | 1 | Data en que s’ha donat d’alta el missatge a l’aplicació |
| nivell | SalutNivell | 1 | Enumerat amb el nivell d’alerta del missatge.  Possibles valors: \[INFO, WARN, ERROR\] |
| missatge | cadena(2048) | 0..1 | Contingut del missatge |

 

| DetallSalut |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena(10) | 1 | Codi, assignat per els desenvolupadors |
| nom | cadena(100) | 1 | Nom descriptiu |
| valor | cadena(1024) | 0..1 | Valor |

Els detalls seran principalment informació de l’estat del maquinari on es troba desplegada la aplicació, tot i que els desenvolupadors poden decidir afegir informació addicional.  
Els detalls que s’espera que s’enviin, tot i que no és obligatori son:

| Codi | Nom | Valor |
| :---- | :---- | :---- |
| PRC | Processadors | Número de nuclis del processador |
| SCPU | Càrrega del sistema | Percentatge de càrrega del sistema |
| MED | Memòria disponible | Memòria disponible, no utilitzada del sistema |
| MET | Memòria total | Memòria total del sistema |
| EDT | Espai total de disc | Mida total del disc on es s’executa l’aplicació |
| EDL | Espai lliure de disc | Mita total lliure del disc on es s’executa l’aplicació |
| SO | Sistema operatiu | Informació del sistema operatiu.*Ex. Linux 3.10.0-957.10.1.el7.x86\_64 (amd64)* |

4. # **Estadístiques** {#estadístiques}

Cada aplicació que es vulgui integrar amb el mòdul de Estadístiques de comanda haurà d’implementar 4 endpoints de tipus GET. Els noms 2 dels endpoinds son lliures, i els altres dos depenen dels anterior. En aquest document utilitzarem com a exemple /estadisticaInfo i /estadistica

* GET /estadistiquesInfo: retorna informació de les dimensions i dels indicadors estadístics que genera l’aplicació.  
* GET /estadistiques: retorna la informació estadística de l’aplicació del dia anterior (darreres estadístiques diàries generades).  
* GET /estadistiques/of/{dia}: retorna informació estadística d’un dia concret.  
* GET /estadistiques/from/{diaInici}/to/{diaFi}: retorna la informació estadística de múltiples dies, entre la data inici i la data fi.

  1. ## **Obtenció de informació estadística de l’aplicació** {#obtenció-de-informació-estadística-de-l’aplicació}

| Obtenció de informació de l’aplicació |  |
| ----- | :---- |
| **Adreça API** | Adreça lliure a decidir pels desenvolupadors de l’aplicació.   Ex. https://dev.caib.es/notibapi/interna/estadistiquesInfo |
| **Descripció** | Aquest servei retorna informació de les dimensions i dels indicadors estadístics que genera l’aplicació (AppInfo) |
| **Mètode** | GET |
| **Autenticació** | Actualment es requereix que el mètode no tingui autenticació. S’espera que en un futur s’utilitzi autenticació BASIC |
| **Paràmetres** | No accepta paràmetres |
| **Resposta** | EstadistiquesInfo |
| **Ex. de petició** | curl \--location 'https://dev.caib.es/notibapi/interna/estadistiquesInfo' |
| **Ex. de resposta** | {     "codi": "NOT",     "versio": "2.0.11",     "dimensions": \[         {             "codi": "ENT",             "nom": "Entitat",             "descripcio": "Codi de l'entitat a la que pertany la comunicació/notificació",             "valors": \[                 "GOIB",                 "LIMIT"             \]         },         {             "codi": "ORG",             "nom": "Organ Gestor",             "descripcio": "Organ gestor al que pertany la comunicació/notificació",             "valors": \[                 "A04003714",                 "A04003715"             \]         }     \],     "indicadors": \[         {             "codi": "PND",             "nom": "Pendent",             "descripcio": "La comunicació/notificació està pendent de ser registrada",             "format": "LONG"         },         {             "codi": "REG",             "nom": "Registrada",             "descripcio": "La comunicació/notificació ha estat registrada i està pendent de ser enviada al destinatari",             "format": "LONG"         },         {             "codi": "NOT\_ENV",             "nom": "Enviada",             "descripcio": "La comunicació/notificació ha estat enviada a Notific@",             "format": "LONG"         },         {             "codi": "NOT\_ACC",             "nom": "Acceptada",             "descripcio": "La comunicació/notificació ha estat acceptada pel destinatari al DEHú",             "format": "LONG"         }     \] } |

4. 

### **Model de dades** {#model-de-dades-2}

| AppInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena(16) | 1 | Codi (normalment de 3 lletres) assignat a l’aplicació. |
| versio | cadena(10) | 0..1 | Número de versió actual de l’aplicació |
| data | Date | 0..1 | Data de la versió |
| dimensions | List\<DimensioDesc\> | 0..N | Informació de les dimensions estadístiques (camps que es poden utilitzar per filtrar la informació estadística |
| indicadors | List\<IndicadorDesc\> | 0..N | Informació dels indicadors estadístics (camps amb les dades estadístiques) |

| DimensioDesc |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena(16) | 1 | Codi intern de la dimensió. |
| nom | cadena(64) | 1 | Nom de la dimensió. Serà en nom que es mostrarà a Comanda. |
| descripcio | cadena(1024) | 0..1 | Descripció de la dimensió. Serà el text d’ajuda o explicatiu de quin és el significat de la dimensió, que es mostrarà a Comanda |
| valors | List\<cadena\> | 0..N | Possibles valors que pot tenir la dimensió |

| IndicadorDesc |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena(16) | 1 | Codi intern de l’indicador. |
| nom | cadena(64) | 1 | Nom de l’indicador. Serà en nom que es mostrarà a Comanda. |
| descripcio | cadena(1024) | 0..1 | Descripció de l’indicador. Serà el text d’ajuda o explicatiu de quin és el significat de la dimensió, que es mostrarà a Comanda |
| format | Format | 0..1 | Format en que s’enviarà el valor de l’indicador. Sempre que sigui possible es recomanable que els valors dels indicadors siguin numèrics. |

2. ## **Obtenció de dades estadístiques** {#obtenció-de-dades-estadístiques}

Per a la obtenció de dades estadístiques s’implementaran 3 mètodes. El mètode principal que recupera la darrera informació estadística generada (dia anterior), i 2 mètodes addicionals, amb la forma URL\_OBTENCIO\_ESTADISTICA**/of/{dia}** i URL\_OBTENCIO\_ESTADISTICA**/from/{dia}/to{dia}**

| Obtenció de dades estadístiques |  |
| ----- | :---- |
| **Adreça API** | Adreça lliure a decidir pels desenvolupadors de l’aplicació.   Ex. https://dev.caib.es/notibapi/interna/estadistiques |
| **Descripció** | Aquest servei retorna les darreres dades estadístiques generades per l’aplicació.Aquestes dades seran les del darrer dia.Comanda les consultarà un vegada al dia. |
| **Mètode** | GET |
| **Autenticació** | Actualment es requereix que el mètode no tingui autenticació. S’espera que en un futur s’utilitzi autenticació BASIC |
| **Paràmetres** | No accepta paràmetres |
| **Resposta** | RegistresEstadistics |
| **Ex. de petició** | curl \--location 'https://dev.caib.es/notibapi/interna/estadistiques |
| **Ex. de resposta** | {     "temps": {         "data": 1761519600000,         "anualitat": 2025,         "trimestre": 3,         "mes": 10,         "setmana": 44,         "diaSetmana": "DL",         "dia": 27     },     "fets": \[         {             "dimensions": \[                 {                     "codi": "ENT",                     "valor": "GOIB"                 },                 {                     "codi": "ORG",                     "valor": "A04003714"                 }             \],             "fets": \[                 {                     "codi": "PND"                     "valor": 4.0,                 },                 {                     "codi": "REG"                     "valor": 4.0,                 },                 {                     "codi": "NOT\_ENV"                     "valor": 4.0,                 },                 {                     "codi": "NOT\_ACC"                     "valor": 1.0,                 }             \]         },         {             "dimensions": \[                 {                     "codi": "ENT"                     "valor": "GOIB",                 },                 {                     "valor": "A04003715",                     "codi": "ORG"                 }             \],             "fets":\[                 {                     "codi": "PND"                     "valor": 3.0,                 },                 {                     "codi": "REG"                     "valor": 3.0,                 },                 {                     "codi": "NOT\_ENV"                     "valor": 2.0,                 },                 {                     "codi": "NOT\_ACC"                     "valor": 0.0,                 }             \]         }     \] } |

5. \*\* Aquest mètode ha de contestar amb el mínim temps possible per no afectar al rendiment de comanda. Per aquest motiu es desaconsella que es generin les estadístiques al realitzar la consulta. És preferible que les dades estadístiques s’hagin generat prèviament a la consulta.

6. 

### **Model de dades** {#model-de-dades-3}

### 

| RegistresEstadistics |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| temps | Temps | 1 | Informació del dia al que pertanyen les estadístiques |
| fets | List\<RegistreEstadistic\> | 0..N | Conjunt de tots els fets (conjunt dimensions-indicadors) de la data indicada al camp temps. |

| RegistreEstadistic |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| dimensions | List\<Dimensio\> | 0..N | Els valors de les dimensions d’uns fets i data concrets |
| fets | List\<Fet\> | 0..N | Els valors dels fets (indicadors) donat una data i unes dimensions concretes. |

Dimensió i Fet són interfícies, que defineixen els mètodes que han d’implementar les classes amb la informació estadística:

| Dimensio (interfície) |  |  |
| ----- | :---- | :---- |
| Mètode | Tipus de retorn | Descripció |
| getCodi() | cadena(16) | Mètode que retorna el codi de la dimensió |
| getValor() | cadena(255) | Mètode que retorna el valor de la dimensió |

| Fet (interfície) |  |  |
| ----- | :---- | :---- |
| Mètode | Tipus de retorn | Descripció |
| getCodi() | cadena(16) | Mètode que retorna el codi de l’indicador |
| getValor() | cadena | Mètode que retorna el valor de l’indicador |

La llibreria ofereix unes implementacions de les interfícies Dimensio i Fet que es poden utilitzar si no es defineix una implementació pròpia:

| GenericDimensio |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena(16) | 1 | Codi de la dimensió |
| valor | cadena | 0..1 | Valor de la dimensió en format text |

| GenericFet |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena(16) | 1 | Codi de l’indicador |
| valor | cadena | 0..1 | Valor de l’indicador en format text |

| Temps |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| data | Date | 1 | Dia al que pertanyen les estadístiques |
| anualitat | sencer | 1 | Any de la data |
| trimestre | sencer | 1 | Trimestre de la data. Possibles valors  \[1..4\] |
| mes | sencer | 1 | Mes de la data. Possibles valors  \[1..12\] |
| setmana | sencer | 1 | Setmana de la data. Possibles valors  \[1..52\] |
| dia | sencer | 1 | dia de la data. Possibles valors  \[1..31\] |
| diaSetmana | DiaSetmanaEnum | 1 | Enumerat amb el dia de la setmana |

| DiaSetmanaEnum |  |
| ----- | :---- |
| Valor | Descripció |
| DL | Dilluns |
| DM | Dimarts |
| DC | Dimecres |
| DJ | Dijous |
| DV | Divendres |
| DS | Dissabte |
| DG | Diumenge |

Per obtenir l’objecte Temps amb tots els camps emplenats es pot utilitzar tant el constructor com el builder de la pròpia classe Temps:

| `Temps temps = new Temps(new Date()); Temps temps = Temps.builder().data(new Date()).build();` |
| :---- |

Per tal de poder recuperar dades estadístiques des del calendari de Comanda, serà necessari implementar els següents dos mètodes.

| Obtenció de dades estadístiques d’una data concreta |  |
| ----- | :---- |
| **Adreça API** | Adreça que depen de la adreça configurada per la consulta per la obtenció de les darreres dades estadístiques.   Ex. https://dev.caib.es/notibapi/interna/estadistiques/of/{dia} |
| **Descripció** | Aquest servei retorna les dades estadístiques generades per l’aplicació del dia indicat per paràmetre.Comanda pot realitzar la consulta des del calendari d’estadístiques. |
| **Mètode** | GET |
| **Autenticació** | Actualment es requereix que el mètode no tingui autenticació. S’espera que en un futur s’utilitzi autenticació BASIC |
| **Paràmetres** | **dia**: Data que es vol consultar amb format “dd-MM-yyyy”La data no ha de ser posterior al dia d’ahir, ja que no retornarà resultats, al no disposar encara d’estadístiques generades, o disposar únicament dades estadístiques parcials, si es consulta el dia d’avui. |
| **Resposta** | RegistresEstadistics |
| **Ex. de petició** | curl \--location 'https://dev.caib.es/notibapi/interna/estadistiques/of/23-10-2025 |

| Obtenció de dades estadístiques entre un rang de dates |  |
| ----- | :---- |
| **Adreça API** | Adreça que depen de la adreça configurada per la consulta per la obtenció de les darreres dades estadístiques.   Ex. https://dev.caib.es/notibapi/interna/estadistiques/from/{dataInici}/to /{dataFi} |
| **Descripció** | Aquest servei retorna les dades estadístiques generades per l’aplicació del rang de dies indicats per paràmetre.Comanda pot realitzar la consulta des del calendari d’estadístiques. |
| **Mètode** | GET |
| **Autenticació** | Actualment es requereix que el mètode no tingui autenticació. S’espera que en un futur s’utilitzi autenticació BASIC |
| **Paràmetres** | **dataInici**: Data d’inici del rang de dates que es vol consultar. **dataFi**: Data de fi del rang de dates que es vol consultar. Ambdues dates s’han de posar amb format “dd-MM-yyyy”La dataFi no ha de ser posterior al dia d’ahir, ja que no retornarà resultats, al no disposar encara d’estadístiques generades, o disposar únicament dades estadístiques parcials, si es consulta el dia d’avui. |
| **Resposta** | List\<RegistresEstadistics\> |
| **Ex. de petició** | curl \--location 'https://dev.caib.es/notibapi/interna/estadistiques/from/01-10-2025/to/23-10-2025 |
| **Ex. de resposta** | \[     {         “temps”: {...},         "fets": \[...\]     },     {         “temps”: {...},         "fets": \[...\]     } \] |

7. Aquests dos mètodes realment no són imprescindibles. Les integració amb Comanda funcionarà sense implementar aquests mètodes, però no serà possible recuperar dades estadístiques manualment des del calendari de Comanda.  
   La funcionalitat del calendari permet obtenir les dades estadístiques de dies en que ha fallat la consulta principal, o permet recuperar dades estadístiques antigues, prèvies a realitzar la integració amb Comanda. 

8. Per tant, és altament recomanable implementar els dos mètodes.

5. # **Tasques** {#tasques}

Les tasques representen ítems d’operativa o seguiment a Comanda associats a una entitat de la vostra aplicació. Els usuaris han de poder visualitzar les tasques a Comanda, amb l’estat en que es troben, i amb un enllaç cap a la vostra aplicació, per tal de poder tramitar la tasca, fins que aquesta disposi d’una data de fi, en que es deixa de mostrar a Comanda.  
Per a poder donar d’alta o modificar tasques a Comanda es disposa d’un endpoint de tipus POST:

* POST /comandaapi/interna/api/jms/tasques: crea o modifica una tasca a Comanda.

| Creació o modificació d’una tasca |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/api/jms/tasques   Ex. https:/dev.caib.es//comandaapi/interna/api/jms/tasques |
| **Descripció** | Aquest servei  crea o modifica una tasca a Comanda. |
| **Mètode** | POST |
| **Autenticació** | BASIC |
| **Paràmetres** | Tasca |
| **Resposta** | HTTP Status OK |
| **Ex. de petició** | curl \--location 'dev.caib.es/comandaapi/interna/api/cues/tasques' \\ \--header 'Content-Type: application/json' \\ \--header 'Authorization: Basic XXXXXXXXXXX==' \\ \--data '{   "appCodi": "NOT",   "entornCodi": "DEV",   "identificador": "31489824",   "tipus": "NOTIFICACIO",   "nom": "Nom de la notificacio",   "descripcio": "Descripcio de la notificació",   "estat": "INICIADA",   "estatDescripcio": "Enviada a Notifica",   "numeroExpedient": "1850488",   "prioritat": null,   "dataInici": "2025-08-28T16:08:28.295",   "dataFi": null,   "dataCaducitat": "2025-08-28T16:08:28.295",   "redireccio": "http://dev.caib.es/notiback/notificacio/31489821/enviament/31489824",   "responsable": "u000000",   "grup": null,   "usuarisAmbPermis": \["u000000", "u999000"\],   "grupsAmbPermis": \["ROL\_001", "ROL\_002"\] }' |

### 

### **Model de dades** {#model-de-dades-4}

### 

| Tasca |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| appCodi | cadena(16) | 1 | Codi (normalment de 3 lletres) assignat a l’aplicació. |
| entornCodi | cadena(16) | 1 | Codi assignat a l’entorn a Comanda |
| identificador | cadena(64) | 1 | Valor únic que identifica la tasca, i que Comanda utilitzarà per saber si ha de crear o actualitzar les dades de la tasca. Si Comanda detecta que ja existeix una tasca amb l’identificador, llavors procedeix a realitzar una actualització. En cas contrari crea una nova tasca. |
| tipus | cadena(64) | 1 | Tipus de tasca. Valor lliure a decidir a cada aplicació que s’integra amb comanda.  |
| nom | cadena(255) | 1 | Nom de la tasca |
| descripcio | cadena(1024) | 0..1 | Descripció de la tasca |
| estat | TascaEstat | 1 | Enumerat amb l’estat en què es troba la tasca |
| estatDescripcio | cadena(1024) | 0..1 | Descripció de l’estat en què es troba la tasca |
| numeroExpedient | cadena(128) | 0..1 | Número de l’expedient al que pertany la tasca |
| prioritat | Prioritat | 1 | Enumerat amb la prioritat de la tasca |
| dataInici | Date | 0..1 | Data en que s’ha iniciat la tasca |
| dataFi | Date | 0..1 | Data en que s’ha finalitzat la tasca. A Comanda, per defecte, al tenir data de fi la tasca es deixa de mostrar. |
| dataCaducitat | Date | 0..1 | Data de caducitat de la tasca. Data en que hauria d’estar finalitzada. |
| redireccio | URL | 1 | URL on es pot accedir per tramitar la tasca a l’aplicació origen |
| responsable | cadena(128) | 0..1 | Responsable o propietari de la tasca |
| grup | cadena(128) | 0..1 | Grup (rol) al que pertany la tasca. En el cas que una tasca requereixi que els usuaris que la puguin visualitzar hagin de pertànyer, sí o sí, a un rol concret. En cas que l’usuari no tingui aquest rol, no veurà la tasca a Comanda, independentment dels permisos que tingui. |
| usuarisAmbPermis | List\<cadena\> | 0..N | Codis dels usuaris que poden accedir a la tasca. |
| grupsAmbPermis | List\<cadena\> | 0..N | Rols que poden accedir a la tasca |

Els usuaris veuran la tasca a Comanda si el seu codi d’usuari es troba a la llista usuarisAmbPermis, o si algun dels seus rols es troba a la llista grupsAmbPermis. En el cas en que grup estigui informat, llavors, a més amés es comprovarà que l’usuari també tingui el rol informat al camp grup.

| TascaEstat |  |
| ----- | :---- |
| Valor | Descripció |
| PENDENT | Tasca pendent d’iniciar |
| INICIADA | Tasca iniciada |
| FINALITZADA | Tasca ja finalitzada |
| CANCELADA | Tasca cancel·lada |
| ERROR | Tasca en estat erroni |

| Prioritat |  |
| ----- | :---- |
| Valor | Descripció |
| NONE | Sense prioritat assignada |
| BAIXA | Baixa prioritat |
| NORMAL | Proritat normal |
| ALTA | Alta prioritat |
| MAXIMA | Màxima prioritat |

6. # **Avisos** {#avisos}

Els avisos són comunicacions informatives mostrades a Comanda. Els usuaris han de poder visualitzar tots els avisos a Comanda.  
Per a poder donar d’alta o modificar avisos a Comanda es disposa d’un endpoint de tipus POST:

* POST /comandaapi/interna/api/jms/avisos: crea o modifica un avís a Comanda.

| Creació o modificació d’una tasca |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/api/jms/avisos   Ex. https:/dev.caib.es//comandaapi/interna/api/jms/avisos |
| **Descripció** | Aquest servei  crea o modifica un avís a Comanda. |
| **Mètode** | POST |
| **Autenticació** | BASIC |
| **Paràmetres** | Avis |
| **Resposta** | HTTP Status OK |
| **Ex. de petició** | curl \--location 'dev.caib.es/comandaapi/interna/api/cues/avisos' \\ \--header 'Content-Type: application/json' \\ \--header 'Authorization: Basic XXXXXXXXXXX==' \\ \--data '{   "appCodi": "NOT",   "entornCodi": "DEV",   "identificador": "31489824",   "tipus": "INFO",   "nom": "Nom de l’avís",   "descripcio": "Descripcio de l’avís",   "dataInici": "2025-08-28T16:08:28.295",   "dataFi": null, }' |

### 

### **Model de dades** {#model-de-dades-5}

### 

| Avis |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| appCodi | cadena(16) | 1 | Codi (normalment de 3 lletres) assignat a l’aplicació. |
| entornCodi | cadena(16) | 1 | Codi assignat a l’entorn a Comanda |
| identificador | cadena(64) | 1 | Valor únic que identifica un avís, i que Comanda utilitzarà per saber si ha de crear o actualitzar les dades de l’avís. Si Comanda detecta que ja existeix un avís amb l’identificador, llavors procedeix a realitzar una actualització. En cas contrari crea un nou avís. |
| tipus | AvisTipus | 1 | Enumerat amb el tipus d’avís  |
| nom | cadena(255) | 1 | Nom de l’avís |
| descripcio | cadena(1024) | 0..1 | Descripció de l’avís |
| dataInici | Date | 0..1 | Data en que s’ha iniciat l’avís |
| dataFi | Date | 0..1 | Data en que s’ha finalitzat l’avís. A Comanda, per defecte, al tenir data de fi l’avís es deixa de mostrar. |
| redireccio | URL | 0..1 | URL on es pot accedir per consultar l’avís a l’aplicació origen |
| responsable | cadena(128) | 0..1 | Responsable o creador de l’avís |
| grup | cadena(128) | 0..1 | Grup (rol) al que pertany l’avís. En el cas que un avís requereixi que els usuaris que el puguin visualitzar hagin de pertànyer, sí o sí, a un rol concret. En cas que l’usuari no tingui aquest rol, no veurà l’avís a Comanda, independentment dels permisos que tingui. |
| usuarisAmbPermis | List\<cadena\> | 0..N | Codis dels usuaris que poden accedir a l’avís. |
| grupsAmbPermis | List\<cadena\> | 0..N | Rols que poden accedir a l’avís |

En el cas d’avisos globals, accessibles per tothom, es deixaran els camps grup, usuarisAmbPermis i grupsAmbPermis buits.  
El camp redirecció bomés s’emplenarà en el cas en que l’aplicació d’origen disposi d’una pàgina per visualitzar l’avís, i que aquest tingui més informació de l’enviada a comanda. En cas contrari es deixarà buit.

| AvisTipus |  |
| ----- | :---- |
| Valor | Descripció |
| NOTICIA | Avís que informa d’alguna notícia de l’aplicació, d’alguna novetat |
| INFO | Avís amb informació general de l’aplicació |
| ALERTA | Avís amb alguna advertència de l’aplicació |
| ERROR | Avís amb informació sobre algún error general de l’aplicació |
| CRITIC | Avís amb informació sobre algún error crític de l’aplicació |

7. # **Exemples** {#exemples}

Tot seguit es mostren uns exemples senzills d’enviament de tasques i avisos:

## **Enviar una tasca** {#enviar-una-tasca}

| `Tasca tasca = Tasca.builder()   .appCodi("APP")   .entornCodi("PRO")   .identificador("notif-987")   .tipus("NOTIFICA")   .nom("Notificació pendent")   .descripcio("Usuari ha d’accedir i signar")   .estat(TascaEstat.PENDENT)   .prioritat(Prioritat.ALTA)   .dataInici(LocalDateTime.now())   .redireccio(new URL("https://app.exemple.org/notificacio/987"))   .usuarisAmbPermis(List.of("USR1"))   .grupsAmbPermis(List.of("ROL_OPERADOR"))   .build();` |
| :---- |

POST {COMANDA\_API\_URL}/api/cues/tasques body: tasca

## **Enviar un avís** {#enviar-un-avís}

| `Avis avis = Avis.builder()   .appCodi("APP")   .identificador("12345")   .tipus(AvisTipus.INFO)   .nom("Manteniment")   .descripcio("Talls diumenge 8:00-10:00")   .dataInici(LocalDateTime.now())   .dataFi(LocalDateTime.now().plusHours(2))   .build();` |
| :---- |

POST {COMANDA\_URL}/api/cues/avisos body: avis

Amb aquests elements, qualsevol aplicació pot integrar-se amb Comanda per a Salut, Estadístiques, Tasques i Avisos adaptant els serveis propis per a obtenir les dades i mapant-les als models de comanda-lib.

[image1]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASQAAABnCAYAAACpQj+aAAA6d0lEQVR4Xu3dB7xdVbE/cN+zF/RJERWxowJKV0DfE3xWwEYTERFFUbAXLIAa8aFiAWkiKIIIUqQ3FQkJRDoJAZJAEkJuQkjvIT33Zv7nu/lP3Nmcc0tuyjXZ8/nsz71nn73XmjVr5rdm1pq1ztOipppqqqmP0NOqN2qqqaaa1hXVgFRTTTX1GaoBqaaaauozVANSTTXV1GeoBqSaaqqpz1ANSDXVVFOfoRqQaqqppj5DNSDVVFNNfYZqQKqpppr6DHUTkJbHsqXzY+704TF9wqCY8dgta+2aNemuWDR/YoOF9ipTNdVU03pGXQPS8uUxe+rQuOevn4ib/7xtDLx4x8a181q7Bly0Qwy6bM8YN/z8aF+2qMpdTTXVtB5Rl4C0ZNGsuH/Al+KWS3aPsQ/8Lqa03RRTxvVfa9fjo6+KIf2/0ACnXWLm5Huq7NVUU03rEXUJSPNmjozbr3pfDL35yzF33py47cHxMW32/EYE1R6Lhj8Us6+4NmZffk0sHnZNLJ9+bXEtHHxNcc/1xK1Xr7jvmdlXPnl/3o3XRPvEJ+8vG9f4/Lcn78+97ppYMvra6PDOjOujY8GomPTo9Q3vbLsY++DvquzVVFNN6xF1CUhzpw+L2658Twz/53Exfurc+Gi/y+NLp90YCxctjQWDh8aojxwaQ1+za8w4dadoH7pVtN+3VUz+4Vtj6OveFkNf/bYYd/iuT95vXNNP2THuf9Ouxf1RH9g1ltyydXF/wbXbxsPvemtxf/jb3hZzzt8u2oe8Idrv3z6WT704pk+4teEhvTVG33dqlb2W1NHREYsXL44nnniiuJYsWdKIPpdXH+sTtGzZsoLXpUuXFp/xiV/32tt7P3emvFmzZsWjjz4aCxcurH7dK8IfPvFL5kg93/jGN+If//hH8b36tc1zra7V0c6+QvPmzYuxY8fG3Llzq1/V1AX1CJDGTZ0T7z3m4njWvifF0Wf3j8mTZ8Wsq2+I4bvvHdNPflN0DNk42gdvEpOOeXMMfsl2MXiThlfzybcU910zTtoq7nvF9sX9h/fYLhbfvFlxf8HVW8SIXd9c3H9w2+1izrmvjY57N4mO+17ZAKQLegxI06dPj0svvTS+8pWvxHvf+97Ya6+94tvf/nZcd911hZL0JWACRtdcc018+ctfjtNPP70wzsmTJ8cJJ5wQX/va1+Kmm27qNb8M5JBDDokXvvCFRbnqXB0ERO688844+uij45hjjolx48YVvJ588snx9Kc/PY488siYP39+IfPf/va3RRtbXbfcckuv29kXiKy/+93vxkYbbVTonPbX1H1aJUB65r6/ihceeEr0O//WmPz49Jh58eUx58I9+wQgTZo0KT772c/GZpttFv/xH/8R//mf/1n8db3yla+M//u//4uZM2dWX1tnxLP43ve+F0972tMK8OTNPfTQQ7HtttvGs5/97PjFL37Ra0PlHe29996FkfBc1Lk6iNdz3nnnFUC3+eabx9133114ST/96U/juc99bvzsZz8r6tIn++yzT9HGVtepp566wsP6d6YZM2bE5z73uULWn//852POnDnVR2rqhFYZkJ7+0V/Flp86M0654p5YMH9hLBt/cXTcv/U6BSTexY9+9KN4xjOeUYzQb37zm+Nb3/pWMQJvueWWxf2PfOQjMWHChOqr64zWBiDxZO64444466yzYuTIkdWvV5maAZJ7P/zhD2P33XePBx98sOC9DEjbbLNNYajl64gjjoj+/fv3up19gYCqdp999tkxdOjQ9QJk1yb1CpCe0bi2OvysuPCmB2PJ4idi+eSzo33oG9cZII0YMSLe/va3F4q/ww47FOHOokWLCrf573//e+EdjBo1aoXi+27IkCFx/fXXF2GT8GP27Nkryps6dWpxj6HdeOONcfHFF8fNN98cU6ZMWfEMhZs4cWIMHDiwCBOFhcOHDy/AMQkgjB8/PgYMGBBXX3113H777YWXlnNF3QUkZSr7b3/7W9xwww0r1SMMe/jhh+O2226L++67Ly666KKiLuDrO7LxnbmNpAULFhTtN9ejXWTT1VyO9ioTgGgvwH/+85+/ApDUpSxXemJlQOK9CmuqVz6rrbwKfF177bVFW7VTX+X3ZKdf9Bl5e7YcGpnD0lYgrIxLLrkk/vnPfz7FMzafNnjw4KIMzymzPO8jdHbvrrvuir/+9a9F/ytTe9zXfjzorwyD/R09enRR/yOPPLJCnmStD8iaLmqTezWtTD0CpPENQPrAcZc0AOmkApAKUGqA02sboHTZLQ9F+5KZ0fHYiTHp2J3WOiBRVIr1ile8ogjPvv71rz8FFChgGjel79evX7z0pS+NF7zgBYVR/dd//Vcc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>

[image2]: <data:image/png;base64,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>