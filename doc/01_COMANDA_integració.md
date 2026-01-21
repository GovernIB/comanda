  
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

[**1\. Objecte	5**](#objecte)

[**2\. Requisits previs	5**](#requisits-previs)

[2.1. Clients	5](#clients)

[**3\. Salut	9**](#salut)

[3.1. Obtenció de informació de l’aplicació	9](#obtenció-de-informació-de-l’aplicació)

[Model de dades	11](#model-de-dades)

[3.2. Obtenció de informació de salut	13](#obtenció-de-informació-de-salut)

[Model de dades	15](#model-de-dades-1)

[Helpers	21](#helpers)

[Model de dades internes	25](#model-de-dades-internes)

[Exemples d’ús	26](#exemples-d’ús)

[**4\. Estadístiques	26**](#estadístiques)

[4.1. Obtenció de informació estadística de l’aplicació	27](#obtenció-de-informació-estadística-de-l’aplicació)

[Model de dades	28](#model-de-dades-2)

[4.2. Obtenció de dades estadístiques	29](#obtenció-de-dades-estadístiques)

[Model de dades	31](#model-de-dades-3)

[Helpers	34](#helpers-1)

[Exemples d’ús	36](#exemples-d’ús-1)

[**5\. Logs	38**](#logs)

[Model de dades	40](#model-de-dades-4)

[Helpers	40](#helpers-2)

[Exemples d’ús	43](#exemples-d’ús-2)

[**6\. Servers	44**](#servers)

[6.1. Salut	44](#salut-1)

[6.2. Estadistica	45](#estadistica)

[6.3. Log	47](#log)

[**7\. Tasques	49**](#tasques)

[Model de dades	55](#model-de-dades-5)

[**8\. Avisos	57**](#avisos)

[Model de dades	61](#model-de-dades-6)

[**9\. Exemples	63**](#exemples)

[Enviar una tasca	63](#enviar-una-tasca)

[Enviar un avís	63](#enviar-un-avís)

[**Annex 1 \- Spring-filter	65**](#annex-1---spring-filter)

[Camp filter – Sintaxi bàsica	65](#camp-filter-–-sintaxi-bàsica)

[Format general	65](#format-general)

[Operadors disponibles	65](#operadors-disponibles)

[Comparació	65](#comparació)

[Text	65](#text)

[Operadors lògics	66](#operadors-lògics)

[Agrupació amb parèntesis	66](#agrupació-amb-parèntesis)

[Valors admesos	66](#valors-admesos)

[Tipus bàsics	66](#tipus-bàsics)

[Accés a camps anidats	67](#accés-a-camps-anidats)

[Operador in	67](#operador-in)

[Exemples complets	67](#exemples-complets)

[Notes importants	67](#notes-importants)  

# 

1. # **Objecte** {#objecte}

En aquest manual es detalla com integrar una aplicació amb Comanda per generar i enviar a Comanda la informació de Salut i Estadístiques de l’aplicació, així com com Tasques i Avisos. 

2. # **Requisits previs** {#requisits-previs}

Comanda ofereix tot una sèrie de clients per a que les aplicacions puguin interactuar amb Comanda, així com serveis autogenerats d’exemple, que es poden utilitzar per a implementar la API que les aplicacions han d’exposar per a que hi accedeixi comanda.

1. ## **Clients** {#clients}

S’han desenvolupat una sèrie de clients, que utilitzen RestEasy per poder enviar dades de tasques, avisos i permisos a Comanda.

Aquests clients es poden afegir com a dependències Maven:

|     `<dependency>         <groupId>es.caib.comanda</groupId>         <artifactId>comanda-api-client-tasca-v1</artifactId>         <version>0.1.0</version>     </dependency>     <dependency>         <groupId>es.caib.comanda</groupId>         <artifactId>comanda-api-client-avis-v1</artifactId>         <version>0.1.0</version>     </dependency>     <dependency>         <groupId>es.caib.comanda</groupId>         <artifactId>comanda-api-client-permis-v1</artifactId>         <version>0.1.0</version>     </dependency>` |
| :---- |

Aquests clients tenen una dependència al mòdul de model de dades:

|     `<dependency>         <groupId>es.caib.comanda</groupId>         <artifactId>comanda-api-models</artifactId>         <version>0.1.1</version>     </dependency>` |
| :---- |

A més, comanda disposa d’un altre mòdul ofereix una sèrie de mètodes d’ajuda per emplenar la informació de salut o estadística. Aquest mòdul és:

|     `<dependency>         <groupId>es.caib.comanda</groupId>         <artifactId>comanda-api-utils</artifactId>         <version>0.1.1</version>     </dependency>` |
| :---- |

Per simplificar, s’ha creat un mòdul (**comanda-lib**) que ja carrega tots els anteriors, i a més implementa una classe de client que simplifica la crida a les diferents operacions dels clients:

|     `<dependency>         <groupId>es.caib.comanda</groupId>         <artifactId>comanda-lib</artifactId>         <version>0.1.0.1</version>     </dependency>` |
| :---- |

Aquestes llibreries inclouen dependències a les llibreries de RestEasy versió 4.7.6.Final. En cas que el seu projecte ja inclogui les dependències, encara que sigui amb una versió inferior (3.x), es poden excloure les dependències, per evitar conflictes.  
Aquesta llibreria (comanda-lib) té una dependència cap a comanda-api-models, que inclou els models de dades que Comanda espera rebre per a les integracions, com:

* Salut:   
  * es.caib.comanda.model.v1.salut.AppInfo  
  * es.caib.comanda.model.v1.salut.SalutInfo  
* Estadístiques:   
  * es.caib.comanda.model.v1.estadistica.DimensioDesc  
  * es.caib.comanda.model.v1.estadistica.IndicadorDesc  
  * es.caib.comanda.model.v1.estadistica.RegistresEstadistics  
  * es.caib.comanda.model.v1.estadistica.EstadistiquesInfo  
* Logs:  
  * es.caib.comanda.model.v1.log.FitxerInfo  
  * es.caib.comanda.model.v1.log.FitxerContingut  
* Tasques:   
  * es.caib.comanda.model.v1.tasca.Tasca  
  * es.caib.comanda.model.v1.tasca.TascaPage  
* Avisos:  
  * es.caib.comanda.model.v1.avis.Avis  
  * es.caib.comanda.model.v1.avis.AvisPage

També inclou un client **ComandaClient**, que implementa els següents mètodes:

**Constructors**

* ComandaClient(String basePath, String user, String password)  
* ComandaClient(Builder b)   
  Si es vol utilitzar el builder, en aquest s’han d’informar 2 camps:  
  * basePath: ruta de la api interna de comanda (PATH/comandapai/interna)  
  * authorization: es correspon a ‘user \+ ":" \+ password’ en base64

**Tasques**

* String **crearTasca**(Tasca tasca)  
* Tasca **consultarTasca**(String identificador, String appCodi, String entornCodi)  
* String **crearMultiplesTasques**(List\<Tasca\> tasques)  
* String **modificarTasca**(String identificador, Tasca tasca)  
* String **modificarMultiplesTasques**(List\<Tasca\> tasques)  
* TascaPage **obtenirLlistatTasques**(String quickFilter, String filter, String page, Integer size)  
  * quickFilter: text que Comanda utilitza per filtrar per el camp nom  
  * filter: filtre en format spring-filter (veure Annex 1\) 


**Avisos**

* String **crearAvis**(Avis avis)  
* Avis **consultarAvis**(String identificador, String appCodi, String entornCodi)  
* String **crearMultiplesAvisos**(List\<Avis\> avisos)  
* String **modificarAvis**(String identificador, Avis avis)  
* String **modificarMultiplesAvisos**(List\<Avis\> avisos)  
* AvisPage **obtenirLlistatAvisos**(String quickFilter, String filter, String page, Integer size)  
  * quickFilter: text que Comanda utilitza per filtrar per el camp nom  
  * filter: filtre en format spring-filter (veure Annex 1\)


**Permisos**

* String **crearPermis**(Permis permis)  
* Permis **consultarPermis**(String identificador, String appCodi, String entornCodi)  
* String **crearMultiplesPermisos**(List\<Permis\> permisos)  
* String **eliminarPermisos**(List\<Permis\> permisos)  
* String **modificarPermis**(String identificador, Permis permis)  
* String **modificarMultiplesPermisos**(List\<Permis\> permisos)

Per utilitzar el client en Java es pot fer amb el següent codi:

| String base \= ClientProps.*get*(*PROP\_BASE*).orElse(null); String user \= ClientProps.*get*(*PROP\_USER*).orElse(null); String pwd \= ClientProps.*get*(*PROP\_PWD*).orElse(null);  ComandaClient client \=new ComandaClient(base, user, pwd); |
| :---- |

Si es vol donar d’alta una tasca o un avís utilizant el client, es pot fer com en els següents exemples:

| var task \= getTask(taskId); var redireccio \= appBaseUrl \+ "/task/"\+task.getId();  var tasca \=  Tasca.*builder*()    .appCodi("APP")    .entornCodi("DEV")    .identificador(task.getId())    .tipus(task.getTipus())    .nom(task.getNom())    .descripcio(task.getDescription())    .dataInici(task.getDataInici())    .dataFi(task.getDataFi())    .dataCaducitat(task.getDataCaducitat())    .estat(TascaEstat.*PENDENT*)    .numeroExpedient(task.getExpedient().getNum())    .responsable(task.getResponsable())    .usuarisAmbPermis(getUsuarisAmbPermis(task))    .grupsAmbPermis(getRolsAmbPermis(task))    .redireccio(new URL(redireccio))    .grup(task.getGrupCodi())    .build(); *client*.crearTasca(tasca); |
| :---- |
|  |
| var warn \= getAvis(avisId); Avis avis \= Avis.*builder*()        .appCodi("APP")        .entornCodi("DEV")        .identificador(warn.getId())        .tipus(AvisTipus.*INFO*)        .nom(warn.getNom())        .descripcio(warn.getMissatge())        .dataInici(warn.getDataInici())        .dataFi(warn.getDataFinal())        .build();  *client*.crearAvis(avis); |

La documentació en format OpenApi de la API és accessible a:

* [https://\[SERVER\_PATH\]/comandaapi/interna/api-docs](https://[SERVER_PATH]/comandaapi/interna/api-docs)

Hi ha un total de 6 serveis REST, que es poden seleccionar des del desplegable que es pot trobar a la capçalera de la documentació:

* Salut  
* Estadístiques  
* Logs  
* Tasques  
* Avisos  
* Permisos (Actualment encara no és funcional)


A sota del títol es troba l’enllaç a l’especificació en format JSON.  
Els serveis REST que les aplicacions també han d’oferir (salut, estadístiques i logs), disposen s’un enllaç cap a un servei s’exemple autogenerat, i que utilitza RestEasy. 

![][image2]

Les especificacions en format JSON dels clients es poden descarregar accedint directament a les següents adreces:

* [http://](http://localhost:8080/comandaapi/interna/api-docs/docs/tasques-v1)[\[SERVER\_PATH\]](https://[SERVER_PATH]/comandaapi/interna/api-docs)[/comandaapi/interna/api-docs/docs/tasques-v1](http://localhost:8080/comandaapi/interna/api-docs/docs/tasques-v1)  
* [http://](http://localhost:8080/comandaapi/interna/api-docs/docs/avisos-v1)[\[SERVER\_PATH\]](https://[SERVER_PATH]/comandaapi/interna/api-docs)[/comandaapi/interna/api-docs/docs/avisos-v1](http://localhost:8080/comandaapi/interna/api-docs/docs/avisos-v1)  
* [http://](http://localhost:8080/comandaapi/interna/api-docs/docs/permisos-v1)[\[SERVER\_PATH\]](https://[SERVER_PATH]/comandaapi/interna/api-docs)[/comandaapi/interna/api-docs/docs/permisos-v1](http://localhost:8080/comandaapi/interna/api-docs/docs/permisos-v1)  
* [http://](http://localhost:8080/comandaapi/interna/api-docs/docs/salut-v1)[\[SERVER\_PATH\]](https://[SERVER_PATH]/comandaapi/interna/api-docs)[/comandaapi/interna/api-docs/docs/salut-v1](http://localhost:8080/comandaapi/interna/api-docs/docs/salut-v1)  
* [http://](http://localhost:8080/comandaapi/interna/api-docs/docs/estadistiques-v1)[\[SERVER\_PATH\]](https://[SERVER_PATH]/comandaapi/interna/api-docs)[/comandaapi/interna/api-docs/docs/estadistiques-v1](http://localhost:8080/comandaapi/interna/api-docs/docs/estadistiques-v1)  
* [http://](http://localhost:8080/comandaapi/interna/api-docs/docs/logs-v1)[\[SERVER\_PATH\]](https://[SERVER_PATH]/comandaapi/interna/api-docs)[/comandaapi/interna/api-docs/docs/logs-v1](http://localhost:8080/comandaapi/interna/api-docs/docs/logs-v1)

3. # **Salut** {#salut}

Cada aplicació que es vulgui integrar amb el mòdul de Salut de comanda haurà d’implementar 2 endpoints de tipus GET:

* GET /salut/info: retorna informació d’aplicació (AppInfo) per a la pantalla de Salut de Comanda.  
* GET /salut: retorna informació de salut de l’aplicació (SalutInfo).

  1. ## **Obtenció de informació de l’aplicació** {#obtenció-de-informació-de-l’aplicació}

| Obtenció de informació de l’aplicació |  |
| ----- | :---- |
| **Adreça API** | \[PATH\]/salut/info  Ex. https://dev.caib.es/notibapi/interna/salut/info |
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
| revisio | cadena(64) | 0..1 | Número de la revisió en el repositori (Número o codi del commit de la versió desplegada) Ex.![][image3] |
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
| **Adreça API** | \[PATH\]/salut/ Ex. https://dev.caib.es/notibapi/interna/salut |
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
| estatGlobal | EstatSalut | 1 | Estat en que es troba l’aplicació i el temps que tarda a contestar (latència) |
| estatBaseDeDades | EstatSalut | 0..1 | Estat en que es troba la BBDD i el temps que tarda a contestar (latència) |
| integracions | List\<IntegracioSalut\> | 0..N | Estat en que es troba cada integració, i Informació de les peticions realitzades mitjançant la integració. |
| subsistemes | List\<SubsistemaSalut\> | 0..N | Estat en que es troba cada subsistema, i Informació de les peticions realitzades mitjançant la integració. |
| missatges | List\<MissatgeSalut\> | 0..N | Missatges rellevants que es mostrin a l’aplicació. *Exemples de missatges poden ser: Es realitzarà un manteniment de l’aplicació el proper dilluns entre les 9:00 i les 12:00 Entorn únicament per a proves dels desenvolupadors* En molts casos aquests missatges seran avisos dins l’aplicació, tot i que decidir quins missatges informar en en mòdul de salut i en el mòdul d’avisos queda en mans dels desenvolupadors de cada aplicació. |
| informacioSistema | InformacioSistema | 0..1 | Altra informació d’interés del sistema (CPU, Memòria, …) |

Per emplenar la informació del sistema (camp informacioSistema), es recomana utilitzar el mètodes de la classe es.caib.comanda.ms.salut.model.MonitorHelper:

* getInfoSistema: obté les mètriques principals del sistema.

| `SalutInfo salutInfo = SalutInfo.builder()                 .codi("APP")                 .versio(versio)                 .data(OffsetDateTime.now())                 .estatGlobal(estatSalut)                 .estatBaseDeDades(salutDatabase)                 .integracions(integracions)                 .subsistemes(subsistemes)                 .missatges(missatges)                 .informacioSistema(MonitorHelper.getInfoSistema())                 .build();` |
| :---- |

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

Per calcular l’estat d’un sistema (aplicació, BBDD, integració o subsistema), si es disposa del percentatge de fallades, per tal que totes les aplicacions apliquin els mateixos percentatges per calcular l’estat, es recomana utilitzar els mètodes de la classe es.caib.comanda.ms.salut.model.EstatHelper:

* calculaEstat: calcula d’estat a partir d’un valor amb el percentatge d’errades.  
* mergeEstats: calcula l’estat a partir de dos estats. Retorna l’estat pitjor dels dos.

| `EstatSalutEnum estat = EstatByPercent.calculaEstat(peticionsOk, peticionsError); EstatSalutEnum estat = EstatByPercent.calculaEstat(percentatgeDeErrors); EstatSalutEnum estatAplicacio = EstatByPercent.mergeEstats(estatApp, estatSubsistemes);` |
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
| peticionsPerEntorn | Map\<cadena, IntegracioPeticions\> | 0..N | Detall de les peticions en el cas en que hi hagi múltiples instàncies d’aquesta. La clau del Map permet un màxim de 32 caràcters. En cas de rebre una quantitat superior a la permesa, Comanda retallarà automàticament la clau rebuda. |

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

## **Helpers** {#helpers}

La llibreria de comanda, al mòdul comanda-api-utils, ofereix unes classes d’ajuda per a la obtenció de informació de salut: 

* es.caib.comanda.ms.salut.helper.EstatHelper  
* es.caib.comanda.ms.salut.helper.MonitorHelper

La classe **EstatHelper** proporciona funcionalitats de suport per al càlcul de l’estat de les integracions i subsistemes. 

A continuació es detallen els mètodes estàtics d’ajuda:

| calculaEstat |  |
| ----- | :---- |
| **Mètode** | EstatSalutEnum **calculaEstat**(long execucionsOk, long execucionsError) |
| **Descripció** | Calcula l’estat de salut d’un servei a partir del nombre d’execucions correctes i errònies. |
| **Comportament** | Suma execucions OK i ERROR per obtenir el total. Calcula el percentatge d’errors amb arrodoniment. Crida el mètode calculaEstat(double) amb el percentatge calculat |
| **Paràmetres** | **execucionsOk**: Nombre d’execucions correctes**execucionsError**:  Nombre d’execucions amb error |
| **Resposta** | EstatSalutEnum |
| **Ús principal** | Determinar l’estat de salut d’un servei en temps real a partir de mètriques d’execució |

| calculaEstat |  |
| ----- | :---- |
| **Mètode** | EstatSalutEnum **calculaEstat**(double percentatgeErrors) |
| **Descripció** | Classifica l’estat de salut segons el percentatge d’errors |
| **Comportament** |  Aplica llindars definits com a constants: \>= 100% → DOWN \> 50% → ERROR \> 20% → DEGRADED \<= 10% → UP entre 10% i 20% → WARN |
| **Paràmetres** | **percentatgeErrors**: Percentatge d’errors calculat |
| **Resposta** | EstatSalutEnum |
| **Ús principal** | Classificació directa de l’estat de salut a partir d’un valor percentual |

| mergeEstats |  |
| ----- | :---- |
| **Mètode** | EstatSalutEnum **mergeEstats**(EstatSalutEnum estat1, EstatSalutEnum estat2) |
| **Descripció** | Fusiona dos estats de salut i retorna el pitjor dels dos |
| **Comportament** | Prioritza els estats segons gravetat: Si algún és DOWN, retorna DOWN. Si algún és ERROR, retorna ERROR. Si algún és DEGRADED, retorna DEGRADED. Si algún és WARN, retorna WARN. Si algun és UP, retorna UP Si cap coincideix, retorna UNKNOWN. |
| **Paràmetres** | **estat1**: Primer estat a comparar **estat2**: Segon estat a comparar |
| **Resposta** | EstatSalutEnum |
| **Ús principal** | Agrupar estats de diversos serveis i obtenir una visió agregada del seu comportament |

La classe **MonitorHelper** proporciona un conjunt complet de funcionalitats per obtenir informació de salut i rendiment del sistema, incloent memòria, CPU, disc, JVM i servidor d’aplicacions. S’utilitza per construir la resposta del servei /salut i per generar objectes InformacioSistema.

A continuació es detallen els mètodes estàtics d’ajuda:

| getInfoSistema |  |
| ----- | :---- |
| **Mètode** | InformacioSistema **getInfoSistema**() |
| **Descripció** | Construeix un objecte InformacioSistema amb totes les mètriques principals del sistema |
| **Comportament** | Obté CPU, memòria, disc i informació del sistema. Converteix valors a formats llegibles (MB, %, etc.). Retorna un InformacioSistema. |
| **Resposta** | InformacioSistema |
| **Ús principal** | Emplenar camp informacioSistema de l’objecte retornat pel servei /salut consumit per COMANDA |

| getJvmMemory |  |
| ----- | :---- |
| **Mètode** | MemoryUsage **getJvmMemory**() |
| **Descripció** | Retorna l’ús de memòria de la JVM |
| **Comportament** | Obté memòria total, lliure i usada de la JVM (Runtime). Construeix un objecte MemoryUsage. En cas d’error, retorna null. |
| **Resposta** | MemoryUsage |
| **Ús principal** | Monitoritzar consum de memòria de l’aplicació. |

| getPhisicalMemory |  |
| ----- | :---- |
| **Mètode** | MemoryUsage **getPhisicalMemory**() |
| **Descripció** | Retorna l’ús de memòria física del sistema operatiu |
| **Comportament** | Utilitza reflexió per accedir a mètodes no estàndard de OperatingSystemMXBean. Calcula memòria total, lliure i usada. Retorna un MemoryUsage. |
| **Resposta** | MemoryUsage |
| **Ús principal** | Obtenir informació real del sistema, independent de la JVM |

| getCpuUsage |  |
| ----- | :---- |
| **Mètode** | CpuUsage **getCpuUsage**() |
| **Descripció** | Retorna informació sobre l’ús de CPU del sistema i del procés |
| **Comportament** | Obté nombre de cores. Obté loadAverage. Obté systemCpuLoad i processCpuLoad via reflexió. Retorna un objecte CpuUsage. |
| **Resposta** | CpuUsage |
| **Ús principal** | Monitoritzar càrrega del sistema i consum de CPU de l’aplicació |

| getRootDiskUsage |  |
| ----- | :---- |
| **Mètode** | DiskUsage **getRootDiskUsage**() |
| **Descripció** | Retorna l’ús del disc arrel / |
| **Comportament** | Calcula espai total, lliure i utilitzat. Retorna un DiskUsage. |
| **Resposta** | DiskUsage |
| **Ús principal** | Monitoritzar espai de disc del sistema |

| getDisksUsage |  |
| ----- | :---- |
| **Mètode** | List\<DiskUsage\> **getDisksUsage**() |
| **Descripció** | Retorna l’ús de tots els discs del sistema |
| **Comportament** | Itera per totes les arrels (File.listRoots()). Calcula espai total, lliure i utilitzat per cada disc. Retorna una llista de DiskUsage. |
| **Resposta** | List\<DiskUsage\> |
| **Ús principal** | Monitoritzar múltiples volums de disc. |

| getSystemInfo |  |
| ----- | :---- |
| **Mètode** | SystemInfo **getSystemInfo**() |
| **Descripció** | Retorna informació general del sistema operatiu i la JVM |
| **Comportament** | Obté nom i versió del SO. Obté versió del JDK i JVM. Obté data d’arrencada i temps en funcionament. Retorna un SystemInfo. |
| **Resposta** | SystemInfo |
| **Ús principal** | Mostrar informació d’entorn i diagnòstic |

| getJvmInfo |  |
| ----- | :---- |
| **Mètode** | JvmInfo **getJvmInfo**() |
| **Descripció** | Retorna informació interna de la JVM |
| **Comportament** | Obté nombre de threads, pics i threads daemon. Suma comptadors de GC i temps de GC. Retorna un JvmInfo. |
| **Resposta** | JvmInfo |
| **Ús principal** | Diagnòstic avançat de rendiment de la JVM |

| getApplicationServerInfo |  |
| ----- | :---- |
| **Mètode** | String **getApplicationServerInfo**() |
| **Descripció** | Detecta el servidor d’aplicacions on s’executa l’aplicació |
| **Comportament** | Comprova propietats del sistema per identificar: JBoss / EAP WebSphere WebLogic GlassFish Tomcat Spring Boot Retorna "Desconegut" si no es pot determinar. |
| **Resposta** | String |
| **Ús principal** | Mostrar informació d’entorn en diagnòstics. |

### **Model de dades internes** {#model-de-dades-internes}

| MemoryUsage |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| totalMemory | Long | 0..1 | Memòria total |
| freeMemory | Long | 0..1 | Memòria  lliure |
| usedMemory | Long | 0..1 | Memòria usada. |

Aquest model disposa de mètodes per retornar les dades en formats humans (Mb, Gb) i en percentatges.

| DiskUsage |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| nom | Cadena | 1 | Nom del disc |
| totalSpace | Long | 0..1 | Espai total |
| freeSpace | Long | 0..1 | Espai  lliure |
| usedSpace | Long | 0..1 | Espai usat. |

Aquest model disposa de mètodes per retornar les dades en formats humans (Mb, Gb).

| CpuUsage |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| cores | Integer | 0..1 | Nuclis del processador |
| loadAverage | Double | 0..1 | Mitjana de càrrega del sistema. Representa el nombre mitjà de processos en execució o esperant CPU. Un valor proper o superior al nombre de cores indica saturació del sistema. |
| systemCpuLoad | Double | 0..1 | Percentatge d’ús de CPU del sistema |
| processCpuLoad | Double | 0..1 | Percentatge de CPU utilitzat exclusivament pel procés de l’aplicació |

Aquest model disposa de mètodes per retornar les dades en percentatges.

| SystemInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| os | Cadena | 1 | Nom i versió del sistema operatiu on s’executa l’aplicació |
| jdkVersion | Cadena | 0..1 | Versió del JDK utilitzat per executar l’aplicació |
| jvm | Cadena | 0..1 | Identificador de la JVM, incloent el proveïdor i la versió |
| startTime | Date | 0..1 | Data i hora d’arrencada de la JVM |
| upTime | Long | 0..1 | Temps total que la JVM porta en funcionament des de l’arrencada. S’expressa en dies, hores, minuts i segons. |

Aquest model disposa de mètodes per retornar les dades en formats llegibles.

| JvmInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| threadCount | Integer | 0..1 | Nombre actual de threads actius dins de la JVM. Inclou tant threads de l’aplicació com threads interns del sistema i de la pròpia JVM. |
| peakThreadCount | Integer | 0..1 | Nombre màxim de threads que han estat actius simultàniament |
| daemonThreadCount | Integer | 0..1 | Nombre de threads daemon actius |
| gcCount | Long | 0..1 | Nombre total de col·leccions de Garbage Collector realitzades per tots els GC de la JVM |
| gcTime | Long | 0..1 | Temps total (en mil·lisegons) dedicat pel Garbage Collector |

### **Exemples d’ús** {#exemples-d’ús}

Calcular estats

| EstatSalutEnum estat \= EstatHelper.calculaEstat(peticionsOk, peticionsError); EstatSalutEnum estat \= EstatHelper.*calculaEstat*(errorRatePct); |
| :---- |

Obtenir informació del sistema per Comanda

| SalutInfo salutInfo \= SalutInfo.*builder*()        .codi("APP")        .versio(versio)        .data(OffsetDateTime.*now*())        .estatGlobal(estatSalut)        .estatBaseDeDades(salutDatabase)        .integracions(integracions)        .subsistemes(subsistemes)        .missatges(missatges)        .informacioSistema(MonitorHelper.*getInfoSistema*())        .build(); |
| :---- |

4. # **Estadístiques** {#estadístiques}

Cada aplicació que es vulgui integrar amb el mòdul de Estadístiques de comanda haurà d’implementar 4 endpoints de tipus GET. 

* GET /estadistiques/info: retorna informació de les dimensions i dels indicadors estadístics que genera l’aplicació.  
* GET /estadistiques: retorna la informació estadística de l’aplicació del dia anterior (darreres estadístiques diàries generades).  
* GET /estadistiques/of/{dia}: retorna informació estadística d’un dia concret.  
* GET /estadistiques/from/{diaInici}/to/{diaFi}: retorna la informació estadística de múltiples dies, entre la data inici i la data fi.

  1. ## **Obtenció de informació estadística de l’aplicació** {#obtenció-de-informació-estadística-de-l’aplicació}

| Obtenció de informació de l’aplicació |  |
| ----- | :---- |
| **Adreça API** | \[PATH\]/estadistiques/info Ex. https://dev.caib.es/notibapi/interna/estadistiques/info |
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
| **Adreça API** | \[PATH\]/estadistiques  Ex. https://dev.caib.es/notibapi/interna/estadistiques |
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
| **Adreça API** | \[PATH\]/estadistiques/of/{dia} Ex. https://dev.caib.es/notibapi/interna/estadistiques/of/{dia} |
| **Descripció** | Aquest servei retorna les dades estadístiques generades per l’aplicació del dia indicat per paràmetre.Comanda pot realitzar la consulta des del calendari d’estadístiques. |
| **Mètode** | GET |
| **Autenticació** | Actualment es requereix que el mètode no tingui autenticació. S’espera que en un futur s’utilitzi autenticació BASIC |
| **Paràmetres** | **dia**: Data que es vol consultar amb format “dd-MM-yyyy”La data no ha de ser posterior al dia d’ahir, ja que no retornarà resultats, al no disposar encara d’estadístiques generades, o disposar únicament dades estadístiques parcials, si es consulta el dia d’avui. |
| **Resposta** | RegistresEstadistics |
| **Ex. de petició** | curl \--location 'https://dev.caib.es/notibapi/interna/estadistiques/of/23-10-2025 |

| Obtenció de dades estadístiques entre un rang de dates |  |
| ----- | :---- |
| **Adreça API** | \[PATH\]/estadistiques/from/{dataInici}/to/{dataFi} Ex. https://dev.caib.es/notibapi/interna/estadistiques/from/{dataInici}/to /{dataFi} |
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

   ## **Helpers** {#helpers-1}

La llibreria de comanda, al mòdul comanda-api-utils, ofereix una classe d’ajuda per a la obtenció de estadísiques: 

* es.caib.comanda.ms.estadistica.helper.EstadisticaHelper

La classe EstadisticaHelper proporciona funcionalitats de suport per a la conversió d’estadístiques. A continuació es detallen els mètodes estàtics d’ajuda:

| toRegistreEstadistic |  |
| ----- | :---- |
| **Mètode** | \<E\> RegistreEstadistic **toRegistreEstadistic**( E entity, Function\<E, Map\<String, String\>\> dimensionsMapper, Function\<E, Map\<String, ? extends Number\>\> fetsMapper) |
| **Descripció** | Construeix un objecte RegistreEstadistic a partir d'una entitat genèrica, aplicant funcions extractores per obtenir dimensions i fets. |
| **Comportament** | Aplica les funcions dimensionsMapper i fetsMapper sobre l'entitat. Construeix una llista de Dimensio i Fet a partir dels mapes obtinguts. Elimina claus buides i valors nuls. Retorna un RegistreEstadistic amb les dimensions i fets construïts. |
| **Paràmetres** | **entity**: Entitat d'origen amb informació estadística**dimensionsMapper**:  Funció per extreure dimentsions a partir de l’entitat d’origen **fetsMapper**: Funció per extreure fets, a partir de l’entitat d’origen |
| **Resposta** | RegistreEstadistic |
| **Ús principal** | Transformar entitats de domini en registres estadístics homogenis per a COMANDA |

| toRegistreEstadistic |  |
| ----- | :---- |
| **Mètode** | \<E\> List\<RegistreEstadistic\> **toRegistreEstadistic**( List\<E\> entities, Function\<E, Map\<String, String\>\> dimensionsMapper, Function\<E, Map\<String, ? extends Number\>\> fetsMapper) |
| **Descripció** | Construeix una llista de RegistreEstadistic a partir d'una col·lecció d'entitats genèriques |
| **Comportament** | Aplica el mètode toRegistreEstadistic a cada entitat de la llista Retorna una llista immutable amb els resultats. |
| **Paràmetres** | **entities**: Llista d’entitats d'origen amb informació estadística**dimensionsMapper**:  Funció per extreure dimentsions a partir de l’entitat d’origen **fetsMapper**: Funció per extreure fets, a partir de l’entitat d’origen |
| **Resposta** | List\<RegistreEstadistic\> |
| **Ús principal** | Transformar lots d’entitats de domini en registres estadístics homogenis per a COMANDA |

| toRegistreEstadistic |  |
| ----- | :---- |
| **Mètode** | RegistreEstadistic **toRegistreEstadistic**(Collection\<Dimensio\> dimensions, Collection\<Fet\> fets) |
| **Descripció** | Construeix un RegistreEstadistic a partir de col·leccions ja preparades de dimensions i fets |
| **Comportament** | Crea còpies de les col·leccions rebudes Elimina elements nuls. Retorna un RegistreEstadistic amb les llistes netes |
| **Paràmetres** | **dimensions**:  Col·lecció de dimensions**fets**: Col·lecció de fets |
| **Resposta** | RegistreEstadistic |
| **Ús principal** | Construir registres estadístics a partir de dades ja estructurades |

| toRegistreEstadistic |  |
| ----- | :---- |
| **Mètode** | RegistreEstadistic **toRegistreEstadistic**(Map\<String, String\> dimensions, Map\<String, ? extends Number\> fets) |
| **Descripció** | Construeix un RegistreEstadistic a partir de mapes de dimensions i fets |
| **Paràmetres** | **dimensions**: Mapa de dimensions**fets**: Mapa de fets numèrics |
| **Resposta** | RegistreEstadistic |
| **Ús principal** | Generar registres estadístics quan les dades ja estan en format mapa |

### **Exemples d’ús** {#exemples-d’ús-1}

Exemples convertint entitats a RegistreEstadistic:

| *// Clase d’exemple amb dades estadístiques, que es poden separar amb indicadors i fets* private static class DummyEntity {    String app;     String canal;     String tipus;     Integer visites;     Long errors;     Double temps;    DummyEntity(          String app,           String canal,           String tipus,           Integer visites,           Long errors,           Double temps) {        this.app \= app;         this.canal \= canal;         this.tipus \= tipus;         this.visites \= visites;         this.errors \= errors;         this.temps \= temps;    } }  *// Exemple d’entitat amb dades estadístiques* DummyEntity e1 \= new DummyEntity("APP", "WEB", "CONSULTA", 10, 2L, 3.5); DummyEntity e2 \= new DummyEntity("APP", "WEB", "CREACIO", 6, 1L, 7.2); DummyEntity e3 \= new DummyEntity("APP", "REST", "CONSULTA", 23, 3L, 3.7); List\<DummyEntity\> entities \= Arrays.*asList*(e1, e2, e3); *// Funció per mapejar les dimensions* Function\<DummyEntity, Map\<String, String\>\> dims \= ent \-\> {    LinkedHashMap\<String, String\> m \= new LinkedHashMap\<\>();    m.put("aplicacio", ent.app);    m.put("canal", ent.canal);    m.put("tipus", ent.tipus);    return m; }; *// Funció per mapejar els fets* Function\<DummyEntity, Map\<String, ? extends Number\>\> fets \= ent \-\> {    LinkedHashMap\<String, Number\> m \= new LinkedHashMap\<\>();    m.put("visites", ent.visites);    m.put("errors", ent.errors);    m.put("temps", ent.temps);    return m; }; *// Convertim una única entitat* RegistreEstadistic re \= EstadisticaHelper.*toRegistreEstadistic*(e1, dims, fets); *// Convertim múltiples entitats* List\<RegistreEstadistic\> lre \= EstadisticaHelper.*toRegistreEstadistic*(entities, dims, fets);  |
| :---- |

Exemple amb dimensions i fets com a mapa:

| LinkedHashMap\<String, String\> dims \= new LinkedHashMap\<\>(); dims.put("a", "1"); dims.put("b", "2"); LinkedHashMap\<String, Number\> fets \= new LinkedHashMap\<\>(); fets.put("i", 1); *// Integer* fets.put("l", 2L); *// Long* fets.put("bd", new BigDecimal("3.25")); *// BigDecimal → double* RegistreEstadistic re \= EstadisticaHelper.*toRegistreEstadistic*(dims, fets); |
| :---- |

Exemple amb dimentions i fets com a colecció

| List\<Dimensio\> dims \= new ArrayList\<\>(Arrays.*asList*(        GenericDimensio.*builder*().codi("x").valor("X").build(),        GenericDimensio.*builder*().codi("y").valor("Y").build() )); List\<Fet\> fets \= new ArrayList\<\>(Arrays.*asList*(        GenericFet.*builder*().codi("a").valor(7.0).build(),        GenericFet.*builder*().codi("b").valor(2.4).build() )); RegistreEstadistic re1 \= EstadisticaHelper.*toRegistreEstadistic*(dims, fets); |
| :---- |

5. # **Logs** {#logs}

Cada aplicació que vulgui fer accessibles els logs de l’aplicació a Comanda haurà d’implementar 3 endpoints de tipus GET:

* GET /logs: retorna una llista amb tots els fitxers de log disponibles a l’aplicació.  
* GET /logs/{nomFitxer}: Retorna el contingut i detalls del fitxer de log.  
* GET /logs/{nomFitxer}/linies/{nLinies}: Retorna les darreres nLinies del fitxer de log indicat per nom.

| Obtenir el llistat de fitxers de log disponibles |  |
| ----- | :---- |
| **Adreça API** | \[PATH\]/logs Ex. https://dev.caib.es/notibapi/interna/logs |
| **Descripció** | Aquest servei retorna una llista amb tots els fitxers que es troben dins la carpeta de logs del servidor de l'aplicació. |
| **Mètode** | GET |
| **Autenticació** | Autenticació BASIC. |
| **Paràmetres** | **dia**: Data que es vol consultar amb format “dd-MM-yyyy”La data no ha de ser posterior al dia d’ahir, ja que no retornarà resultats, al no disposar encara d’estadístiques generades, o disposar únicament dades estadístiques parcials, si es consulta el dia d’avui. |
| **Resposta** | List\<FitxerInfo\> |
| **Ex. de petició** | curl \--location 'https://dev.caib.es/notibapi/interna/v1/logs |
| **Ex. de resposta** | \[     {         "nom": "es.caib.notib.log.2025-10-22.gz",         "mida": 282296,         "mimeType": "application/gzip",         "dataCreacio": "22/10/2025 00:00:00",         "dataModificacio": "22/10/2025 23:59:57"     },     {         "nom": "server.log.2025-11-23.gz",         "mida": 407139,         "mimeType": "application/gzip",         "dataCreacio": "23/11/2025 00:00:00",         "dataModificacio": "23/11/2025 23:55:01"     },     {         "nom": "server.log",         "mida": 29278009,         "mimeType": "text/plain",         "dataCreacio": "24/11/2025 00:00:00",         "dataModificacio": "24/11/2025 14:26:43"     } \] |

| Obtenir contingut complet d'un fitxer de log |  |
| ----- | :---- |
| **Adreça API** | \[PATH\]/logs/{nomFitxer} Ex. https://dev.caib.es/notibapi/interna/logs/{nomFitxer} |
| **Descripció** | Aquest servei retorna el contingut i detalls del fitxer de log que es troba dins la carpeta de logs del servidor, i que té el nom indicat |
| **Mètode** | GET |
| **Autenticació** | Autenticació BASIC. |
| **Paràmetres** | **nomFitxer**: Nom del fitxer que es vol recuperar |
| **Resposta** | FitxerContingut |
| **Ex. de petició** | curl \--location 'https://dev.caib.es/notibapi/interna/logs/server.log |
| **Ex. de resposta** | {     "nom": "server.log",     "mida": 29401469,     "mimeType": "text/x-log",     "dataCreacio": "15/01/2026",     "dataModificacio": "15/01/2026",     "contingut": "MjAyNi0wMS0xNSAwMDowMDowMCwwMTEgRVJST1IgW29… } |

| Obtenir les darreres línies d'un fitxer de log |  |
| ----- | :---- |
| **Adreça API** | \[PATH\]/logs/{nomFitxer}/linies/{nLinies} Ex. https://dev.caib.es/notibapi/interna/logs/{nomFitxer}/linies/{nLinies} |
| **Descripció** | Aquest servei retorna les darreres linies del fitxer de log indicat per nom. Concretament es retorna el número de línies indicat al paràmetre nLinies |
| **Mètode** | GET |
| **Autenticació** | Autenticació BASIC. |
| **Paràmetres** | **nomFitxer**: Nom del fitxer del que es volen recuperar les darreres línies**nLinies**: Número de línies a recuperar |
| **Resposta** | List\<String\> |
| **Ex. de petició** | curl \--location 'https://dev.caib.es/notibapi/interna/logs/server.log/linies/50 |

### 

### **Model de dades** {#model-de-dades-4}

| FitxerInfo |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| nom | cadena | 1 | Nom del fitxer |
| mida | long | 1 | Mida del fitxer en bytes |
| mimeType | cadena | 1 | Tipus MIME del fitxer |
| dataCreacio | cadena | 0..1 | Data de creació del fitxer en format dd/MM/yyyy HH:mm:ss |
| dataModificacio | cadena | 0..1 | Data de modificació del fitxer en format dd/MM/yyyy HH:mm:ss |

| FitxerContingut |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| nom | cadena | 1 | Nom del fitxer |
| mida | long | 1 | Mida del fitxer en bytes |
| mimeType | cadena | 1 | Tipus MIME del fitxer |
| dataCreacio | cadena | 0..1 | Data de creació del fitxer en format dd/MM/yyyy HH:mm:ss |
| dataModificacio | cadena | 0..1 | Data de modificació del fitxer en format dd/MM/yyyy HH:mm:ss |
| contingut | byte\[\] | 1 | Contingut del fitxer en format binari codificat en base64 |

## **Helpers** {#helpers-2}

La llibreria de comanda, al mòdul comanda-api-utils, ofereix una classe d’ajuda per a la obtenció de logs: 

* es.caib.comanda.ms.log.helper.LogHelper

La classe LogHelper proporciona funcionalitats de suport per a la gestió i consulta de fitxers de log dins de l’aplicació. Inclou operacions de llistat, lectura, compressió i detecció de tipus MIME, així com la lectura eficient de les darreres línies d’un fitxer gran.

A continuació es detallen els mètodes estàtics d’ajuda:

| llistarFitxers |  |
| ----- | :---- |
| **Mètode** | List\<FitxerInfo\> **llistarFitxers**(String directoriPath, String appNom) |
| **Descripció** | Retorna la llista de fitxers de log existents dins un directori, filtrats pel nom de l’aplicació. |
| **Comportament** | Valida que el directori existeixi. Llista tots els fitxers regulars del directori. Filtra només: Fitxers que comencen per server.log Fitxers que contenen el nom de l’aplicació (appNom) Per a cada fitxer obté: Nom Mida en bytes Data de creació i modificació (format dd/MM/yyyy HH:mm:ss) Tipus MIME deduït per extensió |
| **Paràmetres** | **directoriPath**: Directori on cercar el fitxers de log**appNom**: Nom de l’aplicació (utilitzada er filtrar els fitxers de log) |
| **Resposta** | List\<FitxerInfo\> |
| **Ús principal** | Mostrar al frontend la llista de logs disponibles per descarregar o consultar |

| getFitxerByNom |  |
| ----- | :---- |
| **Mètode** | FitxerContingut **getFitxerByNom**(String directoriPath, String nom) |
| **Descripció** | Carrega un fitxer concret i en retorna el contingut i metadades encapsulades en un objecte FitxerContingut |
| **Comportament** | Valida que el directori i el fitxer existeixin. Llegeix: Nom Mida en bytes Data de creació i modificació (format dd/MM/yyyy HH:mm:ss) Contingut del fitxer en bytes Tipus MIME detectat amb Apache Tika  Si el fitxer és text pla (text/plain) o no es pot detectar el MIME: Es comprimeix automàticament en ZIP Es canvia el MIME a application/zip Es modifica el nom afegint .zip |
| **Paràmetres** | **directoriPath**: Directori on cercar el fitxer de log**nom**: Nom del fitxer a llegir |
| **Resposta** | FitxerContingut |
| **Ús principal** | Descarregar fitxers de log, evitant problemes de grandària o encoding en fitxers de text grans |

| readLastNLines |  |
| ----- | :---- |
| **Mètode** | List\<String\> **readLastNLines**(String directoriPath, String nomFitxer, Long nLinies) |
| **Descripció** | Llegeix eficientment les darreres N línies d’un fitxer de log, sense carregar-lo completament en memòria |
| **Comportament** | Valida paràmetres d’entrada. Limita nLinies entre 100 i 10000 Utilitza RandomAccessFile per llegir el fitxer cap enrere, byte a byte Construeix les línies trobades i les retorna en ordre correcte |
| **Paràmetres** | **directoriPath**: Directori on cercar el fitxer de log**nomFitxer**: Nom del fitxer a llegir **nLinies**: Número de línies a llegir |
| **Resposta** | List\<String\> |
| **Ús principal** | Mostrar al frontend les darreres línies del log (tail), útil per a diagnòstic en temps real |

| getMimeType |  |
| ----- | :---- |
| **Mètode** | String **getMimeType**(File file) |
| **Descripció** | Detecta el tipus MIME real del fitxer utilitzant Apache Tika |
| **Paràmetres** | **file**: Fitxers del que obtenir el mimeType |
| **Resposta** | String |
| **Ús principal** | Obtenir el tipus mime d’un fitxer |

| isTextPlain |  |
| ----- | :---- |
| **Mètode** | boolean **isTextPlain**(String mime) |
| **Descripció** | Comprova si el MIME és exactament text/plain |
| **Paràmetres** | **mime**: tipus mime a comprovar |
| **Resposta** | boolean |
| **Ús principal** | Decidir si un fitxer s’ha de comprimir abans de retornar-lo |

| compressFile |  |
| ----- | :---- |
| **Mètode** | byte\[\] **compressFile**(byte\[\] fileData, String fileName) |
| **Descripció** | Comprimeix un fitxer en memòria i el retorna com un array de bytes ZIP |
| **Comportament** | Crea un ZIP en memòria amb ZipOutputStream. Afegeix una única entrada amb el nom original del fitxer. Retorna el contingut comprimit |
| **Paràmetres** | **fileData**: Contingut a comprimir**fileName**: Nom del fitxer a generar |
| **Resposta** | byte\[\] |
| **Ús principal** | Reduir la mida de fitxers de log i evitar problemes d’encoding en la descàrrega |

### 

### **Exemples d’ús** {#exemples-d’ús-2}

| public List\<FitxerInfo\> llistarFitxers() {    var directoriPath \= configHelper.getConfig("es.caib.notib.plugin.fitxer.logs.path");    if (Strings.*isNullOrEmpty*(directoriPath)) {        return new ArrayList\<\>();    }    return LogHelper.llistarFitxers(directoriPath, "notib"); } |
| :---- |

| public FitxerContingut getFitxerByNom(String nom) {    try {        var directoriPath \= "/app/logs";        return LogHelper.getFitxerByNom(directoriPath, nom);    } catch (Exception ex) {        *log*.error("Error llegint el fitxer " \+ nom, ex);        return FitxerContingut.*builder*().build();    } } |
| :---- |

| public List\<String\> readLastNLines(String nomFitxer, long nLinies) {    try {        if (Strings.*isNullOrEmpty*(nomFitxer) || nLinies \== null) {            *log*.error("Parametres incorrectes, nomFitxer " \+ nomFitxer \+ " nLinies" \+ nLinies);            return new ArrayList\<\>();        }        var directoriPath \= "/app/logs";        return LogHelper.readLastNLines(directoriPath, nomFitxer, nLinies);    } catch (Exception ex) {        *log*.error("Error no controlat", ex);        return new ArrayList\<\>();    } } |
| :---- |

6. # **Servers** {#servers}

A Comanda s’han generat automàticament serveis d’exemple per a l’implementació dels serveis de salut, estadística i logs, que es troben als mòduls:

* comanda-api-servers/comanda-api-server-salut-v1  
* comanda-api-servers/comanda-api-server-estadistica-v1  
* comanda-api-servers/comanda-api-server-log-v1

Aquests serveis autogenerats utilitzen RestEasy.  
De totes maneres, tot seguit us donem dos exemples que es poden utilitzar per definir el servei si s’utilitza spring, o  jaxrs \+ resteasy:

1. ## **Salut** {#salut-1}

**Spring**

| import es.caib.comanda.model.v1.salut.AppInfo; import es.caib.comanda.model.v1.salut.SalutInfo; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.GetMapping; import org.springframework.web.bind.annotation.RequestMapping; import org.springframework.web.bind.annotation.RestController; import org.springframework.web.server.ResponseStatusException; import javax.servlet.http.HttpServletRequest; */\*\* \* Contracte de l'API de Salut que COMANDA espera que implementin les APPs. \*/* @RestController @RequestMapping("/v1/salut") public class SalutApiController {    @GetMapping("/info")    public AppInfo salutInfo(HttpServletRequest request)  throws java.io.IOException {       ...    }    @GetMapping    public SalutInfo salut(HttpServletRequest request)  throws java.io.IOException {       ...    } } |
| :---- |

**Jaxrs \+ RestEasy**

| import es.caib.comanda.model.v1.salut.AppInfo; import es.caib.comanda.model.v1.salut.SalutInfo; import javax.ws.rs.GET; import javax.ws.rs.Path; import javax.ws.rs.Produces; import javax.ws.rs.core.Context; import javax.ws.rs.core.MediaType; import javax.ws.rs.core.Response; import javax.servlet.http.HttpServletRequest; import java.io.IOException; */\*\* \* Contracte de l'API de Salut que COMANDA espera que implementin les APPs. \*/* @Path("/v1/salut") @Produces(MediaType.APPLICATION\_JSON) public class SalutApiResource {    @GET    @Path("/info")    public AppInfo salutInfo(@Context HttpServletRequest request)            throws IOException {        *...*    }    @GET    public SalutInfo salut(@Context HttpServletRequest request)            throws IOException {        *...*    } } |
| :---- |

2. ## **Estadistica** {#estadistica}

**Spring**

| import es.caib.comanda.model.v1.estadistica.EstadistiquesInfo; import es.caib.comanda.model.v1.estadistica.RegistresEstadistics; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.GetMapping; import org.springframework.web.bind.annotation.PathVariable; import org.springframework.web.bind.annotation.RequestMapping; import org.springframework.web.bind.annotation.RestController; import org.springframework.web.server.ResponseStatusException; import javax.servlet.http.HttpServletRequest; import java.io.IOException; import java.util.List; */\*\* \* Contracte de l'API d'Estadístiques que COMANDA espera que implementin les APPs. \*/* @RestController @RequestMapping("/v1/estadistiques") public class EstadistiquesApiController {    @GetMapping("/info")    public EstadistiquesInfo estadistiquesInfo() throws IOException {       ...    }    @GetMapping    public RegistresEstadistics estadistiques(HttpServletRequest request)  throws IOException {       ...    }    @GetMapping("/of/{data}")    public RegistresEstadistics estadistiquesPerData(            HttpServletRequest request,            @PathVariable String data) throws Exception {       ...    }    @GetMapping("/from/{dataInici}/to/{dataFi}")    public List\<RegistresEstadistics\> estadistiquesPerRang(            HttpServletRequest request,            @PathVariable("dataInici") String dataInici,            @PathVariable("dataFi") String dataFi) throws Exception {       ...    } } |
| :---- |

**Jaxrs \+ RestEasy**

| import es.caib.comanda.model.v1.estadistica.EstadistiquesInfo; import es.caib.comanda.model.v1.estadistica.RegistresEstadistics; import javax.ws.rs.GET; import javax.ws.rs.Path; import javax.ws.rs.PathParam; import javax.ws.rs.Produces; import javax.ws.rs.WebApplicationException; import javax.ws.rs.core.Context; import javax.ws.rs.core.MediaType; import javax.ws.rs.core.Response; import javax.servlet.http.HttpServletRequest; import java.io.IOException; import java.util.List; */\*\* \* Contracte de l'API d'Estadístiques que COMANDA espera que implementin les APPs. \*/* @Path("/v1/estadistiques") @Produces(MediaType.APPLICATION\_JSON) public class EstadistiquesApiResource {    @GET    @Path("/info")    public EstadistiquesInfo estadistiquesInfo() throws IOException {        *...*    }    @GET    public RegistresEstadistics estadistiques(            @Context HttpServletRequest request)            throws IOException {        *...*    }    @GET    @Path("/of/{data}")    public RegistresEstadistics estadistiquesPerData(            @Context HttpServletRequest request,            @PathParam("data") String data)            throws Exception {        *...*    }    @GET    @Path("/from/{dataInici}/to/{dataFi}")    public List\<RegistresEstadistics\> estadistiquesPerRang(            @Context HttpServletRequest request,            @PathParam("dataInici") String dataInici,            @PathParam("dataFi") String dataFi)            throws Exception {        *...*    } } |
| :---- |

3. ## **Log** {#log}

**Spring**

| import es.caib.comanda.model.v1.log.FitxerContingut; import es.caib.comanda.model.v1.log.FitxerInfo; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.GetMapping; import org.springframework.web.bind.annotation.PathVariable; import org.springframework.web.bind.annotation.RequestMapping; import org.springframework.web.bind.annotation.RestController; import org.springframework.web.server.ResponseStatusException; import java.util.List; */\*\* \* Contracte de l'API de Salut que COMANDA espera que implementin les APPs. \*/*  @RestController @RequestMapping("/v1/logs") public class LogApiController {    @GetMapping()    public List\<FitxerInfo\> getFitxers() {       ...    }    @GetMapping("/{nomFitxer}")    public FitxerContingut getFitxerByNom(            @PathVariable("nomFitxer") String nomFitxer) {       ...    }    @GetMapping("/{nomFitxer}/linies/{nLinies}")    public List\<String\> getFitxerLinies(            @PathVariable("nomFitxer") String nomFitxer,            @PathVariable("nLinies") Long nLinies) {       ...    } } |
| :---- |

**Jaxrs \+ RestEasy**

| import es.caib.comanda.model.v1.log.FitxerContingut; import es.caib.comanda.model.v1.log.FitxerInfo; import javax.ws.rs.GET; import javax.ws.rs.Path; import javax.ws.rs.PathParam; import javax.ws.rs.Produces; import javax.ws.rs.core.MediaType; import javax.ws.rs.core.Response; import java.util.List; */\*\* \* Contracte de l'API de Logs que COMANDA espera que implementin les APPs. \*/* @Path("/v1/logs") @Produces(MediaType.APPLICATION\_JSON) public class LogApiResource {    @GET    public List\<FitxerInfo\> getFitxers() {        *...*    }    @GET    @Path("/{nomFitxer}")    public FitxerContingut getFitxerByNom(            @PathParam("nomFitxer") String nomFitxer) {        *...*    }    @GET    @Path("/{nomFitxer}/linies/{nLinies}")    public List\<String\> getFitxerLinies(            @PathParam("nomFitxer") String nomFitxer,            @PathParam("nLinies") Long nLinies) {        *...*    } } |
| :---- |

Al utilitzar jaxrs \+ RestEasy:

* Es pot utilitzar jakarta.ws.rs.\* enlloc de [javax.ws.rs](http://javax.ws.rs).\*  
* Per fer els mètodes amb un estil més REST, es pot fer que retornin Response.  
  Ex:  
    
  @GET  
  @Path("/info")  
  public **Response** salutInfo(@Context HttpServletRequest request)  
          throws IOException {  
    
      AppInfo info \= ...;  
      return Response.ok(info).build();  
  }

7. # **Tasques** {#tasques}

Les tasques representen ítems d’operativa o seguiment a Comanda associats a una entitat de la vostra aplicació. Els usuaris han de poder visualitzar les tasques a Comanda, amb l’estat en que es troben, i amb un enllaç cap a la vostra aplicació, per tal de poder tramitar la tasca, fins que aquesta disposi d’una data de fi, en que es deixa de mostrar a Comanda.

Per a poder donar d’alta o modificar tasques a Comanda es disposa dels següents endpoints:

* POST /comandaapi/interna/v1/tasques: crea o modifica una tasca a Comanda.  
* PUT /comandaapi/interna/v1/tasques/{identificador}: modifica una tasca a Comanda.  
* POST /comandaapi/interna/v1/tasques/multiple: crea o modifica múltiples tasques a Comanda.  
* PUT /comandaapi/interna/v1/tasques/multiple: modifica múltiples tasques a Comanda.  
* GET /comandaapi/interna/v1/tasques/{identificador}: Consulta una tasca a Comanda.  
* GET /comandaapi/interna/v1/tasques: Consulta un llistat de tasques a Comanda.

| Creació o modificació d’una tasca |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/v1/tasques   Ex. https:/dev.caib.es/comandaapi/interna/v1/tasques |
| **Descripció** | Aquest servei  crea o modifica una tasca a Comanda. |
| **Mètode** | POST |
| **Autenticació** | BASIC |
| **Resposta** | HTTP Status OK |
| **Ex. de petició** | curl \--location 'dev.caib.es/comandaapi/interna/v1/tasques' \\ \--header 'Content-Type: application/json' \\ \--header 'Authorization: Basic XXXXXXXXXXX==' \\ \--data '{   "appCodi": "NOT",   "entornCodi": "DEV",   "identificador": "31489824",   "tipus": "NOTIFICACIO",   "nom": "Nom de la notificacio",   "descripcio": "Descripcio de la notificació",   "estat": "INICIADA",   "estatDescripcio": "Enviada a Notifica",   "numeroExpedient": "1850488",   "prioritat": null,   "dataInici": "2025-08-28T16:08:28.295",   "dataFi": null,   "dataCaducitat": "2025-08-28T16:08:28.295",   "redireccio": "http://dev.caib.es/notiback/notificacio/31489821/enviament/31489824",   "responsable": "u000000",   "grup": null,   "usuarisAmbPermis": \["u000000", "u999000"\],   "grupsAmbPermis": \["ROL\_001", "ROL\_002"\] }' |

| Modificació d’una tasca |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/v1/tasques/{identificador}   Ex. https:/dev.caib.es/comandaapi/interna/v1/tasques/1234 |
| **Descripció** | Aquest servei modifica una tasca existent a Comanda. |
| **Mètode** | PUT |
| **Autenticació** | BASIC |
| **Paràmetres** | **identificador**: identificador de la tasca a modificar |
| **Resposta** | HTTP Status OK si les dades son correctes HTTP Status BAD\_REQUEST en cas de dades incorrectes HTTP Status NOT\_FOUND si no es troba la tasca a modificar |
| **Ex. de petició** | curl \--location \--request PUT 'dev.caib.es/comandaapi/interna/v1/tasques/31489824 \\ \--header 'Content-Type: application/json' \\ \--header 'Authorization: Basic XXXXXXXXXXX==' \\ \--data '{   "appCodi": "NOT",   "entornCodi": "DEV",   "identificador": "31489824",   "tipus": "NOTIFICACIO",   "nom": "Nom de la notificacio",   "descripcio": "Descripcio de la notificació",   "estat": "INICIADA",   "estatDescripcio": "Enviada a Notifica",   "numeroExpedient": "1850488",   "prioritat": null,   "dataInici": "2025-08-28T16:08:28.295",   "dataFi": null,   "dataCaducitat": "2025-08-28T16:08:28.295",   "redireccio": "http://dev.caib.es/notiback/notificacio/31489821/enviament/31489824",   "responsable": "u000000",   "grup": null,   "usuarisAmbPermis": \["u000000", "u999000"\],   "grupsAmbPermis": \["ROL\_001", "ROL\_002"\] }' |

| Creació o modificació de múltiples tasques |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/v1/tasques/multiple Ex. https:/dev.caib.es/comandaapi/interna/v1/tasques/multiple |
| **Descripció** | Aquest servei  crea o modifica una tasca a Comanda. |
| **Mètode** | POST |
| **Autenticació** | BASIC |
| **Resposta** | HTTP Status OK |
| **Ex. de petició** | curl \--location 'dev.caib.es/comandaapi/interna/v1/tasques/multiple' \\ \--header 'Content-Type: application/json' \\ \--header 'Authorization: Basic XXXXXXXXXXX==' \\ \--data '\[{   "appCodi": "NOT",   "entornCodi": "DEV",   "identificador": "31489824",   "tipus": "NOTIFICACIO",   "nom": "Nom de la notificacio",   "descripcio": "Descripcio de la notificació",   "estat": "INICIADA",   "estatDescripcio": "Enviada a Notifica",   "numeroExpedient": "1850488",   "prioritat": null,   "dataInici": "2025-08-28T16:08:28.295",   "dataFi": null,   "dataCaducitat": "2025-08-28T16:08:28.295",   "redireccio": "http://dev.caib.es/notiback/notificacio/31489821/enviament/31489824",   "responsable": "u000000",   "grup": null,   "usuarisAmbPermis": \["u000000", "u999000"\],   "grupsAmbPermis": \["ROL\_001", "ROL\_002"\] }\]' |

| Modificació de múltiples tasques |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/v1/tasques/multiple Ex. https:/dev.caib.es/comandaapi/interna/v1/tasques/multiple |
| **Descripció** | Aquest servei modifica múltiples tasques existents a Comanda. |
| **Mètode** | PUT |
| **Autenticació** | BASIC |
| **Resposta** | HTTP Status OK si les dades son correctes HTTP Status BAD\_REQUEST en cas de dades incorrectes HTTP Status NOT\_FOUND si no es troba cap tasca a modificar |
| **Ex. de petició** | curl \--location \--request PUT 'dev.caib.es/comandaapi/interna/v1/tasques' \\ \--header 'Content-Type: application/json' \\ \--header 'Authorization: Basic XXXXXXXXXXX==' \\ \--data '\[{   "appCodi": "NOT",   "entornCodi": "DEV",   "identificador": "31489824",   "tipus": "NOTIFICACIO",   "nom": "Nom de la notificacio",   "descripcio": "Descripcio de la notificació",   "estat": "INICIADA",   "estatDescripcio": "Enviada a Notifica",   "numeroExpedient": "1850488",   "prioritat": null,   "dataInici": "2025-08-28T16:08:28.295",   "dataFi": null,   "dataCaducitat": "2025-08-28T16:08:28.295",   "redireccio": "http://dev.caib.es/notiback/notificacio/31489821/enviament/31489824",   "responsable": "u000000",   "grup": null,   "usuarisAmbPermis": \["u000000", "u999000"\],   "grupsAmbPermis": \["ROL\_001", "ROL\_002"\] }\]' |

| Consulta una tasca |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/v1/tasques/{identificador}   Ex. https:/dev.caib.es/comandaapi/interna/v1/tasques/1234 |
| **Descripció** | Aquest servei consulta una tasca Comanda. |
| **Mètode** | GET |
| **Autenticació** | BASIC |
| **Paràmetres** | **identificador**: identificador de la tasca a cercar |
| **Resposta** | Tasca si s’ha trobat la tasca HTTP Status NOT\_FOUND si no es troba la tasca a modificar |
| **Ex. de petició** | curl \--location 'http://localhost:8080/comandaapi/interna/v1/tasques/31489824 \\ \--header 'Authorization: Basic XXXXXXXXXXX==' |
| **Ex. de resposta** | {   "appCodi": "NOT",   "entornCodi": "DEV",   "identificador": "31489824",   "tipus": "NOTIFICACIO",   "nom": "Nom de la notificacio",   "descripcio": "Descripcio de la notificació",   "estat": "INICIADA",   "estatDescripcio": "Enviada a Notifica",   "numeroExpedient": "1850488",   "prioritat": null,   "dataInici": "2025-08-28T16:08:28.295",   "dataFi": null,   "dataCaducitat": "2025-08-28T16:08:28.295",   "redireccio": "http://dev.caib.es/notiback/notificacio/31489821/enviament/31489824",   "responsable": "u000000",   "grup": null,   "usuarisAmbPermis": \["u000000", "u999000"\],   "grupsAmbPermis": \["ROL\_001", "ROL\_002"\] } |

| Consulta de múltiples tasques |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/v1/tasques   Ex. https:/dev.caib.es/comandaapi/interna/v1/tasques |
| **Descripció** | Aquest servei obté un llistat paginat de Tasca. |
| **Mètode** | GET |
| **Autenticació** | BASIC |
| **Paràmetres** | **quickFilter**: Filtre ràpid que s’aplica sobre el camp nom de les tasques**filter**: Filtre en format SpringFilter (veure annex) **namedQueries**: Consultes predefinides a aplicar. NO IMPLEMENTAT. **perspectives**: Perspectives a aplicar a la consulta: “EXPIRATION”: Emplena el camp diesPerCaducar **page**: Número de pàgina a recuperar **size**: Mida de pàgina a recuperar |
| **Resposta** | TascaPage |
| **Ex. de petició** | curl \--location 'http://localhost:8080/comandaapi/interna/v1/tasques?filter=identificador==”31489824”\&page=0\&size=20’ \\ \--header 'Authorization: Basic XXXXXXXXXXX==' |
| **Ex. de resposta** | {   "content": \[     {         "appCodi": "NOT",         "entornCodi": "DEV",         "identificador": "31489824",         "tipus": "NOTIFICACIO",         "nom": "Nom de la notificacio",         "descripcio": "Descripcio de la notificació",         "estat": "INICIADA",         "estatDescripcio": "Enviada a Notifica",         "numeroExpedient": "1850488",         "prioritat": null,         "dataInici": "2025-08-28T16:08:28.295",         "dataFi": null,         "dataCaducitat": "2025-08-28T16:08:28.295",         "redireccio": "http://dev.caib.es/notiback/notificacio/31489821/enviament/31489824",         "responsable": "u000000",         "grup": null,         "usuarisAmbPermis": \["u000000", "u999000"\],         "grupsAmbPermis": \["ROL\_001", "ROL\_002"\]       }   \],   "page": {     "number": 0,     "size": 20,     "totalElements": 1,     "totalPages": 1   },   "links": \[     {       "rel": "self",       "href": "http://localhost:8080/comandaapi/interna/v1/tasques?filter=identificador==”31489824”\&page=0\&size=20"     }   \] } |

### **Model de dades** {#model-de-dades-5}

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

| TascaPage |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| content | List\<Tasca\> | 1 | Contingut de la pàgina |
| page | PageMetadata | 1 | Metadades de paginació |
| links | List\<TascaPageLink\> | 0..1 | Enllaços HATEOAS |

| PageMetadata |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| number | long | 1 | Número de pàgina |
| size | long | 1 | Mida de pàgina |
| totalElements | long | 1 | Total d'elements |
| totalPages | long | 1 | Total de pàgines |

| TascaPageLink |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| href | PageMetadata | 1 | Amb qui es relaciona l'enllaç (ex. self) |
| rel | List\<Tasca\> | 1 | URL de l'enllaç |

8. # **Avisos** {#avisos}

Els avisos són comunicacions informatives mostrades a Comanda. Els usuaris han de poder visualitzar tots els avisos a Comanda.

Per a poder donar d’alta o modificar avisos a Comanda es disposa dels següents endpoints:

* POST /comandaapi/interna/v1/avisos: crea o modifica un avís a Comanda.  
* PUT /comandaapi/interna/v1/avisos/{identificador}: modifica un avís a Comanda.  
* POST /comandaapi/interna/v1/avisos/multiple: crea o modifica múltiples avisos a Comanda.  
* PUT /comandaapi/interna/v1/avisos/multiple: modifica múltiples avisos a Comanda.  
* GET /comandaapi/interna/v1/avisos/{identificador}: Consulta un avís a Comanda.  
* GET /comandaapi/interna/v1/avisos: Consulta un llistat d’avisos a Comanda.

| Creació o modificació d’un avís |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/v1/avisos   Ex. https:/dev.caib.es/comandaapi/interna/v1/avisos |
| **Descripció** | Aquest servei  crea o modifica un avís a Comanda. |
| **Mètode** | POST |
| **Autenticació** | BASIC |
| **Paràmetres** | Avis |
| **Resposta** | HTTP Status OK |
| **Ex. de petició** | curl \--location 'dev.caib.es/comandaapi/interna/v1/avisos' \\ \--header 'Content-Type: application/json' \\ \--header 'Authorization: Basic XXXXXXXXXXX==' \\ \--data '{   "appCodi": "NOT",   "entornCodi": "DEV",   "identificador": "31489824",   "tipus": "INFO",   "nom": "Nom de l’avís",   "descripcio": "Descripcio de l’avís",   "dataInici": "2025-08-28T16:08:28.295",   "dataFi": null, }' |

| Modificació d’un avís |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/v1/avisos/{identificador}   Ex. https:/dev.caib.es/comandaapi/interna/v1/avisos/1234 |
| **Descripció** | Aquest servei modifica una tasca existent a Comanda. |
| **Mètode** | PUT |
| **Autenticació** | BASIC |
| **Paràmetres** | **identificador**: identificador de la tasca a modificar |
| **Resposta** | HTTP Status OK si les dades son correctes HTTP Status BAD\_REQUEST en cas de dades incorrectes HTTP Status NOT\_FOUND si no es troba la tasca a modificar |
| **Ex. de petició** | curl \--location \--request PUT 'dev.caib.es/comandaapi/interna/v1/avisos/31489824 \\ \--header 'Content-Type: application/json' \\ \--header 'Authorization: Basic XXXXXXXXXXX==' \\ \--data '{   "appCodi": "NOT",   "entornCodi": "DEV",   "identificador": "31489824",   "tipus": "INFO",   "nom": "Nom de l’avís",   "descripcio": "Descripcio de l’avís",   "dataInici": "2025-08-28T16:08:28.295",   "dataFi": null, }' |

| Creació o modificació de múltiples avisos |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/v1/avisos/multiple Ex. https:/dev.caib.es/comandaapi/interna/v1/avisos/multiple |
| **Descripció** | Aquest servei  crea o modifica una tasca a Comanda. |
| **Mètode** | POST |
| **Autenticació** | BASIC |
| **Resposta** | HTTP Status OK |
| **Ex. de petició** | curl \--location 'dev.caib.es/comandaapi/interna/v1/avisos/multiple' \\ \--header 'Content-Type: application/json' \\ \--header 'Authorization: Basic XXXXXXXXXXX==' \\ \--data '\[{   "appCodi": "NOT",   "entornCodi": "DEV",   "identificador": "31489824",   "tipus": "INFO",   "nom": "Nom de l’avís",   "descripcio": "Descripcio de l’avís",   "dataInici": "2025-08-28T16:08:28.295",   "dataFi": null, }\]' |

| Modificació de múltiples avisos |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/v1/avisos/multiple Ex. https:/dev.caib.es/comandaapi/interna/v1/avisos/multiple |
| **Descripció** | Aquest servei modifica múltiples tasques existents a Comanda. |
| **Mètode** | PUT |
| **Autenticació** | BASIC |
| **Resposta** | HTTP Status OK si les dades son correctes HTTP Status BAD\_REQUEST en cas de dades incorrectes HTTP Status NOT\_FOUND si no es troba cap tasca a modificar |
| **Ex. de petició** | curl \--location \--request PUT 'dev.caib.es/comandaapi/interna/v1/avisos \\ \--header 'Content-Type: application/json' \\ \--header 'Authorization: Basic XXXXXXXXXXX==' \\ \--data '\[{   "appCodi": "NOT",   "entornCodi": "DEV",   "identificador": "31489824",   "tipus": "INFO",   "nom": "Nom de l’avís",   "descripcio": "Descripcio de l’avís",   "dataInici": "2025-08-28T16:08:28.295",   "dataFi": null, }\]' |

| Consulta una avís |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/v1/avisos/{identificador}   Ex. https:/dev.caib.es/comandaapi/interna/v1/avisos/1234 |
| **Descripció** | Aquest servei consulta una tasca Comanda. |
| **Mètode** | GET |
| **Autenticació** | BASIC |
| **Paràmetres** | **identificador**: identificador de la tasca a cercar |
| **Resposta** | Tasca si s’ha trobat la tasca HTTP Status NOT\_FOUND si no es troba la tasca a modificar |
| **Ex. de petició** | curl \--location 'http://localhost:8080/comandaapi/interna/v1/avisos/31489824 \\ \--header 'Authorization: Basic XXXXXXXXXXX==' |
| **Ex. de resposta** | {   "appCodi": "NOT",   "entornCodi": "DEV",   "identificador": "31489824",   "tipus": "INFO",   "nom": "Nom de l’avís",   "descripcio": "Descripcio de l’avís",   "dataInici": "2025-08-28T16:08:28.295",   "dataFi": null, } |

| Consulta de múltiples avisos |  |
| ----- | :---- |
| **Adreça API** |  /comandaapi/interna/v1/avisos   Ex. https:/dev.caib.es/comandaapi/interna/v1/avisos |
| **Descripció** | Aquest servei obté un llistat paginat de Tasca. |
| **Mètode** | GET |
| **Autenticació** | BASIC |
| **Paràmetres** | **quickFilter**: Filtre ràpid que s’aplica sobre el camp nom de les tasques**filter**: Filtre en format SpringFilter (veure annex) **namedQueries**: Consultes predefinides a aplicar. NO IMPLEMENTAT. **perspectives**: Perspectives a aplicar a la consulta: “EXPIRATION”: Emplena el camp diesPerCaducar **page**: Número de pàgina a recuperar **size**: Mida de pàgina a recuperar |
| **Resposta** | TascaPage |
| **Ex. de petició** | curl \--location 'http://localhost:8080/comandaapi/interna/v1/avisos?filter=identificador==”31489824”\&page=0\&size=20’ \\ \--header 'Authorization: Basic XXXXXXXXXXX==' |
| **Ex. de resposta** | {   "content": \[     {   "appCodi": "NOT",   "entornCodi": "DEV",   "identificador": "31489824",   "tipus": "INFO",   "nom": "Nom de l’avís",   "descripcio": "Descripcio de l’avís",   "dataInici": "2025-08-28T16:08:28.295",   "dataFi": null, }   \],   "page": {     "number": 0,     "size": 20,     "totalElements": 1,     "totalPages": 1   },   "links": \[     {       "rel": "self",       "href": "http://localhost:8080/comandaapi/interna/v1/avisos?filter=identificador==”31489824”\&page=0\&size=20"     }   \] } |

### **Model de dades** {#model-de-dades-6}

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

| AvisPage |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| content | List\<Tasca\> | 1 | Contingut de la pàgina |
| page | PageMetadata | 1 | Metadades de paginació |
| links | List\<TascaPageLink\> | 0..1 | Enllaços HATEOAS |

| PageMetadata |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| number | long | 1 | Número de pàgina |
| size | long | 1 | Mida de pàgina |
| totalElements | long | 1 | Total d'elements |
| totalPages | long | 1 | Total de pàgines |

| AvisPageLink |  |  |  |
| ----- | :---- | :---- | :---- |
| Camp | Tipus | Car | Descripció |
| href | PageMetadata | 1 | Amb qui es relaciona l'enllaç (ex. self) |
| rel | List\<Tasca\> | 1 | URL de l'enllaç |

9. # **Exemples** {#exemples}

Tot seguit es mostren uns exemples senzills d’enviament de tasques i avisos:

## **Enviar una tasca** {#enviar-una-tasca}

| `Tasca tasca = Tasca.builder()   .appCodi("APP")   .entornCodi("PRO")   .identificador("notif-987")   .tipus("NOTIFICA")   .nom("Notificació pendent")   .descripcio("Usuari ha d’accedir i signar")   .estat(TascaEstat.PENDENT)   .prioritat(Prioritat.ALTA)   .dataInici(LocalDateTime.now())   .redireccio(new URL("https://app.exemple.org/notificacio/987"))   .usuarisAmbPermis(List.of("USR1"))   .grupsAmbPermis(List.of("ROL_OPERADOR"))   .build(); ComandaClient comandaClient = new ComandaClient(url, username, password); var resposta = comandaClient.crearTasca(tasca);` |
| :---- |

Si no s’utilitza el client, es pot realitzar una petició REST a: 

* POST {COMANDA\_API\_URL}/v1/tasques body: tasca

  ## **Enviar un avís** {#enviar-un-avís}

| `Avis avis = Avis.builder()   .appCodi("APP")   .identificador("12345")   .tipus(AvisTipus.INFO)   .nom("Manteniment")   .descripcio("Talls diumenge 8:00-10:00")   .dataInici(LocalDateTime.now())   .dataFi(LocalDateTime.now().plusHours(2))   .build();ComandaClient comandaClient = new ComandaClient(url, username, password); var resposta = comandaClient.crearAvis(avis);` |
| :---- |

Si no s’utilitza el client, es pot realitzar una petició REST a:

* POST {COMANDA\_URL}/api/cues/avisos body: avis

Amb aquests elements, qualsevol aplicació pot integrar-se amb Comanda per a Salut, Estadístiques, Tasques i Avisos adaptant els serveis propis per a obtenir les dades i mapant-les als models de comanda-lib.

# 

# **Annex 1 \- Spring-filter** {#annex-1---spring-filter}

## **Camp filter – Sintaxi bàsica** {#camp-filter-–-sintaxi-bàsica}

El paràmetre `filter` permet definir **condicions de filtratge dinàmiques** sobre els camps de l’entitat, utilitzant una sintaxi similar a una expressió lògica.

### **Format general** {#format-general}

camp operador valor

Exemples:

`name == "Joan"`  
`age > 18`  
`active == true`

## **Operadors disponibles** {#operadors-disponibles}

### **Comparació** {#comparació}

| Operador | Significat |
| ----- | ----- |
| `==` | igual |
| `!=` | diferent |
| `>` | major que |
| `>=` | major o igual |
| `<` | menor que |
| `<=` | menor o igual |

**Exemples**

`age >= 65`  
`status != "CANCELLED"`

### **Text** {#text}

| Operador | Descripció |
| ----- | ----- |
| `~=` | conté (LIKE) |
| `!~=` | no conté |

**Exemples**

`name ~= "mar"`  
`email !~= "@gmail.com"`

### **Operadors lògics** {#operadors-lògics}

Pots combinar múltiples condicions:

| Operador | Significat |
| ----- | ----- |
| `and` | totes les condicions |
| `or` | alguna condició |
| `not` | negació |

**Exemples**

`age > 18 and active == true`  
`status == "NEW" or status == "PENDING"`  
`not deleted`

### **Agrupació amb parèntesis** {#agrupació-amb-parèntesis}

Per controlar la prioritat de les condicions:

`(status == "NEW" or status == "PENDING") and priority == "HIGH"`

## **Valors admesos** {#valors-admesos}

### **Tipus bàsics** {#tipus-bàsics}

* **Text** → entre cometes  
  `name == "Pere"`  
* **Números**  
  `amount > 100.50`  
* **Booleans**  
  `active == true`  
* **Dates** (ISO-8601)  
  `createdAt >= "2024-01-01"`

## **Accés a camps anidats** {#accés-a-camps-anidats}

Es poden filtrar propietats relacionades utilitzant notació amb punts:

`user.name == "Maria"`  
`address.city == "Inca"`

## **Operador in** {#operador-in}

Permet comparar amb una llista de valors:

`status in ("NEW", "PENDING", "IN_PROGRESS")`

## **Exemples complets** {#exemples-complets}

| Descripció | Filter |
| ----- | ----- |
| Actius majors de 18 | `age > 18 and active == true` |
| Nom conté "jo" | `name ~= "jo"` |
| Estat pendent o nou | `status in ("NEW","PENDING")` |
| Usuaris d’Inca no esborrats | `address.city == "Inca" and deleted == false` |

## **Notes importants** {#notes-importants}

* Els noms dels camps han de coincidir amb els **atributs de l’entitat**.  
* Les cadenes de text sempre van entre cometes `" "`.  
* La sintaxi és **case-sensitive** per als camps.  
* Si el `filter` és incorrecte, l’API retornarà un error `400 Bad Request`.

[image1]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASQAAABnCAYAAACpQj+aAAA6d0lEQVR4Xu3dB7xdVbE/cN+zF/RJERWxowJKV0DfE3xWwEYTERFFUbAXLIAa8aFiAWkiKIIIUqQ3FQkJRDoJAZJAEkJuQkjvIT33Zv7nu/lP3Nmcc0tuyjXZ8/nsz71nn73XmjVr5rdm1pq1ztOipppqqqmP0NOqN2qqqaaa1hXVgFRTTTX1GaoBqaaaauozVANSTTXV1GeoBqSaaqqpz1ANSDXVVFOfoRqQaqqppj5DNSDVVFNNfYZqQKqpppr6DHUTkJbHsqXzY+704TF9wqCY8dgta+2aNemuWDR/YoOF9ipTNdVU03pGXQPS8uUxe+rQuOevn4ib/7xtDLx4x8a181q7Bly0Qwy6bM8YN/z8aF+2qMpdTTXVtB5Rl4C0ZNGsuH/Al+KWS3aPsQ/8Lqa03RRTxvVfa9fjo6+KIf2/0ACnXWLm5Huq7NVUU03rEXUJSPNmjozbr3pfDL35yzF33py47cHxMW32/EYE1R6Lhj8Us6+4NmZffk0sHnZNLJ9+bXEtHHxNcc/1xK1Xr7jvmdlXPnl/3o3XRPvEJ+8vG9f4/Lcn78+97ppYMvra6PDOjOujY8GomPTo9Q3vbLsY++DvquzVVFNN6xF1CUhzpw+L2658Twz/53Exfurc+Gi/y+NLp90YCxctjQWDh8aojxwaQ1+za8w4dadoH7pVtN+3VUz+4Vtj6OveFkNf/bYYd/iuT95vXNNP2THuf9Ouxf1RH9g1ltyydXF/wbXbxsPvemtxf/jb3hZzzt8u2oe8Idrv3z6WT704pk+4teEhvTVG33dqlb2W1NHREYsXL44nnniiuJYsWdKIPpdXH+sTtGzZsoLXpUuXFp/xiV/32tt7P3emvFmzZsWjjz4aCxcurH7dK8IfPvFL5kg93/jGN+If//hH8b36tc1zra7V0c6+QvPmzYuxY8fG3Llzq1/V1AX1CJDGTZ0T7z3m4njWvifF0Wf3j8mTZ8Wsq2+I4bvvHdNPflN0DNk42gdvEpOOeXMMfsl2MXiThlfzybcU910zTtoq7nvF9sX9h/fYLhbfvFlxf8HVW8SIXd9c3H9w2+1izrmvjY57N4mO+17ZAKQLegxI06dPj0svvTS+8pWvxHvf+97Ya6+94tvf/nZcd911hZL0JWACRtdcc018+ctfjtNPP70wzsmTJ8cJJ5wQX/va1+Kmm27qNb8M5JBDDokXvvCFRbnqXB0ERO688844+uij45hjjolx48YVvJ588snx9Kc/PY488siYP39+IfPf/va3RRtbXbfcckuv29kXiKy/+93vxkYbbVTonPbX1H1aJUB65r6/ihceeEr0O//WmPz49Jh58eUx58I9+wQgTZo0KT772c/GZpttFv/xH/8R//mf/1n8db3yla+M//u//4uZM2dWX1tnxLP43ve+F0972tMK8OTNPfTQQ7HtttvGs5/97PjFL37Ra0PlHe29996FkfBc1Lk6iNdz3nnnFUC3+eabx9133114ST/96U/juc99bvzsZz8r6tIn++yzT9HGVtepp566wsP6d6YZM2bE5z73uULWn//852POnDnVR2rqhFYZkJ7+0V/Flp86M0654p5YMH9hLBt/cXTcv/U6BSTexY9+9KN4xjOeUYzQb37zm+Nb3/pWMQJvueWWxf2PfOQjMWHChOqr64zWBiDxZO64444466yzYuTIkdWvV5maAZJ7P/zhD2P33XePBx98sOC9DEjbbLNNYajl64gjjoj+/fv3up19gYCqdp999tkxdOjQ9QJk1yb1CpCe0bi2OvysuPCmB2PJ4idi+eSzo33oG9cZII0YMSLe/va3F4q/ww47FOHOokWLCrf573//e+EdjBo1aoXi+27IkCFx/fXXF2GT8GP27Nkryps6dWpxj6HdeOONcfHFF8fNN98cU6ZMWfEMhZs4cWIMHDiwCBOFhcOHDy/AMQkgjB8/PgYMGBBXX3113H777YWXlnNF3QUkZSr7b3/7W9xwww0r1SMMe/jhh+O2226L++67Ly666KKiLuDrO7LxnbmNpAULFhTtN9ejXWTT1VyO9ioTgGgvwH/+85+/ApDUpSxXemJlQOK9CmuqVz6rrbwKfF177bVFW7VTX+X3ZKdf9Bl5e7YcGpnD0lYgrIxLLrkk/vnPfz7FMzafNnjw4KIMzymzPO8jdHbvrrvuir/+9a9F/ytTe9zXfjzorwyD/R09enRR/yOPPLJCnmStD8iaLmqTezWtTD0CpPENQPrAcZc0AOmkApAKUGqA02sboHTZLQ9F+5KZ0fHYiTHp2J3WOiBRVIr1ile8ogjPvv71rz8FFChgGjel79evX7z0pS+NF7zgBYVR/dd//Vcc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>

[image2]: <data:image/png;base64,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>

[image3]: <data:image/png;base64,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>