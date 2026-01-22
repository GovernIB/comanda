import { BaseEntity, IBaseEntity } from './base-entity.model.ts';
import Icon from '@mui/material/Icon';
import { JSX, useCallback, useMemo } from 'react';
import { IAppContext } from './app.model.tsx';
import { useTranslation } from 'react-i18next';
import { useTheme } from '@mui/material/styles';
import { darken } from '@mui/material';

/**
 * @deprecated i18next recomana usar l'API de selectors per a les traduccions
 */
export const ENUM_APP_ESTAT_PREFIX: string = 'enum.appEstat.';
/**
 * @deprecated i18next recomana usar l'API de selectors per a les traduccions
 */
export const ENUM_BD_ESTAT_PREFIX: string = 'enum.bdEstat.';
/**
 * @deprecated i18next recomana usar l'API de selectors per a les traduccions
 */
export const ENUM_INTEGRACIO_ESTAT_PREFIX: string = 'enum.integracioEstat.';
/**
 * @deprecated i18next recomana usar l'API de selectors per a les traduccions
 */
export const TITLE = ".title";
/**
 * @deprecated i18next recomana usar l'API de selectors per a les traduccions
 */
export const TOOLTIP = ".tooltip";

export const useSalutEstatTranslation = () => {
    const { t } = useTranslation();
    const tTitle = (estat: SalutEstatEnum) => {
        switch (estat) {
            case SalutEstatEnum.UP:
                return t($ => $.enum.appEstat.UP.title);
            case SalutEstatEnum.WARN:
                return t($ => $.enum.appEstat.WARN.title);
            case SalutEstatEnum.DEGRADED:
                return t($ => $.enum.appEstat.DEGRADED.title);
            case SalutEstatEnum.DOWN:
                return t($ => $.enum.appEstat.DOWN.title);
            case SalutEstatEnum.MAINTENANCE:
                return t($ => $.enum.appEstat.MAINTENANCE.title);
            case SalutEstatEnum.UNKNOWN:
                return t($ => $.enum.appEstat.UNKNOWN.title);
            case SalutEstatEnum.ERROR:
                return t($ => $.enum.appEstat.ERROR.title);
        }
    };
    const tTooltip = (estat: SalutEstatEnum) => {
        switch (estat) {
            case SalutEstatEnum.UP:
                return t($ => $.enum.appEstat.UP.tooltip);
            case SalutEstatEnum.WARN:
                return t($ => $.enum.appEstat.WARN.tooltip);
            case SalutEstatEnum.DEGRADED:
                return t($ => $.enum.appEstat.DEGRADED.tooltip);
            case SalutEstatEnum.DOWN:
                return t($ => $.enum.appEstat.DOWN.tooltip);
            case SalutEstatEnum.MAINTENANCE:
                return t($ => $.enum.appEstat.MAINTENANCE.tooltip);
            case SalutEstatEnum.UNKNOWN:
                return t($ => $.enum.appEstat.UNKNOWN.tooltip);
            case SalutEstatEnum.ERROR:
                return t($ => $.enum.appEstat.ERROR.tooltip);
        }
    }
    return {
        tTitle,
        tTooltip,
    };
}

export enum SalutEstatEnum {
    UP='UP',
    WARN='WARN',
    DEGRADED='DEGRADED',
    DOWN='DOWN',
    MAINTENANCE='MAINTENANCE',
    UNKNOWN='UNKNOWN',
    ERROR='ERROR'
}

export enum NivellEnum {
    INFO='INFO',
    WARN='WARN',
    ERROR='ERROR'
}

export interface ISalut extends IBaseEntity {
    entornAppId : number;
    data        : string;
    versio      : string;
    appEstat    : SalutEstatEnum;
    appLatencia?: number;
    bdEstat     : SalutEstatEnum;
    bdLatencia? : number;

    appUp?  : boolean;
    bdUp?   : boolean;

    integracioUpCount?          : number;
    integracioDownCount?        : number;
    integracioDesconegutCount?  : number;
    subsistemaUpCount?          : number;
    subsistemaDownCount?        : number;
    subsistemaDesconegutCount?  : number;
    missatgeErrorCount?         : number;
    missatgeWarnCount?          : number;
    missatgeInfoCount?          : number;

