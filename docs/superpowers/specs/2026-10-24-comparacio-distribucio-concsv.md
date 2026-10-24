# Comparació Lot2: Distribucio i ConCSV contra `v2-parametres-visuals`

Data: 2026-10-24. Només lectura. Referència: base-react `/home/sionandreu/git/base-react-lot2`, branca
`v2-parametres-visuals` (cap `fff88af`; `lib/` = `cb7141e`, `src/lot2/` = `7f51df8`).

Rutes curtes que es fan servir a tot el document:

- **DIS** = `distribucio-back/src/main/reactapp/distribucio-back/` (branca `dis-dev`, cap `8a6613c6f`, 2026-10-23)
- **CSV** = `concsv-back/src/main/reactapp/concsv-back/` (branca `concsv-dev`, cap `0f63b87`, 2026-10-23).
  `concsv-front/` és el front públic: no té `lib/` de base-react ni res del Lot2. No s'hi ha trobat res rellevant.
- **LOT2** = `src/lot2/` de base-react; **LIB** = `lib/` de base-react.

Les línies citades són les del cap de cada branca.

---

## 1. Commits del redisseny i sincronització de `lib/`

### Distribucio (`dis-dev`)

| Commit | Data | Què fa |
|--------|------|--------|
| `fd2d96921` | 2026-10-19 | «Millora del disseny del mòdul back REACT #912». És el gros: 106 fitxers. Afegeix `ScreenFrame`, `ScreenFrameContext`, `FilterAutoApply(+Context)`, `titleBarControlsSx`, `util/layout.ts`, `theme.ts` (+245), `icons.ts` (+96), `StyledMuiGrid`/`StyledMuiFilter`, `BaseApp`/`App` i totes les pàgines |
| `ec39c2a12` | 2026-10-20 | Mateix issue. Ajustos a `RegistreGrid`, `RegistreDetail`, `MonitorSistema` i `theme.ts` (+6/-6) |
| `f82b03701` | 2026-10-22 | Mateix issue. Filtres: el bloc de botons passa a `md: 0.5`/`1`, textos «Avançat»/«Bàsic», cerca avançada a `AnnexFilter` i `IntegracioFilter` |
| `f7b5318b8` | 2026-10-23 | «Mostrar la darrera connexió de l'usuari» (`UserLastConection`) |

**`lib/`:** el commit de base-react més proper continua sent `b37225c` (2026-06-04), amb 52 línies diferents.
Les diferències són a tres fitxers:

