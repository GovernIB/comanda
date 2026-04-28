Comanda API Test
================

Mòdul de tests d'integració que utilitza `comanda-lib` per fer crides reals a un servidor COMANDA (o una APP que implementi els endpoints) indicat per paràmetres.

Paràmetres disponibles (System properties o variables d'entorn):
- `comanda.api.basePath` (obligatori per executar): Base URL del servidor, p. ex. `http://localhost:8080`
- `comanda.api.user` i `comanda.api.password` (opcional): credencials per a autenticació Basic
- `comanda.api.authHeader` (opcional): valor complet per a la capçalera `Authorization` (p. ex. `Bearer <token>`). Si s'indica, té prioritat sobre user/password.

- `comanda.api2.basePath` (opcional però necessari per executar els tests addicionals de salut/estadística/log): Base URL d'un segon servidor (APP) contra el qual es faran crides als endpoints de Salut, Estadística i Log
- `comanda.api2.user` i `comanda.api2.password` (opcional): credencials Basic per al segon servidor
- `comanda.api2.authHeader` (opcional): capçalera `Authorization` completa per al segon servidor (prioritza sobre user/password)

Execució (exemples):
1) Amb Basic auth:
```
mvn -pl comanda-lib,comanda-api-test -am -Dcomanda.api.basePath=http://localhost:8080 \
    -Dcomanda.api.user=usuari -Dcomanda.api.password=contrasenya test
```

2) Amb Bearer token:
```
mvn -pl comanda-lib,comanda-api-test -am -Dcomanda.api.basePath=http://localhost:8080 \
    -Dcomanda.api.authHeader="Bearer <token>" test
```

3) Sense autenticació:
```
mvn -pl comanda-lib,comanda-api-test -am -Dcomanda.api.basePath=http://localhost:8080 test
```

4) Activar també els tests de Salut/Estadística/Log contra un altre servidor (APP):
```
mvn -pl comanda-lib,comanda-api-test -am \
  -Dcomanda.api.basePath=http://localhost:8080 \
  -Dcomanda.api2.basePath=https://app.exemple.org/app \
  -Dcomanda.api2.authHeader="Bearer <token-app>" test
```

Si `comanda.api.basePath` no està definit, els tests es marcaran com SKIPPED.

Si `comanda.api2.basePath` no està definit, només s'ometran els tests addicionals que criden Salut/Estadística/Log contra el segon servidor.
