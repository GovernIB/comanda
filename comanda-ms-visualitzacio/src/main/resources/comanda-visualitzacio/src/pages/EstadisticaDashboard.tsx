import * as React from 'react';
import MuiToolbar from '@mui/material/Toolbar';
import { BasePage, useBaseAppContext, useResourceApiService } from 'reactlib';
import { useParams } from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import { AppEstadisticaTest, GridLayoutItem } from './DashboardTest.tsx';
import { isEqual } from 'lodash';

const EstadisticaDashboard: React.FC = () => {
    const { id } = useParams();
    const {
        isReady: apiDashboardIsReady,
        getOne: getOneDashboard,
        artifactReport,
    } = useResourceApiService('dashboard');
    const { isReady: apiDashboardItemIsReady, patch: patchDashboardItem } =
        useResourceApiService('dashboardItem');
    const { temporalMessageShow } = useBaseAppContext();
    const [dashboard, setDashboard] = useState<any>();
    const [dashboardWidgets, setDashboardWidgets] = useState<any>();
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        (async () => {
            if (apiDashboardIsReady && apiDashboardItemIsReady) {
                setLoading(true);
                setDashboard(await getOneDashboard(id));
                setDashboardWidgets(await artifactReport(id, { code: 'widgets_data' }));
                setLoading(false);
            }
        })();
    }, [id, apiDashboardIsReady, apiDashboardItemIsReady]);

    const mappedDashboardItems = useMemo(
        () =>
            dashboardWidgets?.map((widget: any) => ({
                id: String(widget.dashboardItemId),
                x: widget.posX,
                y: widget.posY,
                w: widget.width,
                h: widget.height,
                type: widget.tipus,
            })),
        [dashboardWidgets]
    );

    const onGridLayoutItemsChange = (newLayoutItems: GridLayoutItem[]) => {
        const promises: Promise<any>[] = [];
        mappedDashboardItems.forEach((oldDashboardItem: GridLayoutItem) => {
            const newDashboardItem = newLayoutItems.find(
                (newLayoutItem: GridLayoutItem) => newLayoutItem.id === oldDashboardItem.id
            );

            if (newDashboardItem === undefined) {
                console.error(
                    `Failed to find newDashboardItem with id ${oldDashboardItem.id}, update won't propagate.`
                );
            } else if (!isEqual(oldDashboardItem, newDashboardItem)) {
                const patchPromise = patchDashboardItem(oldDashboardItem.id, {
                    data: {
                        posX: newDashboardItem.x,
                        posY: newDashboardItem.y,
                        width: newDashboardItem.w,
                        height: newDashboardItem.h,
                    },
                });
                promises.push(patchPromise);
            }
        });

        // TODO Traducir mensajes
        Promise.all(promises)
            .then(() => {
                temporalMessageShow(null, 'Guardat correctament', 'success');
            })
            .catch((reason) => {
                temporalMessageShow(null, 'Error al guardar', 'error');
                console.error('Save error', reason);
            });
    };

    if (loading || !dashboard) return 'Loading'; // TODO

    return (
        <BasePage
            toolbar={
                <MuiToolbar
                    disableGutters
                    sx={{
                        width: '100%',
                        display: 'flex',
                        // px: 2,
                        ml: 0,
                        mr: 0,
                        mt: 0,
                        backgroundColor: (theme) => theme.palette.grey[200],
                        // ...sxProp
                    }}
                >
                    {dashboard.titol}
                    {/*{joinReactElementsWithReactElementsWithPositions(toolbarElements, elementsWithPositions, true)}*/}
                </MuiToolbar>
            }
        >
            {loading || !dashboardWidgets ? (
                'Loading' // TODO
            ) : (
                <AppEstadisticaTest
                    dashboardWidgets={dashboardWidgets}
                    gridLayoutItems={mappedDashboardItems}
                    onGridLayoutItemsChange={onGridLayoutItemsChange}
                />
            )}
        </BasePage>
    );
};

export default EstadisticaDashboard;
