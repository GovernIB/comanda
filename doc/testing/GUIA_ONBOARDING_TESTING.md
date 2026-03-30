# Guia d'Onboarding de Testing - COMANDA

## Objectiu
Donar una ruta curta i pràctica perquè una persona nova pugui començar a escriure i mantenir tests a COMANDA des del primer dia.

## A qui va dirigida
- Desenvolupadors nous al projecte.
- Desenvolupadors amb experiència al projecte que necessiten un recordatori ràpid del flux de testing.

## Pre-requisits
- Java 11 (o 17 amb perfil `jboss74` quan pertoqui).
- Maven (`mvn`) disponible.
- Repositori clonat i dependències resoltes.

## Primer contacte (30-60 min)
1. Llegeix `doc/testing/CONVENCIONS_TESTING.md`.
2. Llegeix `doc/testing/templates/testing/README.md`.
3. Executa una passada completa de tests:

```bash
mvn test
```

Si és massa lent, valida primer un mòdul concret:

```bash
mvn -pl comanda-ms-salut test
```

## Mapa ràpid de tipus de tests
- Unitari (`*Test`): lògica pura, helpers, mappers, validadors.
- Integració (`*IntegrationTest` / `*IT`): fluxos entre capes amb Spring context.
- Contracte/API (`*IT` extern): validació d'APIs i clients (`comanda-api-test*`).

## Flux recomanat per crear el primer test
1. Tria una classe amb lògica concreta (helper o servei petit).
2. Copia una plantilla des de `doc/testing/templates/testing/`.
3. Substitueix placeholders `${...}`.
4. Crea com a mínim 3 casos:
   - happy path
   - error/control d'excepcions
   - validació o edge case
5. Executa només el test nou:

```bash
mvn -Dtest=NomDelTest test
```

6. Executa els tests del mòdul:

```bash
mvn -pl <modul> test
```

## Plantilla recomanada segons context
- Classe sense Spring i amb dependències mockejables: `unit-mockito.java.template` o `service-mockito.java.template`.
- Endpoint REST: `controller-webmvctest.java.template`.
- Repositori JPA: `repository-datajpa.java.template`.
- Flux complet amb context: `integration-springboottest.java.template`.
- API externa: `api-external-it.java.template`.

## Convencions mínimes obligatòries abans de PR (Pull Request)
- Noms clars de tests: `accio_quanCondicio_resultatEsperat`.
- Patró AAA visible (Arrange, Act, Assert).
- Sense sleeps ni assertions fràgils amb temps real.
- Sense tests comentats o desactivats sense justificació.
- Tests del mòdul en verd localment.

## Errors habituals i com evitar-los
- Carregar massa context Spring:
  - Solució: usar l'anotació més petita (`@WebMvcTest`, `@DataJpaTest`, etc.).
- Sobreús de mocks:
  - Solució: mockejar només dependències externes.
- Assertions poc informatives:
  - Solució: preferir AssertJ i assertions específiques.
- Dades de test difícils de mantenir:
  - Solució: factories/builders de test i recursos a `src/test/resources`.

## Ruta de creixement (primera setmana)
1. Dia 1: executar tests i afegir 1 unit test simple.
2. Dia 2-3: cobrir una classe de servei amb mocks.
3. Dia 4: afegir un test de controlador o repositori.
4. Dia 5: contribuir una PR amb test de regressió.

## Definició de "Done" per testing
Una tasca de backend es considera completa quan:
- El comportament funcional nou està cobert amb tests.
- Si hi havia bug, existeix test de regressió.
- El mòdul passa tests en local.
- El codi segueix `doc/testing/CONVENCIONS_TESTING.md`.
