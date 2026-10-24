# Millores de Distribucio i ConCSV a Lot2: pla d'implementació

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Objectiu:** Incorporar a `v2-parametres-visuals` (`lib/` i `src/lot2/`) les millores que Distribucio (`dis-dev`) i ConCSV (`concsv-dev`) tenen i nosaltres no, corregir els errors que s'han trobat en comparar-les, i copiar-ho tot a comanda.

**Arquitectura:**
- **`lib/`:** correccions genèriques i paràmetres opcionals. Sense configurar-los, el comportament per defecte no canvia.
- **`src/lot2/`:** valors comuns de totes les apps (tema, props per defecte, components).
- **comanda:** rep la còpia nova, amb un merge a 3 bandes.

**Stack:** React 19, MUI 7, TypeScript, Vite, Vitest.

**Spec:** `docs/superpowers/specs/2026-10-24-comparacio-distribucio-concsv.md`. És l'informe de comparació, i les referències «3.a.N», «3.b.N» i «4.N» d'aquest pla en són apartats. Sion hi va aprovar quatre grups:
- errors i contrast;
- accessibilitat a `lib/`;
- detalls de `lot2` i criteris;
- errors no visuals de `lib/`.

## Restriccions globals

- **base-react:**
  - Checkout principal `/home/sionandreu/git/base-react`, branca `v2-parametres-visuals` (cap `fff88af`).
  - `CLAUDE.md` no està versionat i no és nostre: no s'hi afegeix mai.
  - L'stash `stash@{0}` és de Sion: no es toca.
- **comanda:**
  - Branca `comanda-lot2-visual`.
  - `FRONT = comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio`.
  - Els canvis locals de Sion no es posen mai a l'stage ni es modifiquen (`.editorconfig`, `caib-application.properties`, `comanda-ear/*`, `EstadisticaHelper.java`, `docker-compose.yml`, `versions/v.0.1.4/propietats`, `*.pem`, `doc/scripts/`, `docs/superpowers/plans/2026-09-28-*`).
- **Commits:**
  - Sempre amb rutes explícites: `git commit -m "..." -- <rutes>`.
  - Missatges en català.
  - Cap línia `Co-Authored-By` ni cap menció a Claude o a la IA.
  - **Mai `git push`.**
- **Valors per defecte de `lib/`:** cap paràmetre nou no canvia l'aspecte ni el comportament quan no s'indica. Cada paràmetre té un test que ho comprova. Hi ha dues excepcions, que són correccions d'errors:
  - les fileres de la graella al tema fosc (3.a.4);
  - l'estat d'error de la barra de títol (4.6).
- **`src/lot2/`:**
  - Només importa de `reactlib`, de llibreries npm i dels seus propis fitxers.
  - Sense selectors d'estructura del DOM, excepte l'únic que ja està documentat.
  - Les traduccions es fan amb `useLot2Translation`.
- **Comprovacions a base-react:**
  - `npm test`, `npm run typecheck` i `npm run build` han de passar.
  - `npx tsc --noEmit -p . 2>&1 | grep -E '^(lib|src/lot2)/'` no ha de donar res.
  - Lint: base-react no en té.
- **Comprovacions a comanda (FRONT):**
  - `npm test`, `npm run typecheck` i `npm run build` han de passar.
  - No hi pot haver errors de lint nous fora de `lib/` i `src/lot2/`.
