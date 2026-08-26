import * as React from 'react';
import { useTranslation } from 'react-i18next';
import {
    MuiDataGrid,
    MuiDataGridColDef,
    useResourceApiService,
    useBaseAppContext,
    useConfirmDialogButtons,
    useMuiDataGridApiRef,
} from 'reactlib';
import { ResourceApiError } from '../../lib/components/ResourceApiProvider.tsx';
import PageTitle from '../components/PageTitle.tsx';

const TasquesBackground: React.FC = () => {
    const { t } = useTranslation();
    const { isReady: apiIsReady, delete: apiDelete } =
        useResourceApiService('tascaBackground');
    const gridApiRef = useMuiDataGridApiRef();
    const {
        messageDialogShow,
        temporalMessageShow,
    } = useBaseAppContext();
    const confirmDialogButtons = useConfirmDialogButtons();
    const confirmDialogComponentProps = { maxWidth: 'sm', fullWidth: true };

    const onReiniciarClick = (id: string | number) => {
        messageDialogShow(
            t($ => $.page.tasquesBackground.reiniciar.titol),
            t($ => $.page.tasquesBackground.reiniciar.confirm, { nom: id }),
            confirmDialogButtons,
            confirmDialogComponentProps).
        then((value: any) => {
            if (value && apiIsReady) {
                apiDelete(id)
                    .then(() => {
                        gridApiRef.current?.refresh();
                        temporalMessageShow(null, t($ => $.page.tasquesBackground.reiniciar.success), 'success');
                    })
                    .catch((error: ResourceApiError) => {
                        temporalMessageShow(t($ => $.page.tasquesBackground.reiniciar.error), error.message, 'error');
                    });
            }
        }).
        catch(() => {});
    };

    const columns: MuiDataGridColDef[] = [
        {
            field: 'nom',
            headerName: t($ => $.page.tasquesBackground.columns.nom),
            flex: 2,
        },
        {
            field: 'expressioHuman',
            headerName: t($ => $.page.tasquesBackground.columns.expressioHuman),
            flex: 2,
        },
        {
            field: 'proxExecucio',
            headerName: t($ => $.page.tasquesBackground.columns.proxExecucio),
            flex: 2,
            type: 'dateTime',
            valueGetter: (value: string) => value ? new Date(value) : null,
        },
        {
            field: 'ultimaExecucio',
            headerName: t($ => $.page.tasquesBackground.columns.ultimaExecucio),
            flex: 2,
            type: 'dateTime',
            valueGetter: (value: string) => value ? new Date(value) : null,
        },
        {
            field: 'ultimaEstat',
            headerName: t($ => $.page.tasquesBackground.columns.ultimaEstat),
            flex: 1,
        },
        {
            field: 'ultimaDuracio',
            headerName: t($ => $.page.tasquesBackground.columns.ultimaDuracio),
            flex: 1,
            type: 'number',
        },
    ];

    return (
        <>
            <PageTitle title={t($ => $.page.tasquesBackground.title)} />
            <MuiDataGrid
                apiRef={gridApiRef}
                title={t($ => $.page.tasquesBackground.title)}
                resourceName="tascaBackground"
                columns={columns}
                toolbarType="upper"
                paginationActive
                toolbarHideCreate
                rowHideUpdateButton
                rowHideDeleteButton
                rowAdditionalActions={[
                    {
                        label: t($ => $.page.tasquesBackground.reiniciar.label),
                        icon: 'replay',
                        onClick: onReiniciarClick,
                    },
                ]}
            />
        </>
    );
};

export default TasquesBackground;
