export interface ErrorInfo {
    date: string;
    message: string;
    trace?: string;
}

export interface PerData {
    entornAppId: number;
    dataInici: string;
}

export interface PerInterval {
    entornAppId: number;
    dataInici: string;
    dataFi: string;
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
}
