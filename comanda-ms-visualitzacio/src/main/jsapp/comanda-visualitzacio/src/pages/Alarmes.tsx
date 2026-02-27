import { Button, Icon } from '@mui/material';
import React, { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import {
    GridPage,
    MuiDataGrid,
    useMuiDataGridApiRef,
    useMuiActionReportLogic
} from 'reactlib';

const Alarmes = () => {
    const { t } = useTranslation();
    const [showOnlyActive, setShowOnlyActive] = React.useState<boolean>(true);
    const gridApiRef = useMuiDataGridApiRef();
    const {
        available: actionInitialized,
        formDialogComponent,
        exec,
    } = useMuiActionReportLogic(
        'alarma',
        'ALARMA_ESBORRAR',
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        null,
        undefined,
        () => gridApiRef.current.refresh());
    /*const toolbarElementsWithPositions = [{
        position: 3,
        element: <MuiActionReportButton
            resourceName="alarma"
            action="ALARMA_ESBORRAR_TOTES"
            onSuccess={() => gridApiRef.current.refresh()}
            buttonComponentProps={{ variant: 'contained', sx: { ml: 1 } }} />
    }];*/
    const toolbarElementsWithPositions = [{
        position: 2,
        element: <Button
            onClick={() => setShowOnlyActive(prev => !prev)}
            variant={showOnlyActive ? 'contained' : 'outlined'}
            title={showOnlyActive ?
                    t($ => $.page.alarma.filter.showOnlyActiveEnabled) :
                    t($ => $.page.alarma.filter.showOnlyActiveDisabled)
            }
            sx={{ mr: 2 }}
        >
            <Icon>{'check_circle'}</Icon>
        </Button>,
    }];
    const rowAdditionalActions = [{
        label: t($ => $.page.alarma.action.clear.label),
        action: 'ALARMA_ESBORRAR',
        icon: 'check',
        showInMenu: false,
        onClick: exec,
        hidden: (row:any) => !row?.id || row?.dataEsborrat,
    }];

    const dataGridColumns = useMemo(
        () => [
            {
                field: 'missatge',
                flex: 1,
            },
            {
                field: 'dataActivacio',
                flex: 0.5,
            },
            {
                field: 'dataFinalitzacio',
                flex: 0.5,
            },
            {
                field: 'estat',
                renderCell: (params: any) => {
                    if (params.row?.dataFinalitzacio != null) {
                        if (params.row?.estat === 'ESBORRADA')
                            return t($ => $.page.alarma.estats.finalitzadaEsborrada);
                        return t($ => $.page.alarma.estats.finalitzada);
                    }
                },
                flex: 0.5,
            },
        ],
        [t]
    );

    return (
        <GridPage>
            {actionInitialized && <>
                <MuiDataGrid
                    title={t($ => $.menu.alarmes)}
                    resourceName="alarma"
                    columns={dataGridColumns}
                    paginationActive
                    readOnly
                    apiRef={gridApiRef}
                    toolbarType="upper"
                    toolbarElementsWithPositions={toolbarElementsWithPositions}
                    filter={showOnlyActive ? "estat:'ACTIVA'" : "estat in('ACTIVA', 'ESBORRADA')"}
                    sortModel={[{ field: 'dataActivacio', sort: 'desc' }]}
                    rowAdditionalActions={rowAdditionalActions} />
                {formDialogComponent}
            </>}
        </GridPage>
    );
}

export default Alarmes;