- `ContainerAuthProvider.tsx`: el logout va per navegació completa a `/usuari/logout?origen=react` (és propi de l'app);
- `detail/Detail.tsx`: `apiRef`/`refresh` i `apiGetOne(id, { perspectives })`;
- `mui/form/FormDialog.tsx`: un efecte que sincronitza els botons.

Les dues últimes **ja són a la nostra `lib/`**: venen de `v2`.

### ConCSV (`concsv-dev`)

| Commit | Data | Què fa |
|--------|------|--------|
| `b5850cb` | 2026-10-15 | «Millora del disseny del mòdul back REACT #17». Primera còpia del marc (37 fitxers: `ScreenFrame`, `FilterAutoApply`, `theme.ts` +219, `layout.ts`, `icons.ts`...) |
| `0bd010e` | 2026-10-16 | Comptador de filtres: s'ignoren els literals (`maskLiterals`) |
| `903c871` | 2026-10-20 | Icones |
| `66c5c1b` | 2026-10-22 | Porta les correccions de Distribucio: `containerType` a `.MuiList-root`, panell flotant, `textTransform` de `MuiTab`, segona fila de les `actions` del `ScreenFrame`, botons per defecte de `FormActionDialog` |
| `5c3926f` | 2026-10-22 | **«Millores d'accessibilitat»** (només és a ConCSV): `<li>` al menú, `titleDisabled` a la graella, `lang` de `<html>` |
| `0f63b87` | 2026-10-23 | Darrera connexió de l'usuari |

**`lib/`:** el commit més proper també és `b37225c`, amb 27 línies diferents.

- `ContainerAuthProvider` és igual que el de Distribucio.
- `Detail` només porta la correcció de `perspectives`.
- `FormDialog` té l'efecte de sincronització dels botons **comentat** (vegeu 4.10).

### Relació entre les dues apps

ConCSV és pràcticament una còpia de Distribucio. Per fitxer (diff `<`DIS / `>`CSV):

- iguals: `ScreenFrameContext`, `FilterAutoApplyContext`, `FilterCountChip`, `titleBarControlsSx`,
  `StyledBadge`, `DrassanaFooter`, `layout.ts`;
- 1–34 línies diferents: `ScreenFrame`, `StyledMuiGrid`, `StyledMuiFilter`, `FilterAutoApply`, `BaseApp`,
  `FormActionDialog`;
- `theme.ts`: només canvien els colors i els selectors amb `<li>`.

Distribucio és la versió més nova de quasi tot; ConCSV només hi afegeix l'accessibilitat de `5c3926f`.

---

## 2. Peça per peça: com queda respecte de LOT2/LIB

| Peça | Distribucio / ConCSV | LOT2 / LIB (`v2-parametres-visuals`) | Conclusió |
|------|----------------------|--------------------------------------|-----------|
| `layout.ts` | Idèntic llevat de `SCREEN_GRID_MIN_HEIGHT = 320` (DIS `src/util/layout.ts:45-50`) | No hi és | Cobert. `SCREEN_GRID_MIN_HEIGHT` es comenta a 3.b.10 |
| `theme.ts` | 684 línies. Colors, `accentColor`/`accentFillColor`, `MuiBadge`/`MuiChip`/`MuiPaginationItem`/`MuiTreeItem`, capçalera de graella i barres de desplaçament iguals que LOT2 | `createLot2Themes` | Ja analitzat al doc 4.2. Aquí es concreten les recomanacions (3.b i 4) |
| `MuiDrawer` del tema | Selectors d'estructura (`& > .MuiBox-root:first-of-type`, `& > div > .MuiBox-root` top/left, mida d'icona, `display:none` del text) + `containerType` a `.MuiList-root` | R01–R03 i R11 a la llibreria; el tema només conserva la columna d'icones dins `@container`; `containerType` a `.MuiList-root` des de `7f51df8` | **Cobert, i millor a LOT2** |
| Colors del menú (`getMenuColorSet` + `menuColorSetSx`, DIS `components/BaseApp.tsx:46-92,175-205`) | CSS sobre el DOM | `appearance.menuColors` (R12) + `lot2MenuColors` | Cobert. **Contradiccions de colors** a 4.1 i 4.2 |
| `frameSx` (DIS `BaseApp.tsx:136-156`) | `> div > div > main` | `contentRootStyle`/`contentMainStyle` (R06) | Cobert |
| `ScreenFrame` | Sempre amb barra pròpia (`icon`/`title` obligatoris); la barra d'eines de la graella s'hi superposa amb `position:absolute`, `MutationObserver` + `ResizeObserver`, i salta a una segona fila si no hi cap (DIS `components/ScreenFrame.tsx:57-96,107,125-132`) | Títol dins la barra d'eines de la llibreria (`ScreenTitle` + `lot2ScreenGridProps`) | Arquitectura de LOT2 millor (sense observadors ni selectors). **Però** perdem el salt de fila (3.b.4) i hi ha un problema en estat d'error (4.6) |
| `ScreenTitle` / h1 | `Typography variant=h6 component=h1` (DIS `ScreenFrame.tsx:142`) | Igual (`ScreenTitle.tsx:39`), amb `aria-hidden` a la icona | Cobert (LOT2 una mica millor) |
| `titleBarControlsSx` | Subconjunt del nostre: no té `IconButton`, ni `textTransform`, ni `border: 1px solid`; al desactivat sí que fixa el fons i la vora (DIS `components/titleBarControlsSx.ts:43-51`) | Superconjunt. Fons i vora del desactivat comentats (`titleBarControlsSx.ts:53-55`) | Cobert. Detall a 3.b.9 |
| Filtre ràpid a la barra | Alçada de 32px (DIS `components/StyledMuiGrid.tsx:311-312`) | No | 3.b.8 |
| `FilterCountChip` vs `FilterCount` | Text breu amb portal; `filterCount` com a número o funció; `maskLiterals` amb escapament `\` (DIS `components/FilterCountChip.tsx:175-233`) | `FilterCount` + `countSpringFilterTerms` (ignora els literals, sense escapament; només `springFilter`) | Quasi cobert. 3.b.6 i 3.b.7 |
| Botons del filtre | `GridButton`/`GridButtonField` + `CombinedIcon`; la cerca avançada és un camp `advanced` del formulari del filtre (DIS `components/StyledMuiFilter.tsx:221-235`) | `FilterClearButton`, `FilterAdvancedButton` (estat a l'app) | Cobert. Diferències d'icona i de text a 4.3 |
| `FilterAutoApply` | Aplicació automàtica pròpia: espera 1 s als camps de text, no aplica dates invàlides ni dades iguals, aplica amb Tab | Comportament natiu de `MuiFilter` (decisió del doc 4.1) | Es manté la decisió. Possible millora a LIB a 3.a.6 |
| Botó de crear | `ToolbarButton` propi (text amagat per sota de `md`, tooltip) amb `toolbarHideCreate` (DIS `StyledMuiGrid.tsx:41-71,264-277`) | R16 (`toolbarCreateVariant: 'button'`) | Cobert. Variant responsiva a 3.a.5 |
| `DrassanaFooter` | Només títol, versió i logo; `alt="foot_logo"` | Superconjunt: enllaços, i18n, logos, `linkComponent` | Cobert. Accessibilitat del logo a 3.b.12 |
| `StyledBadge` | `primary` → `accentColor(theme)` (DIS `components/StyledBadge.tsx:13-16`) | `primary` → `palette.primary.main` | 3.b.5 (cap de les dues és la bona) |
| Icones | Catàleg ampli amb seccions comunes «Accions», «Estats», «Filtre», «Selecció» (DIS `src/util/icons.ts:49-127`, CSV `src/util/icons.ts:23-55`) | Pantalles comunes, rols i filtre | 3.b.3 i 4.3 |
| i18n | `common.filterCount_one/_other` amb `{{num}}`, `advancedSearchOpen` = «Avançat» / `advancedSearchClose` = «Bàsic» | `filtresAplicats_*` amb `{{count}}`, «Avançat» / «Simple» | Cobert. Text a 4.3 |
| Capçalera (logo GOIB + separador a `MENU_WIDTH`) | ~20 línies de `logoStyle` repetides a DIS `App.tsx:293-322` i CSV `App.tsx:110-140` | L'app passa `headerLogoStyle` | 3.b.11 |
| Darrera connexió | `UserLastConection` (DIS `components/UserLastConection.tsx`, CSV ídem) | Comanda té el mateix `MenuItem` a `components/BaseApp.tsx:225-235` | 3.c |
| Títol de la pestanya | `TitolPagina` (`document.title` des del `handle` de la ruta, DIS `components/TitolPagina.tsx`) | No | 3.b.13 |
| `lang` de `<html>` | CSV `src/i18n/i18n.ts:16-21` + `index.html:2` | Comanda ja ho fa (`src/i18n/i18n.ts:26`); LOT2 no | 3.b.2 |
| `<li>` al menú | CSV `components/BaseApp.tsx:100-108` + `theme.ts:201,209` | LIB: `<ul>` amb fills que no són `<li>` | 3.a.1 |
| Títol buit de graella | CSV `StyledMuiGrid.tsx:125-128` (`titleDisabled` en lloc de `title: ''`) | LIB `Toolbar.tsx:49-58`: un `title=''` pinta un `<h6>` buit | 3.a.2 |
| Formulari emergent | `popupEditFormDialogOnClose` (no tanca amb clic fora), `fullWidth`/`maxWidth: 'md'`, `avoidSubmitIfAnyValidatorErrors` (DIS `StyledMuiGrid.tsx:377-403`) | No | 3.b.1 |

---

## 3. Millores que no tenim

Esforç: S (< 2 h), M (mig dia), L (1 dia o més). El risc és sobre l'aspecte o el comportament actual.

### (a) Per a `lib/`: paràmetres opcionals o correccions genèriques (el comportament per defecte no canvia)

1. **Entrades del menú dins `<li>` (WCAG 1.3.1; l'OAW ho marca com a llista mal construïda).**
   - **Què fa:** ConCSV embolcalla amb un `<li>` l'enllaç que pinta un `ListItemButton`
     (CSV `src/components/BaseApp.tsx:100-108`) i duplica els selectors del tema (`theme.ts:201,209`).
   - **Per què és genèric:** a LIB, `ListMenuContent` (`lib/components/mui/Menu.tsx:313-358`) pinta dins el
     `StyledList` (`<ul>`) uns `ListItemButton` (`<div>`/`<a>`), `Divider` i `Box`, cap `<li>`. Passa a totes les apps.
   - **El truc de ConCSV no es pot copiar tal qual:**
     - només arregla les entrades amb enllaç (els grups continuen sent fills directes de l'`<ul>`);
     - ha de detectar la classe `MuiListItemButton-root` al `Link` de l'app.
   - **Com encaixa:** nou `appearance.menuListItemSemantics?: boolean` (o `menuItemsAsListItems`) a
     `MuiBaseAppAppearance`. Quan és `true`, `ListMenuContent` embolcalla en un `<li>` (`listStyle: none`) cada
     `MenuItem`, inclosa la `Box` dels fills, i cada `Divider` (`component="li"`). `LOT2_APPEARANCE` el posaria a `true`.
   - **El selector de LOT2 continua funcionant:** `'&.Mui-selected:has(+ .MuiBox-root)'` segueix sent vàlid perquè el
     botó i la `Box` dels fills queden germans dins el mateix `<li>`.
   - Esforç: **M**. Risc: baix (opt-in). Les apps que mantinguin selectors d'estructura propis
     (`& > div > .MuiList-root > .MuiListItemButton-root`, com DIS/CSV) se'ls trencarien en activar-lo, però LOT2 ja no en té.

2. **Títol de les barres d'eines: sense `<h6>` buit i nivell d'encapçalament configurable.**
   - **Què fa:** ConCSV substitueix `title: ''` per `titleDisabled: true` perquè `Toolbar` pintava un `<h6>`
     buit (encapçalament buit i salt h1→h6 a l'OAW; CSV `src/components/StyledMuiGrid.tsx:125-128`).
   - **Per què és genèric:**
     - LIB `lib/components/mui/Toolbar.tsx:49-58` pinta `<Typography variant="h6">` (és a dir, un `<h6>`) per a
       qualsevol `title` de tipus text, també quan és `''`.
     - A les pantalles de LOT2 la graella principal porta `ScreenTitle` (h1), però les subtaules, les graelles de
       diàleg i el missatge d'error de `DataToolbar` (`datacommon.toolbar.error`, `DataToolbar.tsx:129`) pinten `<h6>`
       just després de l'`<h1>`. El salt h1→h6 també el marcarà l'OAW.
   - **Com encaixa:**
     - (i) correcció S: a `Toolbar.tsx`, no pintar el `Typography` quan `title === ''` (canvi de DOM mínim, cap canvi visual);
     - (ii) paràmetre opcional `toolbarTitleComponent?: React.ElementType` (per defecte `'h6'`, com ara) a
       `MuiDataGrid`/`MuiDataList`/`MuiForm` → `DataToolbar` → `Toolbar` (`component` del `Typography`). LOT2 el posaria a
       `'h2'` a `lot2DefaultMuiComponentProps`.
   - Esforç: (i) **S**; (ii) **M**. Risc: baix.

3. **`alt` del logo de la capçalera** (`lib/components/mui/AppBar.tsx:68`, `alt="logo"`).
   - DIS i Comanda passen un logo d'aplicació amb `alt` propi dins `headerTitle`, però el logo GOIB de `headerLogo`
     queda amb «logo».
   - **Com encaixa:** `headerLogoAlt?: string` a `MuiBaseApp`/`AppBar` (per defecte `'logo'`).
   - Esforç **S**. Risc nul.

4. **Fileres de la graella en tema fosc** (es detecta en comparar amb DIS `theme.ts:526-528,648-650`).
   - `lib/components/mui/datagrid/DataGridCustomStyle.tsx:9-10` pinta les files parelles amb `theme.palette.grey[200]`
     (`#eeeeee`) també en mode fosc: amb `striped`, fileres gairebé blanques amb text blanc.
   - DIS ho evita amb el seu propi `getRowClassName` i colors per tema.
   - **Correcció:** en mode fosc fer servir `alpha(theme.palette.common.white, 0.04)` o `theme.palette.action.hover`.
     El clar no canvia.
   - Esforç **S**. Risc: només canvia el fosc amb `striped`, que ara és il·legible.

