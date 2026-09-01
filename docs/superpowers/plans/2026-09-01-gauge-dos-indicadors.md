# Gauge amb 2 indicadors (valor + màxim) i percentatge — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish the `DOS_INDICADORS` gauge-chart mode (1 value indicator + 1 max indicator, currently a stub that throws `NotImplementedException`), fix the gauge `tipusDades` selector to offer the correct 2 options, and reactivate the `tipusValors` (NUMERIC/PERCENTAGE) toggle for gauges with 2 indicators.

**Architecture:** Add a `rol` discriminator column (`VALOR`/`MAXIM`) to the existing `est_indicador_table` so a gauge's two indicators can be told apart reliably (list position alone is not guaranteed by JPA across a reload). Reuse the existing `getValorsGraficVarisIndicadors` query path (already N-indicator capable) instead of writing new SQL. Reactivate the pre-existing but fully commented-out `tipusValors`/`GraficValueTypeEnum` field end-to-end.

**Tech Stack:** Java 11 / Spring Boot / JPA / Liquibase (`comanda-ms-estadistica`); React/TypeScript, `reactlib` in-house form framework, MUI X Charts, Vitest (`comanda-ms-visualitzacio`).

**Spec:** `docs/superpowers/specs/2026-09-01-gauge-dos-indicadors-design.md`

## Global Constraints

- No data migration/backfill needed — dashboards are not yet in production.
- No changes to `VARIS_INDICADORS`, `EstadisticaSimpleWidgetEntity`, or non-gauge chart types.
- `tipusValors` defaults to `NUMERIC` when 2 indicators are configured; it is not required/shown for `UN_INDICADOR`.
- When `DOS_INDICADORS` is configured, the manual `gaugeMax` visual field is hidden — the gauge's max always comes from the max indicator (dynamic) or is fixed at 100 (percentage mode).
- Missing/zero max-indicator data must render as `null`/0%, never crash or divide by zero.
- Run `./mvnw -pl comanda-ms-estadistica -am test` after every backend task; run the frontend module's `npm test` after every frontend task, from `comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio`.

---

## Task 1: Data model — `rol` discriminator column

**Files:**
- Create: `comanda-ms-estadistica/src/main/resources/db/changelog/changes/est/0.1.4/0.1.4_est_013.yaml`
- Create: `comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/intf/model/enumerats/IndicadorRolEnum.java`
- Modify: `comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/persist/entity/estadistiques/IndicadorTaulaEntity.java`
- Modify: `comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/intf/model/estadistiques/IndicadorTaula.java`

**Interfaces:**
- Produces: `IndicadorRolEnum { VALOR, MAXIM }`; `IndicadorTaulaEntity.getRol()/setRol()`; `IndicadorTaula.getRol()/setRol()`. Later tasks (2, 3) use this enum and these accessors to distinguish the "value" indicator row from the "max" indicator row within a `DOS_INDICADORS` widget's `indicadorsInfo` list.

This task adds no new observable behavior by itself (the field is unused until Task 2) — its verification is that the full existing test suite (which exercises H2 schema init from these Liquibase changelogs) stays green with the new column in place.

- [ ] **Step 1: Add the Liquibase changeset**

Create `comanda-ms-estadistica/src/main/resources/db/changelog/changes/est/0.1.4/0.1.4_est_013.yaml`:

```yaml
databaseChangeLog:
  - changeSet:
      id: est-gauge-dos-indicadors-rol-0.1.4-001
      author: limit
      comment: >
        Afegir la columna 'rol' a est_indicador_table per distingir, quan un widget
        gràfic de tipus GAUGE_CHART utilitza dos indicadors (DOS_INDICADORS), quina
        fila és l'indicador de valor (VALOR) i quina és l'indicador de màxim (MAXIM).
        Per a la resta de tipusDades aquesta columna es queda a null.
      preConditions:
        - onFail: MARK_RAN
        - not:
            columnExists:
              tableName: ${db_prefix}est_indicador_table
              columnName: rol
      changes:
        - addColumn:
            tableName: ${db_prefix}est_indicador_table
            columns:
              - column:
                  name: rol
                  type: VARCHAR(16 ${varcharUnit})
      rollback:
        - dropColumn:
            tableName: ${db_prefix}est_indicador_table
            columns:
              - column:
                  name: rol
```

No manual registration needed — the master changelog (`db.changelog-estadistica.yaml`) uses `includeAll` over the `changes` directory.

- [ ] **Step 2: Add `IndicadorRolEnum`**

Create `comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/intf/model/enumerats/IndicadorRolEnum.java`:

```java
package es.caib.comanda.estadistica.logic.intf.model.enumerats;

/**
 * Rol d'un indicador dins la llista d'indicadors d'un widget gràfic, quan cal
 * distingir-los (p. ex. GAUGE_CHART amb DOS_INDICADORS: un indicador de VALOR i
 * un de MAXIM). Per a la resta de tipusDades (UN_INDICADOR, VARIS_INDICADORS,
 * UN_INDICADOR_AMB_DESCOMPOSICIO) aquest camp no s'utilitza i es queda a null.
 *
 * @author Límit Tecnologies
 */
public enum IndicadorRolEnum {
    VALOR,
    MAXIM
}
```

- [ ] **Step 3: Add `rol` to `IndicadorTaulaEntity`**

In `comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/persist/entity/estadistiques/IndicadorTaulaEntity.java`, add the import:

```java
import es.caib.comanda.estadistica.logic.intf.model.enumerats.IndicadorRolEnum;
```

And add the field after `titol`:

```java
    @Column(name = "titol", length = 64)
    private String titol;
    @Column(name = "rol", length = 16)
    @Enumerated(EnumType.STRING)
    private IndicadorRolEnum rol;
```

- [ ] **Step 4: Add `rol` to `IndicadorTaula` (DTO)**

In `comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/intf/model/estadistiques/IndicadorTaula.java`, add the import:

```java
import es.caib.comanda.estadistica.logic.intf.model.enumerats.IndicadorRolEnum;
```

And add the field after `titol` (no `@NotNull` — only meaningful for `DOS_INDICADORS`, validated conditionally in Task 3):

```java
    @NotNull
    @Size(max = 64)
    private String titol;
    private IndicadorRolEnum rol;
```

- [ ] **Step 5: Run the full module test suite to confirm no regressions**

Run: `./mvnw -pl comanda-ms-estadistica -am test`
Expected: all tests pass (same count as before this change), confirming the new column/schema doesn't break H2 initialization or any existing serialization.

- [ ] **Step 6: Commit**

```bash
git add comanda-ms-estadistica/src/main/resources/db/changelog/changes/est/0.1.4/0.1.4_est_013.yaml \
        comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/intf/model/enumerats/IndicadorRolEnum.java \
        comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/persist/entity/estadistiques/IndicadorTaulaEntity.java \
        comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/intf/model/estadistiques/IndicadorTaula.java
git commit -m "feat(estadistica): afegir columna rol a est_indicador_table per a DOS_INDICADORS"
```

---

## Task 2: Backend — persist/read the max indicator (`EstadisticaGraficWidgetHelper`)

**Files:**
- Modify: `comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/persist/entity/widget/EstadisticaGraficWidgetEntity.java`
- Modify: `comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/intf/model/widget/EstadisticaGraficWidget.java`
- Modify: `comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/helper/EstadisticaGraficWidgetHelper.java`
- Test: `comanda-ms-estadistica/src/test/java/es/caib/comanda/estadistica/logic/helper/EstadisticaGraficWidgetHelperTest.java`

**Interfaces:**
- Consumes: `IndicadorRolEnum` (Task 1), `IndicadorTaulaEntity.getRol()/setRol()` (Task 1).
- Produces: `EstadisticaGraficWidget.indicadorMax : ResourceReference<Indicador,Long>`, `.titolIndicadorMax : String`, `.agregacioMax : TableColumnsEnum`, `.unitatAgregacioMax : PeriodeUnitat`, `.tipusValors : GraficValueTypeEnum` — Tasks 3, 4, 6 and 7 read/write these. `EstadisticaGraficWidgetHelper.upsertColumnes(...)`/`.afterCoversionGetColumnes(...)` now correctly round-trip 2 indicator rows for `DOS_INDICADORS`, keyed by `rol` rather than list position.

