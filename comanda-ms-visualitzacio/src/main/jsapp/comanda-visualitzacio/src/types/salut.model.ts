import {BaseEntity, IBaseEntity} from "./base-entity.model.ts";

export const ENUM_APP_ESTAT_PREFIX: string = 'enum.appEstat.';

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
            // Verde suave, transmite "todo ok"
            return "#5ec562"; // pastel green
        case SalutEstatEnum.WARN:
            // Amarillo claro, alerta ligera
            return "#FFF59D"; // pastel yellow
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
            return "#e64646"; // light red
    }
}