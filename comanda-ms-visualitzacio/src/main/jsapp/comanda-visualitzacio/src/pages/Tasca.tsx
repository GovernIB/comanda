import * as React from 'react';
import { useTranslation } from 'react-i18next';
import {
    GridPage,
    MuiDataGrid,
    MuiFilter,
    FormField,
    springFilterBuilder,
    useFormApiRef,
    useFilterApiRef,
} from 'reactlib';
import {
    Box,
    Grid,
    Typography,
    Icon,
    Button,
    IconButton,
} from '@mui/material';
import { useTreeData } from '../hooks/treeData';
import { formatEndOfDay, formatStartOfDay } from '../util/dateUtils';

const perspectives = ['PATH', 'EXPIRATION'];
const sortModel: any = [{field: 'dataInici', sort: 'asc'}];

export const StyledPrioritat = (props: any) => {
    const {entity, children} = props;
    let style: any = {};
    switch (entity?.prioritat) {
        case 'MAXIMA':
            style = { backgroundColor: '#d99b9d' }
            break;
        case 'ALTA':
            style = { backgroundColor: '#ffebae' }
            break;
        case 'NORMAL':
            style = { border: '1px dashed #AAA' }
            break;
        case 'BAIXA':
            style = { backgroundColor: '#c3e8d1' }
            break;
    }
    return <Typography
        variant="caption"
        sx={{
            padding: '1px 4px',
            fontSize: '11px',
            fontWeight: '500',
            borderRadius: '2px',
            display: 'flex',
            alignItems: 'center',
            width: 'max-content',
            ...style
        }}>{children}</Typography>;
}

const gridCommonColumns = [{
    field: 'descripcio',
    flex: 2,
}, {
    field: 'tipus',
    flex: .5,
}, {
    field: 'prioritat',
    flex: 0.5,
    renderCell: (param:any) => <StyledPrioritat entity={param.row}>{param?.formattedValue}</StyledPrioritat>,
}, {
    field: 'dataInici',
    flex: 1,
}, {
    field: 'dataCaducitat',
    flex: 1,
    renderCell: (param:any) => {
        const style = param?.row?.diesPerCaducar == null ? {} :
        param?.row?.diesPerCaducar <= 0 ? { color: 'white', backgroundColor: '#6b0707' } :
        param?.row?.diesPerCaducar <= 3 ? { color: 'white', backgroundColor: 'error.main' } :
        param?.row?.diesPerCaducar <= 5 ? { color: 'white', backgroundColor: 'warning.main' } :
        {};
        return <Typography variant={'inherit'} sx={{p: 1, borderRadius: '4px', ...style}}>
            {param?.formattedValue}
        </Typography>
    },
}, {
    field: 'dataFi',
    flex: 1,
}];

