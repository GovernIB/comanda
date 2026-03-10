import { useTranslation } from 'react-i18next';
import {
    GridPage,
    MuiDataGrid,
    useMuiDataGridApiRef,
    useMuiActionReportLogic,
    useBaseAppContext
} from 'reactlib';
import { useUserContext } from '../components/UserContext';
import { ROLE_ADMIN } from '../components/UserProvider';
import React from 'react';
import { DataCommonAdditionalAction } from '../../lib/components/mui/datacommon/MuiDataCommon';

const dataGridColumns = [{
    field: 'missatge',
    flex: 1,
}, {
    field: 'dataActivacio',
    flex: 0.5,
}, {
    field: 'estat',
    flex: 0.5,
}];

const Alarmes = () => {
    const { t } = useTranslation();
    const { t: tLib } = useBaseAppContext();
    const { currentRole } = useUserContext();
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
    const isAdmin = currentRole === ROLE_ADMIN;
    /*const toolbarElementsWithPositions = [{
        position: 3,
        element: <MuiActionReportButton
            resourceName="alarma"
            action="ALARMA_ESBORRAR_TOTES"
            onSuccess={() => gridApiRef.current.refresh()}
            buttonComponentProps={{ variant: 'contained', sx: { ml: 1 } }} />
    }];*/
    const rowAdditionalActions = React.useMemo(() => {
        const additionalActions: DataCommonAdditionalAction[] = [{
            action: 'ALARMA_ESBORRAR',
            icon: 'check',
            showInMenu: false,
            onClick: exec,
        }];
        if (isAdmin) {
            additionalActions.push({
                label: tLib('datacommon.delete.label'),
                icon: 'delete',
                clickTriggerDelete: true,
            });
        }
        return additionalActions;
    }, [isAdmin, exec, tLib])
    return (
        <GridPage>
            {actionInitialized && <>
                <MuiDataGrid
                    title={t($ => $.menu.alarmes)}
                    resourceName="alarma"
                    columns={dataGridColumns}
                    readOnly
                    apiRef={gridApiRef}
                    toolbarType="upper"
                    //toolbarElementsWithPositions={toolbarElementsWithPositions}
                    filter="estat:'ACTIVA'"
                    sortModel={[{ field: 'dataActivacio', sort: 'desc' }]}
                    rowAdditionalActions={rowAdditionalActions} />
                {formDialogComponent}
            </>}
        </GridPage>
    );
}

export default Alarmes;