    year?               : string;
    yearMonth?          : string;
    yearMonthDay?       : string;
    yearMonthDayHour?   : string;

    integracions?   : ISalutIntegracio[];
    subsistemes?    : ISalutSubsistema[];
    contexts?       : IAppContext[];
    missatges?      : ISalutMissatge[];
    detalls?        : ISalutDetall[];
}

export class SalutModel extends BaseEntity implements Partial<ISalut> {

    // Claves estáticas para poder usarlas como keyof
    static readonly APP_ESTAT: keyof SalutModel = "appEstat";
    static readonly BD_ESTAT: keyof SalutModel = "bdEstat";
    static readonly INTEGRACIONS: keyof SalutModel = "integracions";
    static readonly SUBSISTEMES: keyof SalutModel = "subsistemes";
    static readonly INTEGRACIO_UP_COUNT: keyof SalutModel = "integracioUpCount";
    static readonly INTEGRACIO_WARN_COUNT: keyof SalutModel = "integracioWarnCount";
    static readonly INTEGRACIO_DOWN_COUNT: keyof SalutModel = "integracioDownCount";
    static readonly INTEGRACIO_DESCONEGUT_COUNT: keyof SalutModel = "integracioDesconegutCount";
    static readonly SUBSISTEMA_UP_COUNT: keyof SalutModel = "subsistemaUpCount";
    static readonly SUBSISTEMA_WARN_COUNT: keyof SalutModel = "subsistemaWarnCount";
    static readonly SUBSISTEMA_DOWN_COUNT: keyof SalutModel = "subsistemaDownCount";
    static readonly SUBSISTEMA_DESCONEGUT_COUNT: keyof SalutModel = "subsistemaDesconegutCount";
    static readonly MISSATGE_ERROR_COUNT: keyof SalutModel = "missatgeErrorCount";
    static readonly MISSATGE_WARN_COUNT: keyof SalutModel = "missatgeWarnCount";
    static readonly MISSATGE_INFO_COUNT: keyof SalutModel = "missatgeInfoCount";

    entornAppId: number;
    data: string;
    versio: string;
    appEstat: SalutEstatEnum;
    appLatencia?: number;
    bdEstat: SalutEstatEnum;
    bdLatencia?: number;
    peticioError?: boolean;

    appUp?: boolean;
    bdUp?: boolean;

    integracioUpCount?: number;
    integracioWarnCount?: number;
    integracioDownCount?: number;
    integracioDesconegutCount?: number;
    subsistemaUpCount?: number;
    subsistemaWarnCount?: number;
    subsistemaDownCount?: number;
    subsistemaDesconegutCount?: number;
    missatgeErrorCount?: number;
    missatgeWarnCount?: number;
    missatgeInfoCount?: number;

    year?: string;
    yearMonth?: string;
    yearMonthDay?: string;
    yearMonthDayHour?: string;

    integracions?: SalutIntegracioModel[];
    subsistemes?: SalutSubsistema[];
    contexts?: IAppContext[];
    missatges?: SalutMissatge[];
    detalls?: SalutDetall[];

    /**
     * Constructor
     */
    constructor(salut: ISalut) {
        super(salut);
        this.entornAppId = salut.entornAppId;
        this.data = salut.data;
        this.versio = salut.versio;
        this.appEstat = salut.appEstat;
        this.bdEstat = salut.bdEstat;
        Object.assign(this, salut);
    }
}

export interface ISalutIntegracio extends IBaseEntity {

    codi: string;
    estat: SalutEstatEnum;
    latencia?: number;
    totalOk: number;
    totalError: number;
    totalTempsMig?: number;
    peticionsOkUltimPeriode?: number;
    peticionsErrorUltimPeriode?: number;
    tempsMigUltimPeriode?: number;
    endpoint?: string;

    salut?: ISalut;
    pare?: ISalutIntegracio;

    nom?: string;
    logo?: string | null;
}

export class SalutIntegracioModel extends BaseEntity implements Partial<ISalutIntegracio> {