5. **Botó de crear responsiu.**
   - DIS `ToolbarButton` (`StyledMuiGrid.tsx:41-71`) amaga el text per sota de `md` i el deixa com a tooltip.
   - Amb R16 (`'button'`), en pantalla estreta el text ocupa la barra de títol.
   - **Com encaixa:** `toolbarCreateVariant: 'icon' | 'button' | 'responsive'` (o
     `toolbarCreateIconOnlyBelow?: Breakpoint`). LOT2 podria passar a `'responsive'`.
   - Esforç **S**. Risc baix.

6. **(Opcional, no visual) Aplicació automàtica més robusta del filtre natiu.**
   - DIS `FilterAutoApply.tsx` + `FilterAutoApplyContext.ts`:
     - no reaplica si les dades normalitzades no han canviat (`normalizeFilterData`);
     - espera que una data sigui vàlida;
     - amb Tab aplica el que hi ha pendent.
   - El doc (4.1) ja ho assenyala com a diferència a valorar.
   - **Com encaixa:** a `lib/components/form/Filter.tsx`, opció `autoApplySkipUnchanged`/`autoApplyWaitValidDates`.
   - Esforç **M–L**. Risc mitjà (toca el flux del filtre). Recomanació: no ara; deixar-ho anotat.

7. **(Opcional, no visual) Clau de persistència del filtre.**
   - DIS `StyledMuiFilter.tsx:54-58` inclou `resourceName` i `code`.
   - LIB `Filter.tsx:138` fa servir `persistentStateKey ?? resourceName`: dos filtres del mateix recurs comparteixen
     l'estat. Comanda té pantalles amb dos `MuiFilter`, però el risc només hi és si el `resourceName` coincideix.
   - Canviar la clau per defecte faria perdre l'estat desat. Es pot fer opt-in, o documentar que cal `persistentStateKey`.
   - Esforç **S**.
   - De passada, el comentari de `Filter.tsx:25` diu «LocalStorage per defecte», però el codi fa servir `session`.

