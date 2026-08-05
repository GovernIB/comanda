/**
 * Tipus per als filtres de capçalera d'un Dashboard (backend: DashboardFiltre / DashboardFiltreSeleccio a
 * comanda-ms-estadistica). Es defineixen com a interfícies soltes (no com a BaseEntity) perquè Dashboard/DashboardItem
 * ja es consumeixen com `any` a tot arreu d'aquesta pàgina (veure dashboardRequests.ts) - no hi ha un model tipat de
 * Dashboard a estendre.
 */

export type DashboardFiltreTipus = 'DIMENSIO' | 'PERIODE';

export interface DashboardFiltre {
    id: number | string;
    tipus: DashboardFiltreTipus;
    /** Només per a tipus=DIMENSIO. */
    dimensioCodi?: string;
    /** Etiqueta a mostrar; si és buit s'usa el nom de la dimensió o "Període". */
    titol?: string;
    ordre: number;
    multiple?: boolean;
}

export type PeriodeMode = 'PRESET' | 'RELATIU' | 'ABSOLUT';
export type PresetPeriode =
    | 'DARRERS_7_DIES' | 'DARRERS_14_DIES' | 'DARRERS_30_DIES' | 'DARRERS_90_DIES'
    | 'DARRERS_180_DIES' | 'DARRERS_365_DIES'
    | 'DARRERES_4_SETMANES' | 'DARRERES_12_SETMANES' | 'DARRERES_52_SETMANES'
    | 'DARRERS_3_MESOS' | 'DARRERS_6_MESOS' | 'DARRERS_12_MESOS' | 'DARRERS_4_TRIMESTRES'
    | 'DARRER_1_ANY' | 'DARRERS_2_ANYS' | 'DARRERS_5_ANYS'
    | 'DARRER_COMPLET_DIA' | 'DARRERA_COMPLETA_SETMANA' | 'DARRER_COMPLET_MES'
    | 'DARRER_COMPLET_TRIMESTRE' | 'DARRER_COMPLET_ANY'
    | 'AVUI' | 'AHIR' | 'AQUESTA_SETMANA_FINS_ARA' | 'AQUEST_MES_FINS_ARA'
    | 'AQUEST_TRIMESTRE_FINS_ARA' | 'AQUEST_ANY_FINS_ARA'
    | 'DARRERS_N_DIES' | 'DARRERES_N_SETMANES' | 'DARRERS_N_MESOS' | 'DARRERS_N_TRIMESTRES' | 'DARRERS_N_ANYS';
export type PeriodeAbsolutTipus = 'DATE_RANGE' | 'SPECIFIC_PERIOD_OF_YEAR';

/**
 * Reflecteix es.caib.comanda.estadistica.logic.intf.model.periode.Periode. El header de filtres només construeix
 * les variants PRESET i ABSOLUT/DATE_RANGE (les úniques exposades a DashboardFiltreBar); la resta de camps existeixen
 * per compatibilitat de tipus amb el backend però no s'omplen des d'aquí.
 */
export interface Periode {
    periodeMode?: PeriodeMode;
    presetPeriode?: PresetPeriode;
    presetCount?: number;
    relatiuPuntReferencia?: string;
    relatiuCount?: number;
    relatiueUnitat?: string;
    relatiuAlineacio?: string;
    absolutTipus?: PeriodeAbsolutTipus;
    /** Format ISO (YYYY-MM-DD). */
    absolutDataInici?: string;
    /** Format ISO (YYYY-MM-DD). */
    absolutDataFi?: string;
    absolutAnyReferencia?: string;
    absolutAnyValor?: number;
    absolutPeriodeUnitat?: string;
    absolutPeriodeInici?: number;
    absolutPeriodeFi?: number;
}

/** Selecció feta per l'usuari a la capçalera; es transporta com a `filtreSeleccio` dins InformeWidgetParams. */
export interface DashboardFiltreSeleccio {
    dimensions?: Record<string, string[]>;
    periode?: Periode;
}

export const EMPTY_DASHBOARD_FILTRE_SELECCIO: DashboardFiltreSeleccio = {};
