# Plantilles de tests

Plantilles base per accelerar la creació de tests a COMANDA.

## Fitxers disponibles
- `unit-mockito.java.template`: unit test pur amb Mockito.
- `service-mockito.java.template`: test de servei amb repositoris/client mockejats.
- `controller-webmvctest.java.template`: test de controlador REST amb `@WebMvcTest`.
- `repository-datajpa.java.template`: test de repositori amb `@DataJpaTest`.
- `integration-springboottest.java.template`: test d'integració amb context Spring complet.
- `api-external-it.java.template`: test extern de contracte/API estil `*IT`.

## Ús
1. Copia la plantilla al mòdul objectiu dins `src/test/java`.
2. Substitueix els placeholders `${...}`.
3. Adapta els casos de test (`happy path`, errors, validacions).
4. Executa:

```bash
mvn -pl <modul> test
```
