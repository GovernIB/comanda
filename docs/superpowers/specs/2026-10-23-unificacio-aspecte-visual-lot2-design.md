# Unificació de l'aspecte visual de les aplicacions del Lot2 — Disseny

Data: 2026-10-23 · Estat: pendent d'aprovació

## Intenció

Comanda, notib2, ripea, distribucio i altres aplicacions del Lot2 copien el codi de base-react a la seva
carpeta `lib/`. Volem que totes tinguin **el mateix menú, capçalera, graelles i filtres**, i que **només
canviïn els colors i el logo**. L'aspecte de referència és el que s'ha implementat a **Notib (branca
`notib2-dev`, issue #1087, commits `300aba201`, `37461543d`, `70534db96`, `42eda115b`)**, amb una
excepció: els botons de crear han de ser botons amb text, no només una icona.

Avui Notib i Distribucio aconsegueixen aquest aspecte amb CSS que depèn de l'estructura interna de
base-react, per exemple `'& > div > div > main'`, `:has(...)` sobre la barra d'eines o colors forçats
al menú. Distribucio fins i tot fa servir `MutationObserver` i `ResizeObserver`. Cada aplicació ho ha
resolt pel seu compte i les implementacions ja han divergit (el tema de Notib té 368 línies i el de
Distribucio, 684). L'objectiu és que la següent aplicació ho tingui fàcil:

1. copiar `lib/` i `lot2/`;
2. escriure un fitxer petit amb els seus colors, logos i menú;
3. embolcallar les pantalles amb els components comuns.

L'anàlisi del company (`Modificacions per unificar estils a les aplicacions del Lot2 - Resum informe.csv`)
s'ha pres com a punt de partida, sense ser vinculant. Es mantenen les propostes R01–R06, R09 i R10.
La R07 (observadors) se substitueix per les R13–R15, que són més simples, i s'hi afegeixen les R11, R12 i R16.

## Abast d'aquesta fase

- **Sí:**
  - base-react: una branca nova amb paràmetres visuals opcionals;
  - la carpeta comuna `lot2/`;
  - aplicar-ho tot a comanda amb el seu tema rosa fosc;
  - un document de migració per a les altres aplicacions.
- **No:**
  - migrar Notib, ripea, distribucio o qualsevol altra aplicació, que es farà en una fase posterior;
  - redissenyar el contingut de les pantalles de comanda (dashboards, estadístiques, calendari...).
    Només se'n canvia el marc, el tema, els títols i les barres d'eines.
- **Anti-objectius** (casos que es consideren un fracàs encara que funcionin):
  - Una aplicació que actualitzi `lib/` **sense** fer servir les opcions noves veu qualsevol canvi
    visual. El valor per defecte ha de reproduir l'aspecte actual píxel a píxel.
  - La carpeta `lot2/` acaba tenint diferències entre aplicacions, o importa codi de l'aplicació.
  - Calen selectors CSS que depenen de l'estructura DOM interna de base-react per aconseguir l'aspecte de Notib.

## Arquitectura: tres capes

```
app/
├── lib/            ← còpia de base-react (idèntica a totes les apps)
└── src/
    ├── lot2/       ← còpia de la carpeta comuna (idèntica a totes les apps)
    └── lot2App.ts (o similar) ← colors, logos, codi/títol, menú: l'únic que canvia
```

### 1. base-react: paràmetres visuals opcionals

- **Repositori i branca:** repositori `/home/sionandreu/git/base-react`, branca nova creada a partir de
  `v2` (`bea8c9e`, 2.0.89), amb nom proposat `v2-parametres-visuals`.
- **Valors per defecte:** cap valor per defecte no canvia. Totes les opcions són opcionals i, si no
  s'indiquen, l'aspecte és exactament l'actual.
- **On es configuren:** a nivell d'aplicació, a `MuiBaseApp` o a `defaultMuiComponentProps`, i, quan
  té sentit, també per component.

| Id | Paràmetre | Ara (codi fix) | Notib |
|----|-----------|----------------|-------|
| R01 | `headerHeight` (afecta l'AppBar, el `topMargin` del menú i el `top` sticky de MuiForm) | 64 (`Menu.tsx:342`, `MuiForm`) | 74 |
| R02 | `menuCollapsedWidth`, botó del menú centrat | 49/57 (`Menu.tsx:74-75`) | 74 |
| R03 | `menuIconSize` | 20 (`Menu.tsx:118`) | 24 |
| R04 | `menuSelectedColor` | `hsl(210,100%,60%)` (`Menu.tsx:176-180`) | color del tema |
| R11 | `menuSelectedIndicator` (línia lateral, fons, cap) | línia blava | segons Notib |
| R12 | `menuColors` (fons, text, text secundari, divisor, accent, fons de l'element seleccionat i del hover) | tema MUI | conjunts TEMA / TEMA_INVERTIT / PEU |
| R05 | estil i alçada de la capçalera exposats (fons, logo, separador) | parcial | segons Notib |
| R06 | `contentRootStyle` / `contentMainStyle` | marges 16/24 (fixos) | marges de Notib, sense el selector `> div > div > main` |
| R09 | exportar `ResourceApiError` i `FormFieldRefOptionsResponse` | no s'exporten | — |
| R10 | `sx` com a array a `MuiDataGrid` (i coherent amb la resta) | només objecte | — |
| R13 | `toolbarSx` / `toolbarClassName` / fons de la barra d'eines superior a `MuiDataGrid` i `MuiForm` | gris fix (`Toolbar.tsx:55-56`) | barra de títol |
| R14 | marges de la barra d'eines superior i del formulari | `m:2` (`MuiDataGrid.tsx:1100`), `MuiForm.tsx:104` | segons Notib |
| R15 | `title: ReactNode` a `MuiForm` (i `MuiDataGrid` si cal) | només text | ScreenTitle |
| R16 | `toolbarCreateVariant: 'icon' \| 'button'` i `toolbarCreateLabel` | sempre `IconButton` (`MuiDataCommon.tsx:398-408`) | `'button'` amb text |

Per a cada paràmetre nou s'escriu un test que comprova que el valor per defecte no ha canviat i que
el valor nou s'aplica. Ara mateix base-react no té cap infraestructura de tests. Decisió meva: s'hi
afegeix Vitest + jsdom + Testing Library **només com a `devDependencies`** i amb un script `test`, i
els tests es posen a `lib/` i a `lot2/` amb el sufix `.test.tsx`. Així viatgen amb la còpia i cada
aplicació també els executa. A comanda, `src/lot2/**` s'exclou dels llindars de cobertura, igual que `lib/**`.

Correccions locals de Notib que s'incorporen a base-react. Decisió meva:

- **Protecció contra el doble enviament (`isSaving`):** s'activa per defecte, perquè és una correcció
  d'error i no un canvi visual.
- **`extraActions` / `liveButtons` / `rowUpdateShowInMenu`:** són props noves i opcionals. Si no
  s'indiquen, el comportament és el mateix d'ara.
- **Redirecció de Notib quan es rep un 401:** **no** s'incorpora en aquesta fase. Comanda ja té la seva
  pròpia redirecció a login a `ResourceApiProvider` i es manté. Queda anotat al document de migració de Notib.

**Publicació (decisió de Sion):**
- La branca `v2-parametres-visuals` es puja al remot però **no es fusiona** amb `v2`. La fusió i la
  publicació les farà l'equip més endavant.
- No s'incrementa la versió del `package.json` en aquesta fase.
- Comanda copia `lib/` i `src/lot2/` directament del darrer commit pujat de la branca, i la memòria de
  sincronització registra aquest commit com a base per al següent merge a 3 bandes.

La branca es copia a `comanda/.../lib`
amb el mateix procediment de merge a 3 bandes de la fase anterior, conservant les personalitzacions
pròpies de comanda (vegeu la memòria `sync-base-boot-base-react`).

### 2. Carpeta comuna `lot2/`

- **On viu la còpia mestra:** al repositori de base-react, a la mateixa branca, a `src/lot2/`.
- **Com es copia:** cada aplicació la copia sencera a `src/lot2/`, igual que fa amb `lib/`.
- **Versió:** la de `lot2/` va sempre aparellada amb la de `lib/`.
- **Dependències:** `lot2/` només importa de `lib/` i de llibreries npm, mai de codi de l'aplicació.
- **Ubicació i importacions:** cada aplicació té la carpeta a `src/lot2/`. Importa de `lib/` a través
  de l'àlies `reactlib`, que ja fan servir les aplicacions. A base-react s'afegeix el mateix àlies
  `reactlib` → `./lib/main` (a `tsconfig` i a Vite), perquè el codi sigui idèntic a tot arreu. A
  base-react, la còpia mestra viu a `src/lot2/`. Decisió meva.
  Tot el que és propi de cada aplicació hi entra per props o per context.

Contingut, partint de les versions de Notib i ja net de selectors interns:

- `layout.ts`: `HEADER_HEIGHT` 74, `MENU_WIDTH_COLLAPSED` 74, `ICON_SIZE` 24, etc.
- `ScreenFrame` + `ScreenFrameContext` + `ScreenTitle`, `titleBarControlsSx`, `formScreenTitle`.
- `FilterButtons`, `FilterCount` + `springFilterCount`. El filtre s'aplica automàticament fent servir
  el comportament natiu de `MuiFilter` sense `buttonControlled`.
- `DrassanaFooter` (fons `#5F5D5D`), `StyledBadge`.
- `createLot2Themes({ main, dark, light?, soft })`, que retorna els temes `light`, `dark` i `dracula`
  amb tipografia, `accentColor`, overrides de capçaleres de graella i estils base de components.
- `Lot2BaseApp`: un embolcall de `MuiBaseApp` que fixa tots els paràmetres R01–R16 amb els valors de
  Notib, i que admet `menuAppearance` (`theme` / `inverse` / `footer`), logos clar i fosc, el peu i
  punts d'extensió per a la capçalera, el menú d'usuari i els camps de formulari.
- Claus d'icones comunes i textos comuns (i18n amb el seu propi espai de noms).
- Abans de compartir `GridFormField`, `MassiveActionSelector` i `MenuButton` cal comparar-los amb les
  versions de Distribucio. S'inclouen només si poden ser genèrics, i si no es deixen anotats al
  document de migració.
- `README.md` que explica les regles de la carpeta: no es modifica a l'aplicació, es copia sencera i
  qualsevol canvi es fa a la còpia mestra.

### 3. Configuració pròpia de cada aplicació

Cada aplicació només aporta les dades següents:

- els 4 colors;
- els logos (clar i fosc) i el logo GOIB;
- el codi i el títol;
- les entrades del menú i les seves icones;
- les extensions pròpies de la capçalera.

## Comanda

- **Tema:**
  - Es fa servir rosa fosc, a partir del color del logo de comanda-drassana (`#cb6c88`,
    `src/assets/COM_DRA_COL.svg`). Proposta inicial, decisió meva:
    - `light` = `#cb6c88`;
    - `main` = una versió fosca que aconsegueixi un contrast AA (≥ 4.5:1) amb text blanc,
      aproximadament `#A8456A`;
    - `dark` ≈ `#7A2E4C`;
    - `soft` ≈ `#FAEFF3`.
  - Els valors finals es comproven visualment i amb el càlcul de contrast.
  - Es mantenen els temes que l'usuari pot triar (CLAR / OBSCUR / DRACULA / SISTEMA), generats amb `createLot2Themes`.
- **Coses de comanda que s'han de conservar**, passades per les extensions de `Lot2BaseApp`:
  - entrades de menú a la capçalera;
  - hora del sistema, alarmes i selector d'idioma;
  - al menú d'usuari, la darrera connexió, el perfil i el selector de rol;
  - la insígnia d'administrador;
  - les entrades CAIB;
  - el `fixedContentExpandsToAvailableHeightEnabled`;
  - el `AppFormFieldReference`.
- **Peu:** `ComandaFooter` se substitueix per `DrassanaFooter`, perquè l'objectiu és que només canviïn
  els colors i el logo. Decisió meva.
- **Pantalles:**
  - Les pàgines amb graella o formulari passen a `ScreenFrame` + `ScreenTitle`, amb els botons de
    crear amb text i el recompte de filtres, com a Notib.
  - **Formularis (decisió de Sion):**
    - Els **formularis simples** passen a ser **modals** que s'obren des de la graella, com a Notib
      (commits `37461543d` i `70534db96`). S'eliminen les seves rutes `form` i les seves pàgines.
    - Els **formularis complexos** (amb pestanyes, subtaules, etc.) es mantenen com a pàgines, amb
      `ScreenFrame` i la barra de títol sticky (R01, R13–R15).
    - Qui ho implementi farà la classificació de cada formulari i la deixarà escrita al pla, per
      revisar-la abans de començar.
  - Les pàgines amb disseny propi (dashboards, estadístiques, calendari) només reben el `ScreenFrame` i el títol.
- **Tests:** s'actualitzen els tests de Vitest afectats (`BaseApp.test.tsx`, `ComandaFooter.test.tsx`,
  pàgines...). Tots els tests, el `typecheck` i el `lint` han de passar.

## Document de migració (lliurable)

Es crea a `docs/lot2/migracio-aspecte-visual.md` al repositori de base-react, al costat de `lot2/`,
i se'n posa un enllaç des de comanda. Conté:

1. **Pas comú per a totes les aplicacions:**
   - actualitzar `lib/` (amb merge a 3 bandes si hi ha personalitzacions);
   - copiar `lot2/`;
   - crear el fitxer de configuració;
   - canviar `BaseApp` per `Lot2BaseApp`;
   - embolcallar les pantalles amb `ScreenFrame`.
2. **Una secció per aplicació** que indica què s'esborra, què es canvia i què es manté:
   - **Notib:** substituir `theme.ts`, ScreenFrame, el CSS del menú i de la capçalera i els selectors
     `:has` per `lot2/`; treure la dependència d'`AuthButton` amb codi de l'aplicació; resoldre les 27
     diferències de `lib/`; posar els botons de crear amb text.
   - **Distribucio:** el mateix, i a més eliminar `StyledMuiGrid` (els observadors) i `FilterAutoApply`,
     i decidir què es fa amb les diferències del tema de 684 línies.
   - **Ripea:** adopció des de zero, partint del tema antic `#004B99`.
   - **Comanda:** com a exemple ja fet.
3. Una taula amb els paràmetres de base-react, el valor per defecte i el valor que fa servir `lot2`.

## Què es deixa al criteri de qui ho implementi

- Els noms exactes de les props, sempre que siguin coherents amb l'estil de base-react.
- L'ordre dels commits, que han de ser per funcionalitat i no tot de cop.
- Com es fan els tests visuals de no-regressió dels valors per defecte.
- El nom del fitxer de configuració de cada aplicació.

## Restriccions

- MUI 7, React 19, Vite i Vitest, com ara.
- Els commits es fan a comanda en una branca nova a partir de `comanda-dev-libs-update` (decisió meva), sempre amb
  rutes explícites, per no arrossegar els canvis locals de l'usuari (`.editorconfig`, `comanda-ear/*`,
  `docker-compose.yml`, etc.).
- No es fa cap canvi en altres aplicacions.
