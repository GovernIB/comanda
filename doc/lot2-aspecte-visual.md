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
- `src/lot2/`: còpia literal de la carpeta comuna de base-react (commit `cb7141e`). **No s'edita aquí**:
  qualsevol canvi es fa a base-react i es torna a copiar. `cb7141e` és la base del proper merge de `lib/` i
  de `src/lot2/`.
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

Helpers de comanda proposats per passar a `lot2` (vegeu l'apartat «Propostes per a lot2» del document):
`src/hooks/useScreenGridProps.tsx`, `src/hooks/useAppliedSpringFilter.ts`,
`src/components/FilterClearAllButton.tsx` i `src/util/springFilterSum.ts`.
