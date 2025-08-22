import {BaseEntity, IBaseEntity} from "./base-entity.model.ts";
import Icon from "@mui/material/Icon";
import {JSX} from "react";

export const ENUM_APP_ESTAT_PREFIX: string = 'enum.appEstat.';
export const ENUM_BD_ESTAT_PREFIX: string = 'enum.bdEstat.';
export const TITLE = ".title";
export const TOOLTIP = ".tooltip";

export interface ISalut extends IBaseEntity {
    entornAppId: number | undefined;
    data: string | undefined;
    versio: string | undefined;
    appEstat: SalutEstatEnum | undefined;
    appLatencia: number | undefined;
    bdEstat: SalutEstatEnum | undefined;
    bdLatencia: number | undefined;

    appUp: boolean | undefined;
    bdUp: boolean | undefined;

    integracioUpCount: number | undefined;
    integracioDownCount: number | undefined;
    integracioDesconegutCount: number | undefined;
    subsistemaUpCount: number | undefined;
    subsistemaDownCount: number | undefined;
    missatgeErrorCount: number | undefined;
    missatgeWarnCount: number | undefined;
    missatgeInfoCount: number | undefined;

    year: string | undefined;
    yearMonth: string | undefined;
    yearMonthDay: string | undefined;
    yearMonthDayHour: string | undefined;

    integracions: any[] | undefined;
    subsistemes: any[] | undefined;
    contexts: any[] | undefined;
    missatges: any[] | undefined;
    detalls: any[] | undefined;
}

export class SalutModel extends BaseEntity implements Required<ISalut> {

    // Claves estáticas para poder usarlas como keyof
    static readonly APP_ESTAT: keyof SalutModel = "appEstat";
    static readonly BD_ESTAT: keyof SalutModel = "bdEstat";
    static readonly INTEGRACIONS: keyof SalutModel = "integracions";
    static readonly SUBSISTEMES: keyof SalutModel = "subsistemes";
    static readonly INTEGRACIO_UP_COUNT: keyof SalutModel = "integracioUpCount";
    static readonly INTEGRACIO_DOWN_COUNT: keyof SalutModel = "integracioDownCount";
    static readonly INTEGRACIO_DESCONEGUT_COUNT: keyof SalutModel = "integracioDesconegutCount";
    static readonly SUBSISTEMA_UP_COUNT: keyof SalutModel = "subsistemaUpCount";
    static readonly SUBSISTEMA_DOWN_COUNT: keyof SalutModel = "subsistemaDownCount";
    static readonly MISSATGE_ERROR_COUNT: keyof SalutModel = "missatgeErrorCount";
    static readonly MISSATGE_WARN_COUNT: keyof SalutModel = "missatgeWarnCount";
    static readonly MISSATGE_INFO_COUNT: keyof SalutModel = "missatgeInfoCount";

    entornAppId: number | undefined;
    data: string | undefined;
    versio: string | undefined;
    appEstat: SalutEstatEnum | undefined;
    appLatencia: number | undefined;
    bdEstat: SalutEstatEnum | undefined;
    bdLatencia: number | undefined;

    appUp: boolean | undefined;
    bdUp: boolean | undefined;

    integracioUpCount: number | undefined;
    integracioDownCount: number | undefined;
    integracioDesconegutCount: number | undefined;
    subsistemaUpCount: number | undefined;
    subsistemaDownCount: number | undefined;
    missatgeErrorCount: number | undefined;
    missatgeWarnCount: number | undefined;
    missatgeInfoCount: number | undefined;

    year: string | undefined;
    yearMonth: string | undefined;
    yearMonthDay: string | undefined;
    yearMonthDayHour: string | undefined;

    integracions: any[] | undefined;
    subsistemes: any[] | undefined;
    contexts: any[] | undefined;
    missatges: any[] | undefined;
    detalls: any[] | undefined;

    /**
     * Constructor
     */
    constructor(salut: ISalut) {
        super(salut);
        Object.assign(this, salut);
    }
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

export const GREEN: string = "#72bd75"  // Verde suave, transmite "ok"
export const YELLOW: string = "#efe271" // Amarillo claro, alerta ligera
export const ORANGE: string = "#FFCC80" // Naranja suave, indica problema moderado
export const BLUE: string = "#90CAF9" // Azul claro, estado informativo
export const GRAY: string = "#9c9c9c" // Gris claro, estado desconocido
export const RED_LIGHT: string = "#EF9A9A" // Rojo claro, grave
export const RED_DARK: string = "#e36161" // Rojo intenso, crítico


/**
 * Devuelve el color predeterminado en función del estado
 * @param salutEstatEnum
 */
export function getColorByStatEnum(salutEstatEnum: SalutEstatEnum): string {
    switch (salutEstatEnum) {
        case SalutEstatEnum.UP:
            return GREEN;
        case SalutEstatEnum.WARN:
            return YELLOW;
        case SalutEstatEnum.DEGRADED:
            return ORANGE;
        case SalutEstatEnum.MAINTENANCE:
            return BLUE;
        case SalutEstatEnum.UNKNOWN:
            return GRAY;
        case SalutEstatEnum.DOWN:
            return RED_LIGHT;
        case SalutEstatEnum.ERROR:
        default:
            return RED_DARK;
    }
}

export function getColorByIntegracio(integracioField: keyof SalutModel): string {
    if (SalutModel.INTEGRACIO_UP_COUNT === integracioField) {
        return GREEN;
    } else if (SalutModel.INTEGRACIO_DOWN_COUNT === integracioField) {
        return RED_DARK;
    } else if (SalutModel.INTEGRACIO_DESCONEGUT_COUNT === integracioField) {
        return GRAY
    } else {
        return RED_DARK;
    }
}

export function getColorBySubsistema(subsistemaField: keyof SalutModel): string {
    if (SalutModel.SUBSISTEMA_UP_COUNT === subsistemaField) {
        return GREEN;
    } else if (SalutModel.SUBSISTEMA_DOWN_COUNT === subsistemaField) {
        return RED_DARK;
    } else {
        return RED_DARK;
    }
}

export function getColorByMissatge(missatgeField: keyof SalutModel): string {
    if (SalutModel.MISSATGE_ERROR_COUNT === missatgeField) {
        return RED_DARK;
    } else if (SalutModel.MISSATGE_WARN_COUNT === missatgeField) {
        return ORANGE;
    } else if (SalutModel.MISSATGE_INFO_COUNT === missatgeField) {
        return BLUE;
    } else {
        return RED_DARK;
    }
}

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
            return <Icon fontSize="inherit">highlight_off</Icon>;
        case SalutEstatEnum.ERROR:
        default:
            return <Icon color={"inherit"}>error</Icon>;
    }
}