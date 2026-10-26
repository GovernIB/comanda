# Aspecte visual Lot2 a comanda

El front de comanda (`comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio`) fa servir l'aspecte
comú de les aplicacions del Lot2 (menú, capçalera, graelles i filtres de Notib; només canvien els colors i
el logo).

## Document de migració

La guia completa per adoptar-lo en una aplicació (paràmetres de base-react R01–R16, pas comú amb el merge a
3 bandes de `lib/`, apartats de Notib, Distribucio i Ripea, components pendents i limitacions) és al
repositori de base-react:

- Branca: `v2-parametres-visuals` (encara no pujada al remot ni fusionada amb `v2`)
- Fitxer: `docs/lot2/migracio-aspecte-visual.md`
- Quan la branca sigui al remot: <https://github.com/programari-limit/base-react/blob/v2-parametres-visuals/docs/lot2/migracio-aspecte-visual.md>

## Què s'ha fet a comanda

Branca `comanda-lot2-visual`, creada a partir de `comanda-dev-libs-update`.

- `lib/`: merge a 3 bandes amb base-react `v2-parametres-visuals` (base anterior `bea8c9e`), conservant les
  personalitzacions de comanda (`KeycloakAuthProvider`, navegació real a `ContainerAuthProvider`, redirecció
  a login i `stackTrace` a `ResourceApiProvider`, alçada textual de `DataGridDialog`/`MuiDataGrid`).
- `src/lot2/`: còpia literal de la carpeta comuna de base-react (commit `56b2a4c`). **No s'edita aquí**:
  qualsevol canvi es fa a base-react i es torna a copiar. Bases del proper merge: `lib/` = `56b2a4c` i
  `src/lot2/` = `56b2a4c` (correccions de la revisió final: autofill de Chrome, subtítol d'error genèric amb
  títol element i comentaris).
- Tema rosa fosc (`src/theme.ts`, `createLot2Themes`): `main` `#A8456A`, `dark` `#7A2E4C`, `light` `#cb6c88`,
  `soft` `#FAEFF3`.
- `src/components/BaseApp.tsx` sobre `Lot2BaseApp`, amb el peu `DrassanaFooter` en lloc de `ComandaFooter`.
- Pantalles dins `ScreenFrame` amb `ScreenTitle`, botons de crear amb text, filtres amb `FilterClearButton`,
  `FilterAdvancedButton` i `FilterCount`. Formularis simples com a modals (`AlarmaConfigForm`) i complexos com
  a pàgines (`AppForm`).
- Pendent: verificació visual en navegador (temes clar, fosc i Dràcula; menú `theme`, `inverse` i `footer`).

| Commit | Descripció |
|--------|------------|
| `088e9645` | Actualitzar la còpia de base-react a v2-parametres-visuals |
| `33c301cc` | Afegir la carpeta comuna lot2 |
| `5df8bdb2` | Tema rosa fosc i BaseApp de comanda sobre lot2 |
| `ee98de7a` | Formularis simples com a modals i complexos com a pàgines amb ScreenFrame |
| `84287d31` | Graelles Lot2: hook de props de pantalla, catàleg d'icones i textos de crear |
| `7a34f8a5` | Configuració (apps, entorns, versions, històric, entitats, integracions) sobre ScreenFrame |
| `2803192e` | Monitorització (monitor, monitor BD, caché, tasques) sobre ScreenFrame |
| `80141851` | Estadístiques (dimensions, indicadors, widgets, plantilles, paletes, dashboards) sobre ScreenFrame |
| `255a2342` | Alarmes, avisos i configuració d'alarmes sobre ScreenFrame |
| `fffa40a4` | Filtres de les graelles amb FilterClearButton, FilterAdvancedButton i FilterCount |
| `3a63d05e` | Hook useAppliedSpringFilter per al recompte de filtres i neteja de les icones del menú |
| `4aa3fe10` | Filtres dobles de monitor, tasques i avisos amb botons de lot2 i recompte sumat |
| `9af3d4b3` | AlarmaConfig i widgets: contingut dins el ScreenFrame i botó de crear fórmula amb text |
| `ffe9e4b7` | Marc lot2 a les pàgines amb disseny propi i neteja d'alçades fixes |
| `0289dddc` | Correccions del marc lot2: selector d'agrupació de salut, alertes dins el marc i títol únic a accessibilitat |
| `4e92935d` | lot2: menú del tema sense selectors d'estructura (còpia de base-react 7f51df8) |
| `1623ad44` | AlarmaConfig: sense l'acció Detalls nova a les files i test de tancament del diàleg real |

