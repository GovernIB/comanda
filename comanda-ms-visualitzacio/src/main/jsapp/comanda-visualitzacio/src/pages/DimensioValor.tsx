import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import {
    MuiDataGrid,
    MuiDataGridColDef,
    springFilterBuilder,
    FormField,
    MuiFilter,
    useFilterApiRef,
    useResourceApiService,
    useBaseAppContext, useMuiDataGridApiRef,
} from 'reactlib';
import PageTitle from '../components/PageTitle.tsx';
import useReadOnlyGestor from '../hooks/useReadOnlyGestor.ts';

// const DimensioValorForm: React.FC = () => {
//     const { data } = useFormContext();
//     console.log('DimensioValorForm data.agrupable:', data?.agrupable);
//     return (
//         <Grid container spacing={2}>
//             <Grid size={12}><FormField name="valor" readOnly disabled /></Grid>
//             <Grid size={12}><FormField name="agrupable" /></Grid>
//             {data?.agrupable === true && (
//                 <Grid size={12}><FormField name="valorAgrupacio" /></Grid>
//             )}
//         </Grid>
//     );
// };

const DimensioValorFilter: React.FC<{ onSpringFilterChange?: (f?: string) => void; onDataChange?: (f?: any) => void }> = ({ onSpringFilterChange, onDataChange }) => {
    const { t } = useTranslation();
    const filterApiRef = useFilterApiRef();

    const netejar = () => {
        filterApiRef?.current?.clear();
    };

    return (
        <MuiFilter
            apiRef={filterApiRef}
            resourceName="dimensioValor"
            code="dimensioValorFilter"
            commonFieldComponentProps={{ size: 'small' }}
            onSpringFilterChange={onSpringFilterChange}
            springFilterBuilder={(data) => {
                onDataChange?.(data)
                return springFilterBuilder.and(
                    data?.valor && springFilterBuilder.like('valor', data?.valor),
                    // data?.agrupable != null && springFilterBuilder.eq('agrupable', data?.agrupable),
                    // data?.valorAgrupacio && springFilterBuilder.like('valorAgrupacio', data?.valorAgrupacio),
                ) || '';
            }}
        >
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Grid container spacing={1} sx={{ flexGrow: 1, mr: 1 }}>
                    <Grid size={{ xs: 12, md: 4 }}><FormField name={'valor'} /></Grid>
                    {/*<Grid size={{ xs: 12, md: 4 }}><FormField name={'agrupable'} type={'checkbox'} /></Grid>*/}
                    {/*<Grid size={{ xs: 12, md: 4 }}><FormField name={'valorAgrupacio'} /></Grid>*/}
                </Grid>
                <IconButton onClick={netejar} title={t($ => $.components.clear)} sx={{ mr: 1 }}>
                    <Icon>filter_alt_off</Icon>
                </IconButton>
            </Box>
        </MuiFilter>
    );
};

const DimensioValor: React.FC = () => {
    const { t } = useTranslation();
    const gestorReadOnly = useReadOnlyGestor();
    const { id } = useParams();
    const { goBack, anyHistoryEntryExist } = useBaseAppContext();
    const { isReady, getOne: getDimensio } = useResourceApiService('dimensio');
    const { artifactAction: apiAction } = useResourceApiService('dimensioValor');
    const { temporalMessageShow } = useBaseAppContext();

    const [dimension, setDimension] = React.useState<any>();

    React.useEffect(() => {
        if (id && isReady) {
            getDimensio(id as string).then((d: { nom?: string; description?: string } | null) => setDimension(d));
        }
    }, [id, isReady, getDimensio]);

    const columns: MuiDataGridColDef[] = [
        { field: 'codiNom', flex: 2 },
        // { field: 'agrupable', flex: 1 },
        // { field: 'valorAgrupacio', flex: 2 },
    ];

    const fixedFilter = React.useMemo(() => springFilterBuilder.eq('dimensio.id', id), [id]);

    const toolbarElementsWithPositions = React.useMemo(() => {
        const backButtonDisabled = !anyHistoryEntryExist();
        return [
            {
                position: 0,
                element: (
                    <IconButton
                        onClick={() => goBack('/dimensio')}
                        disabled={backButtonDisabled}
                        sx={{ mr: 1 }}
                    >
                        <Icon>arrow_back</Icon>
                    </IconButton>
                ),
            },
        ];
    }, [anyHistoryEntryExist, goBack, t]);

    const [quickfilter, setQuickfilter] = React.useState<string | undefined>();
    const filterElement = <DimensioValorFilter onDataChange={(data) => setQuickfilter(data.valor) } />;
    const namedQueries = React.useMemo(() => {
        const queries: string[] = [];
        if (quickfilter) queries.push(`filterByUONom:${quickfilter}`);
        return queries.length > 0 ? queries : undefined;
    }, [quickfilter]);

    const gridTitle = `Valors dimensió ${dimension?.nom ?? dimension?.description ?? ''}`;

    const gridApiRef = useMuiDataGridApiRef();
    const refresh = () => {
        gridApiRef?.current?.refresh?.();
    }
    return (
        <>
            <PageTitle title={gridTitle} />
            <MuiDataGrid
                apiRef={gridApiRef}
                title={gridTitle}
                resourceName="dimensioValor"
                columns={columns}
                toolbarType="upper"
                paginationActive
                toolbarHideQuickFilter
                toolbarAdditionalRow={filterElement}
                toolbarElementsWithPositions={toolbarElementsWithPositions}
                toolbarHideCreate
                fixedFilter={fixedFilter}
                namedQueries={namedQueries}
                popupEditActive={false}
                rowHideDeleteButton={gestorReadOnly}
                rowAdditionalActions={[
                    {
                        label: t($ => $.page.dimensions.action.sincronitzar.label),
                        icon: 'refresh',
                        showInMenu: false,
                        action: 'UO_DIR3',
                        onClick: (id) => {
                            apiAction(id, {code: 'UO_DIR3'})
                                .then(() => {
                                    refresh()
                                    temporalMessageShow(null, t($ => $.page.dimensions.action.sincronitzar.ok), 'success')
                                })
                                .catch(error => temporalMessageShow(null, error.message, 'error'))
                        },
                        hidden: !dimension?.tipus || (dimension?.tipus != 'ORGAN_GESTOR' && dimension?.tipus != 'CONSELLERIA')
                    }
                ]}
                // popupEditFormContent={<DimensioValorForm />}
            />
        </>
    );
};

export default DimensioValor;