const TascaFilter = (props:any) => {
    const { onSpringFilterChange } = props;
    const { t } = useTranslation();
    const [finishedOnly, setFinishedOnly] = React.useState<boolean>(true);
    const [moreFields, setMoreFields] = React.useState<boolean>(false);
    const appEntornFilterApiRef = useFilterApiRef();
    const moreFilterApiRef = useFilterApiRef();
    const moreFormApiRef = useFormApiRef();
    const netejar = () => {
        appEntornFilterApiRef?.current?.clear();
        moreFilterApiRef?.current?.clear({ finalitzada: finishedOnly });
    }
    React.useEffect(() => {
        moreFormApiRef.current?.setFieldValue('finalitzada', finishedOnly);
    }, [finishedOnly]);
    return <>
        <MuiFilter
            apiRef={appEntornFilterApiRef}
            resourceName={'entornApp'}
            code={'salut_entornApp_filter'}
            commonFieldComponentProps={{ size: 'small' }}
            springFilterBuilder={data => {
                moreFormApiRef.current?.setFieldValue('appId', data.app);
                moreFormApiRef.current?.setFieldValue('entornId', data.entorn);
                return '';
            }}>
            <Box sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
            }}>
                <Grid container spacing={1} sx={{ flexGrow: 1, mr: 1 }}>
                    <Grid size={6}><FormField name={'app'} /></Grid>
                    <Grid size={6}><FormField name={'entorn'} /></Grid>
                </Grid>
                <Button
                    onClick={() => setFinishedOnly(fo => !fo)}
                    variant={finishedOnly ? 'contained' : 'outlined'}
                    title="Només finalitzades"
                    sx={{ mr: 2 }}>
                    <Icon>done_all</Icon>
                </Button>
                <IconButton
                    onClick={netejar}
                    title={t('components.clear')}
                    sx={{ mr: 1 }}>
                    <Icon>filter_alt_off</Icon>
                </IconButton>
                <IconButton
                    onClick={() => setMoreFields((mf) => !mf)}
                    title="Més camps">
                    <Icon>filter_list</Icon>
                </IconButton>
            </Box>
        </MuiFilter>
        <MuiFilter
            apiRef={moreFilterApiRef}
            formApiRef={moreFormApiRef}
            resourceName={'tasca'}
            code={'FILTER'}
            initialData={{ finalitzada: finishedOnly }}
            springFilterBuilder={data => springFilterBuilder.and(
                springFilterBuilder.eq('appId', data?.appId?.id),
                springFilterBuilder.eq('entornId', data?.entornId?.id),
                springFilterBuilder.like('nom', data?.nom),
                springFilterBuilder.like('descripcio', data?.descripcio),
                springFilterBuilder.like('tipus', data?.tipus),
                springFilterBuilder.eq('prioritat', `'${data?.prioritat}'`),
                data?.dataInici1 && springFilterBuilder.gte('dataInici', `'${formatStartOfDay(data?.dataInici1)}'`),
                data?.dataInici2 && springFilterBuilder.lte('dataInici', `'${formatEndOfDay(data?.dataInici2)}'`),
                data?.dataFi1 && springFilterBuilder.gte('dataFi', `'${formatStartOfDay(data?.dataFi1)}'`),
                data?.dataFi2 && springFilterBuilder.lte('dataFi', `'${formatEndOfDay(data?.dataFi2)}'`),
                data?.dataCaducitat1 && springFilterBuilder.gte('dataCaducitat', `'${formatStartOfDay(data?.dataCaducitat1)}'`),
                data?.dataCaducitat2 && springFilterBuilder.gte('dataCaducitat', `'${formatEndOfDay(data?.dataCaducitat2)}'`),
                data?.finalitzada && springFilterBuilder.eq('dataFi', null),
            )}
            onSpringFilterChange={onSpringFilterChange}
            commonFieldComponentProps={{ size: 'small' }}>
            <Grid container spacing={1} sx={{ display: moreFields ? undefined : 'none', mt: 1 }}>
                <Grid size={3}><FormField name={'nom'}/></Grid>
                <Grid size={3}><FormField name={'descripcio'}/></Grid>
                <Grid size={3}><FormField name={'tipus'}/></Grid>
                <Grid size={3}><FormField name={'prioritat'}/></Grid>
                <Grid size={2}><FormField name={'dataInici1'}/></Grid>
                <Grid size={2}><FormField name={'dataInici2'}/></Grid>
                <Grid size={2}><FormField name={'dataFi1'}/></Grid>
                <Grid size={2}><FormField name={'dataFi2'}/></Grid>
                <Grid size={2}><FormField name={'dataCaducitat1'}/></Grid>
                <Grid size={2}><FormField name={'dataCaducitat2'}/></Grid>
            </Grid>
        </MuiFilter>
    </>;
}

const Tasca = () => {
    const { t } = useTranslation();
    const [filter, setFilter] = React.useState<string>();
    const {
        treeView,
        treeViewSwitch,
        dataGridProps: treeDataGridProps,
    } = useTreeData(
        (row) => row?.treePath,
        'Nom',
        1.5,
        { valueFormatter: (value: any, row: any) => row?.id ? row?.nom : value });
    const columns = [
        ...(!treeView ? [{ field: 'nom', flex: 1 }] : []),
        ...(filter?.includes('dataFi is null') ? gridCommonColumns.slice(0, -1) : gridCommonColumns),
    ];
    const actions = [{
        icon: 'open_in_new',
        label: 'Obrir tasca',
        showInMenu: false,
        linkTo: (row: any) => row?.url,
        linkTarget: '_blank',
        disabled: (row: any) => !row?.url,
        hidden: (row: any) => !row?.id,
    }];
    const filterElement =<TascaFilter onSpringFilterChange={setFilter}/>;
    return <GridPage>
        <MuiDataGrid
            title={t('menu.tasca')}
            resourceName={'tasca'}
            perspectives={perspectives}
            sortModel={sortModel}
            findDisabled={filter == null}
            filter={filter}
            columns={columns}
            readOnly
            toolbarType="upper"
            toolbarHideQuickFilter
            toolbarElementsWithPositions={[{ position: 1, element: treeViewSwitch }]}
            toolbarAdditionalRow={filterElement}
            rowAdditionalActions={actions}
            {...treeDataGridProps}
        />
    </GridPage>;
}

export default Tasca;