# COMANDA
Quadre de comandaments de les aplicacions per l'administració digital de la CAIB.

## Compilar
Executar la següent comanda:

```
$ mvn clean install
```
La compilació del projecte crea varis fitxers per a executar o desplegar l'aplicació:
- comanda-back/target/comanda-back.war: fitxer per a executar l'aplicació directament amb Java.
- comanda-ear/target/comanda.ear: fitxer per a executar l'aplicació sobre un servidor JBoss EAP 7.2.

## Execució amb Java
Crear un arxiu application.properties amb la configuració de l'aplicació a la mateixa carpeta a on es troba l'arxiu comanda-back.war. El contingut de l'arxiu ha de tenir, com a mínim, les següents propietats:

```
spring.datasource.url=jdbc:oracle:thin:@DB_HOST:DB_PORT:DB_SID
spring.datasource.username=DB_USERNAME
spring.datasource.password=DB_PASSWORD
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://AUTH_HOST/realms/AUTH_REALM
es.caib.comanda.client.app.url=http://localhost:8080/api/apps
es.caib.comanda.client.entorn.url=http://localhost:8080/api/entorns
es.caib.comanda.client.entorn.app.url=http://localhost:8080/api/entornApps
es.caib.comanda.scheduler.app.info.cron=0 */1 * * * *
es.caib.comanda.scheduler.salut.info.cron=0 */1 * * * *
es.caib.comanda.scheduler.estadistica.info.cron=1 0 * * * *
es.caib.comanda.keycloak.base.url=https://AUTH_HOST
es.caib.comanda.keycloak.realm=AUTH_REALM
es.caib.comanda.keycloak.client.id=AUTH_CLIENT_ID
es.caib.comanda.keycloak.username=AUTH_USERNAME
es.caib.comanda.keycloak.password=AUTH_PASSWORD
```
Iniciar l'aplicació amb la següent comanda:

```
$ java -jar comanda-back.war
```
## Execució sobre JBoss EAP 7.2

### Configuració dels datasources
Modificar la secció `<subsystem xmlns="urn:jboss:domain:datasources:5.0">` del fitxer /standalone/configuration/standalone.xml amb el següent contingut:

```
        <subsystem xmlns="urn:jboss:domain:datasources:5.0">
            <datasources>
                <datasource jndi-name="java:jboss/datasources/comandaDS" pool-name="comandaDB" enabled="true">
                    <driver>oracle</driver>
                    <connection-url>JBOSS_DB_URL</connection-url>
                    <security>
                        <user-name>JBOSS_DB_USERNAME</user-name>
                        <password>JBOSS_DB_PASSWORD</password>
                    </security>
                    <pool>
                        <min-pool-size>1</min-pool-size>
                        <max-pool-size>10</max-pool-size>
                        <prefill>true</prefill>
                    </pool>
                </datasource>
                <drivers>
                    <driver name="oracle" module="com.oracle">
                        <driver-class>oracle.jdbc.driver.OracleDriver</driver-class>
                    </driver>
                </drivers>
            </datasources>
        </subsystem>
```
En aquest contingut d'exemple s'han de substituir les següents variables:
- JBOSS_DB_URL: URL (en el format requerit pel driver JDBC) d'accés a la base de dades.
- JBOSS_DB_USERNAME: Usuari d'accés a la base de dades.
- JBOSS_DB_PASSWORD: Contrasenya d'accés a la base de dades.

### Configuració de Keycloak
Modificar la secció `<subsystem xmlns="urn:jboss:domain:keycloak:1.1">` del fitxer /standalone/configuration/standalone.xml amb el següent contingut:

```
        <subsystem xmlns="urn:jboss:domain:keycloak:1.1">
            <realm name="GOIB">
                <auth-server-url>JBOSS_AUTH_URL</auth-server-url>
                <ssl-required>external</ssl-required>
            </realm>
            <secure-deployment name="comanda-back.war">
                <realm>JBOSS_AUTH_REALM}</realm>
                <resource>JBOSS_AUTH_CLIENTID</resource>
                <use-resource-role-mappings>false</use-resource-role-mappings>
                <public-client>true</public-client>
                <verify-token-audience>true</verify-token-audience>
                <principal-attribute>preferred_username</principal-attribute>
            </secure-deployment>
        </subsystem>
```
En aquest contingut d'exemple s'han de substituir les següents variables:
- JBOSS_AUTH_URL: URL d'accés al servidor Keycloak.
- JBOSS_AUTH_REALM: Realm que s'utilitzarà per a l'autenticació.
- JBOSS_AUTH_CLIENTID: Id del client que s'utilitzarà per a l'autenticació.

### Configuració de les propietats
Modificar o afegir la secció `<system-properties>` del fitxer /standalone/configuration/standalone.xml amb el següent contingut:

```
    <system-properties>
        <property name="es.caib.comanda.properties" value="JBOSS_PROPS_PATH/jboss.properties"/>
        <property name="es.caib.comanda.system.properties" value="JBOSS_PROPS_PATH/jboss_system.properties"/>
    </system-properties>
```
En aquest contingut d'exemple s'han de substituir les següents variables:
- JBOSS_PROPS_PATH: carpeta a on es troben els fitxers de propietats.

Exemple de contingut del fitxer jboss.properties:

```
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://AUTH_HOST/realms/AUTH_REALM
es.caib.comanda.client.app.url=http://localhost:8080/api/apps
es.caib.comanda.client.entorn.url=http://localhost:8080/api/entorns
es.caib.comanda.client.entorn.app.url=http://localhost:8080/api/entornApps
es.caib.comanda.scheduler.app.info.cron=0 */1 * * * *
es.caib.comanda.scheduler.salut.info.cron=0 */1 * * * *
es.caib.comanda.scheduler.estadistica.info.cron=1 0 * * * *
es.caib.comanda.keycloak.base.url=https://AUTH_HOST
es.caib.comanda.keycloak.realm=AUTH_REALM
es.caib.comanda.keycloak.client.id=AUTH_CLIENT_ID
es.caib.comanda.keycloak.username=AUTH_USERNAME
es.caib.comanda.keycloak.password=AUTH_PASSWORD
```
Exemple de contingut del fitxer jboss_system.properties:

```
es.caib.comanda.files.path=FILES_PATH
```
### Desplegament i execució
Per a desplegar l'aplicació s'ha de copiar el fitxer comanda-ear/target/comanda.ear generat amb la copilació de maven a la carpeta /standalone/deployments.
Per a iniciar el servidor jboss s'ha d'anar a la carpeta bin del servidor i executar la següent comanda:

```
$ ./standalone.sh
```

## Canvi de versió

```
$ mvn versions:set -DnewVersion=X.Y.Z
$ mvn versions:commit
```
