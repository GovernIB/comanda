# Disseny: Gauge amb 2 indicadors (valor + màxim) i visualització en percentatge

- **Data**: 2026-09-01
- **Mòduls afectats**: `comanda-ms-estadistica` (backend), `comanda-ms-visualitzacio` (frontend)
- **Estat**: Aprovat, pendent de pla d'implementació

## 1. Context i problema

Els widgets de tipus `GAUGE_CHART` es configuren mitjançant el camp `tipusDades`
(`TipusGraficDataEnum`), que té 4 valors possibles: `UN_INDICADOR`,
`UN_INDICADOR_AMB_DESCOMPOSICIO`, `VARIS_INDICADORS` i `DOS_INDICADORS`.

Investigant el codi actual s'han trobat tres problemes:

1. **El selector de `tipusDades` mostra les opcions equivocades per a gauge.**
   `EstadisticaGraficWidgetForm.tsx` (`tipusDadesOcultar`, línies 95-109) amaga
   `['UN_INDICADOR', 'VARIS_INDICADORS']` quan el tipus de gràfic és `GAUGE_CHART`.
   Això vol dir que **`UN_INDICADOR` — l'única configuració de gauge que funciona
   avui — queda amagada**, i només es pot triar `UN_INDICADOR_AMB_DESCOMPOSICIO`
   (no té sentit per a un gauge, veure punt 2) o `DOS_INDICADORS` (llança
   `NotImplementedException` en temps d'execució, veure punt 3). Avui dia és
   impossible crear un widget de tipus gauge que funcioni correctament des de la UI.

2. **`UN_INDICADOR_AMB_DESCOMPOSICIO` no és significatiu per a un gauge.** Aquest
   mode permet una dimensió de descomposició que pot retornar diverses files (una
   per valor de dimensió). El renderitzat del gauge
   (`GraficWidgetVisualization.tsx`, `renderGaugeChart`) només agafa `dades[0].value`
   — descarta silenciosament la resta de files sense cap indicació a l'usuari. Un
   gauge només pot mostrar un valor escalar, així que aquesta opció no té encaix
   aquí.

3. **`DOS_INDICADORS` ("1 indicador + 1 indicador per màxim") és un stub sense
   acabar.** Existeix l'enum i l'etiqueta ("1 indicador + 1 indicador per màxim"),
   però:
   - El formulari (`EstadisticaGraficWidgetForm.tsx:184-206`) no renderitza cap
     segon selector d'indicador — `isDosIndicadors` cau al mateix bloc JSX que
     `isUnIndicador`.
   - `EstadisticaGraficWidgetHelper.upsertColumnes`/`afterCoversionGetColumnes`
     tenen dos comentaris `// TODO: Segon indicador` buits.
   - `ValidGraficWidgetValidator.validateTipusDades` té un `// Pendent` buit per
     a `DOS_INDICADORS`.
   - `ConsultaEstadisticaHelper` llança
     `NotImplementedException("La configuració de 2 indicadors encara no ha estat
     implementada")` si mai s'arriba a consultar dades per a un widget amb
     `DOS_INDICADORS`.

   És a dir: si algú aconsegueix guardar un widget amb `DOS_INDICADORS` (cosa que
   avui el formulari no permet fer correctament perquè no hi ha camp pel segon
   indicador), consultar-ne les dades trenca en temps d'execució.

4. **No hi ha manera d'escollir entre mostrar els valors reals dels indicadors o
   un percentatge (0-100).** Existeix un enum `GraficValueTypeEnum {NUMERIC,
   PERCENTAGE}` i un camp `tipusValors` **totalment comentats** a
   `EstadisticaGraficWidgetEntity`, `EstadisticaGraficWidget`,
   `InformeWidgetGraficItem` i al formulari — un esquelet que mai es va acabar de
   connectar.

Actualment `gaugeMax` (l'escala del dial) és sempre un número estàtic configurat
manualment als atributs visuals (`AtributsVisualsGrafic.gaugeMax`, per defecte
100) — mai es deriva de cap indicador de màxim, perquè aquest concepte no existeix
funcionalment enlloc del codi avui.

## 2. Objectius

- Deixar `GAUGE_CHART` amb exactament 2 opcions vàlides de `tipusDades`:
  `UN_INDICADOR` i `DOS_INDICADORS`.
- Implementar `DOS_INDICADORS` de cap a cap: selecció del segon indicador
  (màxim) al formulari, persistència, validació, i consulta/agregació de dades.
- Afegir un selector "mostrar com: valors / percentatge" (`tipusValors`),
  disponible només quan `tipusDades == DOS_INDICADORS`, reactivant l'enum i els
  camps ja escatats però desconnectats.
- Quan hi ha 2 indicadors, l'escala del gauge (`valueMax`) s'ha de derivar
  sempre de l'indicador màxim (en mode valors) o ser fixa 0-100 (en mode
  percentatge) — mai del camp estàtic `gaugeMax`.

## 3. No-objectius

- No es toca `VARIS_INDICADORS`, `EstadisticaSimpleWidgetEntity`, ni cap altre
  tipus de gràfic (`BAR_CHART`, `LINE_CHART`, `PIE_CHART`, etc.).
- No cal migració/backfill de dades existents: els dashboards encara no estan en
  producció.
- No s'implementa cap límit/topall addicional sobre `gaugeMax`; quan hi ha 2
  indicadors, el camp `gaugeMax` simplement desapareix del formulari (veure
  punt 6).

## 4. Canvis al selector `tipusDades` (frontend)

`EstadisticaGraficWidgetForm.tsx`, `tipusDadesOcultar` (línia 105-107):

```tsx
// Abans
if (isGaugeTypeVisible) {
    return ['UN_INDICADOR', 'VARIS_INDICADORS'];
}
// Després
if (isGaugeTypeVisible) {
    return ['UN_INDICADOR_AMB_DESCOMPOSICIO', 'VARIS_INDICADORS'];
}
```

Resultat: el selector de gauge mostra només "1 indicador" i "1 indicador + 1
indicador per màxim".

## 5. Model de dades — discriminar el rol de cada indicador

`IndicadorTaulaEntity` (taula `est_indicador_table`) no té cap columna que
distingeixi el rol d'una fila dins la llista `indicadorsInfo` — només la posició
(`get(0)`, `get(1)`), i un `@OneToMany` sense `@OrderBy` no garanteix l'ordre
després d'una recàrrega des de BD.

**Canvi**: afegir una columna nova `rol` (nullable), amb un nou enum
`IndicadorRolEnum { VALOR, MAXIM }`.

- Per a `UN_INDICADOR`, `UN_INDICADOR_AMB_DESCOMPOSICIO` i `VARIS_INDICADORS`:
  `rol` es queda `null` (no s'utilitza, cap canvi de comportament).
- Per a `DOS_INDICADORS`: `indicadorsInfo` tindrà exactament 2 files, una amb
  `rol = VALOR` i una amb `rol = MAXIM`.

Migració Liquibase nova a `comanda-back/liquibase/` afegint la columna
`rol` (varchar, nullable) a `est_indicador_table`. No cal backfill (dades no en
producció).

## 6. Backend

### 6.1. `EstadisticaGraficWidgetHelper`

Substituir els dos `// TODO: Segon indicador` per lògica que, quan
`tipusDades == DOS_INDICADORS`:

- `upsertColumnes`: persisteix una segona `IndicadorTaulaEntity` amb
  `rol = MAXIM`, seguint el mateix patró d'aplanament que ja s'usa per a la fila
  `VALOR` (que es manté sense canvis, ara amb `rol = VALOR` explícit).
- `afterCoversionGetColumnes`: llegeix la fila amb `rol = MAXIM` (filtrant per
  rol, no per índex) i l'exposa com a nous camps transitoris a
  `EstadisticaGraficWidget`: `indicadorMaxim`, `titolIndicadorMaxim`,
  `agregacioMaxim`, `unitatAgregacioMaxim` — mateix parell de camps que ja
  existeix per a l'indicador principal (`indicador`, `titolIndicador`,
  `agregacio`, `unitatAgregacio`).

### 6.2. `ValidGraficWidgetValidator`

- `validateTipusDades`: implementar la branca `DOS_INDICADORS` (avui
  `// Pendent`) exigint `indicador`, `agregacio` (+ `unitatAgregacio` si
  `AVERAGE`) per a l'indicador principal, i els mateixos 3 camps per a
  `indicadorMaxim`/`agregacioMaxim`/`unitatAgregacioMaxim`.
- `validateIndicadorsInfo`: la seva guarda actual (`if
  (!TipusGraficDataEnum.VARIS_INDICADORS.equals(...)) return true;`) exclou
  `DOS_INDICADORS` de les validacions creuades existents (unitats
  d'agregació mesclades, `PERCENTAGE` mesclat amb altres agregacions). Ampliar
  aquesta guarda perquè també s'apliquin a `DOS_INDICADORS`.

### 6.3. `ConsultaEstadisticaHelper`

Substituir el `throw new NotImplementedException(...)` de la branca
`DOS_INDICADORS` per lògica real que:

- Obté els valors agregats de tots dos indicadors (valor i màxim) per al
  període consultat.
- Retorna ambdós valors en la mateixa fila/sèrie amb noms diferenciats (p. ex.
  `value` i `maxValue`) en lloc de l'esquema d'una sola columna que fa servir
  `UN_INDICADOR`.
- Si l'indicador de màxim no té dades per al període (valor `null` o absent),
  `maxValue` ha de arribar com a `null` al frontend — no com a `0` — perquè el
  frontend pugui distingir "sense dades" de "màxim és zero" (veure 6.4 i 7.2).

### 6.4. Reactivar `tipusValors` (`GraficValueTypeEnum`)

Descomentar i connectar `GraficValueTypeEnum {NUMERIC, PERCENTAGE}` i el camp
`tipusValors` a `EstadisticaGraficWidgetEntity` (columna BD), `EstadisticaGraficWidget`
(DTO) i `InformeWidgetGraficItem` (el DTO que arriba al frontend). Per defecte
`NUMERIC`. Només és rellevant/obligatori quan `tipusDades == DOS_INDICADORS`;
per a `UN_INDICADOR` es pot deixar `null`/ignorar-se.

## 7. Frontend

### 7.1. Formulari (`EstadisticaGraficWidgetForm.tsx`)

- Quan `isDosIndicadors`: mantenir el bloc existent d'indicador principal
  (indicador, títol, agregació, unitat), i afegir un segon bloc anàleg per a
  l'indicador màxim (`indicadorMaxim`, `titolIndicadorMaxim`,
  `agregacioMaxim`, `unitatAgregacioMaxim`), amb etiquetes que distingeixin
  "Indicador (valor)" / "Indicador (màxim)".
- Afegir el selector `tipusValors` ("Valors" / "Percentatge"), visible només
  quan `isDosIndicadors`, per defecte "Valors" (`NUMERIC`).
- Amagar el camp `gaugeMax` (atribut visual estàtic) sempre que
  `isDosIndicadors` sigui cert, tant en mode `NUMERIC` com `PERCENTAGE` — el
  màxim ve sempre de l'indicador de màxim. `gaugeMax` es manté visible només
  per a `UN_INDICADOR`.

### 7.2. Renderitzat (`GraficWidgetVisualization.tsx`, `renderGaugeChart`)

Consumir els nous camps `maxValue` i `tipusValors` que arriben del backend:

- **`NUMERIC`** (o `UN_INDICADOR`, sense `tipusValors`): `value = dades[0].value`;
  `valueMax = dades[0].maxValue` si existeix, si no `gaugeMax` (cas
  `UN_INDICADOR`, comportament actual sense canvis).
- **`PERCENTAGE`**: `value = (dades[0].value / dades[0].maxValue) * 100`,
  `valueMin = 0`, `valueMax = 100` sempre.
- **Guarda de dades absents**: si `maxValue` és `null`/`undefined` (l'indicador
  de màxim no té dades per al període) en un widget `DOS_INDICADORS`, tant en
  `NUMERIC` com en `PERCENTAGE`, no s'ha de dividir ni caure a `gaugeMax` (que
  ja no aplica quan hi ha 2 indicadors) — el gauge es mostra amb el mateix
  tractament visual que ja existeix per a "sense dades" (`senseAccesDades`/
  buit), no cal inventar un estat nou. En `PERCENTAGE`, `maxValue === 0`
  també cau en aquesta guarda (evita divisió per zero / `NaN`).