    codi: string;
    estat: SalutEstatEnum;
    latencia?: number;
    totalOk: number;
    totalError: number;
    totalTempsMig?: number;
    peticionsOkUltimPeriode?: number;
    peticionsErrorUltimPeriode?: number;
    tempsMigUltimPeriode?: number;
    endpoint?: string;

    salut?: SalutModel;
    pare?: SalutIntegracioModel;

    nom?: string;
    logo?: string | null;

    /**
     * Constructor
     */
    constructor(salutIntegracio: ISalutIntegracio) {
        super(salutIntegracio);
        this.codi = salutIntegracio.codi;
        this.estat = salutIntegracio.estat;
        this.latencia = salutIntegracio.latencia;
        this.totalOk = salutIntegracio.totalOk;
        this.totalError = salutIntegracio.totalError;
        this.totalTempsMig = salutIntegracio.totalTempsMig;
        this.peticionsOkUltimPeriode = salutIntegracio.peticionsOkUltimPeriode;
        this.peticionsErrorUltimPeriode = salutIntegracio.peticionsErrorUltimPeriode;
        this.tempsMigUltimPeriode = salutIntegracio.tempsMigUltimPeriode;
        this.endpoint = salutIntegracio.endpoint;
        Object.assign(this, salutIntegracio);
    }
}

export interface ISalutSubsistema extends IBaseEntity {

    codi: string;
    estat: SalutEstatEnum;
    latencia?: number;
    totalOk: number;
    totalError: number;
    totalTempsMig?: number;
    peticionsOkUltimPeriode?: number;
    peticionsErrorUltimPeriode?: number;
    tempsMigUltimPeriode?: number;

    salut?: SalutModel;

    nom?: string;
}

export class SalutSubsistema extends BaseEntity implements Partial<ISalutSubsistema>{

    codi: string;
    estat: SalutEstatEnum;
    latencia?: number;
    totalOk: number;
    totalError: number;
    totalTempsMig?: number;
    peticionsOkUltimPeriode?: number;
    peticionsErrorUltimPeriode?: number;
    tempsMigUltimPeriode?: number;

    salut?: SalutModel;

    nom?: string;

    /**
     * Constructor
     */
    constructor(salutSubsistema: ISalutSubsistema) {
        super(salutSubsistema);
        this.codi = salutSubsistema.codi;
        this.estat = salutSubsistema.estat;
        this.latencia = salutSubsistema.latencia;
        this.totalOk = salutSubsistema.totalOk;
        this.totalError = salutSubsistema.totalError;
        this.totalTempsMig = salutSubsistema.totalTempsMig;
        this.peticionsOkUltimPeriode = salutSubsistema.peticionsOkUltimPeriode;
        this.peticionsErrorUltimPeriode = salutSubsistema.peticionsErrorUltimPeriode;
        this.tempsMigUltimPeriode = salutSubsistema.tempsMigUltimPeriode;
        Object.assign(this, salutSubsistema);
    }
}

export interface ISalutMissatge extends IBaseEntity {

    data: string;
    nivell: NivellEnum;
    missatge: string;

    salut?: ISalut;
}

export class SalutMissatge extends BaseEntity implements Partial<ISalutMissatge> {

    data: string;
    nivell: NivellEnum;
    missatge: string;

    salut?: SalutModel;

    /**
     * Constructor
     */
    constructor(salutMissatge: ISalutMissatge) {
        super(salutMissatge);
        this.data = salutMissatge.data;
        this.nivell = salutMissatge.nivell;
        this.missatge = salutMissatge.missatge;
        Object.assign(this, salutMissatge);
    }
}

export interface ISalutDetall extends IBaseEntity {

    codi: string;
    nom: string
    valor: string;

    salut?: ISalut;
}

export class SalutDetall extends BaseEntity implements Partial<ISalutDetall> {

    codi: string;
    nom: string
    valor: string;

    salut?: SalutModel;

    constructor(salutDetall: ISalutDetall) {
        super(salutDetall);
        this.codi = salutDetall.codi;
        this.nom = salutDetall.nom;
        this.valor = salutDetall.valor;
        Object.assign(this, salutDetall);
    }
}

