import { icons } from '../lot2/icons';

/**
 * Icones (Material Icons) de les pantalles de comanda, per concepte. Són les mateixes que les
 * entrades de menú de `App.tsx`: menú, títol de pantalla i botons del mateix concepte comparteixen icona.
 */
export const screenIcons = {
    salut: 'monitor_heart',
    estadistiques: 'bar_chart',
    tasca: 'assignment',
    avis: icons.avis,
    monitor: 'monitor',
    monitoritzacio: 'monitor',
    monitorDb: 'dns',
    cache: 'storage',
    broker: 'send_time_extension',
    tasquesBackground: 'schedule',
    alarma: 'notifications',
    dashboard: 'dashboard_customize',
    apps: 'apps',
    entorn: 'layers',
    integracio: 'integration_instructions',
    dimensio: 'category',
    indicador: 'insights',
    plantilla: 'palette',
    paleta: 'format_color_fill',
    calendari: 'calendar_month',
    configuracio: 'settings',
    parametre: 'settings',
    entitat: 'all_inbox',
    widget: 'widgets',
    historic: 'history',
    versions: 'layers',
    sitemap: 'account_tree',
    accessibilitat: 'info',
} as const;
