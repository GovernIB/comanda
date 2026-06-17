import { Box, Button, Icon } from '@mui/material';
import React, { useMemo, useCallback } from 'react';
import { Trans, useTranslation } from 'react-i18next';
import {
    MuiDataGrid,
    useMuiDataGridContext,
    useMuiDataGridApiRef,
    useMuiActionReportLogic,
    springFilterBuilder,
    MuiActionReportButton,
} from 'reactlib';
import { DEFAULT_ROW_SELECTION } from '../../lib/components/mui/datagrid/DataGridContext';
import { useGridApiRef } from '@mui/x-data-grid-pro';

const AlarmaEsborrarMultipleActionButton = ({ onSuccess }: { onSuccess?: () => void }) => {
    const { selection, apiRef } = useMuiDataGridContext();
    const selectedIds = useMemo(
        () => Array.from(selection?.ids ?? []),
        [selection?.ids]
    );
    const selectedCount = selectedIds.length;
    const actionForm = useMemo(() => (
        <Trans i18nKey={$ => $.page.alarma.action.clearMultiple.label}
            values={{ count: selectedCount }}
            components={{ strong: <Box component="span" fontWeight="bold" /> }} />
    ), [selectedCount]);
    const onCompleteAction = () => {
        onSuccess?.();
        apiRef?.current?.refresh();
    };
    return (
        <MuiActionReportButton
            resourceName="alarma"
            action="ALARMA_ESBORRAR_MULTIPLE"
            onSuccess={onCompleteAction}
            buttonComponentProps={{
                variant: 'contained',
                sx: { mr: 1 },
                disabled: selectedCount === 0,
            }}
            selectedCount={selectedCount}
            formAdditionalData={{ ids: selectedIds }}
            formDialogContent={actionForm}
        />
    );
};

const Alarmes: React.FC<{ filterBy?: { entornAppId?: number | string }; }> = ({ filterBy }) => {
    const { t } = useTranslation();
    const [showOnlyActive, setShowOnlyActive] = React.useState<boolean>(true);
    const gridApiRef = useMuiDataGridApiRef();
    const datagridApiRef = useGridApiRef();
    const {
        available: actionEsborrarInitialized,
        formDialogComponent: formEsborrarDialogComponent,
        exec: execEsborrar,
    } = useMuiActionReportLogic(
        'alarma', 'ALARMA_ESBORRAR', undefined, undefined, undefined, undefined,
        undefined, undefined, undefined, undefined, undefined, undefined,
        null, undefined, () => gridApiRef.current?.refresh()
    );
    const {
        available: actionReactivarInitialized,
        formDialogComponent: formReactivarDialogComponent,
        exec: execReactivar,
    } = useMuiActionReportLogic(
        'alarma', 'ALARMA_REACTIVAR', undefined, undefined, undefined, undefined,
        undefined, undefined, undefined, undefined, undefined, undefined,
        null, undefined, () => gridApiRef.current?.refresh()
    );
    const handleClearSelectionSuccess = useCallback(() => {
        datagridApiRef.current?.setRowSelectionModel(DEFAULT_ROW_SELECTION);
    }, [datagridApiRef]);
    const toolbarElementsWithPositions = useMemo(() => [
        {
            position: 2,
            element: <AlarmaEsborrarMultipleActionButton onSuccess={handleClearSelectionSuccess} />
        },
        {
            position: 2,
            element: (
                <Button
                    onClick={() => setShowOnlyActive(prev => !prev)}
                    variant={showOnlyActive ? 'contained' : 'outlined'}
                    title={showOnlyActive ?
                        t($ => $.page.alarma.filter.showOnlyActiveEnabled) :
                        t($ => $.page.alarma.filter.showOnlyActiveDisabled)
                    }
                    sx={{ mr: 2 }}
                >
                    <Icon>{'check_circle'}</Icon>
                </Button>
            ),
        },
    ], [showOnlyActive, t, handleClearSelectionSuccess]);
    const rowAdditionalActions = useMemo(() => [
        {
            label: t($ => $.page.alarma.action.clear.label),
            action: 'ALARMA_ESBORRAR',
            icon: 'check',
            showInMenu: false,
            onClick: execEsborrar,
            hidden: (row: any) => !row?.id || row?.dataEsborrat,
        },
        {
            label: t($ => $.page.alarma.action.reactivate.label),
            action: 'ALARMA_REACTIVAR',
            icon: 'restore',
            showInMenu: false,
            onClick: execReactivar,
            hidden: (row: any) => !row?.id || !row?.dataEsborrat,
        },
    ], [t, execEsborrar, execReactivar]);

    const dataGridColumns = useMemo(
        () => [
            { field: 'missatge', flex: 1 },
            { field: 'dataActivacio', flex: 0.5 },
            { field: 'dataFinalitzacio', flex: 0.5 },
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

    const filter = springFilterBuilder.and(
        showOnlyActive ? "estat:'ACTIVA'" : "estat in('ACTIVA', 'ESBORRADA')",
        filterBy?.entornAppId ? `entornAppId:${filterBy.entornAppId}` : "",
    );

    return (<>{actionEsborrarInitialized && actionReactivarInitialized && <>
                <MuiDataGrid
                    title={t($ => $.menu.alarmes)}
                    resourceName="alarma"
                    columns={dataGridColumns}
                    paginationActive
                    readOnly
                    apiRef={gridApiRef}
                    datagridApiRef={datagridApiRef}
                    toolbarType="upper"
                    toolbarElementsWithPositions={toolbarElementsWithPositions}
                    filter={filter}
                    sortModel={[{ field: 'dataActivacio', sort: 'desc' }]}
                    rowAdditionalActions={rowAdditionalActions}
                    selectionActive
                />
                {formEsborrarDialogComponent}
                {formReactivarDialogComponent}
            </>}</>);
}

export default Alarmes;