export class SalutInformeEstatItemModel extends BaseEntity {

    // Claves estáticas para poder usarlas como keyof
    static readonly ALWAYS_UP: keyof SalutInformeEstatItemModel = "alwaysUp";
    static readonly ALWAYS_DOWN: keyof SalutInformeEstatItemModel = "alwaysDown";

    static readonly UP_PERCENT: keyof SalutInformeEstatItemModel = "upPercent";
    static readonly WARN_PERCENT: keyof SalutInformeEstatItemModel = "warnPercent";
    static readonly DEGRADED_PERCENT: keyof SalutInformeEstatItemModel = "degradedPercent";
    static readonly DOWN_PERCENT: keyof SalutInformeEstatItemModel = "downPercent";
    static readonly ERROR_PERCENT: keyof SalutInformeEstatItemModel = "errorPercent";
    static readonly MAINTENANCE_PERCENT: keyof SalutInformeEstatItemModel = "maintenancePercent";
    static readonly UNKNOWN_PERCENT: keyof SalutInformeEstatItemModel = "unknownPercent";

    alwaysUp: boolean;
    alwaysDown: boolean;

    upPercent: number;
    warnPercent: number;
    degradedPercent: number;
    downPercent: number;
    errorPercent: number;
    maintenancePercent: number;
    unknownPercent: number;

    constructor(salut: any) {
        super(salut);
        this.alwaysUp = salut?.alwaysUp;
        this.alwaysDown = salut?.alwaysDown;
        this.upPercent = salut?.upPercent;
        this.warnPercent = salut?.warnPercent;
        this.degradedPercent = salut?.degradedPercent;
        this.downPercent = salut?.downPercent;
        this.errorPercent = salut?.errorPercent;
        this.maintenancePercent = salut?.maintenancePercent;
        this.unknownPercent = salut?.unknownPercent;
    }
}

const LIGHT_MODE_COLORS = {
    GREEN: "#72bd75",  // Verde suave, transmite "ok"
    YELLOW: "#efe271", // Amarillo claro, alerta ligera
    ORANGE: "#FFCC80", // Naranja suave, indica problema moderado
    BLUE: "#90CAF9", // Azul claro, estado informativo
    GRAY: "#9c9c9c", // Gris claro, estado desconocido
    RED_LIGHT: "#EF9A9A", // Rojo claro, grave
    RED_DARK: "#e36161", // Rojo intenso, crítico
}

const DARK_MODE_COLORS = {
    GREEN: darken(LIGHT_MODE_COLORS.GREEN, 0.1),
    YELLOW: darken(LIGHT_MODE_COLORS.YELLOW, 0.1),
    ORANGE: darken(LIGHT_MODE_COLORS.ORANGE, 0.1),
    BLUE: darken(LIGHT_MODE_COLORS.BLUE, 0.1),
    GRAY: darken(LIGHT_MODE_COLORS.GRAY, 0.1),
    RED_LIGHT: darken(LIGHT_MODE_COLORS.RED_LIGHT, 0.1),
    RED_DARK: darken(LIGHT_MODE_COLORS.RED_DARK, 0.1),
}

const useCurrentColorScheme = () => {
    const theme = useTheme();
    return useMemo(
        () => (theme.palette.mode === 'light' ? LIGHT_MODE_COLORS : DARK_MODE_COLORS),
        [theme.palette.mode]
    );
}

/**
 * Devuelve el color predeterminado en función del estado
 * @param salutEstatEnum
 */
export const useGetColorByStatEnum = () => {
    const currentColorScheme = useCurrentColorScheme();
    return useCallback(function (salutEstatEnum: SalutEstatEnum): string {
        switch (salutEstatEnum) {
            case SalutEstatEnum.UP:
                return currentColorScheme.GREEN;
            case SalutEstatEnum.WARN:
                return currentColorScheme.YELLOW;
            case SalutEstatEnum.DEGRADED:
                return currentColorScheme.ORANGE;
            case SalutEstatEnum.MAINTENANCE:
                return currentColorScheme.BLUE;
            case SalutEstatEnum.UNKNOWN:
                return currentColorScheme.GRAY;
            case SalutEstatEnum.DOWN:
                return currentColorScheme.RED_DARK;
            case SalutEstatEnum.ERROR:
            default:
                return currentColorScheme.RED_LIGHT;
        }
    }, [currentColorScheme]);
};