- [ ] **Step 1: Reactivate `tipusValors` and add the max-indicator transient fields on the entity/DTO**

In `EstadisticaGraficWidgetEntity.java`, add the import:

```java
import es.caib.comanda.estadistica.logic.intf.model.enumerats.GraficValueTypeEnum;
```

Replace the commented block:

```java
//    // Format dels valors a mostrar: NO_MOSTRAR, NUMERIC, PERCENTATGE
//    @Column(name = "tipus_valors", length = 16, nullable = false)
//    @Enumerated(EnumType.STRING)
//    private GraficValueTypeEnum tipusValors;
```

with:

```java
    // Format dels valors a mostrar quan tipusDades == DOS_INDICADORS: NUMERIC, PERCENTAGE
    @Column(name = "tipus_valors", length = 16)
    @Enumerated(EnumType.STRING)
    private GraficValueTypeEnum tipusValors;
```

(Note: `nullable = false` is dropped — the column is only meaningful for `DOS_INDICADORS`, so it must be nullable for every other `tipusDades`.)

In `EstadisticaGraficWidget.java`, replace:

```java
//    @NotNull
//    private GraficValueTypeEnum tipusValors;
```

with:

```java
    private GraficValueTypeEnum tipusValors;
```

and add the import `import es.caib.comanda.estadistica.logic.intf.model.enumerats.GraficValueTypeEnum;`.

In the same file, add the max-indicator transient fields right after the existing single-indicator transient block:

```java
    @Transient
    private ResourceReference<Indicador, Long> indicador;
    @Size(max = 64)
    @Transient
    private String titolIndicador;
    @Transient
    @JsonSetter(nulls = Nulls.SKIP)
    private TableColumnsEnum agregacio;
    @Transient
    @JsonSetter(nulls = Nulls.SKIP)
    private PeriodeUnitat unitatAgregacio;

    // Segon indicador (màxim), només per a tipusDades == DOS_INDICADORS
    @Transient
    private ResourceReference<Indicador, Long> indicadorMax;
    @Size(max = 64)
    @Transient
    private String titolIndicadorMax;
    @Transient
    @JsonSetter(nulls = Nulls.SKIP)
    private TableColumnsEnum agregacioMax;
    @Transient
    @JsonSetter(nulls = Nulls.SKIP)
    private PeriodeUnitat unitatAgregacioMax;
}
```

- [ ] **Step 2: Write the failing tests for `upsertColumnes`/`afterCoversionGetColumnes`**

In `EstadisticaGraficWidgetHelperTest.java`, replace the two existing coverage-only stub tests
(`upsertColumnes_quanDosIndicadors_llavorsExecutaSenseErrors` and
`afterCoversionGetColumnes_quanDosIndicadors_llavorsExecutaSenseErrors`) with:

```java
@Test
@DisplayName("upsertColumnes: quan és DOS_INDICADORS, persisteix 2 files amb rol VALOR i MAXIM")
void upsertColumnes_quanDosIndicadorsAmbIndicadorMax_llavorsPersisteixDuesFilesAmbRolCorrecte() {
    // Arrange
    entity.setTipusDades(TipusGraficDataEnum.DOS_INDICADORS);
    entity.setIndicadorsInfo(new ArrayList<>());

    IndicadorEntity indicadorValor = new IndicadorEntity();
    indicadorValor.setId(1L);
    IndicadorEntity indicadorMaxim = new IndicadorEntity();
    indicadorMaxim.setId(2L);

    resource.setIndicador(ResourceReference.toResourceReference(1L));
    resource.setTitolIndicador("Valor");
    resource.setAgregacio(TableColumnsEnum.SUM);
    resource.setIndicadorMax(ResourceReference.toResourceReference(2L));
    resource.setTitolIndicadorMax("Màxim");
    resource.setAgregacioMax(TableColumnsEnum.SUM);

    when(indicadorRepository.findById(1L)).thenReturn(Optional.of(indicadorValor));
    when(indicadorRepository.findById(2L)).thenReturn(Optional.of(indicadorMaxim));
    when(indicadorTaulaRepository.save(any(IndicadorTaulaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

    // Act
    estadisticaGraficWidgetHelper.upsertColumnes(entity, resource);

    // Assert
    assertThat(entity.getIndicadorsInfo()).hasSize(2);
    IndicadorTaulaEntity filaValor = entity.getIndicadorsInfo().stream()
        .filter(f -> IndicadorRolEnum.VALOR.equals(f.getRol())).findFirst().orElseThrow();
    IndicadorTaulaEntity filaMaxim = entity.getIndicadorsInfo().stream()
        .filter(f -> IndicadorRolEnum.MAXIM.equals(f.getRol())).findFirst().orElseThrow();
    assertThat(filaValor.getTitol()).isEqualTo("Valor");
    assertThat(filaValor.getIndicador().getId()).isEqualTo(1L);
    assertThat(filaMaxim.getTitol()).isEqualTo("Màxim");
    assertThat(filaMaxim.getIndicador().getId()).isEqualTo(2L);
    verify(indicadorTaulaRepository, times(2)).save(any(IndicadorTaulaEntity.class));
}

@Test
@DisplayName("afterCoversionGetColumnes: llegeix per rol, no per posició, encara que la llista arribi en ordre invers")
void afterCoversionGetColumnes_quanDosIndicadorsAmbFilesEnOrdreInvers_llavorsOmpleIndicadorIIndicadorMaxCorrectament() {
    // Arrange
    entity.setTipusDades(TipusGraficDataEnum.DOS_INDICADORS);

    IndicadorEntity indicadorValor = new IndicadorEntity();
    indicadorValor.setId(10L);
    indicadorValor.setCodi("IND_VALOR");
    IndicadorEntity indicadorMaxim = new IndicadorEntity();
    indicadorMaxim.setId(20L);
    indicadorMaxim.setCodi("IND_MAXIM");

    IndicadorTaulaEntity filaMaxim = new IndicadorTaulaEntity();
    filaMaxim.setRol(IndicadorRolEnum.MAXIM);
    filaMaxim.setIndicador(indicadorMaxim);
    filaMaxim.setTitol("Màxim");
    filaMaxim.setAgregacio(TableColumnsEnum.SUM);

    IndicadorTaulaEntity filaValor = new IndicadorTaulaEntity();
    filaValor.setRol(IndicadorRolEnum.VALOR);
    filaValor.setIndicador(indicadorValor);
    filaValor.setTitol("Valor");
    filaValor.setAgregacio(TableColumnsEnum.SUM);

    // Deliberadament en ordre invers (MAXIM abans que VALOR) per demostrar que la
    // lectura és per rol, no per get(0)/get(1).
    entity.setIndicadorsInfo(List.of(filaMaxim, filaValor));

    // Act
    estadisticaGraficWidgetHelper.afterCoversionGetColumnes(entity, resource);

    // Assert
    assertThat(resource.getIndicador().getId()).isEqualTo(10L);
    assertThat(resource.getTitolIndicador()).isEqualTo("Valor");
    assertThat(resource.getIndicadorMax().getId()).isEqualTo(20L);
    assertThat(resource.getTitolIndicadorMax()).isEqualTo("Màxim");
}
```

(Keep the existing `import static org.mockito.ArgumentMatchers.any;`/`org.mockito.Mockito.{times,verify,when}` etc. already present in the file; add `import es.caib.comanda.estadistica.logic.intf.model.enumerats.IndicadorRolEnum;` and `import java.util.Optional;` if not already imported.)

- [ ] **Step 3: Run the tests to verify they fail**

