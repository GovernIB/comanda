import React from 'react';
import { useResourceApiService } from 'reactlib';
import { useIsUserAdmin, useUserContext } from '../components/UserContext';

export const useHasDashboardAccess = (): boolean | undefined => {
    const isUserAdmin = useIsUserAdmin();
    const { user } = useUserContext();
    const { isReady: entornAppApiIsReady, find: entornAppFind } = useResourceApiService('entornApp');
    const { isReady: dashboardApiIsReady, find: dashboardFind } = useResourceApiService('dashboard');
    const [hasDashboardAccess, setHasDashboardAccess] = React.useState<boolean>();

    React.useEffect(() => {
        if (isUserAdmin) {
            setHasDashboardAccess(true);
            return;
        }
        if (user == null || !entornAppApiIsReady || !dashboardApiIsReady) {
            return;
        }
        let isMounted = true;
        Promise.allSettled([
            entornAppFind({
                page: 0,
                size: 1,
                namedQueries: ['permis_disseny'],
                filter: 'activa:true and app.activa:true',
            }),
            dashboardFind({
                page: 0,
                size: 1,
                namedQueries: ['WRITE'],
            }),
        ]).then(([entornAppResult, dashboardResult]) => {
            if (!isMounted) return;
            const hasEntornAppAccess = entornAppResult.status === 'fulfilled' && (entornAppResult.value.rows?.length ?? 0) > 0;
            const hasDashboardWrite = dashboardResult.status === 'fulfilled' && (dashboardResult.value.rows?.length ?? 0) > 0;
            setHasDashboardAccess(hasEntornAppAccess || hasDashboardWrite);
        }).catch(() => {
            if (!isMounted) return;
            setHasDashboardAccess(false);
        });

        return () => {
            isMounted = false;
        };
    }, [dashboardApiIsReady, dashboardFind, entornAppApiIsReady, entornAppFind, isUserAdmin, user]);

    return hasDashboardAccess;
};

export default useHasDashboardAccess;
