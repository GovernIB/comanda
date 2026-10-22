# Unificació de l'aspecte visual Lot2: pla d'implementació

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Objectiu:** A base-react s'afegeixen paràmetres visuals opcionals, sense canviar res si no s'indiquen, i una carpeta comuna `lot2/` que reprodueix l'aspecte de Notib. Després s'aplica tot a comanda amb el seu tema rosa fosc i es redacta el document de migració per a les altres aplicacions.

**Arquitectura:** Hi ha tres capes:
1. **`lib/` (base-react):** rep els paràmetres de manera centralitzada, amb una prop `appearance` a `MuiBaseApp`, i props noves a les graelles i als formularis, configurables via `defaultMuiComponentProps`.
2. **`src/lot2/`:** carpeta idèntica a totes les aplicacions. Fixa aquests valors amb els de Notib i aporta el marc, els títols, els filtres, el peu i la fàbrica de temes.
3. **Configuració de l'aplicació:** un fitxer petit amb els colors, els logos i el menú.

**Stack tecnològic:** React 19, MUI 7 (+ MUI X DataGrid Pro), TypeScript 5, Vite, Vitest + Testing Library + jsdom.

**Spec:** `docs/superpowers/specs/2026-10-23-unificacio-aspecte-visual-lot2-design.md`

## Restriccions globals

- **Valors per defecte:** cap paràmetre nou no canvia l'aspecte actual quan no s'indica. Cada paràmetre té un test que ho comprova.
- **Dependències de `src/lot2/`:** només importa de `reactlib`, de llibreries npm i dels seus propis fitxers. Mai de codi de l'aplicació.
- **base-react, branca i worktree:**
  - El treball es fa a la branca `v2-parametres-visuals`, creada a partir de `v2` (`bea8c9e`) en un **worktree a part**, `/home/sionandreu/git/base-react-lot2`.
  - El checkout `/home/sionandreu/git/base-react` té canvis locals d'algú altre (`BaseAppContext.tsx`, `GridPage.tsx`, `Dialog.tsx`, `CLAUDE.md`) i **no es toca**.
- **base-react, publicació:**
  - La branca es puja amb `git push -u origin v2-parametres-visuals`.
  - No es fusiona amb `v2` i no es canvia la `version` del `package.json`.
- **comanda, branca:** el treball es fa a la branca `comanda-lot2-visual`, creada a partir de `comanda-dev-libs-update`.
- **comanda, commits:**
  - Sempre amb rutes explícites: `git commit -- <rutes>`.
  - No s'inclouen mai els canvis locals de l'usuari: `.editorconfig`, `caib-application.properties`, `comanda-ear/*`, `EstadisticaHelper.java`, `docker-compose.yml`, `versions/v.0.1.4/propietats`, `*.pem`, `doc/scripts/`.
- **Format dels commits:** un per funcionalitat, amb el missatge en català i acabat amb `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.
- **Personalitzacions de comanda a `lib/` que s'han de conservar** en copiar:
  - `KeycloakAuthProvider` i el seu export a `main.ts`;
  - la navegació real a `ContainerAuthProvider`;
  - la redirecció a login i el `stackTrace` a `ResourceApiProvider`;
  - `DATA_GRID_DIALOG_DEFAULT_HEIGHT` i l'alçada `number | string` de `MuiDataGrid`;
  - els `@deprecated` a `BaseAppContext` i `GridPage`.
- **Colors de comanda** (decisió del disseny, ajustables després de la verificació visual):
  - `light` = `#cb6c88`;
  - `main` ≈ `#A8456A`, amb un contrast amb blanc de ≥ 4.5:1;
  - `dark` ≈ `#7A2E4C`;
  - `soft` ≈ `#FAEFF3`.
- **Peu:** `DrassanaFooter`, amb fons `#5F5D5D`.
- **Referència visual:**
  - `/home/sionandreu/git/notib`, branca `notib2-dev`, app a `notib-back/src/main/reactapp/notib-back/src` (a partir d'aquí, `NOTIB`);
  - commits `300aba201`, `37461543d`, `70534db96` i `42eda115b`.
- **Comprovacions a comanda:** les ordres s'executen a `comanda-ms-visualitzacio/src/main/jsapp/comanda-visualitzacio` (a partir d'aquí, `FRONT`). Al final de cada tasca hi han de passar `npm test`, `npm run typecheck` i `npm run lint`.

## Punts d'atenció per a la revisió

1. **Aplicacions que actualitzen `lib/` sense indicar res:** han de veure exactament el mateix d'abans: capçalera de 64/56, menú plegat de 49/57 px, icones de 20 i línia de submenú `hsl(210, 100%, 60%)`. Test: tasques 2 a 5.
2. **Pantalla petita (`useSmallHeader`) amb `appearance.headerHeight` indicat:** el menú, el `top` sticky dels formularis i el detall fan servir el valor indicat, i no el 56 fix. Test: tasca 2.
3. **Tema fosc i Dracula:** amb `createLot2Themes`, el menú (en els modes `theme`, `inverse` i `footer`), les capçaleres de graella i el botó de crear han de mantenir el contrast. Test: tasca 7, i verificació visual a la tasca 15.
4. **Botó de crear desactivat** (`toolbarDisableCreateLink`, error de configuració) amb la variant `'button'`: s'ha de veure desactivat i no ha de fer cap acció. Test: tasca 5.
5. **Formulari en modal amb canvis sense desar en tancar-lo** (AlarmaConfig): s'ha de mantenir l'avís de canvis pendents que donava la pàgina (`FormBlocker`), o el mateix comportament que té Notib. Test: tasca 13.

