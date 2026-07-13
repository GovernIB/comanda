import { useResourceApiService } from 'reactlib';
import { useEffect, useState } from 'react';

interface Plantilla {
    id: number;
    nom: string;
    mostrarVora?: boolean;
    ampleVora?: number;
    midaFontTitol?: string;
    midaFontDescripcio?: string;
    icona?: string;
    midaFontValor?: number;
    midaFontUnitats?: number;
    midaFontCanviPercentual?: number;
    mostrarReticula?: boolean;
    barStacked?: boolean;
    barHorizontal?: boolean;
    lineShowPoints?: boolean;
    area?: boolean;
    lineSmooth?: boolean;
    lineWidth?: number;
    outerRadius?: number;
    pieDonut?: boolean;
    innerRadius?: number;
    pieShowLabels?: boolean;
    labelSize?: number;
    gaugeMin?: number;
    gaugeMax?: number;
    gaugeRangs?: string;
    heatmapMinValue?: number;
    heatmapMaxValue?: number;
    mostrarCapcalera?: boolean;
    mostrarAlternancia?: boolean;
    mostrarVoraTaula?: boolean;
    ampleVoraTaula?: number;
    mostrarSeparadorHoritzontal?: boolean;
    ampleSeparadorHoritzontal?: number;
    mostrarSeparadorVertical?: boolean;
    ampleSeparadorVertical?: number;
    tipusGrafic?: string;
    colors?: Record<string, string>;
    paletes?: any[];
    paletteGroups?: any[];
    styleProperties?: any[];
}

export const useDashboardPlantilla = (plantillaId?: number) => {
    const { isReady, getOne: getPlantilla } = useResourceApiService('plantilla');
    const [plantilla, setPlantilla] = useState<Plantilla | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!plantillaId || !isReady) {
            setPlantilla(null);
            return;
        }
        setLoading(true);
        getPlantilla(plantillaId).then(value => {
                setPlantilla(value);
            }).catch((error) => {
                console.error('Error loading plantilla:', error);
                setPlantilla(null);
            })
            .finally(() => setLoading(false));;
    }, [plantillaId, isReady, getPlantilla]);

    return { plantilla, loading };
};

export const useEntornCodi = (entornId?: number | string) => {
    const { isReady, getOne: getEntorn } = useResourceApiService('entorn');
    const [entornCodi, setEntornCodi] = useState<string | undefined>(undefined);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!entornId || !isReady) {
            setEntornCodi(undefined);
            return;
        }

        setLoading(true);
        getEntorn(entornId)
            .then((value: any) => {
                setEntornCodi(value?.codi);
            })
            .catch((error) => {
                console.error('Error loading entorn:', error);
                setEntornCodi(undefined);
            })
            .finally(() => setLoading(false));
    }, [entornId, isReady, getEntorn]);

    return { entornCodi, loading };
};