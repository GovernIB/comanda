import { useTranslation } from 'react-i18next';
import {
    GridPage,
    MuiDataGrid,
    useMuiDataGridApiRef,
    useMuiActionReportLogic
} from 'reactlib';

const dataGridColumns = [{
    field: 'missatge',
    flex: 1,
}, {
    field: 'dataActivacio',
    flex: 0.5,
}, {
    field: 'estat',
    flex: 0.5,
}, {
    field: 'estat',
    flex: 0.5,
}];

const Alarmes = () => {
    const { t } = useTranslation();
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
    const rowAdditionalActions = [{
        action: 'ALARMA_ESBORRAR',
        icon: 'check',
        showInMenu: false,
        onClick: exec
    }];
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