8. **(No visual, revisar) `FormDialog`: l'efecte que reinicia els botons** (`lib/components/mui/form/FormDialog.tsx:111-113`).
   - ConCSV el té comentat des de la primera còpia (`12fd981`).
   - Si el component pare torna a pintar amb un `dialogButtons` en línia (array nou), l'efecte substitueix els botons
     que s'havien passat a `show(id, { dialogButtons })` (línia 178).
   - **Correcció:** recordar si el `show` actual en va passar i, si és així, no sobreescriure'ls.
   - Esforç **S**. Risc baix. Cal un test.

9. **(No visual, específic de servidor) Logout per navegació completa** (`ContainerAuthProvider`, DIS/CSV `lib/...:55,140-148`).
   - L'argument és genèric: un `fetch` a una cadena de redireccions de Keycloak d'un altre origen no tanca la sessió SSO.
   - Notib té un canvi equivalent (doc 3.3).
   - **Com encaixa:** opció `signOutMode: 'fetch' | 'navigate'` i `signOutPath`.
   - Fora de l'abast visual. Esforç **M**.

### (b) Per a `src/lot2/` (comú a totes les apps)

1. **El formulari emergent no es tanca en clicar fora.** Mitiga la limitació 9 del doc: sense avís de canvis
   pendents, un clic fora perd les dades.
   - **Què fa DIS** (`StyledMuiGrid.tsx:377-403`):
     - `popupEditFormDialogOnClose: (reason) => reason !== 'backdropClick'`;
     - `popupEditFormDialogComponentProps: { fullWidth: true, maxWidth: 'md' }`;
     - `popupEditFormComponentProps: { avoidSubmitIfAnyValidatorErrors: true }`.
   - **On va:** a `lot2DefaultMuiComponentProps(theme).dataGrid` (`lot2ComponentProps.ts:22-24`).
     - El primer és clarament comú.
     - El segon (mida) convé, perquè Comanda ha passat formularis a modal (2.8 del doc).
     - El tercer canvia el comportament del desat (no envia si hi ha errors de validació al client): es pot deixar a l'app.
     - Atenció a la combinació a un sol nivell: si una pantalla passa el seu propi `popupEditFormDialogComponentProps`,
       substitueix el de LOT2.
   - Esforç **S**. Risc baix.

2. **`lang` de `<html>` sincronitzat amb l'idioma** (WCAG 3.1.1).
   - ConCSV (`src/i18n/i18n.ts:16-21`) i Comanda (`src/i18n/i18n.ts:26`) ho han fet cadascuna pel seu compte;
     Distribucio no (`index.html:2` sense `lang`).
   - **On va:** `syncDocumentLanguage(i18n, supported?)` a `lot2/i18n.ts`, o un efecte dins `Lot2BaseApp` (un fill que
     llegeixi `currentLanguage` de `useBaseAppContext`).
   - Esforç **S**. Risc nul.

3. **Icones comunes d'acció, estat i selecció.**
   - DIS (`src/util/icons.ts:49-127`) i CSV (`:23-55`) declaren seccions «comunes amb les altres aplicacions»:
     `crea`, `modifica`, `esborra`, `desa`, `cancela`, `executa`, `refresca`, `descarrega`, `detall`, `plega`,
     `desplega`, `seleccionaTot`... Les dues apps hi coincideixen exactament; Notib no en té.
   - **On va:** afegir-les a `icons` de `lot2/icons.ts`. No canvia res visual; evita que cada app les repeteixi.
   - Abans cal resoldre les diferències de 4.3.
   - Esforç **S**.

