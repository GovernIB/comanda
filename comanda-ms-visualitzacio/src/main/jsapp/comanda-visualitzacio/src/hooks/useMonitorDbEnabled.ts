import React from 'react';
import { useResourceApiService } from 'reactlib';

const MONITOR_DB_ACTIU_CODE = 'es.caib.comanda.monitor.db.actiu';

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

const useMonitorDbEnabled = () => {
    const { isReady, find } = useResourceApiService('parametre');
    const [monitorDbEnabled, setMonitorDbEnabled] = React.useState<boolean>();

    React.useEffect(() => {
        if (!isReady) {
            return;
        }

        void find({
            page: 0,
            size: 1,
            filter: `codi:'${MONITOR_DB_ACTIU_CODE}'`,
        }).then(response => {
            const param = response.rows?.[0];
            setMonitorDbEnabled(resolveBooleanParamValue(param));
        }).catch(() => {
            setMonitorDbEnabled(false);
        });
    }, [find, isReady]);

    return monitorDbEnabled;
};

export default useMonitorDbEnabled;
