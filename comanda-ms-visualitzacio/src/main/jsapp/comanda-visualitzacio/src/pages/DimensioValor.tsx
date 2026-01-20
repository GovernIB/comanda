import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import {
    GridPage,
    MuiGrid,
    MuiDataGridColDef,
    springFilterBuilder,
    FormField,
    MuiFilter,
    useFilterApiRef,
    useFormApiRef,
    useResourceApiService,
    useBaseAppContext, useFormContext,
} from 'reactlib';
import PageTitle from '../components/PageTitle.tsx';

const DimensioValorForm: React.FC = () => {
    const { data } = useFormContext();
    console.log('DimensioValorForm data.agrupable:', data?.agrupable);
    return (
        <Grid container spacing={2}>
            <Grid size={12}><FormField name="valor" readOnly disabled /></Grid>
            <Grid size={12}><FormField name="agrupable" /></Grid>
            {data?.agrupable === true && (
                <Grid size={12}><FormField name="valorAgrupacio" /></Grid>
            )}
        </Grid>
    );
};

const DimensioValorFilter: React.FC<{ onSpringFilterChange: (f?: string) => void } > = ({ onSpringFilterChange }) => {
    const { t } = useTranslation();
    const filterApiRef = useFilterApiRef();
    const formApiRef = useFormApiRef();

    const netejar = () => {
        filterApiRef?.current?.clear();
    };

    return (
        <MuiFilter
            apiRef={filterApiRef}
            resourceName="dimensioValor"
            code="dimensioValorFilter"
            persistentState
            formApiRef={formApiRef}
            commonFieldComponentProps={{ size: 'small' }}
            onSpringFilterChange={onSpringFilterChange}
            springFilterBuilder={(data) => {
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
                <IconButton onClick={netejar} title={t('components.clear')} sx={{ mr: 1 }}>
                    <Icon>filter_alt_off</Icon>
                </IconButton>
            </Box>
        </MuiFilter>
    );
};

const DimensioValor: React.FC = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const { goBack, anyHistoryEntryExist } = useBaseAppContext();
    const { getOne: getDimensio } = useResourceApiService('dimensio');

    const [dimensionName, setDimensionName] = React.useState<string>('');
    const [filter, setFilter] = React.useState<string | undefined>(undefined);

    React.useEffect(() => {
        if (id) {
            getDimensio(id as string).then((d: { nom?: string; description?: string } | null) => setDimensionName(d?.nom ?? d?.description ?? ''));
        }
    }, [id, getDimensio]);

    const columns: MuiDataGridColDef[] = [
        { field: 'valor', flex: 2 },
        // { field: 'agrupable', flex: 1 },
        // { field: 'valorAgrupacio', flex: 2 },
    ];

    const staticFilter = React.useMemo(() => springFilterBuilder.eq('dimensio.id', id), [id]);

    const toolbarElementsWithPositions = React.useMemo(() => {
        const backButtonDisabled = !anyHistoryEntryExist();
        return [
            {
                position: 0,
                element: (
                    <IconButton
                        title={t('form.goBack.title')}
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

    const filterElement = <DimensioValorFilter onSpringFilterChange={setFilter} />;

    const gridTitle = `Valors dimensió ${dimensionName ?? ''}`;

    return (
        <GridPage>
            <PageTitle title={gridTitle} />
            <MuiGrid
                title={gridTitle}
                resourceName="dimensioValor"
                columns={columns}
                toolbarType="upper"
                paginationActive
                toolbarHideQuickFilter
                toolbarAdditionalRow={filterElement}
                toolbarElementsWithPositions={toolbarElementsWithPositions}
                toolbarHideCreate
                staticFilter={staticFilter}
                filter={filter}
                popupEditActive={false}
                // popupEditFormContent={<DimensioValorForm />}
            />
        </GridPage>
    );
};

export default DimensioValor;