4. **La barra de títol passa a dues files quan no hi cap.**
   - DIS ho fa mesurant (`ScreenFrame.tsx:57-96`). A LOT2 el títol és dins el `MuiToolbar` de la llibreria (flex sense
     `wrap`): en pantalla estreta el `ScreenTitle` s'escurça amb el·lipsi i les eines poden desbordar (és un punt
     pendent de la verificació visual del doc 9, «barres de títol sense desbordar»).
   - **On va:** a `titleBarSx` (`titleBarControlsSx.ts:96-104`), `flexWrap: 'wrap'`, `rowGap` i `py` petit. No calen
     observadors.
   - Esforç **S**. Risc baix–mitjà: cal mirar que el separador `flexGrow` de `Toolbar.tsx:54` no deixi una fila buida.

5. **`StyledBadge` amb el color de farciment del tema.**
   - LOT2 `StyledBadge.tsx:12-13` pinta `primary` amb `palette.primary.main` i text blanc, mentre que el tema
     (`theme.ts:140-149`) pinta els `MuiBadge` primaris amb `accentFillColor`.
   - DIS (`StyledBadge.tsx:16`) fa servir `accentColor`, que en fosc és el `light` i amb text blanc no té prou
     contrast (p. ex. `#cb6c88` amb blanc ≈ 3.4:1).
   - **Proposta:** `badgecolor === 'primary'` → `accentFillColor(theme)` i text `getContrastText`.
   - Esforç **S**. Risc baix (canvia el to del badge primari al clar: `main` → `dark`).

6. **`FilterCount` amb un recompte ajustable.**
   - DIS `FilterCountChip.tsx:229-233` (`filterCount: number | (n) => n`) resta els termes que la pantalla afegeix
     sempre al Spring Filter i que l'usuari no ha posat.
   - **On va:** prop opcional `count?: number | ((terms: number) => number)` a `FilterCount` (`FilterCount.tsx:9-12`).
     Encaixa amb `sumSpringFilterTerms` de Comanda (proposta 8 del doc).
   - Esforç **S**.

7. **`countSpringFilterTerms` amb l'escapament de cometes.**
   - DIS/CSV `maskLiterals` (`FilterCountChip.tsx:11-27`, commit `0bd010e` de ConCSV) tracta `\'` dins un literal.
   - Ara mateix LIB no escapa les cometes al `springFilterBuilder`, i per tant només afecta els builders propis de
     les apps.
   - Esforç **S**. Risc nul. Cal un test a `springFilterCount.test.ts`.

8. **Filtre ràpid de 32px a la barra de títol** (DIS `StyledMuiGrid.tsx:308-312`). El `TextField` small fa uns 37–40px
   i queda més alt que els botons (32px).
   - **On va:** a `titleBarControlsSx` (`'& .MuiOutlinedInput-root': { height: 32 }`, input `py: 0`).
   - Esforç **S**. Risc baix (cal verificar-ho visualment).

9. **Botó desactivat a la barra de títol.**
   - DIS (`titleBarControlsSx.ts:44-51`) fixa al desactivat `backgroundColor: paper` i `borderColor`.
   - LOT2 ho té comentat (`titleBarControlsSx.ts:53-55`): un botó `contained` desactivat (p. ex. el de crear amb
     `'button'` si ho fos) pot sortir amb el gris de MUI damunt del degradat.
   - Cal comprovar visualment quina és la bona. Esforç **S**.

10. **Alçada mínima de la graella principal** (DIS `StyledMuiGrid.tsx:300-307`: `flex: '1 1 0'`, `minHeight: 320px`).
    En finestres baixes la graella no es comprimeix fins a desaparèixer: fa scroll el contingut del marc.
    - A LOT2, `lot2ScreenGridProps` no posa cap mínim.
    - **On va:** `sx: { minHeight: SCREEN_GRID_MIN_HEIGHT }` a `lot2ScreenGridProps` + la constant a `layout.ts`,
      **si** la verificació visual mostra el problema.
    - Esforç **S**. Risc baix.