## 8. Testing

**Backend**:
- `ValidGraficWidgetValidatorTest` (nou o ampliat): branca `DOS_INDICADORS`
  — falta indicador principal, falta indicador màxim, falta unitat quan
  `AVERAGE`, i que les validacions creuades (unitats mesclades, percentatge
  mesclat) també s'apliquin.
- `EstadisticaGraficWidgetHelperTest`: `upsertColumnes`/
  `afterCoversionGetColumnes` amb `DOS_INDICADORS` — verificar que es
  persisteixen/llegeixen 2 files amb `rol` correcte, i que el rol (no
  l'índex) determina quin és quin.
- `ConsultaEstadisticaHelperTest`: consulta amb `DOS_INDICADORS` retorna
  `value`/`maxValue` correctes; cas amb l'indicador de màxim sense dades
  (`maxValue` arriba `null`, no `0`).

**Frontend**:
- `EstadisticaGraficWidgetForm.test.tsx`: el selector `tipusDades` per a
  `GAUGE_CHART` només ofereix `UN_INDICADOR`/`DOS_INDICADORS`; el bloc
  d'indicador màxim i el selector `tipusValors` apareixen només amb
  `isDosIndicadors`; `gaugeMax` desapareix amb `isDosIndicadors`.
- `GraficWidgetVisualization.test.tsx`: `renderGaugeChart` amb `NUMERIC`
  (valueMax des de `maxValue`), amb `PERCENTAGE` (0-100, càlcul correcte), i
  amb `maxValue` absent/zero en mode percentatge (no crash, no `NaN`).

## 9. Resum de fitxers afectats

- `comanda-back/liquibase/` — changeset nou: columna `rol` a
  `est_indicador_table`.
- `IndicadorTaulaEntity.java`, `IndicadorTaula.java` — camp `rol` +
  `IndicadorRolEnum` (nou).
- `EstadisticaGraficWidgetEntity.java`, `EstadisticaGraficWidget.java` —
  descomentar/finalitzar `tipusValors`; nous camps transitoris per a
  l'indicador màxim.
- `InformeWidgetGraficItem.java` — descomentar `tipusValors`.
- `EstadisticaGraficWidgetHelper.java` — implementar els 2 `TODO`.
- `ValidGraficWidgetValidator.java` — implementar branca `DOS_INDICADORS` +
  ampliar guarda de `validateIndicadorsInfo`.
- `ConsultaEstadisticaHelper.java` — substituir `NotImplementedException` per
  la consulta real de 2 indicadors.
- `EstadisticaGraficWidgetForm.tsx` — `tipusDadesOcultar`, bloc d'indicador
  màxim, selector `tipusValors`, visibilitat de `gaugeMax`.
- `GraficWidgetVisualization.tsx` — `renderGaugeChart`: valueMax dinàmic +
  mode percentatge.
- Tests corresponents a cada fitxer anterior.