### Millores de Distribucio i ConCSV (2026-10-26)

Base-react `v2-parametres-visuals`, `fff88af..0cd9ed5` (13 commits): barra d'eines amb error que manté el
títol, fileres del tema fosc, entrades del menú dins `<li>` (`menuListItemSemantics`), `toolbarTitleComponent`
(`h2`), `headerLogoAlt`, botó de crear `'responsive'`, `persistentStateKeyIncludesCode`, `textTransform: 'none'`,
`DialogActions` amb `gap`, colors del menú `footer` i `inverse` amb contrast, formulari emergent `md` que no es
tanca amb clic fora, barra de títol en dues files, filtre ràpid de 32px, `StyledBadge` amb contrast, icones
comunes, text «Bàsic», `lang` de `<html>` i estil del logo a `Lot2BaseApp`. Detall a l'apartat 1.2 del
document de migració. Fora d'abast: `FilterAutoApply`, logout per navegació, títol de la pestanya, botó
desactivat i alçada mínima de la graella (només si la comprovació visual ho demana) i títol de diàleg en color.

| Commit | Descripció |
|--------|------------|
| `d0c7b1cc` | Actualitzar la còpia de base-react (millores de Distribucio i ConCSV) |
| `cac06a1e` | Actualitzar la carpeta comuna lot2 |
| `b8d10cc4` | Comanda: adaptar-se a les millores de lot2 (`lang` sincronitzat per `Lot2BaseApp`, `App.tsx` amb `lot2HeaderLogoStyle`, `screenIcons.avis = icons.avis`, `MemoryStorage` enumerable a `vitest.setup.ts`) |
| `bc2217bc` | Test de persistència del filtre: còpia de base-react sense `Object.keys` de `sessionStorage` |

Helpers de comanda proposats per passar a `lot2` (vegeu l'apartat «Propostes per a lot2» del document):
`src/hooks/useScreenGridProps.tsx`, `src/hooks/useAppliedSpringFilter.ts`,
`src/components/FilterClearAllButton.tsx` i `src/util/springFilterSum.ts`.

## Comprovació visual pendent

Encara no s'ha revisat en un navegador. Abans de fusionar, cal fer aquesta comprovació:

**Com executar:** a FRONT, `npm run dev` (Vite) amb un backend local arrencat i el proxy/Keycloak configurats; entrar amb un usuari admin. Canviar tema (clar/fosc/Dracula) des de les preferències d'usuari i el mode de menú (theme/inverse/footer) des de la configuració del menú. Repetir cada punt en els 3 temes; els modes de menú només cal revisar-los on s'indica.
- Per defecte: tots els temes (CLAR, OBSCUR, DRACULA) i menú `theme`; a més, una passada amb `inverse` i `footer` a Salut, Dashboards i Parametres, amb menú plegat i desplegat.
- **Salut, selector d'agrupació:** l'opció seleccionada ha de veure's emplenada amb el color primari i la resta amb contorn i fons de paper. Revisar els 3 temes, i canviar d'opció (aplicació, entorn, cap, versions). Revisar també `inverse` i `footer`.
- **Salut, detall `/salut/:id`:** botó enrere, estat, pestanyes i scroll intern. Temes: CLAR i DRACULA.
- **Editor de dashboards, panell lateral:** plegat i desplegat; el llenç ha d'omplir l'alçada, sense doble scroll. Interruptor clar/fosc del disseny. Temes: tots tres, menú `theme`.
- **Vista de dashboard, dues barres apilades:** la barra de títol del marc («Dashboards») i, a sota, la toolbar grisa amb el selector. Comprovar que no queda massa feixuc, el color de fons del dashboard i que ocupa l'alçada. Temes: CLAR i OBSCUR.
- **Calendari, overlay de càrrega:** el `global-loading-overlay` és `position: fixed`: comprovar que dins la Card (`overflow: hidden`) no es retalla i que cobreix la pantalla. Comprovar també que el calendari omple l'alçada. Temes: CLAR i OBSCUR.
- **DimensioValor:** barra de títol, graella i filtres. Tots els temes.
- **NotFound** (ruta inexistent), **Sitemap**, **Accessibilitat** (un sol títol visible, ca i es), **Parametres** (filtre i dos panells), **Broker** i **cua de missatges**. CLAR i DRACULA.
- **Resta d'elements assenyalats abans:**
  - contrast de l'accent de Dracula (3.60:1 sobre `#303341`) a icones, títols i barra de títol;
  - diàleg d'AlarmaConfig (mida `lg` i selector d'entorn);
  - barra de títol enganxada d'AppForm en fer scroll;
  - pestanyes d'EstadisticaWidget;
  - recompte del doble filtre de Monitor;
  - espaiat del peu respecte de Notib.