Run: `./mvnw -pl comanda-ms-estadistica test -Dtest=EstadisticaGraficWidgetHelperTest`
Expected: FAIL — `entity.getIndicadorsInfo()` has size 1 (only the value row), `resource.getIndicadorMax()` is `null` (compile error until Step 1's fields exist; behavioral failure once it compiles).

- [ ] **Step 4: Implement `upsertColumnes`/`afterCoversionGetColumnes` for `DOS_INDICADORS`**

In `EstadisticaGraficWidgetHelper.java`, add the import:

```java
import es.caib.comanda.estadistica.logic.intf.model.enumerats.IndicadorRolEnum;
```

and `import java.util.ArrayList;` (not currently imported).

Replace the whole `else` branch of `upsertColumnes` (the non-`VARIS_INDICADORS` branch) plus the trailing `DOS_INDICADORS` TODO block with:

```java
        } else {
            IndicadorTaulaEntity indicadorTaulaEntity = findIndicadorTaulaByRol(entity, IndicadorRolEnum.VALOR);
            if (indicadorTaulaEntity == null) {
                // Compatibilitat amb UN_INDICADOR/UN_INDICADOR_AMB_DESCOMPOSICIO, que no usen 'rol':
                // la fila de valor és senzillament la primera (única) fila de la llista.
                indicadorTaulaEntity = entity.getIndicadorsInfo() == null || entity.getIndicadorsInfo().isEmpty()
                        ? null
                        : entity.getIndicadorsInfo().get(0);
            }
            if (indicadorTaulaEntity == null) {
                indicadorTaulaEntity = new IndicadorTaulaEntity();
                indicadorTaulaEntity.setWidget(entity);
            }
            indicadorTaulaEntity.setTitol(resource.getTitolIndicador());
            indicadorTaulaEntity.setAgregacio(resource.getAgregacio());
            indicadorTaulaEntity.setUnitatAgregacio(TableColumnsEnum.AVERAGE.equals(resource.getAgregacio()) ? resource.getUnitatAgregacio() : null);
            indicadorTaulaEntity.setRol(TipusGraficDataEnum.DOS_INDICADORS.equals(entity.getTipusDades()) ? IndicadorRolEnum.VALOR : null);
            if (resource.getIndicador() != null && resource.getIndicador().getId() != null) {
                if (Objects.isNull(indicadorTaulaEntity.getIndicadorId()) ||
                        !Objects.equals(indicadorTaulaEntity.getIndicadorId(), resource.getIndicador().getId())) {
                    indicadorRepository.findById(resource.getIndicador().getId())
                            .ifPresent(indicadorTaulaEntity::setIndicador);
                }
            }
            indicadorTaulaEntity = indicadorTaulaRepository.save(indicadorTaulaEntity);

            List<IndicadorTaulaEntity> novaLlista = new ArrayList<>();
            novaLlista.add(indicadorTaulaEntity);

            if (TipusGraficDataEnum.DOS_INDICADORS.equals(entity.getTipusDades())) {
                IndicadorTaulaEntity indicadorMaxEntity = upsertIndicadorMax(entity, resource);
                if (indicadorMaxEntity != null) {
                    novaLlista.add(indicadorMaxEntity);
                }
            } else {
                // Ja no és DOS_INDICADORS: eliminar una possible fila MAXIM residual d'una configuració anterior.
                IndicadorTaulaEntity maxResidual = findIndicadorTaulaByRol(entity, IndicadorRolEnum.MAXIM);
                if (maxResidual != null) {
                    indicadorTaulaRepository.delete(maxResidual);
                }
            }
            entity.setIndicadorsInfo(novaLlista);
        }
    }

    private IndicadorTaulaEntity upsertIndicadorMax(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource) {
        if (resource.getIndicadorMax() == null || resource.getIndicadorMax().getId() == null) {
            return null;
        }
        IndicadorTaulaEntity indicadorMaxEntity = findIndicadorTaulaByRol(entity, IndicadorRolEnum.MAXIM);
        if (indicadorMaxEntity == null) {
            indicadorMaxEntity = new IndicadorTaulaEntity();
            indicadorMaxEntity.setWidget(entity);
        }
        indicadorMaxEntity.setTitol(resource.getTitolIndicadorMax());
        indicadorMaxEntity.setAgregacio(resource.getAgregacioMax());
        indicadorMaxEntity.setUnitatAgregacio(TableColumnsEnum.AVERAGE.equals(resource.getAgregacioMax()) ? resource.getUnitatAgregacioMax() : null);
        indicadorMaxEntity.setRol(IndicadorRolEnum.MAXIM);
        if (Objects.isNull(indicadorMaxEntity.getIndicadorId()) ||
                !Objects.equals(indicadorMaxEntity.getIndicadorId(), resource.getIndicadorMax().getId())) {
            indicadorRepository.findById(resource.getIndicadorMax().getId())
                    .ifPresent(indicadorMaxEntity::setIndicador);
        }
        return indicadorTaulaRepository.save(indicadorMaxEntity);
    }

    private IndicadorTaulaEntity findIndicadorTaulaByRol(EstadisticaGraficWidgetEntity entity, IndicadorRolEnum rol) {
        if (entity.getIndicadorsInfo() == null) {
            return null;
        }
        return entity.getIndicadorsInfo().stream()
                .filter(ind -> rol.equals(ind.getRol()))
                .findFirst()
                .orElse(null);
    }
```

(This removes the old trailing `if (TipusGraficDataEnum.DOS_INDICADORS.equals(entity.getTipusDades())) { // TODO: Segon indicador }` block at the end of `upsertColumnes` entirely — it's now handled inline above.)

Replace the `else` branch of `afterCoversionGetColumnes` plus its trailing `DOS_INDICADORS` TODO block with:

```java
        } else {
            IndicadorTaulaEntity indicadorTaula = findIndicadorTaulaByRol(entity, IndicadorRolEnum.VALOR);
            if (indicadorTaula == null && entity.getIndicadorsInfo() != null && !entity.getIndicadorsInfo().isEmpty()) {
                indicadorTaula = entity.getIndicadorsInfo().get(0);
            }
            if (indicadorTaula != null) {
                IndicadorEntity indicador = indicadorTaula.getIndicador();
                resource.setIndicador(ResourceReference.toResourceReference(indicador.getId(), indicador.getCodiNomDescription()));
                resource.setTitolIndicador(indicadorTaula.getTitol());
                resource.setAgregacio(indicadorTaula.getAgregacio());
                resource.setUnitatAgregacio(indicadorTaula.getUnitatAgregacio());
            }

            if (TipusGraficDataEnum.DOS_INDICADORS.equals(entity.getTipusDades())) {
                IndicadorTaulaEntity indicadorMaxTaula = findIndicadorTaulaByRol(entity, IndicadorRolEnum.MAXIM);
                if (indicadorMaxTaula != null) {
                    IndicadorEntity indicadorMax = indicadorMaxTaula.getIndicador();
                    resource.setIndicadorMax(ResourceReference.toResourceReference(indicadorMax.getId(), indicadorMax.getCodiNomDescription()));
                    resource.setTitolIndicadorMax(indicadorMaxTaula.getTitol());
                    resource.setAgregacioMax(indicadorMaxTaula.getAgregacio());
                    resource.setUnitatAgregacioMax(indicadorMaxTaula.getUnitatAgregacio());
                }
            }
        }
    }
```

- [ ] **Step 5: Run the tests to verify they pass**

Run: `./mvnw -pl comanda-ms-estadistica test -Dtest=EstadisticaGraficWidgetHelperTest`
Expected: PASS, all tests in the class green.

- [ ] **Step 6: Run the full module suite**

Run: `./mvnw -pl comanda-ms-estadistica -am test`
Expected: all green, no regressions in `UN_INDICADOR`/`UN_INDICADOR_AMB_DESCOMPOSICIO`/`VARIS_INDICADORS` handling.

- [ ] **Step 7: Commit**

```bash
git add comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/persist/entity/widget/EstadisticaGraficWidgetEntity.java \
        comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/intf/model/widget/EstadisticaGraficWidget.java \
        comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/helper/EstadisticaGraficWidgetHelper.java \
        comanda-ms-estadistica/src/test/java/es/caib/comanda/estadistica/logic/helper/EstadisticaGraficWidgetHelperTest.java
git commit -m "feat(estadistica): persistir i llegir l'indicador màxim de DOS_INDICADORS per rol"
```

---

## Task 3: Backend — validate `DOS_INDICADORS` (`ValidGraficWidgetValidator`)

**Files:**
- Modify: `comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/back/validation/ValidGraficWidgetValidator.java`
- Test: `comanda-ms-estadistica/src/test/java/es/caib/comanda/estadistica/back/validation/ValidGraficWidgetValidatorTest.java`

**Interfaces:**
- Consumes: `EstadisticaGraficWidget.indicadorMax/agregacioMax/unitatAgregacioMax` (Task 2).
- Produces: validation errors on `indicadorMax`/`agregacioMax`/`unitatAgregacioMax` paths when missing for `DOS_INDICADORS`.

Note on the spec: the spec (§6.2) suggested extending `validateIndicadorsInfo`'s cross-row checks (mixed units, mixed percentage) to `DOS_INDICADORS`. That method iterates `widget.getIndicadorsInfo()` (the raw list DTO field), which `DOS_INDICADORS` does **not** populate — Task 2 stores the two indicators via the dedicated `indicador`/`indicadorMax` transient fields instead, matching how the frontend form will bind them (Task 6). So the equivalent checks are implemented directly against those two fields below, reusing the same message keys, rather than by widening that method's guard.

- [ ] **Step 1: Write the failing tests**

Add to `ValidGraficWidgetValidatorTest.java` (same `@BeforeEach`/mock pattern as the existing tests in the file):

```java
@Test
void testInvalidDosIndicadorsSenseIndicadorMax() {
    EstadisticaGraficWidget widget = new EstadisticaGraficWidget();
    widget.setTipusDades(TipusGraficDataEnum.DOS_INDICADORS);
    widget.setTempsAgrupacio(PeriodeUnitat.MES);
    widget.setIndicador(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(1L));
    widget.setAgregacio(TableColumnsEnum.SUM);

    assertFalse(validator.isValid(widget, context));
}

@Test
void testValidDosIndicadorsAmbTotsElsCamps() {
    EstadisticaGraficWidget widget = new EstadisticaGraficWidget();
    widget.setTipusDades(TipusGraficDataEnum.DOS_INDICADORS);
    widget.setTempsAgrupacio(PeriodeUnitat.MES);
    widget.setIndicador(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(1L));
    widget.setAgregacio(TableColumnsEnum.SUM);
    widget.setIndicadorMax(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(2L));
    widget.setAgregacioMax(TableColumnsEnum.SUM);

    assertTrue(validator.isValid(widget, context));
}

@Test
void testInvalidDosIndicadorsPercentatgeMesclat() {
    EstadisticaGraficWidget widget = new EstadisticaGraficWidget();
    widget.setTipusDades(TipusGraficDataEnum.DOS_INDICADORS);
    widget.setTempsAgrupacio(PeriodeUnitat.MES);
    widget.setIndicador(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(1L));
    widget.setAgregacio(TableColumnsEnum.PERCENTAGE);
    widget.setIndicadorMax(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(2L));
    widget.setAgregacioMax(TableColumnsEnum.SUM);

    assertFalse(validator.isValid(widget, context));
}

@Test
void testInvalidDosIndicadorsUnitatsDiferents() {
    EstadisticaGraficWidget widget = new EstadisticaGraficWidget();
    widget.setTipusDades(TipusGraficDataEnum.DOS_INDICADORS);
    widget.setTempsAgrupacio(PeriodeUnitat.MES);
    widget.setIndicador(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(1L));
    widget.setAgregacio(TableColumnsEnum.AVERAGE);
    widget.setUnitatAgregacio(PeriodeUnitat.DIA);
    widget.setIndicadorMax(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(2L));
    widget.setAgregacioMax(TableColumnsEnum.AVERAGE);
    widget.setUnitatAgregacioMax(PeriodeUnitat.MES);

    assertFalse(validator.isValid(widget, context));
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./mvnw -pl comanda-ms-estadistica test -Dtest=ValidGraficWidgetValidatorTest`
Expected: `testInvalidDosIndicadorsSenseIndicadorMax` FAILS (currently valid since the `DOS_INDICADORS` branch is a no-op `// Pendent`); `testValidDosIndicadorsAmbTotsElsCamps` passes trivially (also a no-op false positive); the other two also FAIL (no mixed-check exists yet).

- [ ] **Step 3: Implement the `DOS_INDICADORS` validation branch**

In `ValidGraficWidgetValidator.java`, replace:

```java
        } else if (TipusGraficDataEnum.DOS_INDICADORS.equals(widget.getTipusDades())) {
            // Pendent
        }
```

with:

```java
        } else if (TipusGraficDataEnum.DOS_INDICADORS.equals(widget.getTipusDades())) {
            isValid = validateField(widget.getIndicador() != null, context, "indicador", MSG_CAMP_OBLIGATORI) && isValid;
            isValid = validateField(widget.getAgregacio() != null, context, "agregacio", MSG_CAMP_OBLIGATORI) && isValid;
            if (TableColumnsEnum.AVERAGE.equals(widget.getAgregacio())) {
                isValid = validateField(widget.getUnitatAgregacio() != null, context, "unitatAgregacio", MSG_CAMP_OBLIGATORI) && isValid;
            }
            isValid = validateField(widget.getIndicadorMax() != null, context, "indicadorMax", MSG_CAMP_OBLIGATORI) && isValid;
            isValid = validateField(widget.getAgregacioMax() != null, context, "agregacioMax", MSG_CAMP_OBLIGATORI) && isValid;
            if (TableColumnsEnum.AVERAGE.equals(widget.getAgregacioMax())) {
                isValid = validateField(widget.getUnitatAgregacioMax() != null, context, "unitatAgregacioMax", MSG_CAMP_OBLIGATORI) && isValid;
            }

            boolean valorEsPercentatge = TableColumnsEnum.PERCENTAGE.equals(widget.getAgregacio());
            boolean maximEsPercentatge = TableColumnsEnum.PERCENTAGE.equals(widget.getAgregacioMax());
            if (valorEsPercentatge != maximEsPercentatge) {
                addConstraintViolation(context, MSG_PERCENTATGE_MIX, "agregacioMax");
                isValid = false;
            }

            if (TableColumnsEnum.AVERAGE.equals(widget.getAgregacio()) && TableColumnsEnum.AVERAGE.equals(widget.getAgregacioMax())
                    && widget.getUnitatAgregacio() != null && !widget.getUnitatAgregacio().equals(widget.getUnitatAgregacioMax())) {
                addConstraintViolation(context, MSG_DIFERENTS_UNITATS, "unitatAgregacioMax");
                isValid = false;
            }
        }
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./mvnw -pl comanda-ms-estadistica test -Dtest=ValidGraficWidgetValidatorTest`
Expected: PASS, all tests in the class green.

- [ ] **Step 5: Commit**

```bash
git add comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/back/validation/ValidGraficWidgetValidator.java \
        comanda-ms-estadistica/src/test/java/es/caib/comanda/estadistica/back/validation/ValidGraficWidgetValidatorTest.java
git commit -m "feat(estadistica): validar l'indicador màxim de DOS_INDICADORS"
```

---

## Task 4: Backend — query and shape 2-indicator gauge data (`ConsultaEstadisticaHelper`)

**Files:**
- Modify: `comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/intf/model/consulta/InformeWidgetGraficItem.java`
- Modify: `comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/helper/ConsultaEstadisticaHelper.java`
- Test: `comanda-ms-estadistica/src/test/java/es/caib/comanda/estadistica/logic/helper/ConsultaEstadisticaHelperTest.java`

**Interfaces:**
- Consumes: `IndicadorRolEnum` (Task 1), `FetRepositoryCustom.getValorsGraficVarisIndicadors(...)` (existing, unchanged signature).
- Produces: for a `GAUGE_CHART` + `DOS_INDICADORS` widget, `InformeWidgetGraficItem.dades` is `[{"value": Double|null, "max": Double|null}]` — Task 8 (frontend) consumes exactly this shape via `dades[0].value`/`dades[0].max`.

- [ ] **Step 1: Uncomment `tipusValors` on `InformeWidgetGraficItem`**

In `InformeWidgetGraficItem.java`, replace:

```java
    private TipusGraficDataEnum tipusDades;
//    private GraficValueTypeEnum tipusValors;
```

with:

```java
    private TipusGraficDataEnum tipusDades;
    private GraficValueTypeEnum tipusValors;
```

Add the import: `import es.caib.comanda.estadistica.logic.intf.model.enumerats.GraficValueTypeEnum;`

- [ ] **Step 2: Write the failing tests**

Add to `ConsultaEstadisticaHelperTest.java`. First, **delete** the now-obsolete test
`getDadesWidgetGrafic_quanDosIndicadors_llancaNotImplementedException` (it asserts behavior we're
removing). Then add:

```java
@Test
@DisplayName("filesToSeries: GAUGE_CHART amb DOS_INDICADORS produeix value i max separats")
void filesToSeries_quanGaugeChartIDosIndicadors_llavorsRetornaValueIMaxSeparats() {
    // Arrange
    List<Map<String, String>> files = Collections.singletonList(
        Map.of("agrupacio", "Total", "col1", "40", "col2", "200"));

    // Act
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
        consultaEstadisticaHelper, "filesToSeries", files, TipusGraficEnum.GAUGE_CHART, TipusGraficDataEnum.DOS_INDICADORS);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).containsEntry("value", 40.0);
    assertThat(result.get(0)).containsEntry("max", 200.0);
}

@Test
@DisplayName("filesToSeries: GAUGE_CHART amb DOS_INDICADORS i sense dades de màxim retorna max null, no 0")
void filesToSeries_quanGaugeChartIDosIndicadorsSenseMaxim_llavorsRetornaMaxNull() {
    // Arrange
    Map<String, String> fila = new java.util.HashMap<>();
    fila.put("agrupacio", "Total");
    fila.put("col1", "40");
    fila.put("col2", null);
    List<Map<String, String>> files = Collections.singletonList(fila);

    // Act
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
        consultaEstadisticaHelper, "filesToSeries", files, TipusGraficEnum.GAUGE_CHART, TipusGraficDataEnum.DOS_INDICADORS);

    // Assert
    assertThat(result.get(0).get("max")).isNull();
}

@Test
@DisplayName("getDadesWidgetGrafic: DOS_INDICADORS consulta ambdós indicadors i no llança excepció")
void getDadesWidgetGrafic_quanDosIndicadors_llavorsConsultaAmbdosIndicadorsSenseError() {
    // Arrange
    DashboardItemEntity dashboardItem = new DashboardItemEntity();
    dashboardItem.setId(1L);
    dashboardItem.setEntornId(1L);

    IndicadorEntity indicadorValor = new IndicadorEntity();
    indicadorValor.setId(1L);
    indicadorValor.setCodi("IND_VALOR");
    IndicadorTaulaEntity filaValor = new IndicadorTaulaEntity();
    filaValor.setRol(IndicadorRolEnum.VALOR);
    filaValor.setIndicador(indicadorValor);
    filaValor.setTitol("Valor");
    filaValor.setAgregacio(TableColumnsEnum.SUM);

    IndicadorEntity indicadorMaxim = new IndicadorEntity();
    indicadorMaxim.setId(2L);
    indicadorMaxim.setCodi("IND_MAXIM");
    IndicadorTaulaEntity filaMaxim = new IndicadorTaulaEntity();
    filaMaxim.setRol(IndicadorRolEnum.MAXIM);
    filaMaxim.setIndicador(indicadorMaxim);
    filaMaxim.setTitol("Màxim");
    filaMaxim.setAgregacio(TableColumnsEnum.SUM);

    EstadisticaGraficWidgetEntity widget = new EstadisticaGraficWidgetEntity();
    widget.setTipusDades(TipusGraficDataEnum.DOS_INDICADORS);
    widget.setTipusGrafic(TipusGraficEnum.GAUGE_CHART);
    widget.setIndicadorsInfo(List.of(filaValor, filaMaxim));
    dashboardItem.setWidget(widget);

    DadesComunsWidgetConsulta dadesComuns = DadesComunsWidgetConsulta.builder()
        .entornAppId(1L).entornCodi("DEV")
        .periodeDates(new PeriodeResolverHelper.PeriodeDates(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)))
        .build();

    when(fetRepository.getValorsGraficVarisIndicadors(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Collections.singletonList(Map.of("agrupacio", "Total", "col1", "40", "col2", "200")));

    // Act
    InformeWidgetItem result = (InformeWidgetItem) ReflectionTestUtils.invokeMethod(
        consultaEstadisticaHelper, "getDadesWidgetGrafic", dashboardItem, dadesComuns, null, null);

    // Assert
    assertThat(result).isInstanceOf(InformeWidgetGraficItem.class);
    InformeWidgetGraficItem graficItem = (InformeWidgetGraficItem) result;
    assertThat(graficItem.getDades()).hasSize(1);
    assertThat(graficItem.getDades().get(0)).containsEntry("value", 40.0);
    assertThat(graficItem.getDades().get(0)).containsEntry("max", 200.0);
}
```

- [ ] **Step 3: Run the tests to verify they fail**

Run: `./mvnw -pl comanda-ms-estadistica test -Dtest=ConsultaEstadisticaHelperTest`
Expected: the `filesToSeries` tests FAIL (falls through to the generic `keys` path, wrong shape); the `getDadesWidgetGrafic` test FAILS with `NotImplementedException`.

- [ ] **Step 4: Implement the query branch and gauge shaping for `DOS_INDICADORS`**

In `ConsultaEstadisticaHelper.java`, add the import: `import es.caib.comanda.estadistica.logic.intf.model.enumerats.IndicadorRolEnum;`

Replace the `indicadorInfo`/`indicadorAgregacio` construction at the top of the `UN_INDICADOR`/`UN_INDICADOR_AMB_DESCOMPOSICIO`/`DOS_INDICADORS` block — it currently does `widget.getIndicadorsInfo().get(0)` positionally, which is unsafe for `DOS_INDICADORS` after a DB reload (list order isn't guaranteed). Replace:

```java
            IndicadorTaulaEntity indicadorInfo = widget.getIndicadorsInfo() != null ? widget.getIndicadorsInfo().get(0) : null;
```

with:

```java
            IndicadorTaulaEntity indicadorInfo = resolveIndicadorInfoPerRol(widget, IndicadorRolEnum.VALOR);
```

Then replace the stub:

```java
            } else if (DOS_INDICADORS.equals(widget.getTipusDades())) {
                throw new NotImplementedException("La configuració de 2 indicadors encara no ha estat implementada");
            }
```

with:

```java
            } else if (DOS_INDICADORS.equals(widget.getTipusDades())) {
                IndicadorTaulaEntity indicadorMaxInfo = resolveIndicadorInfoPerRol(widget, IndicadorRolEnum.MAXIM);
                IndicadorAgregacio indicadorMaxAgregacio = indicadorMaxInfo != null ?
                    IndicadorAgregacio.builder()
                        .indicadorCodi(indicadorMaxInfo.getIndicador().getCodi())
                        .agregacio(indicadorMaxInfo.getAgregacio())
                        .unitatAgregacio(indicadorMaxInfo.getUnitatAgregacio())
                        .termesFormula(resoldreTermesFormula(indicadorMaxInfo.getIndicador().getCodi(), dadesComunsConsulta.getEntornAppId()))
                        .build()
                    : null;

                labels.add(Map.of("id", "agrupacio", "label", getLabelAgrupacioTemporal(tempsAgrupacio)));
                labels.add(Map.of("id", "col1", "label", StringUtils.defaultString(indicadorInfo.getTitol())));
                labels.add(Map.of("id", "col2", "label", indicadorMaxInfo != null ? StringUtils.defaultString(indicadorMaxInfo.getTitol()) : ""));

                files = fetRepository.getValorsGraficVarisIndicadors(
                    dadesComunsConsulta.getEntornAppId(),
                    dadesComunsConsulta.getPeriodeDates().getStart(),
                    dadesComunsConsulta.getPeriodeDates().getEnd(),
                    dimensionsFiltre,
                    List.of(indicadorAgregacio, indicadorMaxAgregacio),
                    tempsAgrupacio,
                    seguretat);
                // files: [{'agrupacio': '', 'col1': '<valor>', 'col2': '<màxim>'}]
            }
```

Add the helper method (near `resoldreTermesFormula`, private, same class):

```java
    private IndicadorTaulaEntity resolveIndicadorInfoPerRol(EstadisticaGraficWidgetEntity widget, IndicadorRolEnum rol) {
        if (widget.getIndicadorsInfo() == null || widget.getIndicadorsInfo().isEmpty()) {
            return null;
        }
        if (!DOS_INDICADORS.equals(widget.getTipusDades())) {
            return widget.getIndicadorsInfo().get(0);
        }
        return widget.getIndicadorsInfo().stream()
            .filter(ind -> rol.equals(ind.getRol()))
            .findFirst()
            .orElse(rol == IndicadorRolEnum.VALOR ? widget.getIndicadorsInfo().get(0) : null);
    }
```

Now wire the gauge shaping. In `filesToSeries`, change the `GAUGE_CHART`/`isSimpleMapping` dispatch:

```java
            case GAUGE_CHART:
                boolean isSimpleMapping = files.get(0).size() == 2;

                if (isSimpleMapping) {
```

to:

```java
            case GAUGE_CHART:
                if (TipusGraficDataEnum.DOS_INDICADORS.equals(tipusDades)) {
                    return convertToGaugeChartSeriesDosIndicadors(files);
                }

                boolean isSimpleMapping = files.get(0).size() == 2;

                if (isSimpleMapping) {
```

(the `case BAR_CHART:`/`case LINE_CHART:`/etc. fall-through above the `switch` on `tipusGrafic` stays a single `case GAUGE_CHART:` shared with the others — only the body of that specific case changes; `isSimpleMapping`'s own `switch (tipusGrafic)` inside stays as-is for the other chart types.)

Add the new shaping method next to `convertToGaugeChartSeriesSimple`:

```java
    private List<Map<String, Object>> convertToGaugeChartSeriesDosIndicadors(List<Map<String, String>> files) {
        return files.stream()
            .map(file -> {
                Map<String, Object> row = new HashMap<>();
                row.put("value", file.get("col1") != null ? (Object) toDouble(file.get("col1")) : null);
                row.put("max", file.get("col2") != null ? (Object) toDouble(file.get("col2")) : null);
                return row;
            })
            .collect(Collectors.toList());
    }
```

Add `import java.util.HashMap;` if not already present (`Map.of(...)` doesn't allow `null` values, hence the mutable `HashMap` here — the spec requires `max` to arrive as `null`, not `0`, when the max indicator has no data for the period).

- [ ] **Step 5: Run the tests to verify they pass**

Run: `./mvnw -pl comanda-ms-estadistica test -Dtest=ConsultaEstadisticaHelperTest`
Expected: PASS, all tests in the class green.

- [ ] **Step 6: Run the full module suite**

Run: `./mvnw -pl comanda-ms-estadistica -am test`
Expected: all green.

- [ ] **Step 7: Commit**

```bash
git add comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/intf/model/consulta/InformeWidgetGraficItem.java \
        comanda-ms-estadistica/src/main/java/es/caib/comanda/estadistica/logic/helper/ConsultaEstadisticaHelper.java \
        comanda-ms-estadistica/src/test/java/es/caib/comanda/estadistica/logic/helper/ConsultaEstadisticaHelperTest.java
git commit -m "feat(estadistica): implementar la consulta i el mapejat de dades per a gauge DOS_INDICADORS"
```

---

## Task 5: Frontend — fix `tipusDades` options and add the max-indicator + `tipusValors` fields

**Files:**
- Modify: `comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio/src/components/estadistiques/EstadisticaGraficWidgetForm.tsx`
- Test: `comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio/src/components/estadistiques/EstadisticaGraficWidgetForm.test.tsx`

**Interfaces:**
- Consumes: backend fields from Task 2/3 — `indicadorMax`, `titolIndicadorMax`, `agregacioMax`, `unitatAgregacioMax`, `tipusValors` (bound by field `name`, resolved dynamically from backend metadata by `reactlib`'s `FormField`/`FormFieldCustomAdvancedSearch` — no frontend enum mirror needed).
- Produces: form data shape `{ ..., indicadorMax, titolIndicadorMax, agregacioMax, unitatAgregacioMax, tipusValors }` that Task 6 (wizard) and Task 7 (backend save flow, already wired since Task 2) consume.

- [ ] **Step 1: Write the failing tests**

Add to `EstadisticaGraficWidgetForm.test.tsx`:

```tsx
it('EstadisticaGraficWidgetForm_quanEsGaugeChartAmbDosIndicadors_mostraElsCampsDelIndicadorMaxIElSelectorTipusValors', () => {
    mocks.useFormContextMock.mockReturnValue({
        data: {
            aplicacio: { id: 7 },
            tipusGrafic: 'GAUGE_CHART',
            tipusDades: 'DOS_INDICADORS',
            agregacio: 'SUM',
            agregacioMax: 'SUM',
        },
        apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
    });

    render(<EstadisticaGraficWidgetForm />);

    expect(screen.getByTestId('advanced-search-indicador')).toBeInTheDocument();
    expect(screen.getByTestId('advanced-search-indicadorMax')).toBeInTheDocument();
    expect(screen.getByTestId('field-titolIndicadorMax')).toBeInTheDocument();
    expect(screen.getByTestId('field-agregacioMax')).toBeInTheDocument();
    expect(screen.getByTestId('field-unitatAgregacioMax')).toBeInTheDocument();
    expect(screen.getByTestId('field-tipusValors')).toBeInTheDocument();
    expect(screen.queryByTestId('field-gaugeMax')).not.toBeInTheDocument();
});

it('EstadisticaGraficWidgetForm_quanEsGaugeChartAmbUnIndicador_mostraGaugeMaxIAmagaCampsDeDosIndicadors', () => {
    mocks.useFormContextMock.mockReturnValue({
        data: {
            aplicacio: { id: 7 },
            tipusGrafic: 'GAUGE_CHART',
            tipusDades: 'UN_INDICADOR',
            agregacio: 'SUM',
        },
        apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
    });

    render(<EstadisticaGraficWidgetForm />);

    expect(screen.getByTestId('field-gaugeMax')).toBeInTheDocument();
    expect(screen.queryByTestId('advanced-search-indicadorMax')).not.toBeInTheDocument();
    expect(screen.queryByTestId('field-tipusValors')).not.toBeInTheDocument();
});
```

- [ ] **Step 2: Run the tests to verify they fail**

Run (from `comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio`): `npm test -- EstadisticaGraficWidgetForm.test.tsx`
Expected: FAIL — none of the `indicadorMax`/`tipusValors` test ids exist yet; `field-gaugeMax` renders unconditionally (not yet hidden for `UN_INDICADOR` test... actually it's shown for both today, so the first test's `queryByTestId('field-gaugeMax')).not.toBeInTheDocument()` assertion fails).

- [ ] **Step 3: Fix `tipusDadesOcultar` and add the new fields**

In `EstadisticaGraficWidgetForm.tsx`, fix the gauge allow-list:

```tsx
        if (isGaugeTypeVisible) {
            return ['UN_INDICADOR', 'VARIS_INDICADORS'];
        }
```

becomes:

```tsx
        if (isGaugeTypeVisible) {
            return ['UN_INDICADOR_AMB_DESCOMPOSICIO', 'VARIS_INDICADORS'];
        }
```

In `renderIndicatorFields`, right after the existing `(isUnIndicador || isUnIndicadorAmbDescomposicio || isDosIndicadors)` block (the one rendering `indicador`/`titolIndicador`/`agregacio`/`unitatAgregacio`), add:

```tsx
                        { isDosIndicadors && (
                            <>
                                <Grid size={4}>
                                    <FormFieldCustomAdvancedSearch
                                        name="indicadorMax"
                                        namedQueries={indicadorDimensioNamedQueries}
                                        advancedSearchColumns={columnesIndicador}
                                        advancedSearchDataGridProps={{ rowHeight: 30, }}
                                        advancedSearchDialogHeight={500}
                                        required
                                    />
                                </Grid>
                                <Grid size={4}><FormField name="titolIndicadorMax" required={false} /></Grid>
                                <Grid size={2}>
                                    <FormField name="agregacioMax" hiddenEnumValues={['FIRST_SEEN', 'LAST_SEEN']} required/>
                                </Grid>
                                <Grid size={2}>
                                    <FormField name="unitatAgregacioMax" required={data.agregacioMax === 'AVERAGE'} disabled={data.agregacioMax !== 'AVERAGE'}/>
                                </Grid>
                                <Grid size={4}>
                                    <FormField name="tipusValors" required/>
                                </Grid>
                            </>
                        )}
```

Remove the old dead line `{/*<Grid size={4}><FormField name="tipusValors" /></Grid>*/}` (it's superseded by the active field above, now correctly scoped to `isDosIndicadors`).

In the gauge-specific visual fields block, hide `gaugeMax` when `isDosIndicadors`:

```tsx
                {isGaugeTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>{t($ => $.page.widget.form.graficGug)}</Typography></Grid>
                        <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="gaugeMin" label={t($ => $.page.widget.atributsVisuals.gaugeMin)} type="number" required={false} /></Grid>
                        { !isDosIndicadors && (
                            <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="gaugeMax" label={t($ => $.page.widget.atributsVisuals.gaugeMax)} type="number" required={false} /></Grid>
                        )}
                        <Grid size={12} sx={{backgroundColor: 'background.paper'}}><FormField name="gaugeRangs" label={t($ => $.page.widget.atributsVisuals.gaugeRangs)} /></Grid>
                    </>
                )}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `npm test -- EstadisticaGraficWidgetForm.test.tsx`
Expected: PASS, all tests in the file green, including the pre-existing field-ordering test (verify it still passes — if the new `indicadorMax`/`tipusValors` fields shift the asserted `data-testid` sequence for a `DOS_INDICADORS` scenario in that test, update its expected order to match; if that test doesn't exercise `DOS_INDICADORS`, it is unaffected).

- [ ] **Step 5: Run the full frontend suite**

Run: `npm test`
Expected: all green.

- [ ] **Step 6: Commit**

```bash
git add comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio/src/components/estadistiques/EstadisticaGraficWidgetForm.tsx \
        comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio/src/components/estadistiques/EstadisticaGraficWidgetForm.test.tsx
git commit -m "feat(visualitzacio): afegir camps d'indicador màxim i tipusValors per a gauge amb 2 indicadors"
```

---

## Task 6: Frontend — mirror the new required fields in `WidgetCreationWizard`

**Files:**
- Modify: `comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio/src/components/estadistiques/WidgetCreationWizard.tsx`
- Test: `comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio/src/components/estadistiques/WidgetCreationWizard.test.tsx`

**Interfaces:**
- Consumes: same field names as Task 5 (`indicadorMax`, `agregacioMax`, `unitatAgregacioMax`, `tipusValors`).

- [ ] **Step 1: Write the failing test**

Add to `WidgetCreationWizard.test.tsx`, following the existing `...quanFaltaUnCampObligatoriALaPassaDIndicadors_noEsPotAvancar` pattern:

```tsx
it('WidgetCreationWizard_quanEsGraficGaugeDosIndicadorsSenseIndicadorMax_noEsPotAvancar', () => {
    mocks.useFormContextMock.mockReturnValue({
        data: {
            aplicacio: { id: 3 },
            tipusGrafic: 'GAUGE_CHART',
            tipusDades: 'DOS_INDICADORS',
            indicador: { id: 1 },
            agregacio: 'SUM',
            tempsAgrupacio: 'MES',
            // falten indicadorMax, agregacioMax i tipusValors
        },
        apiRef: { current: { setFieldValue: vi.fn() } },
    });

    render(
        <WidgetCreationWizard
            open
            dashboard={defaultDashboard}
            dashboardId="1"
            initialWidgetType="GRAFIC"
            initialEntornId={5}
            initialAplicacio={{ id: 3 }}
            onClose={vi.fn()}
            onCreated={vi.fn()}
        />
    );

    fireEvent.click(screen.getByRole('button', { name: 'Següent' }));
    expect(screen.getByRole('button', { name: 'Següent' })).toBeDisabled();
});
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `npm test -- WidgetCreationWizard.test.tsx`
Expected: FAIL — the "Següent" button is currently enabled (the wizard's `isIndicatorsStepValid` doesn't check `indicadorMax`/`agregacioMax`/`tipusValors` for `DOS_INDICADORS`).

- [ ] **Step 3: Implement the extra required-field checks**

In `WidgetCreationWizard.tsx`, inside `isIndicatorsStepValid`, right after the existing block:

```tsx
        if (tipusDades === 'UN_INDICADOR' || tipusDades === 'UN_INDICADOR_AMB_DESCOMPOSICIO' || tipusDades === 'DOS_INDICADORS') {
            if (isEmpty(data?.indicador) || isEmpty(data?.agregacio)) return false;
            if (data.agregacio === 'AVERAGE' && isEmpty(data?.unitatAgregacio)) return false;
        }
```

add:

```tsx
        if (tipusDades === 'DOS_INDICADORS') {
            if (isEmpty(data?.indicadorMax) || isEmpty(data?.agregacioMax)) return false;
            if (data.agregacioMax === 'AVERAGE' && isEmpty(data?.unitatAgregacioMax)) return false;
            if (isEmpty(data?.tipusValors)) return false;
        }
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `npm test -- WidgetCreationWizard.test.tsx`
Expected: PASS, all tests in the file green (confirm the pre-existing `...quanEsTriaSimpleIDadesValides_permetAvancarAIndicadors` and similar tests aren't affected, since this new check is scoped to `tipusDades === 'DOS_INDICADORS'`).

- [ ] **Step 5: Commit**

```bash
git add comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio/src/components/estadistiques/WidgetCreationWizard.tsx \
        comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio/src/components/estadistiques/WidgetCreationWizard.test.tsx
git commit -m "feat(visualitzacio): validar l'indicador màxim i tipusValors al pas d'indicadors de l'assistent"
```

---

## Task 7: Frontend — render the gauge with a dynamic max and percentage mode

**Files:**
- Modify: `comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio/src/components/estadistiques/GraficWidgetVisualization.tsx`
- Test: `comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio/src/components/estadistiques/GraficWidgetVisualization.test.tsx`

**Interfaces:**
- Consumes: `dades[0].value`/`dades[0].max` (`Object`/`number|null`, produced by Task 4's backend shaping); new prop `tipusValors?: 'NUMERIC' | 'PERCENTAGE'`.

- [ ] **Step 1: Extend the `Gauge` mock and write the failing tests**

In `GraficWidgetVisualization.test.tsx`, replace the existing bare `Gauge` mock:

```tsx
    Gauge: () => <div data-testid="gauge-chart">Gauge</div>,
```

with one that forwards the props under test:

```tsx
    Gauge: ({value, valueMin, valueMax, text}: { value?: number; valueMin?: number; valueMax?: number; text?: (params: { value: number | null }) => string }) => (
        <div data-testid="gauge-chart" data-value={value} data-value-min={valueMin} data-value-max={valueMax} data-text={text ? text({ value: value ?? null }) : undefined}>Gauge</div>
    ),
```

Add the new tests:

```tsx
it('GraficWidgetVisualization_quanEsGaugeAmbDosIndicadorsIModeNumeric_elMaximVeDelSegonIndicador', () => {
    renderComponent(
        <GraficWidgetVisualization
            titol="Gràfic gauge"
            tipusGrafic="GAUGE_CHART"
            tipusValors="NUMERIC"
            dades={[{ value: 40, max: 200 }]}
            mostrarVora={false}
            ampleVora={1}
        />
    );

    const gauge = screen.getByTestId('gauge-chart');
    expect(gauge).toHaveAttribute('data-value', '40');
    expect(gauge).toHaveAttribute('data-value-max', '200');
});

it('GraficWidgetVisualization_quanEsGaugeAmbDosIndicadorsIModePercentatge_calculaElPercentatge', () => {
    renderComponent(
        <GraficWidgetVisualization
            titol="Gràfic gauge"
            tipusGrafic="GAUGE_CHART"
            tipusValors="PERCENTAGE"
            dades={[{ value: 40, max: 200 }]}
            mostrarVora={false}
            ampleVora={1}
        />
    );

    const gauge = screen.getByTestId('gauge-chart');
    expect(gauge).toHaveAttribute('data-value', '20');
    expect(gauge).toHaveAttribute('data-value-min', '0');
    expect(gauge).toHaveAttribute('data-value-max', '100');
});

it('GraficWidgetVisualization_quanEsGaugePercentatgeSenseMaxim_noDividiuPerZeroIMostraZero', () => {
    renderComponent(
        <GraficWidgetVisualization
            titol="Gràfic gauge"
            tipusGrafic="GAUGE_CHART"
            tipusValors="PERCENTAGE"
            dades={[{ value: 40 }]}
            mostrarVora={false}
            ampleVora={1}
        />
    );

    const gauge = screen.getByTestId('gauge-chart');
    expect(gauge).toHaveAttribute('data-value', '0');
});
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `npm test -- GraficWidgetVisualization.test.tsx`
Expected: FAIL — `renderGaugeChart` today only reads `dades[0].value` and always uses the static `gaugeMax` prop; `tipusValors` doesn't exist as a prop yet (TS error until Step 3).

- [ ] **Step 3: Implement the dynamic max / percentage rendering**

In `GraficWidgetVisualizationProps`, add:

```tsx
    // Gauge chart
    gaugeMin?: number;
    gaugeMax?: number;
    gaugeColors?: string;
    gaugeRangs?: string;
    tipusValors?: 'NUMERIC' | 'PERCENTAGE';
```

In the props destructuring, add `tipusValors,` next to the other gauge-specific defaults:

```tsx
        // Gauge chart specific
        gaugeMin = 0,
        gaugeMax = 100,
        // gaugeColors = '#d4e6f1,#3498db,#1a5276',
        gaugeRangs = '50,75,100',
        tipusValors,
```

Replace the full `renderGaugeChart` function body:

```tsx
    // Render a gauge chart
    const renderGaugeChart = () => {
        const primeraFila = Array.isArray(dades) && dades.length > 0 ? dades[0] : undefined;
        const valorGauge = primeraFila && primeraFila.value !== undefined && primeraFila.value !== null ? Number(primeraFila.value) : 0;
        const maxValueRaw = primeraFila && primeraFila.max !== undefined && primeraFila.max !== null ? Number(primeraFila.max) : undefined;
        const maximDisponible = maxValueRaw !== undefined && !isNaN(maxValueRaw) && maxValueRaw !== 0;
        const esPercentatge = tipusValors === 'PERCENTAGE';

        const valorMostrat = esPercentatge
            ? (maximDisponible ? (valorGauge / (maxValueRaw as number)) * 100 : 0)
            : valorGauge;
        const valueMinMostrat = esPercentatge ? 0 : gaugeMin;
        const valueMaxMostrat = esPercentatge
            ? 100
            : (maxValueRaw !== undefined && !isNaN(maxValueRaw) ? maxValueRaw : gaugeMax);

        const colors = colorsPaleta ? colorsPaleta.split(',').map(c => c.trim()) : ["#000000"];
        const rangs = gaugeRangs ? gaugeRangs.split(',').map(r => Number(r.trim())).filter(v => !isNaN(v)) : [];
        const getColor = (value: number) => {
            for (let i = 0; i < colors.length && i < rangs.length; i++) {
                if (value < rangs[i]) {
                    return colors[i];
                }
            }
            return colors[colors.length - 1];//Por defecto devolvemos el ultimo valor.
        };

        return (
            <Box sx={{
                width: '100%',
                height: chartHeight,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                overflow: 'hidden',
            }}>
                <Gauge
                    value={valorMostrat}
                    valueMin={valueMinMostrat}
                    valueMax={valueMaxMostrat}
                    text={esPercentatge ? ({value}) => `${Math.round(value ?? 0)}%` : undefined}
                    sx={() => ({
                        [`& .${gaugeClasses.valueArc}`]: {
                            fill: getColor(valorMostrat),
                        },
                        [`& .${gaugeClasses.valueText}`]: {
                            fill: `${chartTextColor} !important`,
                            color: `${chartTextColor} !important`,
                        },
                        [`& .${gaugeClasses.valueText} *`]: {
                            fill: `${chartTextColor} !important`,
                            color: `${chartTextColor} !important`,
                        },
                    })}
                />
            </Box>
        );
    };
```

Finally, update `generateSampleData` for `'GAUGE_CHART'` so the widget-config preview shows a representative max too:

```ts
        case 'GAUGE_CHART':
            return [{ value: 75, max: 100 }];
```

(replacing whatever single-key sample object is there today — locate it via the `generateSampleData` switch, `case 'GAUGE_CHART':`).

- [ ] **Step 4: Run the tests to verify they pass**

Run: `npm test -- GraficWidgetVisualization.test.tsx`
Expected: PASS, all tests in the file green, including the pre-existing
`GraficWidgetVisualization_quanEsRenderitzaEnModeGauge_mostraElGauge` test (no `dades`/`tipusValors`
passed → falls back to `valorGauge = 0`, `NUMERIC` behavior, `valueMax = gaugeMax = 100` — unchanged
from before).

- [ ] **Step 5: Run the full frontend suite**

Run: `npm test`
Expected: all green.

- [ ] **Step 6: Commit**

```bash
git add comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio/src/components/estadistiques/GraficWidgetVisualization.tsx \
        comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio/src/components/estadistiques/GraficWidgetVisualization.test.tsx
git commit -m "feat(visualitzacio): renderitzar el gauge amb màxim dinàmic i mode percentatge"
```

---

## Self-review notes

- **Spec coverage:** §4 (selector fix) → Task 5 Step 3. §5 (`rol` column) → Task 1. §6.1 (helper) → Task 2. §6.2 (validator) → Task 3 (adapted: direct field checks instead of widening `validateIndicadorsInfo`, see note in Task 3 — same validation outcomes). §6.3 (query) → Task 4. §6.4 (`tipusValors` reactivation) → Task 2 Step 1 (entity/DTO) + Task 4 Step 1 (`InformeWidgetGraficItem`). §7.1 (form) → Task 5. §7.2 (rendering) → Task 7. §8 (testing) → embedded per-task. §9 (file list) → matches Tasks 1–7 exactly, plus `WidgetCreationWizard.tsx` (Task 6), which the spec's file list omitted but is required for the wizard's client-side validation mirror not to silently diverge from the backend validator.
- **Placeholder scan:** no TBD/TODO left in any step; every step has literal code.
- **Type consistency:** `indicadorMax`/`titolIndicadorMax`/`agregacioMax`/`unitatAgregacioMax`/`tipusValors` field names are identical across Task 2 (backend DTO), Task 3 (validator), Task 5 (form), and Task 6 (wizard). `IndicadorRolEnum.VALOR`/`MAXIM` identical across Tasks 1, 2, 4. Gauge data shape `{value, max}` identical across Task 4 (backend) and Task 7 (frontend).
