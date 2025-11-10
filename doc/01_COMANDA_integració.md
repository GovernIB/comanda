  
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

[**5\. Tasques	23**](#tasques)

[**6\. Avisos	24**](#avisos)

[**9\. Exemple	26**](#exemples)

# 

# **Objecte** {#objecte}

En aquest manual es detalla com integrar una aplicació amb Comanda per generar i enviar a Comanda la informació de Salut i Estadístiques de l’aplicació, així com com Tasques i Avisos. 

# **Requisits previs** {#requisits-previs}

És aconsellable disposar de la llibreria de models de Comanda al classpath (**comanda-lib**).  
Si s’utilitza Maven es pot afegir la següent dependència (utilitzant la darrera versió disponible):

```xml
<dependency>
  <groupId>es.caib.comanda</groupId>
  <artifactId>comanda-lib</artifactId>
  <version>0.1.1</version>
</dependency>
```


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

# **Salut** {#salut}

Cada aplicació que es vulgui integrar amb el mòdul de Salut de comanda haurà d’implementar 2 endpoints de tipus GET. Els noms dels endpoinds son lliures, tot i que en aquest document utilitzarem com a exemple /appInfo i /salut

* GET /appInfo: retorna informació d’aplicació (AppInfo) per a la pantalla de Salut de Comanda.  
* GET /salut: retorna informació de salut de l’aplicació (SalutInfo).

  ## **Obtenció de informació de l’aplicació** {#obtenció-de-informació-de-l’aplicació}

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



### **Model de dades** {#model-de-dades}

| AppInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena | 1 | Codi (normalment de 3 lletres) assignat a l’aplicació. |
| nom | cadena | 1 | Nom de l’aplicació |
| versio | cadena | 1 | Número de versió actual de l’aplicació |
| data | Date | 0..1 | Data de la versió (es podria utilitzar la data en que s’ha pujat la versió al repositori, o la data en que ha estat compilada) |
| revisio | cadena | 0..1 | Número de la revisió en el repositori (Número o codi del commit de la versió desplegada) Ex.![][image2] |
| jdkVersion | cadena | 0..1 | Versió de Java amb la que està compilada l'aplicació |
| integracions | List\<IntegracioInfo\> | 0..N | Informació de les integracions de l’aplicació (aplicacions externes amb les que es comunica) |
| subsistemes | List\<SubsistemaInfo\> | 0..N | Informació dels subsitemes de l’aplicació.Entenem com a subsistema qualsevol part de l’aplicació amb prou entitat o importància com per controlar si està funcionant correctament o no.Ex. En una aplicació de gestió d’expedients: Alta d’expedients Tramitació de tasca Adjunció de documents … \*\* Només s’han d’emplenar els camps **codi** i **nom** en la informació de subsistemes |
| contexts | List\<ContextInfo\> | 0..N | Informació dels diferents contexts que es publiquen al desplegar l’aplicació.Habitualment ens trobarem 3 contexts: backoffice, api interna i api externa. |

| IntegracioInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena | 1 | Codi de la integració. |
| nom | cadena | 1 | Nom de la integració |

Per emplenar la informació de la integració es pot utilitzar l’enumerat es.caib.comanda.ms.salut.model.IntegracioApp, que és un llistat de integracions, amb el codi i el nom.

Si s’utilitza aquest enumerat per informar d’una integració es pot utilitzar el builder:

```java 
IntegracioInfo integracioInfo = IntegracioInfo.builder().integracioApp(IntegracioApp.COD).build();
```


| SubsistemaInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena | 1 | Codi del subsistema. |
| nom | cadena | 1 | Nom del subsistema |

| ContextInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena | 1 | Codi del context. Valor lliure a decidir pels desenvolupadors de l’aplicació. |
| nom | cadena | 1 | Nom del context. Valor lliure a decidir pels desenvolupadors de l’aplicació. |
| path | cadena | 1 | Url on es pot accedir al context |
| manuals | List\<Manual\> | 0..1 | Informació dels manuals de l’aplicació |
| api | cadena | 0..1 | Url on es pot accedir a la informació swagger de la API del context |

En cas de utilitzar els contexts de backoffice, api interna i api externa es recomana utilitzar com a codi i nom els següents valors (per manetenir la coherència entre les deferents aplicacions):

| Codi | Nom |
| :---- | :---- |
| BACK | Backoffice |
| INT | API interna |
| EXT | API externa |

| Manual |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| nom | cadena | 1 | Nom del manual |
| path | cadena | 1 | URL on es pot accedir al manual |

## **Obtenció de informació de salut** {#obtenció-de-informació-de-salut}

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
\*\* Aquest mètode ha de contestar amb el mínim temps possible per no afectar al rendiment de comanda. Per aquest motiu es desaconsella que al consultar les dades de les integracions es facin peticions a les aplicacions integrades. La informació de salut s’hauria d’obtenir únicament amb dades de la pròpia aplicació.

 

### **Model de dades** {#model-de-dades-1}

| SalutInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena | 1 | Codi (normalment de 3 lletres) assignat a l’aplicació. |
| versio | cadena | 1 | Número de versió actual de l’aplicació |
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

```java 
EstatSalutEnum estat = EstatByPercent.calculaEstat(percentatgeDeErrors); 
EstatSalutEnum estatAplicacio = EstatByPercent.mergeEstats(estatApp, estatSubsistemes);
```

| IntegracioSalut |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena | 1 | Codi de la integració. Serà el mateix codi informat en AppInfo. |
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
| endpoint | cadena | 0..1 | Adreça on es realitzen les peticions |
| peticionsPerEntorn | Map\<cadena, IntegracioPeticions | 0..N | Detall de les peticions en el cas en que hi hagi múltiples instàncies d’aquesta |

En el cas en que hi hagi múltiples instàncies d’una mateixa integració (per exemple, diferents configuracions de la integració per diferents entitats o òrgans), en el camp peticionsPerEntorn es detallaran les peticions realitzades a cada una de les instàncies de la integració. En el cas en que no hi hagi múltiples instàncies, el camp es deixarà buid.

En el cas de múltiples instàncies, la clau del mapa serà un codi identificatiu de la instància (per exemple, el codi de l’entitat, si hi ha diferents configuracions per entitat), i la IntegracioPeticios serà el detall de les peticions únicament per a la instància concreta.

Exemple:

*Suposem que tenim una integració amb el Registre, configurada de manera diferent per dues entitats: GOIB i SALUT, podriem tenir com a IntegracioPeticions:*

```json
{
    "estat": "UP",
    "latencia": 0,
    "codi": "REG",
    "peticions": {
        "totalOk": 34, 
        "totalError": 3,
        "totalTempsMig": 267,
        "peticionsOkUltimPeriode": 7,
        "peticionsErrorUltimPeriode": 1,
        "tempsMigUltimPeriode": 275,
        "peticionsPerEntorn": {
            “GOIB” : {
                "codi": "REG",
                "peticions": {
                    "totalOk": 28,
                    "totalError": 0,
                    "totalTempsMig": 242,
                    "peticionsOkUltimPeriode": 5,
                    "peticionsErrorUltimPeriode": 0,
                    "tempsMigUltimPeriode": 234,
                    "endpoint": "https://dev.caib.es/regweb3"
                }
            },
            “SALUT”: {
                "codi": "REG",
                "peticions": {
                    "totalOk": 6,
                    "totalError": 3,
                    "totalTempsMig": 385,
                    "peticionsOkUltimPeriode": 2,
                    "peticionsErrorUltimPeriode": 1,
                    "tempsMigUltimPeriode": 377,
                    "endpoint": "https://dev.salut.es/registre"
                }
            }
        }
    } 
}
```

*El bloc GOIB conté les mètriques de les peticions dirigides cap al registre de l’entitat GOIB, incloent també l’adreça on es troba el registre de l’entitat GOIB.*

*El bloc SALUT conté les mètriques de les peticions dirigides cap al registre de l’entitat SALUT, incloent també l’adreça on es troba el registre de l’entitat SALUT.*

*El bloc del principi inclou les mètriques totals de la integració de registre. Així. per exemple, si es sumen el número de peticions de cada entitat, ens donarà el valor informat en aquest bloc inicial de totals.*

| SubsistemaSalut |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena | 1 | Codi del subsistema. Serà el mateix codi informat en AppInfo. |
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
| missatge | cadena | 0..1 | Contingut del missatge |

 

| DetallSalut |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena | 1 | Codi, assignat per els desenvolupadors |
| nom | cadena | 1 | Nom descriptiu |
| valor | cadena | 0..1 | Valor |

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

# **Estadístiques** {#estadístiques}

Cada aplicació que es vulgui integrar amb el mòdul de Estadístiques de comanda haurà d’implementar 4 endpoints de tipus GET. Els noms 2 dels endpoinds son lliures, i els altres dos depenen dels anterior. En aquest document utilitzarem com a exemple /estadisticaInfo i /estadistica

* GET /estadistiquesInfo: retorna informació de les dimensions i dels indicadors estadístics que genera l’aplicació.  
* GET /estadistiques: retorna la informació estadística de l’aplicació del dia anterior (darreres estadístiques diàries generades).  
* GET /estadistiques/of/{dia}: retorna informació estadística d’un dia concret.  
* GET /estadistiques/from/{diaInici}/to/{diaFi}: retorna la informació estadística de múltiples dies, entre la data inici i la data fi.

  ## **Obtenció de informació estadística de l’aplicació** {#obtenció-de-informació-estadística-de-l’aplicació}

| Obtenció de informació de l’aplicació |  |
| ----- | :---- |
| **Adreça API** | Adreça lliure a decidir pels desenvolupadors de l’aplicació.   Ex. https://dev.caib.es/notibapi/interna/estadistiquesInfo |
| **Descripció** | Aquest servei retorna informació de les dimensions i dels indicadors estadístics que genera l’aplicació (AppInfo) |
| **Mètode** | GET |
| **Autenticació** | Actualment es requereix que el mètode no tingui autenticació. S’espera que en un futur s’utilitzi autenticació BASIC |
| **Paràmetres** | No accepta paràmetres |
| **Resposta** | EstadistiquesInfo |
| **Ex. de petició** | curl \--location 'https://dev.caib.es/notibapi/interna/estadistiquesInfo' |
| **Ex. de resposta** | {     "codi": "NOT",     "versio": "2.0.11",     "dimensions": \[         {             "codi": "ENT",             "nom": "Entitat",             "descripcio": "Codi de l'entitat a la que pertany la comunicació/notificació",             "valors": \[                 "GOIB",                 "LIMIT"             \]         },         {             "codi": "ORG",             "nom": "Organ Gestor",             "descripcio": "Organ gestor al que pertany la comunicació/notificació",             "valors": \[                 "A04003003",                 "A04003714",                 "A04003715"             \]         }     \],     "indicadors": \[         {             "codi": "PND",             "nom": "Pendent",             "descripcio": "La comunicació/notificació està pendent de ser registrada",             "format": "LONG"         },         {             "codi": "REG",             "nom": "Registrada",             "descripcio": "La comunicació/notificació ha estat registrada i està pendent de ser enviada al destinatari",             "format": "LONG"         },         {             "codi": "NOT\_ENV",             "nom": "Enviada",             "descripcio": "La comunicació/notificació ha estat enviada a Notific@",             "format": "LONG"         },         {             "codi": "NOT\_ACC",             "nom": "Acceptada",             "descripcio": "La comunicació/notificació ha estat acceptada pel destinatari al DEHú",             "format": "LONG"         }     \] } |



### **Model de dades** {#model-de-dades-2}

| AppInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena | 1 | Codi (normalment de 3 lletres) assignat a l’aplicació. |
| versio | cadena | 0..1 | Número de versió actual de l’aplicació |
| data | Date | 0..1 | Data de la versió |
| dimensions | List\<DimensioDesc\> | 0..N | Informació de les dimensions estadístiques (camps que es poden utilitzar per filtrar la informació estadística |
| indicadors | List\<IndicadorDesc\> | 0..N | Informació dels indicadors estadístics (camps amb les dades estadístiques) |

| DimensioDesc |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena | 1 | Codi intern de la dimensió. |
| nom | cadena | 1 | Nom de la dimensió. Serà en nom que es mostrarà a Comanda. |
| descripcio | cadena | 0..1 | Descripció de la dimensió. Serà el text d’ajuda o explicatiu de quin és el significat de la dimensió, que es mostrarà a Comanda |
| valors | List\<cadena\> | 0..N | Possibles valors que pot tenir la dimensió |

| IndicadorDesc |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena | 1 | Codi intern de l’indicador. |
| nom | cadena | 1 | Nom de l’indicador. Serà en nom que es mostrarà a Comanda. |
| descripcio | cadena | 0..1 | Descripció de l’indicador. Serà el text d’ajuda o explicatiu de quin és el significat de la dimensió, que es mostrarà a Comanda |
| format | Format | 0..1 | Format en que s’enviarà el valor de l’indicador. Sempre que sigui possible es recomanable que els valors dels indicadors siguin numèrics. |

## **Obtenció de dades estadístiques** {#obtenció-de-dades-estadístiques}

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

 \*\* Aquest mètode ha de contestar amb el mínim temps possible per no afectar al rendiment de comanda. Per aquest motiu es desaconsella que es generin les estadístiques al realitzar la consulta. És preferible que les dades estadístiques s’hagin generat prèviament a la consulta.



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
| getCodi() | cadena | Mètode que retorna el codi de la dimensió |
| getValor() | cadena | Mètode que retorna el valor de la dimensió |

| Fet (interfície) |  |  |
| ----- | :---- | :---- |
| Mètode | Tipus de retorn | Descripció |
| getCodi() | cadena | Mètode que retorna el codi de l’indicador |
| getValor() | cadena | Mètode que retorna el valor de l’indicador |

La llibreria ofereix unes implementacions de les interfícies Dimensio i Fet que es poden utilitzar si no es defineix una implementació pròpia:

| GenericDimensio |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena | 1 | Codi de la dimensió |
| valor | cadena | 0..1 | Valor de la dimensió en format text |

| GenericFet |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| codi | cadena | 1 | Codi de l’indicador |
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

```java
Temps temps = new Temps(new Date()); 
Temps temps = Temps.builder().data(new Date()).build();
```

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

Aquests dos mètodes realment no són imprescindibles. Les integració amb Comanda funcionarà sense implementar aquests mètodes, però no serà possible recuperar dades estadístiques manualment des del calendari de Comanda.  
   La funcionalitat del calendari permet obtenir les dades estadístiques de dies en que ha fallat la consulta principal, o permet recuperar dades estadístiques antigues, prèvies a realitzar la integració amb Comanda. 

Per tant, és altament recomanable implementar els dos mètodes.

# **Tasques** {#tasques}

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

### **Model de dades**

### 

| Tasca |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| appCodi | cadena(16) | 1 | Codi (normalment de 3 lletres) assignat a l’aplicació. |
| entornCodi | cadena | 1 | Codi assignat a l’entorn a Comanda |
| identificador | cadena(64) | 1 | Valor únic que identifica la tasca, i que Comanda utilitzarà per saber si ha de crear o actualitzar les dades de la tasca. Si Comanda detecta que ja existeix una tasca amb l’identificador, llavors procedeix a realitzar una actualització. En cas contrari crea una nova tasca. |
| tipus | cadena(64) | 1 | Tipus de tasca. Valor lliure a decidir a cada aplicació que s’integra amb comanda.  |
| nom | cadena(255) | 1 | Nom de la tasca |
| descripcio | cadena(1024) | 0..1 | Descripció de la tasca |
| estat | TascaEstat | 1 | Enumerat amb l’estat en què es troba la tasca |
| estatDescripcio | cadena(1024) | 0..1 | Descripció de l’estat en què es troba la tasca |
| numeroExpedient | cadena(64) | 0..1 | Número de l’expedient al que pertany la tasca |
| prioritat | Prioritat | 1 | Enumerat amb la prioritat de la tasca |
| dataInici | Date | 0..1 | Data en que s’ha iniciat la tasca |
| dataFi | Date | 0..1 | Data en que s’ha finalitzat la tasca. A Comanda, per defecte, al tenir data de fi la tasca es deixa de mostrar. |
| dataCaducitat | Date | 0..1 | Data de caducitat de la tasca. Data en que hauria d’estar finalitzada. |
| redireccio | URL | 1 | URL on es pot accedir per tramitar la tasca a l’aplicació origen |
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

### **Model de dades**

### 

| Avis |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| appCodi | cadena(16) | 1 | Codi (normalment de 3 lletres) assignat a l’aplicació. |
| entornCodi | cadena | 1 | Codi assignat a l’entorn a Comanda |
| identificador | cadena(64) | 1 | Valor únic que identifica un avís, i que Comanda utilitzarà per saber si ha de crear o actualitzar les dades de l’avís. Si Comanda detecta que ja existeix un avís amb l’identificador, llavors procedeix a realitzar una actualització. En cas contrari crea un nou avís. |
| tipus | AvisTipus | 1 | Enumerat amb el tipus d’avís  |
| nom | cadena(255) | 1 | Nom de l’avís |
| descripcio | cadena(1024) | 0..1 | Descripció de l’avís |
| dataInici | Date | 0..1 | Data en que s’ha iniciat l’avís |
| dataFi | Date | 0..1 | Data en que s’ha finalitzat l’avís. A Comanda, per defecte, al tenir data de fi l’avís es deixa de mostrar. |

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

## **Enviar una tasca**

```java
Tasca tasca = Tasca.builder()   
        .appCodi("APP")
        .entornCodi("PRO")
        .identificador("notif-987")
        .tipus("NOTIFICA")
        .nom("Notificació pendent")
        .descripcio("Usuari ha d’accedir i signar")   
        .estat(TascaEstat.PENDENT)
        .prioritat(Prioritat.ALTA)
        .dataInici(LocalDateTime.now())
        .redireccio(new URL("https://app.exemple.org/notificacio/987")) 
        .usuarisAmbPermis(List.of("USR1")) 
        .grupsAmbPermis(List.of("ROL_OPERADOR"))
        .build();
```

POST {COMANDA\_API\_URL}/api/cues/tasques body: tasca

## **Enviar un avís**

```java
Avis avis = Avis.builder()  
        .appCodi("APP") 
        .identificador("12345") 
        .tipus(AvisTipus.INFO) 
        .nom("Manteniment") 
        .descripcio("Talls diumenge 8:00-10:00") 
        .dataInici(LocalDateTime.now()) 
        .dataFi(LocalDateTime.now().plusHours(2))
        .build();
```
POST {COMANDA\_URL}/api/cues/avisos body: avis

Amb aquests elements, qualsevol aplicació pot integrar-se amb Comanda per a Salut, Estadístiques, Tasques i Avisos adaptant els serveis propis per a obtenir les dades i mapant-les als models de comanda-lib.  