11. **Estil del logo de la capçalera Drassana.**
    - DIS `App.tsx:293-322` i CSV `App.tsx:110-140` repeteixen el mateix `logoStyle`: amplada
      `MENU_WIDTH - LOGO_BOX_LEFT`, separador a la vora del menú obert, `LOGO_SEPARATOR_GAP`, logo de l'app a `headerTitle`.
    - LOT2 ja té les constants (`layout.ts:18-25,98`), però no l'estil. Comanda fa servir un estil antic sense alinear
      (`src/App.tsx:280-288`).
    - **On va:** `lot2HeaderLogoStyle(theme)` a LOT2 (o valor per defecte de `headerLogoStyle` a `Lot2BaseApp`,
      combinat amb el de l'app).
    - Esforç **S**. Risc baix.

12. **Accessibilitat del peu** (no ve de les apps; s'ha vist en comparar-lo).
    - `DrassanaFooter.tsx:137` té `alt="foot_logo"` i `cursor: pointer` sense enllaç.
    - El `span` de la data i la revisió (`:120`) té el color del fons, de manera que és text invisible però llegible
      pels lectors de pantalla. Probablement l'OAW el marcarà per contrast.
    - **Proposta:** `alt="Drassana"` (o una prop `logoAlts`) i treure el `cursor`. El `span` caldria decidir si es mostra,
      s'amaga amb `aria-hidden` o s'elimina.
    - Esforç **S**.

13. **Títol de la pestanya del navegador** (WCAG 2.4.2). DIS `components/TitolPagina.tsx` el pren del `handle` de la
    ruta (`react-router`).
    - LOT2 no pot dependre del router, però `ScreenTitle` (o `ScreenFrame` amb `title`) podria fixar
      `document.title = \`${prefix} - ${title}\`` quan el títol és text, amb el prefix passat per `Lot2BaseApp`
      (`documentTitle?: string`).
    - Esforç **M**. Risc baix: cal no trepitjar les apps que ja el gestionen; ha de ser opt-in.

14. **Retocs del tema (doc 4.2) que convé adoptar a `createLot2Themes`.** Ordenats per valor:
    - **Botons `outlined primary` al tema fosc.** LOT2 només els corregeix a Dràcula (`theme.ts:313-328`). Al fosc el
      text és `main` sobre `#121212`, que dona 3.3:1 per a Comanda i 2.2:1 per a `#004B99` de Ripea: **no arriba a
      4.5:1**. DIS posa text i vora blancs (`theme.ts:545-561`).
      Proposta: la mateixa variant que Dràcula (text `text.primary`, vora `LIGHT`). S. Risc baix.
    - **Focus dels camps al fosc i a Dràcula.** DIS `theme.ts:489-501,611-623`: vora `LIGHT` de 2px al focus. El focus
      per defecte de MUI fa servir `primary.main`, que amb colors foscos queda per sota de 3:1 (WCAG 1.4.11). També hi
      ha la vora del camp desactivat. S.
    - **Casella marcada d'una fila seleccionada al fosc i a Dràcula.** DIS `theme.ts:532-535,654-657`: color `SOFT`
      (gairebé blanc). Amb el fons de selecció de LOT2 (`darken(MAIN, 0.55)`), la casella `primary.main` gairebé
      no es veu. S.
    - **`textTransform: 'none'` global a `MuiButton` i `MuiTab`** (DIS `theme.ts:131-136,271-276`). LOT2 ja el posa
      localment als botons de la barra de títol i dels filtres. Sense el global, el botó de crear d'una subtaula o
      d'un diàleg surt en MAJÚSCULES i el de la graella principal no: incoherent amb R16. S. Risc: canvia l'aspecte
      de tots els botons (era la intenció del doc 4.2).
    - **Autofill de Chrome** (DIS `theme.ts:121-125`), a `MuiCssBaseline`. S. Risc nul.
    - **`MuiDialogActions` amb `gap: 8px`** (DIS `theme.ts:156-167`), sense el `marginLeft: 10px` global (vegeu 4.5). S.
    - **Títol de diàleg amb fons `primary.main`** i icona de tancar blanca (DIS `theme.ts:300-310,411-431,502-521`).
      És una decisió de disseny, no una millora objectiva. LOT2 ja garanteix `main` amb blanc ≥ 4.5:1. El selector
      `& > .MuiIconButton-root` depèn del DOM de `lib/components/mui/Dialog.tsx`. Decisió de l'equip. M.

### (c) Propi de cada app (no s'inclou)

- `FilterAutoApply` + `useFilterAutoApply` a `GridFormField`: el doc ja decideix el comportament natiu.
- `StyledMuiGrid`:
  - mida de pàgina del perfil i `OPCIONS_PAGINACIO`;
  - `getRowHeight: 'auto'` i padding de cel·la;
  - files parelles per `getRowClassName`;
  - accions massives, `onRefresh`, botó de crear propi.
  Comanda ho fa per `defaultMuiComponentProps.dataGrid`.
- `StyledMuiFilter`: `FILTER_BUTTONS_COLUMNS` amb `md: 0.5`, sessió i auto-cerca inicial, `onReady`.
- `UserLastConection` / la darrera connexió de Comanda: l'origen de la dada és de cada app. Com que el `MenuItem` és
  idèntic a les tres, un `LastConnectionMenuItem({ date })` presentacional a LOT2 seria possible (S), però té poc valor.
- `AvisosBanner` (avisos per SSE), `TitolPagina` amb `handle` de ruta, `TemaProvider`, `getThemeForTema`,
  `TemaAplicacio`/`MenuEstil` (preferències de l'usuari).
- `ContainerAuthProvider` amb `/usuari/logout?origen=react`.
- Colors d'identitat (DIS `#439798`; CSV `#904e95`, `#653768`, `#b57dba`, `#F3ECF4`).
- Ajustos de tipografia `h4`/`h5`, `MuiCardHeader`, `MuiDialogContent` `paddingTop: 20px !important`.
- `MuiDataGrid` base (DIS `theme.ts:328-369`): fila de 45px, cel·les a dalt, casella de capçalera a escala 0.8. Van
  lligats a les files d'alçada `auto` de l'app.

---

## 4. Contradiccions amb les nostres decisions i errors que no s'han de copiar

1. **Estil de menú `footer` (PEU): l'accent.**
   - DIS (`BaseApp.tsx:58-67`) fa servir `accent: '#FFFFFF'` i `selectedBackground: rgba(255,255,255,0.12)`.
   - LOT2 (`menuColors.ts:26-36`) fa servir `accent: primary.main` i `alpha(main, 0.25)`.
   - Contrast amb el gris `#5F5D5D`:

     | Color | Contrast |
     |-------|----------|
     | Comanda `#A8456A` | 1.16:1 |
     | ConCSV `#904e95` | 1.16:1 |
     | Ripea `#004B99` | 1.3:1 |
     | Distribucio `#439798` | 1.9:1 |
     | Notib `#83cb41` | 3.3:1 |
     | Blanc | 6.5:1 |

   - L'accent de LIB s'aplica a la icona seleccionada i també al **text del títol del panell compacte**
     (`lib/components/mui/Menu.tsx:519`). Amb el valor de LOT2 queda il·legible a totes les apps excepte Notib.
   - **Millor la de DIS:** accent blanc (o `getContrastText`/el que doni ≥ 4.5:1) i fons de selecció blanc translúcid.
     Canvi S a `menuColors.ts`, prioritari.

2. **Estil de menú `inverse`.**
   - DIS fa servir paletes fixes Tailwind (slate `#1E293B`, accent blau `#60A5FA` / `#1976D2`): perd el color de l'app.
   - LOT2 fa servir la paleta MUI del mode contrari amb `primary.main`. **Millor la de LOT2** perquè manté la identitat.
   - Té el mateix defecte que l'anterior quan el menú surt fosc (tema clar): `main` sobre `#121212` dona 2.2:1 per a
     Ripea i 3.3:1 per a Comanda.
   - **Proposta:** quan la paleta invertida és fosca, accent = `colors.light` (o `lighten(main, 0.3)`), com fan els
     temes foscos de LOT2. S.

3. **Icones i textos del filtre.**
   - `avis`: DIS/CSV `campaign` (megàfon, avisos de l'administrador) contra LOT2/Notib `notifications`.
   - `cercaAvancadaObre/Tanca`: DIS/CSV `zoom_in`/`zoom_out` contra LOT2 `expand_more`/`expand_less`.
   - Text del commutador: DIS «Avançat»/«Bàsic» contra LOT2 «Avançat»/«Simple».
   - Recomanació:
     - **`campaign`** per a `avis` (distingeix els avisos dels administradors de les notificacions o alarmes, i Comanda
       també té alarmes);
     - mantenir **chevron** per al filtre avançat: el botó de LOT2 ja té text i `aria-expanded`, i el chevron
       comunica plegar/desplegar;
     - el text es pot unificar a «Bàsic».
   - Decisió de l'equip; afecta 3.b.3.

4. **Dràcula i paletes.**
   - DIS: `divider` `#44475a`, `greyBackground` `#303341`; clar `customBackground` `#f5f5f5`, `secondary` `#2E2E2E`;
     fosc `greyBackground` `#222222`.
   - LOT2: `#7a7d8b` (≈ 3:1 sobre el paper `#303341`, separa millor) i `greyBackground = main`.
   - **Millor LOT2** per al divisor (contrast). La resta és subjectiu.

5. **Errors o codi mort de DIS/CSV que NO s'han de copiar:**
   - `MuiButton` `'&:not(.MuiButtonGroup-grouped)': { marginLeft: '10px' }` (DIS `theme.ts:135`):
     - desplaça també el primer botó de qualsevol contenidor (cel·les de `Grid`, barres);
     - obliga a neutralitzar-lo a `MuiDialogActions` (`theme.ts:162-164`).
   - `'&.Mui-disabled': { cursor: 'not-allowed' }` (DIS `theme.ts:137-140`): MUI posa `pointer-events: none` al botó
     desactivat i el cursor no es veu mai (codi mort). L'`opacity: 0.6` duplica la pèrdua de color de MUI.
   - Comentari de `MuiDialogActions` (DIS `theme.ts:144-155`): explica una inversió `row-reverse` que està comentada.
     El comentari és enganyós.
   - Hover de graella al tema clar (DIS `theme.ts:439-441`): `'& .MuiDataGrid-row:hover'` amb `!important` i sense
     `:not(.Mui-selected)`. Té la mateixa especificitat que la regla de seleccionada amb hover, de manera que el
     resultat depèn de l'ordre. LOT2 ho fa bé al fosc (`:not(.Mui-selected)`).
   - ConCSV `StyledMuiGrid.tsx:305-308`: amb `autoHeight` posa `height: '100%'`. Distribucio ho va corregir (comprimia
     la graella dins les pestanyes i en retallava les files). Si mai es porta alguna cosa d'aquest component, cal fer-ho
     des de DIS.
   - Selectors d'estructura del menú (`& > div > .MuiList-root > ...`, `first-of-type`, `& > div > .MuiBox-root`
     top/left) i el `<li>` de ConCSV per CSS: ho substitueixen R01–R03 i 3.a.1.
   - `StyledBadge` de DIS amb `accentColor` com a fons (contrast baix al fosc): vegeu 3.b.5.
   - `DrassanaFooter` de DIS: `id="versioData"` fix (repetible) i el `span` invisible.

6. **Arquitectura de la barra de títol: un problema nostre que DIS no té.**
   - DIS pinta el títol (`<h1>`) fora de la barra d'eines de la llibreria. LOT2 el passa com a `title` del `MuiDataGrid`.
   - Quan l'API dona error, `DataToolbar` (`lib/components/mui/datacommon/DataToolbar.tsx:129-130`) **substitueix el
     `title`** pel text `datacommon.toolbar.error` (un `<h6>`): la pantalla perd l'`<h1>` i la icona mentre es mostra
     l'error.
   - A més, `Toolbar` posa `backgroundColor: red[100]` en error, però el `background:` (degradat) de `titleBarSx`
     (`titleBarControlsSx.ts:102`) va després a l'array `sx` i el tapa: **l'error ja no es distingeix pel color**.
   - Proposta, a LIB (`DataToolbar`/`Toolbar`, M): quan el `title` és un element, mantenir-lo i mostrar l'error al
     `subtitle`, i aplicar el color d'error després del `toolbarSx` (o amb una vora o icona que el degradat no tapi).
     Des de LOT2 no es pot arreglar: `lot2ScreenGridProps` no coneix l'estat d'error.
   - Cal tenir-ho present a la verificació visual.

7. **`FilterAutoApply` contra el filtre natiu.** Es manté la decisió del doc (natiu). Les avantatges de DIS són reals
   (no hi ha consultes repetides i no s'apliquen dates a mig escriure). Vegeu 3.a.6.

8. **Cerca avançada com a camp del formulari (`advanced`) contra estat de l'app.**
   - DIS (`StyledMuiFilter.tsx:221-235`, `FilterAutoApplyContext.ts:145-165`) desa l'estat obert o tancat amb les
     dades del filtre: sobreviu a la persistència i no compta com a filtre.
   - LOT2 (`FilterAdvancedButton` amb `open`/`onClick`) és més simple i no embruta les dades.
   - Cap de les dues és clarament millor. Si es vol persistència, una variant `FilterAdvancedButton name="advanced"`
     enllaçada al formulari (M). No és prioritari.

---

## 5. Ja cobert per la nostra branca (comprovat)

- `containerType: 'inline-size'` a `.MuiList-root` en lloc del paper (DIS `theme.ts:206`, CSV `66c5c1b`): LOT2
  `theme.ts:113` des de `7f51df8`.
- Capçalera de 74px, menú plegat de 74px, icones de 24px, posició del panell flotant, text amagat en plegat (DIS
  `theme.ts:191-233`): R01–R03 (`LOT2_APPEARANCE`) a LIB. El panell compacte ja surt amb la mida i els colors de
  `appearance`.
- Colors del menú per CSS (DIS `BaseApp.tsx:175-205`): R12 `menuColors` (fons, text, divisor, hover, seleccionat,
  icona de l'accent).
- Guia vertical dels submenús «en blau fix» (DIS `theme.ts:288`, `BaseApp.tsx:200`) i vora doblada als submenús
  (CSV/DIS `frameSx`): R04 `menuSelectedColor` i R11 `'background'` (sense guia).
- La vora de 4px amb l'accent del joc de colors del menú (DIS `BaseApp.tsx:196`): **no** és coberta. La proposta 8 del
  doc (camp de `MenuColors` per a la marca) ja hi és, i 4.1 la fa prioritària.
- Marc de contingut `> div > div > main` (DIS `BaseApp.tsx:136-147`): R06.
- Peu per flux dins un `div` de 36px (DIS `App.tsx:336-343`): `Lot2BaseApp`.
- `APPBAR_PADDING_LEFT` i `minHeight` de la capçalera (DIS `App.tsx:331-335`): `Lot2BaseApp` + R01.
- `layout.ts` (totes les constants llevat de `SCREEN_GRID_MIN_HEIGHT`), `accentColor`, `accentFillColor`,
  `gridColumnHeadersOverride`, `scrollbarOverrides`, `hatchPattern`, `MuiBadge`/`MuiChip`/`MuiPaginationItem`/
  `MuiTreeItem`/`MuiTab` (seleccionada al fosc): idèntics.
- `titleBarControlsSx` / `outlinedControlBorderSx`: LOT2 n'és un superconjunt (`IconButton`, `textTransform`, vora sòlida).
- `DrassanaFooter`: LOT2 n'és un superconjunt.
- Recompte de filtres com a text al costat del títol (portal a `infoSlot`): `FilterCount` + `ScreenTitle`.
- Literals ignorats al recompte: `countSpringFilterTerms` (excepte l'escapament, 3.b.7).
- Botó de crear amb text: R16. Botó de netejar només amb icona i nom accessible: `FilterClearButton`.
- `lib/`:
  - `Detail` `apiRef`/`refresh`/`useDetailApiRef` i `apiGetOne(id, { perspectives })`;
  - `FormDialog` sincronitzant `dialogButtons`.
  Ja són a la nostra LIB (venen de `v2`; vegeu 3.a.8 per a l'efecte).
- `GridFormField`, `MassiveActionSelector`, `MenuButton`: DIS no hi ha fet canvis rellevants des de l'anàlisi del doc
  (5); `fd2d96921` només els toca 2–13 línies (estils i icones). Es mantenen pendents com diu el doc.

---

## 6. Prioritats recomanades

| # | Element | Cat. | Esforç | Motiu |
|---|---------|------|--------|-------|
| 1 | Accent del menú `footer` (blanc) i `inverse` fosc (`light`) | b | S | Contrast 1.16:1 a Comanda i ConCSV (icona i títol del panell compacte) |
| 2 | Botó `outlined primary`, focus dels camps i casella seleccionada als temes foscos | b | S | WCAG 1.4.3 / 1.4.11 amb colors base foscos |
| 3 | `<li>` al menú (opt-in a `appearance`) | a | M | WCAG 1.3.1, OAW |
| 4 | Títol buit / nivell d'encapçalament de les barres d'eines | a | S + M | OAW (h6 buit, salt h1→h6) |
| 5 | Pèrdua de l'`<h1>` i del fons vermell en error a la graella principal | a | M | Error nostre detectat en comparar arquitectures |
| 6 | Formulari emergent sense tancar amb clic fora + mida `md` | b | S | Pèrdua de dades sense avís (doc 9) |
| 7 | `lang` de `<html>` | b | S | WCAG 3.1.1; dues apps ja ho fan pel seu compte |
| 8 | `textTransform: 'none'` global, autofill, `DialogActions` amb `gap` | b | S | Coherència (botó de crear en majúscules fora de la barra de títol) |
| 9 | Barra de títol en dues files, filtre ràpid de 32px, `minHeight` de la graella | b | S | Pantalla estreta o baixa; cal verificar-ho visualment |
| 10 | Icones comunes (+ decidir `avis`/filtre avançat), `StyledBadge`, `FilterCount.count`, estil del logo | b | S | Unificació entre apps |
