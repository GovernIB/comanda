/**
 * Catàleg d'icones (Material Icons) per **concepte**. L'entrada de menú, el títol de la pantalla i els
 * botons que representen el mateix concepte han de fer servir sempre la mateixa icona.
 *
 * Aquí només hi ha les claus comunes que fan servir els components de `lot2`. Les icones de les
 * pantalles pròpies de cada aplicació es declaren a l'aplicació, fora d'aquesta carpeta.
 */
export const icons = {
    // --- Pantalles comunes ---------------------------------------------------------------------
    inici: 'home',
    avis: 'notifications',
    mapaWeb: 'map',
    accessibilitat: 'accessibility_new',
    noTrobat: 'search_off',

    // --- Rols (insígnia del botó d'usuari de la capçalera) -------------------------------------
    rolSuper: 'shield',
    rolAdmin: 'admin_panel_settings',
    rolAdminLectura: 'visibility',
    rolOrgan: 'account_tree',
    rolAplicacio: 'apps',
    rolUsuari: 'person',

    // --- Filtre i cerca ------------------------------------------------------------------------
    filtre: 'filter_alt',
    neteja: 'filter_alt_off',
    cercaAvancadaObre: 'expand_more',
    cercaAvancadaTanca: 'expand_less',
} as const;

export type IconName = (typeof icons)[keyof typeof icons];

/**
 * Icona d'una pantalla: una de les claus comunes o qualsevol altre nom de Material Icons (les icones
 * de les pantalles pròpies es declaren a l'aplicació).
 */
export type ScreenIconName = IconName | (string & {});
