# Convencions de Testing - COMANDA

## Objectiu
Definir unes convencions comunes per escriure tests consistents, llegibles i mantenibles a tots els mòduls del projecte COMANDA.

## Documents relacionats
- `doc/testing/GUIA_ONBOARDING_TESTING.md`
- `doc/testing/templates/testing/README.md`
- `doc/testing/THRESHOLD_COBERTURA_MINIM.md`

## Abast
Aquest document aplica a:
- `src/test/java` de tots els mòduls Java.
- Tests unitaris, d'integració i de contracte/API.

## Stack de testing
- JUnit 5 (`org.junit.jupiter`)
- Mockito (`org.mockito`)
- AssertJ (`org.assertj.core.api.Assertions`)
- Spring Boot Test (quan toqui)
- Testcontainers (quan hi hagi dependències externes reals)

## Estructura de carpetes
- Ubicar els tests a `src/test/java` replicant el paquet de `src/main/java`.
- Recursos de test a `src/test/resources`.
- Evitar fitxers temporals o residuals dins `src/test` (ex: `.DS_Store`).

## Nomenclatura
- Unit test: `NomClasseTest`
- Test d'integració: `NomClasseIntegrationTest` o `NomClasseIT` (triar un patró i mantenir-lo al mòdul)
- Test parametrizat: `NomClasseParameterizedTest`
- Test de "smoke": `NomFuncionalitatSmokeTest`

Per als mètodes de test:
- Preferir format `accio_quanCondicio_resultatEsperat`
- Exemples:
  - `calculaEstatGlobal_quanNoHiHaComponents_retornUnknown`
  - `save_quanDadesValides_persistenciaCorrecta`

## Estil dels tests
- Patró AAA (Arrange, Act, Assert) clar i separat.
- Un sol comportament verificable per test.
- Noms descriptius (evitar `test1`, `provaX`).
- No reutilitzar estat mutable entre tests.
- Si un setup és complex, encapsular-lo en builders/factories de test.

## Assertions
- Prioritzar AssertJ (`assertThat(...)`) per llegibilitat.
- Usar `assertThrows` per excepcions.
- Evitar assertions massa genèriques (`assertTrue(x != null)`), preferir assertions específiques.

## Mocks i stubs
- Per unit tests, mockejar dependències externes (repositoris, clients HTTP, missatgeria, etc.).
- Verificar interaccions només quan aportin valor funcional.
- Evitar sobreverificar implementació interna.
- No mockejar la classe sota prova.

## Tipus de test i quan usar-los
- Unitari:
  - Lògica de negoci, helpers, mappers, validadors.
  - Sense context Spring complet.
- Integració:
  - Controladors + capa de servei + persistència.
  - Amb Spring context i base de dades de test quan calgui.
- Contracte/API:
  - Validar compatibilitat i disponibilitat de endpoints/models.
  - Útil per mòduls `comanda-api-test*`.

## Dades de test
- Construir dades mínimes necessàries per entendre cada cas.
- Evitar literals duplicats; usar constants locals semàntiques.
- Si hi ha JSON de mostra, guardar-lo a `src/test/resources`.

## Gestió de temps i aleatorietat
- Evitar `now()` directament dins assertions si pot fer flaky el test.
- Injecció de rellotge o valors fixos quan sigui possible.
- No dependre d'ordre no determinista de col.leccions.

## Convencions per Spring tests
- Triar l'anotació més petita possible:
  - `@ExtendWith(MockitoExtension.class)` per unit tests purs.
  - `@WebMvcTest` per controladors.
  - `@DataJpaTest` per repositoris.
  - `@SpringBootTest` només quan realment es necessita context complet.
- Evitar carregar tot el context si no aporta valor al test.

## Qualitat i manteniment
- Cada bug rellevant corregit ha d'incorporar almenys un test de regressió.
- Els tests han de ser independents entre ells i executables en qualsevol ordre.
- No desactivar tests (`@Disabled`) sense justificació documentada.
- Revisar i eliminar tests comentats o obsolets.

## Cobertura (criteri orientatiu)
- Objectiu mínim orientatiu per mòdul de negoci: 70% línies.
- Prioritzar cobertura sobre:
  - Helpers i serveis crítics
  - Seguretat/permisos
  - Transformacions de dades
- Gestió d'errors
- La cobertura no substitueix la qualitat dels casos.

## Plantilles oficials de tests
Per accelerar la creació de tests, hi ha plantilles reutilitzables a:
- `doc/testing/templates/testing/README.md`

Plantilles disponibles:
- `doc/testing/templates/testing/unit-mockito.java.template`
- `doc/testing/templates/testing/service-mockito.java.template`
- `doc/testing/templates/testing/controller-webmvctest.java.template`
- `doc/testing/templates/testing/repository-datajpa.java.template`
- `doc/testing/templates/testing/integration-springboottest.java.template`
- `doc/testing/templates/testing/api-external-it.java.template`

Quan usar cada plantilla:
- `unit-mockito`: lògica pura sense Spring context.
- `service-mockito`: serveis amb dependències mockejades.
- `controller-webmvctest`: proves de capa REST amb `MockMvc`.
- `repository-datajpa`: queries i comportament de repositoris JPA.
- `integration-springboottest`: fluxos complets amb context complet.
- `api-external-it`: proves de contracte/API contra entorn extern.

Flux recomanat:
1. Copiar la plantilla adequada a `src/test/java` del mòdul.
2. Substituir placeholders `${...}`.
3. Afegir casos mínims: happy path, error i validació.
4. Executar tests del mòdul abans de pujar canvis.

## Execució local
Executar tots els tests:

```bash
mvn test
```

Executar tests d'un mòdul:

```bash
mvn -pl comanda-ms-salut test
```

Executar un sol test:

```bash
mvn -Dtest=SalutInfoHelperTest test
```

## Checklist ràpid abans de pujar canvis
- El test falla abans del canvi i passa després (si és regressió).
- Nom i intenció del test són clars.
- No hi ha sleeps ni dependències temporals fràgils.
- No hi ha mocks innecessaris.
- El conjunt de tests del mòdul passa en local.