---

## Part A: base-react (worktree `/home/sionandreu/git/base-react-lot2`)

### Tasca 1: worktree, infraestructura de tests i àlies `reactlib`

**Fitxers:**
- Modificar: `package.json` (scripts `test`, `typecheck`; `devDependencies` `vitest`, `jsdom`, `@testing-library/react`, `@testing-library/jest-dom`, `@testing-library/user-event`, amb les mateixes versions majors que fa servir comanda a `FRONT/package.json`). No cal afegir `lot2` a `files`, perquè la carpeta és a `src/` i no es publica a npm.
- Modificar: `vite.config.ts` (bloc `test`: `environment: 'jsdom'`, `setupFiles: ['./vitest.setup.ts']`, `globals: true`)
- Crear: `vitest.setup.ts` (`import '@testing-library/jest-dom'`)
- Modificar: `tsconfig.json` (`paths`: afegir `"reactlib": ["./lib/main"]` i mantenir `@programari-limit/base-react`)
- Test: `lib/util/equals.test.ts`

- [ ] **Pas 1:** Crear el worktree i la branca: `git -C /home/sionandreu/git/base-react worktree add -b v2-parametres-visuals ../base-react-lot2 bea8c9e`. Resultat esperat: `git -C /home/sionandreu/git/base-react-lot2 status` net. Executar-hi `npm ci`.
- [ ] **Pas 2:** Escriure el test de prova `lib/util/equals.test.ts`, que comprova un cas d'igualtat i un de desigualtat de la funció que exporta `lib/util/equals.ts`.
- [ ] **Pas 3:** Afegir la configuració anterior. Executar `npm test`. Resultat esperat: 1 fitxer i tots els tests en verd.
- [ ] **Pas 4:** Executar `npm run build`. Resultat esperat: el build passa, perquè l'àlies no ha d'afectar el paquet.
- [ ] **Pas 5:** Commit: "Afegir Vitest i l'àlies reactlib a base-react".

### Tasca 2: `appearance` a `MuiBaseApp` i alçada de capçalera (R01, R05)

**Fitxers:**
- Modificar: `lib/components/mui/MuiBaseAppContext.tsx`, `lib/components/mui/MuiBaseApp.tsx`, `lib/components/mui/AppBar.tsx`, `lib/components/mui/Menu.tsx:342`, `lib/components/mui/form/MuiForm.tsx:115`, `lib/components/mui/detail/MuiDetail.tsx`
- Crear: `lib/util/useHeaderHeight.ts`
- Modificar: `lib/main.ts` (exports)
- Test: `lib/components/mui/MuiBaseApp.appearance.test.tsx`

**Interfícies:**
- Produeix (`MuiBaseAppContext.tsx`):
  ```ts
  export type MenuColors = {
      background?: string; textPrimary?: string; textSecondary?: string; divider?: string;
      accent?: string; selectedBackground?: string; hoverBackground?: string;
  };
  export type MuiBaseAppAppearance = {
      headerHeight?: number;
      menuCollapsedWidth?: number;
      menuIconSize?: number;
      menuSelectedColor?: string;
      menuSelectedIndicator?: 'line' | 'background' | 'none';
      menuColors?: MenuColors;
  };
  // MuiBaseAppContextType afegeix: appearance: MuiBaseAppAppearance
  export const useMuiBaseAppAppearance: () => MuiBaseAppAppearance; // {} fora del provider
  ```
- Produeix: la prop `MuiBaseAppProps.appearance?: MuiBaseAppAppearance`; `useHeaderHeight(): number` a `lib/util/useHeaderHeight.ts`, que retorna `appearance.headerHeight ?? (smallHeader ? 56 : 64)`.
- Tots els tipus i hooks s'exporten des de `lib/main.ts`.

- [ ] **Pas 1:** Escriure els tests:
  - `headerHeight per defecte és 64`: renderitzar `MuiBaseApp` mínim (mock de la mida de pantalla gran) i comprovar que `useHeaderHeight()` retorna 64 i que l'AppBar té `minHeight`/`height` igual que ara;
  - `headerHeight indicat s'aplica`: amb `appearance={{ headerHeight: 74 }}`, el hook retorna 74, l'AppBar té `height: 74px`, el `marginTop` del menú és 74px i el `top` sticky de la barra de `MuiForm` és `74px`;
  - `pantalla petita sense valor`: el hook retorna 56.
