export interface ErrorInfo {
    date: string;
    message: string;
    trace?: string;
}

export interface PerData {
    entornAppId: number;
    dataInici: string;
    baixaPrioritat?: boolean;
}

export interface PerInterval {
    entornAppId: number;
    dataInici: string;
    dataFi: string;
    baixaPrioritat?: boolean;
    pausaMs?: number;
}

export type EstatProcesBaixaPrioritat = 'PENDENT' | 'EN_EXECUCIO' | 'FINALITZAT' | 'ERROR' | 'CANCELAT';

export interface ProcesBaixaPrioritat {
    id: string;
    entornAppId: number;
    entornAppNom?: string;
    dataInici: string;
    dataFi: string;
    pausaMs?: number;
    estat: EstatProcesBaixaPrioritat;
    dataIniciExecucio?: string;
    dataFiExecucio?: string;
    totalDies: number;
    diesProcessats: number;
    diesAmbDades: number;
    diesAmbErrors: number;
    percentatge: number;
    diaActual?: string;
    missatge?: string;
}

export interface Temps {
    data: string;
    anualitat: number;
    trimestre: number;
    mes: number;
    setmana: number;
    dia: number;
    diaSetmana: string;
}

export interface DadesDia {
    temps: Temps;
    dimensionsJson: Record<string, string>;
    indicadorsJson: Record<string, number>;
    entornAppId: number;
}

export interface CalendarStatusButtonProps {
  hasError: boolean;
  isLoading: boolean;
  esDisponible: boolean;
  isBackgroundProcessing?: boolean;
}
