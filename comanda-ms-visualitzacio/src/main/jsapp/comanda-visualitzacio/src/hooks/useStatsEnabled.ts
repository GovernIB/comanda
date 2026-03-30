import React from 'react';
import { useResourceApiService } from 'reactlib';

const STATS_ENABLED_CODE = 'es.caib.comanda.stats.enabled';

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
            const enabled = param?.valorBoolean ?? (param?.valor === 'true');
            setStatsEnabled(enabled ?? false);
        }).catch(() => {
            setStatsEnabled(false);
        });
    }, [find, isReady]);

    return statsEnabled;
};

export default useStatsEnabled;