export const useGetColorByNivellEnum = () => {
    const currentColorScheme = useCurrentColorScheme();
    return useCallback(function(nivellEnum: NivellEnum): string {
        switch (nivellEnum) {
            case NivellEnum.INFO:
                return currentColorScheme.BLUE;
            case NivellEnum.WARN:
                return currentColorScheme.YELLOW;
            case NivellEnum.ERROR:
            default:
                return currentColorScheme.RED_DARK;
        }
    }, [currentColorScheme])
}

export const useGetColorByIntegracio = () => {
    const currentColorScheme = useCurrentColorScheme();
    return useCallback(
        function (integracioField: keyof SalutModel): string {
            if (SalutModel.INTEGRACIO_UP_COUNT === integracioField) {
                return currentColorScheme.GREEN;
            } else if (SalutModel.INTEGRACIO_WARN_COUNT === integracioField) {
                return currentColorScheme.ORANGE;
            } else if (SalutModel.INTEGRACIO_DOWN_COUNT === integracioField) {
                return currentColorScheme.RED_DARK;
            } else if (SalutModel.INTEGRACIO_DESCONEGUT_COUNT === integracioField) {
                return currentColorScheme.GRAY;
            } else {
                return currentColorScheme.RED_DARK;
            }
        },
        [currentColorScheme]
    );
};

export const useGetColorBySubsistema = () => {
    const currentColorScheme = useCurrentColorScheme();
    return useCallback(
        function (subsistemaField: keyof SalutModel): string {
            if (SalutModel.SUBSISTEMA_UP_COUNT === subsistemaField) {
                return currentColorScheme.GREEN;
            } else if (SalutModel.SUBSISTEMA_WARN_COUNT === subsistemaField) {
                return currentColorScheme.ORANGE;
            } else if (SalutModel.SUBSISTEMA_DOWN_COUNT === subsistemaField) {
                return currentColorScheme.RED_DARK;
            } else if (SalutModel.SUBSISTEMA_DESCONEGUT_COUNT === subsistemaField) {
                return currentColorScheme.GRAY;
            } else {
                return currentColorScheme.RED_DARK;
            }
        },
        [currentColorScheme]
    );
};

export const useGetColorByMissatge = () => {
    const currentColorScheme = useCurrentColorScheme();
    return useCallback(
        function (missatgeField: keyof SalutModel): string {
            if (SalutModel.MISSATGE_ERROR_COUNT === missatgeField) {
                return currentColorScheme.RED_DARK;
            } else if (SalutModel.MISSATGE_WARN_COUNT === missatgeField) {
                return currentColorScheme.ORANGE;
            } else if (SalutModel.MISSATGE_INFO_COUNT === missatgeField) {
                return currentColorScheme.BLUE;
            } else {
                return currentColorScheme.RED_DARK;
            }
        },
        [currentColorScheme]
    );
};

/**
 * Devuelve un icono de Material UI para un estado dado
 * @param state El estado de tipo SalutEstatEnum
 */
export function getMaterialIconByState(state: SalutEstatEnum): JSX.Element {
    switch (state) {
        case SalutEstatEnum.UP:
            return <Icon color={"inherit"}>check_circle</Icon>;
        case SalutEstatEnum.WARN:
            return <Icon color={"inherit"}>warning_amber</Icon>;
        case SalutEstatEnum.DEGRADED:
            return <Icon color={"inherit"}>trending_down</Icon>;
        case SalutEstatEnum.MAINTENANCE:
            return <Icon color={"inherit"}>build_circle</Icon>;
        case SalutEstatEnum.UNKNOWN:
            return <Icon color={"inherit"}>help_outline</Icon>;
        case SalutEstatEnum.DOWN:
            return <Icon color={"inherit"}>highlight_off</Icon>;
        case SalutEstatEnum.ERROR:
        default:
            return <Icon color={"inherit"}>error</Icon>;
    }
}
