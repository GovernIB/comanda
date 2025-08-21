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

/**
 * Devuelve el color predeterminado en función del estado
 * @param salutEstatEnum
 */
export function getColorByStatEnum(salutEstatEnum: SalutEstatEnum): string {
    switch (salutEstatEnum) {
        case SalutEstatEnum.UP:
            // Verde suave, transmite "ok"
            return "#72bd75"; // pastel green
        case SalutEstatEnum.WARN:
            // Amarillo claro, alerta ligera
            return "#efe271"; // pastel yellow
        case SalutEstatEnum.DEGRADED:
            // Naranja suave, indica problema moderado
            return "#FFCC80"; // pastel orange
        case SalutEstatEnum.MAINTENANCE:
            // Azul claro, estado informativo
            return "#90CAF9"; // pastel blue
        case SalutEstatEnum.UNKNOWN:
            // Gris claro, estado desconocido
            return "#9c9c9c"; // light grey
        case SalutEstatEnum.DOWN:
            // Rojo claro, crítico
            return "#EF9A9A"; // pastel red
        case SalutEstatEnum.ERROR:
        default:
            // Rojo más intenso que DOWN, para diferenciarlo
            return "#e36161"; // light red
    }
}

export function getColorByIntegracio(integracioField: keyof SalutModel): string {
    if (SalutModel.INTEGRACIO_UP_COUNT === integracioField) {
        // Verde suave, transmite "ok"
        return "#72bd75"; // pastel green
    } else if (SalutModel.INTEGRACIO_DOWN_COUNT === integracioField) {
        // Rojo más intenso
        return "#e36161"; // light red
    } else if (SalutModel.INTEGRACIO_DESCONEGUT_COUNT === integracioField) {
        // Gris claro, estado desconocido
        return "#9c9c9c"; // light grey
    } else {
        // Rojo más intenso
        return "#e36161"; // light red
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