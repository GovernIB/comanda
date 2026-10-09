import { useEffect, useState, useMemo, useCallback, ReactNode } from 'react';
import { useResourceApiService, springFilterBuilder } from 'reactlib';
import { ParameterContext } from './ParameterContext';

const PARAMETRE_CODI_MAX_RESULT_SELECTS = 'es.caib.comanda.monitor.general.max.result.selects';

const ParameterProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const { isReady, find } = useResourceApiService('parametre');
    const [maxResultSelects, setMaxResultSelects] = useState<number | null>(null);
    const fetchParameter = useCallback(async () => {
        if (!isReady) return;
        try {
            const response = await find({
                filter: springFilterBuilder.eq('codi', `'${PARAMETRE_CODI_MAX_RESULT_SELECTS}'`),
                unpaged: true,
                size: 1,
            });
            if (response.rows && response.rows.length > 0) {
                const param = response.rows[0];
                const parsedValue = param.valor !== null && param.valor !== undefined
                    ? Number(param.valor)
                    : null;
                setMaxResultSelects(parsedValue);
            } else {
                setMaxResultSelects(null);
            }
        } catch (error) {
            console.error('Error al cargar el parámetro maxResultSelects:', error);
            setMaxResultSelects(null);
        }
    }, [isReady, find]);
    useEffect(() => {
        fetchParameter();
    }, [fetchParameter]);
    const contextValue = useMemo(() => ({ maxResultSelects }), [maxResultSelects]);
    return (
        <ParameterContext.Provider value={contextValue}>
            {children}
        </ParameterContext.Provider>
    );
};

export default ParameterProvider;