- **Millores de Distribucio i ConCSV (2026-10-26).** Temes CLAR, OBSCUR i DRACULA, tret que s'indiqui:
  - **Botó desactivat a la barra de títol** (3.b.9): p. ex. el de crear o desar desactivat. Comprovar que no surt
    amb el gris de MUI damunt del degradat de la barra; si es veu malament, afegir el fons `paper` i la vora a
    base-react.
  - **Alçada mínima de la graella en finestres baixes** (3.b.10): reduir l'alçada de la finestra (~500px) amb
    una graella principal; comprovar que la graella no desapareix i que el marc fa scroll. Si no és així,
    afegir `SCREEN_GRID_MIN_HEIGHT` a base-react.
  - **Barra de títol en dues files:** finestra estreta amb títol, filtre i botons; ha de passar a dues files sense
    fila buida ni desbordament. Els controls de la segona fila queden alineats a l'esquerra (acceptat, confirmar
    que no es veu malament).
  - **Filtre ràpid de 32px:** el camp ha de tenir la mateixa alçada que els botons de la barra de títol.
  - **Menús `footer` i `inverse`:** l'accent (icona de l'entrada seleccionada, títol del panell compacte) ha de
    tenir contrast: blanc al `footer`, to clar o blanc a l'`inverse`. Menú plegat i desplegat.
  - **Graella amb error d'API** (p. ex. aturant el backend d'una pantalla): la barra té fons rogenc i vora
    esquerra de 4px en color d'error (també al tema fosc), amb el text del subtítol en color normal (no
    vermell). Amb el títol de `ScreenTitle` (element) el títol es manté i el missatge va al subtítol; amb un
    títol de text, aquest se substitueix pel text genèric d'error. Amb el títol element i un error sense
    missatge, el subtítol és el text genèric d'error.
  - **Logo de capçalera nou de 59px** (abans 38px) amb el separador sobre la vora del menú obert i alt
    «Govern de les Illes Balears».
  - **Formulari emergent de les graelles:** un clic fora no el tanca; Escape i la creu sí. Mida `md` de `lot2`
    a les graelles que no la sobreescriuen (p. ex. `Entorns`); `AlarmaConfig` la sobreescriu amb `lg`.
  - **Autofill de Chrome als temes fosc i Dràcula:** el text emplenat automàticament es llegeix.
  - Botons sense majúscules; fileres de la graella al tema fosc; badge primari amb contrast (`primary.dark` al clar).
- **Pantalla estreta (< 768px):** menú plegat i barres de títol sense desbordar (Salut, Dashboards, Calendari).
- **Menú plegat: obrir el submenú flotant d'un grup (Monitorització, Configuració) en tots tres modes de menú** (`theme`, `inverse`, `footer`): el panell ha de sortir sencer, sense tallar, a la dreta del menú plegat i sota la capçalera.
- **Icones del menú plegat centrades amb el botó hamburguesa** (i, amb el menú obert, les icones de nivell 0 a la mateixa columna).
- **Icones de submenú amb el menú obert i al panell flotant:** comprovar que no queden desplaçades uns 7px a l'esquerra respecte de Notib.