- **Minimitzar el diff de `lib/`:** cada app copia `lib/` amb un merge a 3 bandes, així que no es reformata el codi que no es toca.
- **Fora d'abast** (es deixa anotat al document de migració):
  - l'aplicació automàtica del filtre (3.a.6);
  - el logout per navegació (3.a.9);
  - el títol de la pestanya (3.b.13);
  - el botó desactivat a la barra de títol (3.b.9);
  - l'alçada mínima de la graella (3.b.10);
  - el títol de diàleg amb fons de color (3.b.14, l'últim punt).

  Els dos que depenen de la verificació visual (3.b.9 i 3.b.10) s'afegeixen a la checklist de `doc/lot2-aspecte-visual.md`.

## Punts d'atenció per a la revisió

1. **Graella principal amb un error de l'API:**
   - l'`<h1>` del `ScreenTitle` s'ha de continuar veient;
   - l'error es mostra (subtítol o text), amb un indicador de color que el degradat de `titleBarSx` no tapi.
2. **`menuListItemSemantics` activat:**
   - cap fill directe de l'`<ul>` del menú no és res que no sigui `<li>`;
   - el selector `&.Mui-selected:has(+ .MuiBox-root)` continua funcionant;
   - el panell flotant del menú plegat també compleix el mateix.
3. **Menú `footer` i `inverse` fosc:** l'accent té un contrast ≥ 4.5:1 sobre el fons del menú amb els colors de comanda (`#A8456A`) i de Ripea (`#004B99`).
4. **`FormDialog`:** els botons passats a `show(id, { dialogButtons })` sobreviuen quan el component pare torna a pintar amb un array nou.
5. **Formulari emergent:** un clic fora (backdrop) no el tanca. La tecla Escape i el botó de tancar sí que el tanquen.

---

## Part A: base-react

### Tasca 1: errors visuals a `lib/` (4.6 i 3.a.4)

**Fitxers:** `lib/components/mui/datacommon/DataToolbar.tsx` (~129-130), `lib/components/mui/Toolbar.tsx` (fons d'error), `lib/components/mui/datagrid/DataGridCustomStyle.tsx:9-10`, i els tests al costat de cada fitxer.

**Interfícies:** cap API nova. Comportament:
- **Barra en error:** quan `title` és un element React, es manté el títol i el text `datacommon.toolbar.error` passa al subtítol. Si `title` és text, continua com ara.
- **Indicador d'error:** el fons d'error (`red[100]` / `red[900]`) s'aplica **després** del `toolbarSx` de l'app, de manera que guanya al degradat. A més, s'afegeix una vora esquerra d'error de 4px, que no depèn del fons.
- **Fileres al tema fosc:** amb `striped`, les fileres parelles fan servir `alpha(theme.palette.common.white, 0.04)`. Al tema clar no canvia res.

- [ ] Escriure els tests:
  - graella amb error i `title` element → el títol continua sent al DOM i el text d'error apareix;
  - amb `toolbarSx: { background: 'linear-gradient(...)' }` i en error → el fons calculat de la barra és el d'error;
  - `title` text en error → el comportament d'ara;
  - fileres al fosc → el color no és `#eeeeee`;
  - fileres al clar → el color no canvia.
- [ ] Executar-los. Resultat esperat: FALLEN.
- [ ] Implementar-ho.
- [ ] Executar els tests i les comprovacions. Resultat esperat: PASSEN.
- [ ] Commit: "Barra d'eines: mantenir el títol i fer visible l'error amb toolbarSx; fileres de la graella al tema fosc".

### Tasca 2: accessibilitat a `lib/` (3.a.1, 3.a.2, 3.a.3)

**Fitxers:** `lib/components/mui/MuiBaseAppContext.tsx`, `lib/components/mui/Menu.tsx` (`ListMenuContent` ~313-358 i el panell compacte), `lib/components/mui/Toolbar.tsx:49-58`, `lib/components/mui/datacommon/DataToolbar.tsx`, `MuiDataGrid.tsx`, `MuiDataList.tsx`, `MuiForm.tsx`, `lib/components/mui/AppBar.tsx:68`, `lib/components/mui/MuiBaseApp.tsx`, i els tests.

**Interfícies:**
- `MuiBaseAppAppearance.menuListItemSemantics?: boolean`, per defecte `false`.
  - Quan és `true`, cada `MenuItem` (el botó i la `Box` dels fills) queda dins d'un `<li style="list-style:none">`.
  - Cada `Divider` es pinta amb `component="li"`.
  - Val també per al panell compacte.
- `Toolbar`: quan `title === ''` o `title == null`, no es pinta cap `Typography`.
- `toolbarTitleComponent?: React.ElementType`, per defecte `'h6'`.
  - El tenen `MuiDataGrid`, `MuiDataList` i `MuiForm`, i passa per `DataToolbar` fins al `component` del `Typography` de `Toolbar`.
  - Es pot configurar via `defaultMuiComponentProps`.
- `MuiBaseAppProps.headerLogoAlt?: string`, per defecte `'logo'`. `AppBar` l'aplica a l'`alt` del logo.

- [ ] Escriure els tests:
  - menú per defecte → el DOM és com ara (sense `<li>`);
  - amb `menuListItemSemantics` → tots els fills directes de cada `ul` del menú són `li`, i el selector de grup desplegat continua trobant el germà;
  - `title=''` → cap `h6`;
  - `toolbarTitleComponent='h2'` → `h2`;
  - per defecte → `h6`;
  - `headerLogoAlt` → s'aplica;
  - per defecte → `'logo'`.
- [ ] Executar-los. Resultat esperat: FALLEN. Implementar-ho. Executar-los. Resultat esperat: PASSEN.
- [ ] Commit: "Accessibilitat: entrades del menú com a li opcionals, títol de barra sense h6 buit i nivell configurable, alt del logo".

### Tasca 3: botó de crear responsiu i errors no visuals de `lib/` (3.a.5, 3.a.7, 3.a.8)

**Fitxers:** `lib/components/mui/datacommon/MuiDataCommon.tsx` (~398-430), `MuiDataGrid.tsx` (tipus), `lib/components/mui/form/FormDialog.tsx:111-113,178`, `lib/components/form/Filter.tsx:25,138`, i els tests.

**Interfícies:**
- `toolbarCreateVariant: 'icon' | 'button' | 'responsive'`. Amb `'responsive'` es mostra el botó amb text a partir de `md`, i per sota de `md` un `IconButton` amb `title` igual al text. La resta de comportament és el de `'button'`.
- **`FormDialog`:** si la crida activa a `show()` ha passat `dialogButtons`, l'efecte que sincronitza amb la prop `dialogButtons` no els sobreescriu fins al següent `show()` sense `dialogButtons`.
- **`Filter`:**
  - Nova prop `persistentStateKeyIncludesCode?: boolean`, per defecte `false`. Quan és `true`, la clau és `${persistentStateKey ?? resourceName}:${code}`.
  - Es corregeix el comentari de la línia 25, que ha de dir «sessionStorage per defecte».

- [ ] Escriure els tests:
  - `'responsive'` amb `useMediaQuery` simulat, per sobre i per sota de `md`;
  - el botó de `show()` sobreviu a un re-render del pare amb un array nou;
  - dos filtres del mateix recurs amb `code` diferent i l'opció activa no comparteixen l'estat;
  - per defecte, la clau és igual que ara.
- [ ] Executar-los. Resultat esperat: FALLEN. Implementar-ho. Executar-los. Resultat esperat: PASSEN.
- [ ] Commit: "Botó de crear responsiu, botons de FormDialog passats a show i clau de persistència del filtre per codi".

### Tasca 4: tema i colors del menú de `lot2` (4.1, 4.2, 3.b.14 sense el títol de diàleg)

**Fitxers:** `src/lot2/menuColors.ts`, `src/lot2/theme.ts`, i els seus tests.

**Valors:**
- **Menú `footer`:** `accent: '#FFFFFF'`, `selectedBackground: 'rgba(255,255,255,0.12)'`.
- **Menú `inverse` quan la paleta invertida és fosca:** `accent = colors.light`. Si no n'hi ha, `lighten(main, 0.3)`. Si aquest valor no arriba a 4.5:1 sobre el fons, es fa servir `#FFFFFF`. Quan la paleta invertida és clara, l'accent es queda com ara.
- **Tema fosc:**
  - `MuiButton` `outlined primary` amb text `text.primary` i vora `LIGHT`, igual que Dràcula ara;
  - focus dels camps `OutlinedInput` amb vora `LIGHT` de 2px (també a Dràcula);
  - casella marcada d'una fila seleccionada de la graella amb `SOFT` (també a Dràcula).
- **Tots els temes:**
  - `textTransform: 'none'` a `MuiButton` i `MuiTab`;
  - autofill de Chrome neutralitzat a `MuiCssBaseline` (com DIS `theme.ts:121-125`);
  - `MuiDialogActions` amb `gap: 8px`.
  - **No** es copia el `marginLeft: 10px` ni el `cursor: not-allowed` (4.5).

- [ ] Escriure els tests:
  - contrast de l'accent ≥ 4.5 per a `footer` i `inverse` fosc, amb `#A8456A` i `#004B99`;
  - `inverse` clar sense canvis;
  - el tema fosc conté les regles d'`outlined`, focus i casella;
  - `textTransform` és `none`.
- [ ] Executar-los. Resultat esperat: FALLEN. Implementar-ho. Executar-los. Resultat esperat: PASSEN.
- [ ] Commit: "lot2: accent del menú peu i invers amb contrast, i contrast dels temes foscos".

### Tasca 5: props per defecte i barra de títol de `lot2` (3.b.1, 3.b.4, 3.b.8, i activar els paràmetres de les tasques 2 i 3)

**Fitxers:** `src/lot2/lot2ComponentProps.ts`, `src/lot2/titleBarControlsSx.ts`, `src/lot2/menuColors.ts` (`LOT2_APPEARANCE`), i els tests.

**Valors:**
- `lot2DefaultMuiComponentProps(theme).dataGrid` passa a ser:
  - `toolbarCreateVariant: 'responsive'`;
  - `toolbarTitleComponent: 'h2'`;
  - `popupEditFormDialogOnClose: (reason) => reason !== 'backdropClick'`;
  - `popupEditFormDialogComponentProps: { fullWidth: true, maxWidth: 'md' }`.
- `form` i `dataList`: `toolbarTitleComponent: 'h2'`.
- **No** s'hi afegeix `avoidSubmitIfAnyValidatorErrors`.
- `LOT2_APPEARANCE` afegeix `menuListItemSemantics: true`.
- `titleBarSx`:
  - `flexWrap: 'wrap'` i `rowGap: 1`, sense deixar cap fila buida pel separador `flexGrow` (verificar-ho amb un test d'estil);
  - `'& .MuiOutlinedInput-root': { height: 32 }`, amb el padding vertical de l'input a 0.
- **Comprovar que el `ScreenTitle` continua sent `h1`.** El `toolbarTitleComponent` només afecta els títols de text.

- [ ] Escriure els tests:
  - els valors de les props per defecte;
  - un backdrop click no tanca el formulari i Escape sí;
  - `flexWrap` és `wrap`;
  - l'alçada del filtre ràpid és de 32px.
- [ ] Executar-los. Resultat esperat: FALLEN. Implementar-ho. Executar-los. Resultat esperat: PASSEN.
- [ ] Commit: "lot2: formulari emergent sense tancar amb clic fora, barra de títol en dues files i paràmetres d'accessibilitat activats".

### Tasca 6: filtres, icones, textos i badge de `lot2` (3.b.3, 3.b.5, 3.b.6, 3.b.7, 4.3)

**Fitxers:** `src/lot2/icons.ts`, `src/lot2/i18n.ts`, `src/lot2/StyledBadge.tsx`, `src/lot2/FilterCount.tsx`, `src/lot2/springFilterCount.ts`, i els tests.

**Valors:**
- **Icones:**
  - `avis: 'campaign'`;
  - s'afegeixen les icones comunes d'acció, estat i selecció de DIS `src/util/icons.ts:49-127`, amb els mateixos noms i valors (`crea`, `modifica`, `esborra`, `desa`, `cancela`, `executa`, `refresca`, `descarrega`, `detall`, `plega`, `desplega`, `seleccionaTot`...);
  - es manté el chevron del filtre avançat.
- **i18n:** «Simple» passa a «Bàsic» (ca), i a «Básico»/«Basic» a es/en.
- **`StyledBadge`:** `primary` passa a `accentFillColor(theme)`, amb el text `getContrastText`.
- **`FilterCount`:** prop opcional `count?: number | ((terms: number) => number)`.
- **`countSpringFilterTerms`:** ignora les cometes escapades `\'` dins dels literals (com `maskLiterals` de DIS).

- [ ] Escriure els tests de cada canvi (inclòs `"a~'o\\'b' and c:1"` → 2). Executar-los. Resultat esperat: FALLEN. Implementar-ho. Executar-los. Resultat esperat: PASSEN.
- [ ] Commit: "lot2: icones comunes, text Bàsic, badge amb contrast i recompte de filtres ajustable".

### Tasca 7: `Lot2BaseApp`: idioma, logo de capçalera i peu (3.b.2, 3.b.11, 3.b.12)

**Fitxers:** `src/lot2/Lot2BaseApp.tsx`, `src/lot2/DrassanaFooter.tsx`, `src/lot2/layout.ts` (si cal), i els tests.

**Interfícies:**
- **`Lot2BaseApp`:** sincronitza `document.documentElement.lang` amb l'idioma actual de base-react. Ho fa amb un efecte dins un fill que llegeix el context, i agafa els dos primers caràcters (`ca`, `es`, `en`). Es pot desactivar amb `syncDocumentLang={false}`.
- **`lot2HeaderLogoStyle(theme)`:** s'exporta, i és el valor per defecte de `headerLogoStyle`, combinat amb el que passa l'app (l'app guanya).
  - Amplada `MENU_WIDTH - LOGO_BOX_LEFT`.
  - Separador a la vora del menú obert, amb `LOGO_SEPARATOR_GAP`.
  - És com DIS `App.tsx:293-322`, però amb les constants de `layout.ts`.
- **`Lot2BaseApp`** passa `headerLogoAlt`, per defecte `"Govern de les Illes Balears"`.
- **`DrassanaFooter`:**
  - el logo té `alt="Drassana"` i no té `cursor: pointer`;
  - el `span` invisible de data i revisió passa a `aria-hidden="true"` i manté l'aspecte.

- [ ] Escriure els tests:
  - el `lang` canvia en canviar d'idioma, i no ho fa quan està desactivat;
  - l'estil del logo per defecte, i que l'app hi guanyi;
  - l'`alt` del logo GOIB;
  - el peu (`alt`, `aria-hidden`, sense `cursor`).
- [ ] Executar-los. Resultat esperat: FALLEN. Implementar-ho. Executar-los. Resultat esperat: PASSEN.
- [ ] Actualitzar `src/lot2/README.md` amb les noves exportacions.
- [ ] Commit: "lot2: idioma del document, estil del logo de capçalera i accessibilitat del peu".

---

## Part B: comanda

### Tasca 8: copiar a comanda i adaptar-la

**Fitxers:** `FRONT/lib/**` (merge a 3 bandes: base `cb7141e` i nova versió el cap de base-react), `FRONT/src/lot2/**` (còpia literal), `FRONT/src/App.tsx` (logo), `FRONT/src/i18n/i18n.ts` (`lang`), `FRONT/src/util/screenIcons.ts` (`avis`), i els tests afectats.

- [ ] **Merge.** Fer el merge a 3 bandes de `lib/` amb el procediment del document de migració, apartat «merge-lib».
  - A `FRONT/lib`, la diferència respecte del cap de base-react només ha de ser la llista de personalitzacions de comanda: `KeycloakAuthProvider` i el seu export, la navegació de `ContainerAuthProvider` i el seu test, la redirecció i el `stackTrace` de `ResourceApiProvider`, `DATA_GRID_DIALOG_DEFAULT_HEIGHT`, l'alçada `number | string` de `MuiDataGrid`, i els `@deprecated`.
  - `diff -r` de `src/lot2` ha de quedar buit.
- [ ] **Adaptar comanda:**
  - treure la sincronització pròpia de `lang` de `i18n.ts` si `Lot2BaseApp` ja la fa (deixar-ne només una);
  - `App.tsx` ha de fer servir l'estil de logo per defecte de `lot2`, eliminant el `logoStyle` antic si el de `lot2` el cobreix;
  - fer que la icona d'avisos del menú i del títol sigui la de `lot2` (`campaign`);
  - revisar les pantalles amb subtaules o diàlegs, on el botó de crear ara és `'responsive'`. No cal canviar res si els tests passen.
- [ ] **Tests de comanda:** actualitzar els que depenien de `h6`, de «Simple», de `notifications` o dels botons en majúscules.
- [ ] **Comprovacions:** `npm test`, `npm run typecheck`, `npm run build`, i que no hi hagi lint nou fora de les còpies.
- [ ] **Commits:**
  - "Actualitzar la còpia de base-react (millores de Distribucio i ConCSV)", amb `lib`;
  - "Actualitzar la carpeta comuna lot2", amb `src/lot2`;
  - "Comanda: adaptar-se a les millores de lot2", amb la resta.
- [ ] **Memòria de sincronització:** actualitzar-la amb el nou commit base de `lib/` i de `src/lot2/`.

### Tasca 9: documentació

**Fitxers:** `docs/lot2/migracio-aspecte-visual.md` a base-react, i `doc/lot2-aspecte-visual.md` a comanda.

- [ ] **Document de migració:**
  - afegir els paràmetres nous a la taula (`menuListItemSemantics`, `toolbarTitleComponent`, `headerLogoAlt`, `'responsive'`, `persistentStateKeyIncludesCode`);
  - afegir els canvis de comportament (error a la barra, fileres al tema fosc, formulari emergent, `textTransform`);
  - afegir les noves exportacions de `lot2`;
  - posar la secció de Distribucio i ConCSV al dia: què ja els dona `lot2` i què han d'esborrar (`FilterAutoApply` es manté com a decisió pròpia, el `<li>` per CSS de ConCSV, els colors del menú);
  - llistar les millores que queden fora d'abast.
- [ ] **Doc de comanda:** afegir els commits nous i, a la checklist visual:
  - el botó desactivat a la barra de títol (3.b.9);
  - l'alçada mínima de la graella en finestres baixes (3.b.10);
  - la barra de títol en dues files;
  - el filtre ràpid de 32px;
  - els menús `footer` i `inverse`;
  - una graella amb error.
- [ ] **Commits** (sense push): "Document de migració: millores de Distribucio i ConCSV" a base-react, i "Document Lot2: millores de Distribucio i ConCSV" a comanda.
