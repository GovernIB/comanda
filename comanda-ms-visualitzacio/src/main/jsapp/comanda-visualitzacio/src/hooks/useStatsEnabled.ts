import React from 'react';
import { useResourceApiService } from 'reactlib';

const STATS_ENABLED_CODE = 'es.caib.comanda.stats.enabled';

const resolveBooleanParamValue = (param: any) => {
    if (typeof param?.valorBoolean === 'boolean') {
        return param.valorBoolean;
    }
    if (typeof param?.valor === 'boolean') {
        return param.valor;
    }
    if (typeof param?.valor === 'string') {
        return param.valor.trim().toLowerCase() === 'true';
    }
    return false;
};

const useStatsEnabled = () => {
    const { isReady, find } = useResourceApiService('parametre');
    const [statsEnabled, setStatsEnabled] = React.useState<boolean>();

    React.useEffect(() => {
        if (!isReady) {
            return;
        }

        void find({
            page: 0,
            size: 1,
            filter: `codi:'${STATS_ENABLED_CODE}'`,
        }).then(response => {
            const param = response.rows?.[0];
            setStatsEnabled(resolveBooleanParamValue(param));
        }).catch(() => {
            setStatsEnabled(false);
        });
    }, [find, isReady]);

    return statsEnabled;
};

export default useStatsEnabled;