- [ ] **Pas 2:** Executar `npm test -- MuiBaseApp.appearance`. Resultat esperat: FALLA (`appearance` no existeix).
- [ ] **Pas 3:** Implementar el tipus, el context i el hook. Canviar els valors fixos `theme.spacing(7)/(8)` de `Menu.tsx:342` i `'56px'/'64px'` de `MuiForm.tsx:115` (i l'equivalent de `MuiDetail`) per `useHeaderHeight()`. L'AppBar només fixa l'alçada quan `appearance.headerHeight` està definit; si no, es manté el comportament per defecte de la `Toolbar` de MUI.
- [ ] **Pas 4:** Executar `npm test` i `npx tsc --noEmit -p .`. Resultat esperat: PASSA.
- [ ] **Pas 5:** Commit: "Paràmetre appearance a MuiBaseApp amb alçada de capçalera configurable".

### Tasca 3: aparença del menú (R02, R03, R04, R11, R12)

**Fitxers:**
- Modificar: `lib/components/mui/Menu.tsx` (línies 74-75 `drawerClosedWidth*`, línia 118 `fontSize: 20`, línies 176-180 la línia de submenú, `StyledList`)
- Test: `lib/components/mui/Menu.appearance.test.tsx`

**Interfícies:**
- Consumeix: `useMuiBaseAppAppearance()` (tasca 2).
- Semàntica:
  - **`menuCollapsedWidth`:** substitueix totes dues amplades plegades (petita i estàndard) i centra la icona i el botó del menú.
  - **`menuIconSize`:** substitueix el 20.
  - **`menuSelectedColor`:** substitueix `hsl(210, 100%, 60%)`.
  - **`menuSelectedIndicator`:** pot ser `'line'` (el comportament actual, per defecte), `'background'` (fons `menuColors.selectedBackground` a l'element seleccionat, com a Notib) o `'none'`.
  - **`menuColors`:** s'aplica al fons del drawer i del panell compacte, als textos, als divisors, a les icones (`textSecondary`), al títol (`accent`) i al hover. Quan no s'indica, es fan servir els colors de MUI d'ara.
- Per reproduir exactament l'estil de Notib, la referència és el CSS que avui posa `NOTIB/components/BaseApp.tsx` (conjunts TEMA / TEMA_INVERTIT / PEU) i l'equivalent de comanda a `FRONT/src/components/BaseApp.tsx:334-` (`menuColorSetSx`).

- [ ] **Pas 1:** Escriure els tests:
  - `per defecte`: l'amplada plegada és `calc(theme.spacing(7) + 1px)`, la icona té `font-size: 20px` i la línia del subelement seleccionat té `background: hsl(210, 100%, 60%)`;
  - `amb aparença`: amb `{ menuCollapsedWidth: 74, menuIconSize: 24, menuSelectedColor: '#f00', menuSelectedIndicator: 'background', menuColors: { background: '#111', selectedBackground: '#222' } }`, l'amplada és 74px, les icones són de 24px, la línia ha desaparegut, l'element seleccionat té el fons `#222` i el `.MuiDrawer-paper` té el fons `#111`.
- [ ] **Pas 2:** Executar els tests. Resultat esperat: FALLA.
- [ ] **Pas 3:** Implementar-ho passant l'aparença als `styled` i als `sx` existents. No s'han d'afegir selectors que depenguin de l'estructura del DOM.
- [ ] **Pas 4:** Executar `npm test`. Resultat esperat: PASSA.
- [ ] **Pas 5:** Commit: "Aparença del menú configurable (amplada, icones, seleccionat, colors)".

### Tasca 4: marges del contingut (R06)

**Fitxers:**
- Modificar: `lib/components/BaseApp.tsx:184-216`, `lib/components/mui/MuiBaseApp.tsx`
- Test: `lib/components/BaseApp.content.test.tsx`

**Interfícies:**
- Produeix: les props `BaseAppProps.contentRootStyle?: React.CSSProperties` i `contentMainStyle?: React.CSSProperties`. Es combinen **després** dels estils actuals de l'element arrel i de `<main>`. `MuiBaseApp` les reenvia.

- [ ] **Pas 1:** Escriure els tests:
  - `per defecte`: `<main>` té `margin: 16px 24px`, i quan hi ha `marginsDisabled` no en té;
  - `amb contentMainStyle={{ padding: 24 }}`: `<main>` té `padding: 24px` i manté els altres estils.
- [ ] **Pas 2:** Executar els tests. Resultat esperat: FALLA.
- [ ] **Pas 3:** Implementar-ho.
- [ ] **Pas 4:** Executar els tests. Resultat esperat: PASSA.
- [ ] **Pas 5:** Commit: "Estils configurables de l'arrel i del main del contingut".

### Tasca 5: barra d'eines de graelles i formularis, marges, títol, `sx` com a array i botó de crear (R10, R13–R16)

**Fitxers:**
- Modificar: `lib/components/mui/Toolbar.tsx:55-56`, `lib/components/mui/datagrid/MuiDataGrid.tsx` (props, línia 1100 `gridMargins`, `sx`), `lib/components/mui/form/MuiForm.tsx` (props, línia 104), `lib/components/mui/datacommon/MuiDataCommon.tsx:398-408`, `lib/components/mui/datacommon/DataToolbar.tsx`
- Test: `lib/components/mui/datagrid/MuiDataGrid.toolbar.test.tsx`, `lib/components/mui/form/MuiForm.toolbar.test.tsx`

**Interfícies** (s'afegeixen a `MuiDataGridProps` i a `MuiFormProps`, i per tant es poden configurar via `defaultMuiComponentProps.dataGrid` / `.form`):
- `toolbarSx?: SxProps<Theme>`: es combina darrere de l'`sx` actual de la barra superior, de manera que pot substituir-ne el fons gris.
- `marginsSx?: SxProps<Theme>`: substitueix els marges per defecte (`{ m: 2 }` a la graella amb la barra superior, i `{ m: 2, mt: 3 }` al formulari).
- `MuiForm.title?: string | React.ReactElement`: amplia el tipus que hi ha ara.
- `MuiDataGrid.sx`: tipus `SxProps<Theme>`, acceptant arrays. Es combina amb els valors per defecte amb el patró `[defaults, ...(Array.isArray(sx) ? sx : [sx])]`.
- Només a `MuiDataGridProps`:
  - `toolbarCreateVariant?: 'icon' | 'button'`, per defecte `'icon'`;
  - `toolbarCreateLabel?: string`, que per defecte és `t('datacommon.create.label')`.
  - Amb `'button'` es mostra un `Button` de MUI amb `variant="contained"`, `startIcon` `add` i el text indicat. Es manté `linkTo` / `linkState` / `onClick` / `disabled` exactament com a la icona.

- [ ] **Pas 1:** Escriure els tests de la graella (amb `ResourceApiContext` simulat, igual que els tests de comanda a `FRONT/src/test`; si cal, copiar-ne el helper a `lib/test-utils.tsx`):
  - `per defecte`: la barra superior té el fons `grey[200]`, la graella té el marge de 16px i el botó de crear és un `IconButton` amb el títol `datacommon.create.label`;
  - `toolbarSx={{ backgroundColor: 'transparent' }}`: s'aplica a la barra;
  - `marginsSx={{ m: 0 }}`: s'aplica a la graella;
  - `sx={[{ color: 'red' }, { opacity: 0.5 }]}`: s'apliquen tots dos sense error;
  - `toolbarCreateVariant="button" toolbarCreateLabel="Nova aplicació"`: es mostra un `button` amb el text "Nova aplicació" i, en fer-hi clic, es crida la mateixa acció de crear;
  - `toolbarCreateVariant="button"` amb `toolbarDisableCreateLink`: el botó apareix desactivat (punt d'atenció 4).
- [ ] **Pas 2:** Escriure els tests del formulari:
  - `per defecte`: marges `m: 2, mt: 3`;
  - `title={<span data-testid="t" />}`: el títol es renderitza dins la barra;
  - `toolbarSx` i `marginsSx`: s'apliquen.
- [ ] **Pas 3:** Executar els tests. Resultat esperat: FALLA.
- [ ] **Pas 4:** Implementar-ho. `DataToolbar` ha d'acceptar i reenviar `sx`.
- [ ] **Pas 5:** Executar `npm test` i `npx tsc --noEmit -p .`. Resultat esperat: PASSA.
- [ ] **Pas 6:** Commit en dos commits:
  - "Barra d'eines, marges i títol configurables a graelles i formularis";
  - "Botó de crear amb text opcional a les graelles".

### Tasca 6: exports (R09) i correccions genèriques de Notib

**Fitxers:**
- Modificar: `lib/main.ts`, i els fitxers de `lib/` on Notib té les correccions.
- Test: un test per correcció, al costat del component.

- [ ] **Pas 1:** Executar `diff -r /home/sionandreu/git/base-react-lot2/lib NOTIB/../lib` per localitzar les 27 diferències. Classificar-les. **Només** es porten aquestes:
  - la protecció contra el doble enviament (`isSaving`) a `MuiForm` / `FormDialog`, activa per defecte;
  - `extraActions` / `liveButtons`;
  - `rowUpdateShowInMenu`.
  - La resta, incloent-hi la redirecció quan es rep un 401 i la importació de codi de l'aplicació a `AuthButton`, s'anota en una llista per a la tasca 16.
- [ ] **Pas 2:** Escriure els tests:
  - `doble clic a desar només envia una petició`;
  - `extraActions es mostren a la barra`;
  - `rowUpdateShowInMenu mou l'acció d'editar al menú de fila`;
  - `per defecte, sense les props noves, la barra i el menú de fila no canvien`.
- [ ] **Pas 3:** Executar els tests. Resultat esperat: FALLA.
- [ ] **Pas 4:** Portar el codi i exportar `ResourceApiError` i `FormFieldRefOptionsResponse` des de `lib/main.ts`.
- [ ] **Pas 5:** Executar `npm test`, `npx tsc --noEmit -p .` i `npm run build`. Resultat esperat: PASSA.
- [ ] **Pas 6:** Commit: "Portar correccions genèriques de Notib i exportar tipus d'errors i opcions".

### Tasca 7: `src/lot2`, fonaments (layout, temes, icones, textos)

**Fitxers** (tot a `src/lot2/`):
- Crear: `layout.ts` (copiat de `NOTIB/utils/layout.ts`), `icons.ts` (de `NOTIB/utils/icons.ts`, només les claus comunes que fan servir els components de `lot2`), `i18n.ts`, `theme.ts`, `README.md`, `index.ts`
- Test: `src/lot2/theme.test.ts`

**Interfícies:**
```ts
// theme.ts
export type Lot2Colors = { main: string; dark: string; light?: string; soft: string };
export type Lot2Themes = { light: Theme; dark: Theme; dracula: Theme };
export const createLot2Themes: (colors: Lot2Colors) => Lot2Themes;
export const accentColor: (theme: Theme) => string;
export const accentFillColor: (theme: Theme) => string;
// i18n.ts — espai de noms 'lot2', idiomes ca/es/en
export const lot2Translations: Record<'ca' | 'es' | 'en', Record<string, any>>;
export const LOT2_I18N_NS = 'lot2';
// index.ts reexporta tot el que és públic de lot2
```
- **`createLot2Themes`:** és `NOTIB/theme.ts` parametritzat. El contingut que avui fa servir les constants MAIN, DARK, LIGHT i SOFT passa a llegir `colors`. `light` per defecte és `lighten(main, 0.3)`. Es mantenen la tipografia, `gridColumnHeadersOverride`, `baseComponentStyles` i Dracula.
- **`README.md`:** explica les regles de la carpeta (no s'edita a l'aplicació; es copia sencera des de base-react; només depèn de `reactlib`) i quin és l'origen de cada fitxer.

- [ ] **Pas 1:** Escriure els tests:
  - `createLot2Themes({ main: '#83cb41', dark: '#497e3a', light: '#a8db7a', soft: '#F2F9EB' })`: el `palette.primary.main` del tema `light` és `#83cb41` i els tres temes tenen `palette.mode` `light`, `dark` i `dark`;
  - `accentColor` del tema clar és igual al que retorna avui Notib amb els seus colors (valor copiat de l'execució a Notib);
  - test d'importació: cap fitxer de `src/lot2/**` no conté `from '../` fora de `src/lot2` (fer-ho amb un `glob` + `readFileSync`).
- [ ] **Pas 2:** Executar els tests. Resultat esperat: FALLA.
- [ ] **Pas 3:** Implementar-ho.
- [ ] **Pas 4:** Executar els tests. Resultat esperat: PASSA.
- [ ] **Pas 5:** Commit: "Carpeta lot2: layout, fàbrica de temes, icones i textos comuns".

### Tasca 8: `src/lot2`, marc de pantalla, títol i filtres

**Fitxers:**
- Crear:
  - `src/lot2/ScreenFrame.tsx`, `ScreenFrameContext.ts`, `ScreenTitle.tsx`, `titleBarControlsSx.ts`, `formScreenTitle.tsx`;
  - `FilterButtons.tsx`, `FilterCount.tsx`, `springFilterCount.ts`, `StyledBadge.tsx`, `lot2ComponentProps.ts`.
  - Origen: els fitxers homònims de `NOTIB/components` i `NOTIB/utils`.
- Test: `src/lot2/ScreenFrame.test.tsx`, `src/lot2/springFilterCount.test.ts`, `src/lot2/FilterCount.test.tsx`

**Interfícies:**
- `ScreenFrame` té les mateixes props que `NOTIB/components/ScreenFrame.tsx`: `icon?`, `title?`, `actions?`, `padded?`, `fitContent?` i `children`.
- `ScreenTitle` i `useScreenFrame()` tenen les mateixes signatures que a Notib.
- `countSpringFilterTerms(filter?: string): number`.
- Produeix:
  ```ts
  // lot2ComponentProps.ts — el que abans feia el CSS de ScreenFrame amb :has i > div > div > main
  export const lot2DefaultMuiComponentProps: (theme: Theme) => DefaultMuiComponentProps;
  // dataGrid: { toolbarType: 'upper', toolbarSx: <barra de títol sticky, fons transparent>,
  //             marginsSx: { m: `${MAIN_PADDING}px` }, toolbarCreateVariant: 'button' }
  // form:     { toolbarSx: <idem>, marginsSx: <marges de Notib> }
  ```
- **Canvi respecte a Notib:** `screenFrameSx` es queda només amb el que no és de base-react (padding del marc i `fitContent`). **S'eliminen** tots els selectors `:has(...)` i `LIB_TITLE_TOOLBAR`, que se substitueixen per `lot2DefaultMuiComponentProps`.

- [ ] **Pas 1:** Escriure els tests:
  - el test de `countSpringFilterTerms` copia els casos dels tests de Notib, si n'hi ha; si no, cobreix com a mínim `undefined→0`, `"a:1"→1` i `"a:1 and b~'x'"→2`;
  - `ScreenFrame` renderitza el títol i la icona;
  - `FilterCount` mostra el recompte al títol del `ScreenFrame` per portal;
  - `ScreenFrame.tsx` no conté `:has(` (test de text).
- [ ] **Pas 2:** Executar els tests. Resultat esperat: FALLA.
- [ ] **Pas 3:** Implementar-ho.
- [ ] **Pas 4:** Executar els tests. Resultat esperat: PASSA.
- [ ] **Pas 5:** Commit: "Carpeta lot2: marc de pantalla, títol, filtres i props per defecte dels components".

### Tasca 9: `src/lot2`, `Lot2BaseApp` i `DrassanaFooter`

**Fitxers:**
- Crear: `src/lot2/Lot2BaseApp.tsx`, `src/lot2/DrassanaFooter.tsx`, `src/lot2/menuColors.ts`, `src/lot2/assets/drassana.png` (copiat de `NOTIB/assets`)
- Test: `src/lot2/Lot2BaseApp.test.tsx`

**Interfícies:**
```ts
export type Lot2MenuAppearance = 'theme' | 'inverse' | 'footer';
export const lot2MenuColors: (theme: Theme, appearance: Lot2MenuAppearance) => MenuColors; // TEMA / TEMA_INVERTIT / PEU de Notib
export const LOT2_APPEARANCE: MuiBaseAppAppearance; // headerHeight 74, menuCollapsedWidth 74, menuIconSize 24, menuSelectedIndicator 'background'
export type Lot2BaseAppProps = Omit<MuiBaseAppProps, 'appearance' | 'footer' | 'footerHeight'> & {
    logo: string; logoDark?: string;               // es tria segons theme.palette.mode
    menuAppearance?: Lot2MenuAppearance;           // per defecte 'theme'
    footer?: React.ReactElement | false;           // per defecte <DrassanaFooter />
};
export const Lot2BaseApp: React.FC<Lot2BaseAppProps>;
export const DrassanaFooter: React.FC<{ links?: { label: string; to: string }[] }>;
```
- `Lot2BaseApp` combina:
  - `appearance`: `LOT2_APPEARANCE` + `menuColors` de `lot2MenuColors`;
  - `contentMainStyle`: el padding i el gap de Notib (`MAIN_PADDING`, `MAIN_GAP`);
  - `defaultMuiComponentProps`: `lot2DefaultMuiComponentProps(theme)`, combinat en profunditat amb el que passa l'aplicació, de manera que l'aplicació guanya.
- La resta de props passa directament a `MuiBaseApp` (capçalera, extensions de l'usuari, i18n, router...).
- `DrassanaFooter` no importa `react-router-dom` directament, sinó que fa servir el `linkComponent` del context de base-react.

- [ ] **Pas 1:** Escriure els tests:
  - `Lot2BaseApp` renderitza l'AppBar de 74px, el peu de Drassana i el logo clar en tema clar;
  - amb tema fosc i `logoDark`, es mostra el logo fosc;
  - `menuAppearance="inverse"` aplica el fons invers al drawer;
  - els `defaultMuiComponentProps` de l'aplicació (`dataGrid.toolbarCreateVariant: 'icon'`) guanyen als de `lot2`.
- [ ] **Pas 2:** Executar els tests. Resultat esperat: FALLA.
- [ ] **Pas 3:** Implementar-ho.
- [ ] **Pas 4:** Executar els tests. Resultat esperat: PASSA.
- [ ] **Pas 5:** Commit: "Carpeta lot2: Lot2BaseApp i peu de Drassana".

### Tasca 10: pujar la branca de base-react

- [ ] **Pas 1:** Executar `npm test`, `npx tsc --noEmit -p .`, `npm run lint` i `npm run build` al worktree. Resultat esperat: tot passa.
- [ ] **Pas 2:** Executar `git push -u origin v2-parametres-visuals`. Anotar el hash del darrer commit com a `LOT2_BASE`; es farà servir a les tasques 11 i 16.

---

## Part B: comanda (branca `comanda-lot2-visual`)

### Tasca 11: copiar `lib/` i `src/lot2/` a comanda

**Fitxers:**
- Modificar: `FRONT/lib/**`, mitjançant un merge a 3 bandes per fitxer amb `git merge-file <comanda> <base-react@bea8c9e> <base-react@LOT2_BASE>`. S'ha de fer servir un directori de treball amb rutes completes per evitar col·lisions de noms de fitxer.
- Crear: `FRONT/src/lot2/**`, copiat literalment.
- Modificar: `FRONT/vite.config.ts:40`, on `coverage.exclude` passa a ser `['lib/**', 'src/lot2/**']`.
- Modificar: `/home/sionandreu/.claude/projects/-home-sionandreu-git-comanda/memory/sync-base-boot-base-react.md`, amb la nova base de base-react: `v2-parametres-visuals @ LOT2_BASE`.

- [ ] **Pas 1:** Crear la branca: `git switch -c comanda-lot2-visual comanda-dev-libs-update`. Els canvis locals de l'usuari es mantenen sense afegir-los a l'stage.
- [ ] **Pas 2:** Fer el merge. Resultat esperat: cap conflicte sense resoldre, i totes les personalitzacions de comanda de les restriccions globals hi continuen. Es comprova amb `grep -n "KeycloakAuthProvider" lib/main.ts` i `grep -n "stackTrace" lib/components/ResourceApiProvider.tsx`.
- [ ] **Pas 3:** Executar `diff -r /home/sionandreu/git/base-react-lot2/src/lot2 FRONT/src/lot2`. Resultat esperat: cap diferència.
- [ ] **Pas 4:** Executar `npm test`, `npm run typecheck` i `npm run lint`. Resultat esperat: PASSA, i l'aspecte de comanda encara no ha canviat perquè cap paràmetre està indicat.
- [ ] **Pas 5:** Fer dos commits amb rutes explícites:
  - "Actualitzar la còpia de base-react a v2-parametres-visuals";
  - "Afegir la carpeta comuna lot2".

### Tasca 12: tema i `BaseApp` de comanda sobre `lot2`

**Fitxers:**
- Modificar: `FRONT/src/theme.ts`, que passa a ser `createLot2Themes(COMANDA_COLORS)` i exporta `lightTheme`, `darkTheme` i `draculaTheme` amb els mateixos noms que ara.
- Modificar: `FRONT/src/components/BaseApp.tsx`, on `MuiBaseApp` se substitueix per `Lot2BaseApp`. S'eliminen `getMenuColorSet` / `menuColorSetSx`, perquè ara ho fa `lot2MenuColors`, i s'elimina `ComandaFooter`.
- Modificar: `FRONT/src/App.tsx:270-300`, per passar `logo` / `logoDark` i deixar d'utilitzar `appbarBackgroundColor`, si `lot2` ja ho resol.
- Eliminar: `FRONT/src/components/ComandaFooter.tsx` i `ComandaFooter.test.tsx`.
- Test: `FRONT/src/theme.test.ts`, `FRONT/src/components/BaseApp.test.tsx`

**Interfícies:**
- Produeix `export const COMANDA_COLORS: Lot2Colors` a `theme.ts`.
- Es mantenen intactes, dins de `Lot2BaseApp`:
  - `headerAdditionalComponents`: entrades de menú, `SystemTimeDisplay`, `Alarms` i idiomes;
  - `headerAdditionalAuthComponents`: darrera connexió, perfil i `RoleSelector`;
  - `headerAuthBadgeIcon`, `formFieldComponents` (`AppFormFieldReference`), `fixedContentExpandsToAvailableHeightEnabled` i `marginsDisabled`;
  - els diàlegs d'alarmes i de perfil.

- [ ] **Pas 1:** Escriure els tests:
  - `COMANDA_COLORS.light === '#cb6c88'`;
  - el contrast entre `COMANDA_COLORS.main` i `#fff` és ≥ 4.5 (funció de luminància relativa WCAG dins del test);
  - els temes `lightTheme`, `darkTheme` i `draculaTheme` tenen `primary.main` derivat de `COMANDA_COLORS`;
  - a `BaseApp.test.tsx`: la capçalera continua mostrant l'hora del sistema, les alarmes (si hi ha l'enllaç `alarma`) i el selector de rol; el peu de Drassana és present.
- [ ] **Pas 2:** Executar `npm test -- theme BaseApp`. Resultat esperat: FALLA.
- [ ] **Pas 3:** Implementar-ho. Si `#A8456A` no arriba a 4.5, enfosquir `main` fins que hi arribi.
- [ ] **Pas 4:** Executar `npm test`, `npm run typecheck` i `npm run lint`. Resultat esperat: PASSA.
- [ ] **Pas 5:** Commit: "Tema rosa fosc i BaseApp de comanda sobre lot2".

### Tasca 13: formularis (modals simples, pàgines complexes)

**Fitxers:**
- Modificar: `FRONT/src/pages/AlarmaConfig.tsx`, `FRONT/src/pages/Apps.tsx`, `FRONT/src/AppRoutes.tsx:151-156` i `212-217`
- Test: `FRONT/src/pages/AlarmaConfig.test.tsx`, `FRONT/src/pages/Apps.test.tsx`, `FRONT/src/AppRoutes.test.tsx`

**Criteri (decisió de Sion):**
- un formulari és **complex** si té pestanyes, subtaules (`MuiDataGrid` a dins) o diverses seccions de dades relacionades; en aquest cas es queda com a pàgina amb `ScreenFrame`, i es fa servir `formScreenTitle` com a `title` de `MuiForm`;
- si no, és **simple** i passa a ser un modal (`popupEditActive` + `popupEditFormContent` a la graella, com els altres 18 formularis de comanda que ja s'obren en un diàleg).

**Classificació proposada** (s'ha de confirmar al pas 1):
- `AppForm` (`Apps.tsx:792`) és complex, perquè té les subtaules d'entorns. Es queda com a pàgina.
- `AlarmaConfigForm` (`AlarmaConfig.tsx:450`) és simple si no té pestanyes ni subtaules. En aquest cas, passa a ser un modal i s'eliminen les rutes `alarma/form` i `alarma/form/:id`.

- [ ] **Pas 1:** Llegir tots dos formularis, aplicar-hi el criteri i escriure la classificació al missatge del commit. Si `AlarmaConfigForm` resulta complex, es queda com a pàgina i el pas 3 es salta.
- [ ] **Pas 2:** Escriure els tests:
  - `crear una alarma obre un diàleg i no navega`;
  - `tancar el diàleg amb canvis demana confirmació` (punt d'atenció 5), seguint el comportament de `FormDialog` de base-react;
  - `/alarma/form` ja no té ruta` (si n'hi ha);
  - `AppForm mostra el títol dins la barra sticky`.
- [ ] **Pas 3:** Executar els tests. Resultat esperat: FALLA.
- [ ] **Pas 4:** Implementar-ho.
- [ ] **Pas 5:** Executar `npm test`, `npm run typecheck` i `npm run lint`. Resultat esperat: PASSA.
- [ ] **Pas 6:** Commit: "Formularis simples com a modals i complexos com a pàgines amb ScreenFrame".

### Tasca 14: pàgines amb graella sobre `ScreenFrame`

**Fitxers:**
- Modificar les 22 pàgines que contenen `<MuiDataGrid` / `<GridPage` (`grep -rln "<MuiDataGrid\|<GridPage" FRONT/src/pages`), juntament amb els seus `*.test.tsx`.

**Patró per pàgina** (seguir com a model `NOTIB/pages/entitat` o `NOTIB/pages/organ`):
- l'arrel és `<ScreenFrame icon=… title=…>`;
- `PageTitle` es manté si fixa el títol del document;
- el filtre passa per `FilterButtons`, i `FilterCount` s'hi afegeix quan la pàgina té filtre;
- el botó de crear surt amb text, que ja s'aplica per defecte via `lot2DefaultMuiComponentProps`. Només s'indica `toolbarCreateLabel` si el text "Crear" no és prou explícit (per exemple "Nova aplicació"), i el text s'afegeix a l'i18n de comanda;
- s'eliminen els estils locals que dupliquen marges o barres d'eines.

- [ ] **Pas 1:** Fer la llista de pàgines i, per a cadascuna, la icona (una clau de `lot2/icons.ts` o d'icones de comanda). Executar `npm test` per tenir el punt de partida.
- [ ] **Pas 2:** Per a cada pàgina, adaptar el test perquè comprovi que el títol és dins del `ScreenFrame` i, si la pàgina té creació, que el botó de crear té text. Llavors adaptar la pàgina.
- [ ] **Pas 3:** Executar `npm test`, `npm run typecheck` i `npm run lint`. Resultat esperat: PASSA.
- [ ] **Pas 4:** Fer un commit per àrea funcional, per exemple "configuració (apps, entorns, versions)", "monitorització (monitor, integracions, cache, broker, tasques)", "estadístiques (dimensions, indicadors, widgets, plantilles, paletes)" i "alarmes, avisos i paràmetres".

### Tasca 15: pàgines amb disseny propi, i verificació visual

**Fitxers:**
- Modificar: `EstadisticaDashboards.tsx`, `EstadisticaDashboardView.tsx`, `EstadisticaDashboardEdit.tsx`, `CalendariEstadistiques.tsx`, `pages/salut/**`, `Sitemap.tsx`, `pages/accessibilitat/**`, `NotFound.tsx`, `DimensioValor.tsx`, `QueueMessages.tsx` i les que quedin sense `ScreenFrame` després de la tasca 14, juntament amb els seus tests.

- [ ] **Pas 1:** Embolcallar cada pàgina amb `<ScreenFrame icon title>`, amb `fitContent` quan el contingut necessiti tota l'alçada (dashboards i calendari). El contingut no es toca.
- [ ] **Pas 2:** Executar `npm test`, `npm run typecheck` i `npm run lint`. Resultat esperat: PASSA.
- [ ] **Pas 3:** Verificació visual amb l'skill `run`:
  - arrencar el frontend en mode dev contra un backend local;
  - capturar el menú (plegat i desplegat, en els modes theme/inverse/footer), una graella amb filtre, el modal d'alarma, `AppForm`, un dashboard i el peu, en els temes clar, fosc i Dracula;
  - comparar-ho amb Notib en les mateixes pantalles.
  - Si cal ajustar algun color, es fa a `COMANDA_COLORS`, mai a `src/lot2`.
- [ ] **Pas 4:** Commit: "Marc lot2 a les pàgines amb disseny propi" (i "Ajustar colors de comanda", si escau).

### Tasca 16: document de migració

**Fitxers:**
- Crear: `/home/sionandreu/git/base-react-lot2/docs/lot2/migracio-aspecte-visual.md`
- Crear: `/home/sionandreu/git/comanda/doc/lot2-aspecte-visual.md`. És un fitxer breu: un enllaç al document de base-react (branca `v2-parametres-visuals`) i un resum del que s'ha fet a comanda.

**Contingut** (seguint l'apartat "Document de migració" del spec):
1. **Pas comú per a totes les aplicacions.** Actualitzar `lib/` amb un merge a 3 bandes (amb les ordres concretes de la tasca 11), copiar `src/lot2/`, afegir l'exclusió de cobertura, crear el fitxer de colors, canviar `BaseApp` per `Lot2BaseApp`, embolcallar les pantalles amb `ScreenFrame` i aplicar el criteri modal/pàgina.
2. **Taula de paràmetres.** Una fila per paràmetre R01–R16 amb el nom de la prop, el valor per defecte i el valor de `lot2`.
3. **Una secció per aplicació:**
   - **Notib:** quins fitxers s'eliminen perquè ara són a `lot2` (`theme.ts`, `ScreenFrame*`, `ScreenTitle`, `titleBarControlsSx`, `FilterButtons`, `FilterCount`, `springFilterCount`, `DrassanaFooter`, `StyledBadge`, `utils/layout.ts` i el CSS del menú i de la capçalera de `BaseApp.tsx`), la llista de diferències de `lib/` de la tasca 6 que no s'han portat (com la redirecció quan es rep un 401, o la importació de codi de l'aplicació a `AuthButton`), i els botons de crear amb text.
   - **Distribucio** (`dis-dev`): eliminar `StyledMuiGrid` (els observadors) i `FilterAutoApply`, i les diferències del tema de 684 línies que s'han de revisar. Cal llegir `/home/sionandreu/git/distribucio` per fer-ne la llista.
   - **Components pendents de reconciliar** (`GridFormField`, `MassiveActionSelector`, `MenuButton`): comanda no els fa servir, i per això no entren a `lot2` en aquesta fase (decisió del pla). El document indica en què difereixen les versions de Notib i de Distribucio, perquè es puguin unificar en la fase de migració.
   - **Ripea** (`ripea-1.0-dev`): adopció des de zero, partint del `theme.ts` antic (`#004B99`).
   - **Comanda:** com a exemple, amb enllaços als commits de la part B.

- [ ] **Pas 1:** Redactar el document i comprovar que cada fitxer que s'esmenta existeix a la branca indicada (`git -C <repo> ls-tree -r <branca> --name-only | grep`).
- [ ] **Pas 2:** Fer commit a base-react, "Document de migració a l'aspecte visual Lot2", i `git push`. Fer commit a comanda: "Enllaç al document de migració Lot2".
- [ ] **Pas 3:** Si el commit de base-react ha canviat respecte a `LOT2_BASE`, actualitzar el commit de la memòria de sincronització. El document no és dins de `lib/` ni de `src/lot2/`, així que no cal tornar a copiar res.

### Tasca 17: verificació final

- [ ] **Pas 1:** A base-react: `npm test`, `npx tsc --noEmit -p .`, `npm run lint` i `npm run build`. Resultat esperat: tot passa.
- [ ] **Pas 2:** A comanda (`FRONT`): `npm test`, `npm run test:coverage` (els llindars de `doc/testing/THRESHOLD_COBERTURA_MINIM.md` s'han de complir), `npm run typecheck`, `npm run lint` i `npm run build`. Resultat esperat: tot passa.
- [ ] **Pas 3:** Executar `diff -r /home/sionandreu/git/base-react-lot2/src/lot2 FRONT/src/lot2`. Resultat esperat: cap diferència.
- [ ] **Pas 4:** Executar `git status`. Resultat esperat: només hi queden els canvis locals de l'usuari, igual que abans de començar.
