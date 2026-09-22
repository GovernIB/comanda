import React from 'react';
import { useResourceApiService } from 'reactlib';
import { useIsUserAdmin, useIsUserConsulta, useIsUserUsuari, useUserContext } from '../components/UserContext';

export const useHasSalutAccess = (): boolean | undefined => {
    const isUserAdmin = useIsUserAdmin();
    const isUserConsulta = useIsUserConsulta();
    const isUserUsuari = useIsUserUsuari();
    const { user } = useUserContext();
    const { isReady: entornAppApiIsReady, find: entornAppFind } = useResourceApiService('entornApp');
    const [hasSalutAccess, setHasSalutAccess] = React.useState<boolean>();

    React.useEffect(() => {
        if (!isUserUsuari && (isUserAdmin || isUserConsulta)) {
            setHasSalutAccess(true);
            return;
        }
        if (user == null || !entornAppApiIsReady) {
            return;
        }
        void entornAppFind({
            page: 0,
            size: 1,
            namedQueries: ['permis_salut'],
            filter: 'activa:true and app.activa:true',
        }).then(response => {
            setHasSalutAccess((response.rows?.length ?? 0) > 0);
        }).catch(() => {
            setHasSalutAccess(false);
        });
    }, [entornAppApiIsReady, entornAppFind, isUserAdmin, isUserConsulta, isUserUsuari, user]);

    return hasSalutAccess;
};

export default useHasSalutAccess;
