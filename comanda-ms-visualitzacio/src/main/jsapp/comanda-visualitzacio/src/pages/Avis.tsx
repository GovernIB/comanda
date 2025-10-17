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
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import Icon from '@mui/material/Icon';
import IconButton from '@mui/material/IconButton';
import Button from '@mui/material/Button';
import { useTreeData } from '../hooks/treeData';
import { formatEndOfDay, formatStartOfDay } from '../util/dateUtils.ts';
import { useGridApiRef } from '@mui/x-data-grid-pro';

const AvisFilter = (props: any) => {
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
            resourceName="entornApp"
            code="salut_entornApp_filter"
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
                    <Grid size={6}><FormField name="app" /></Grid>
                    <Grid size={6}><FormField name="entorn" /></Grid>
                </Grid>
                <Button
                    onClick={() => setFinishedOnly(fo => !fo)}
                    variant={finishedOnly ? 'contained' : 'outlined'}
                    title={t('page.avisos.filter.unfinished')}
                    sx={{ mr: 2 }}>
                    <Icon>pending_actions</Icon>
                </Button>
                <IconButton
                    onClick={netejar}
                    title={t('components.clear')}
                    sx={{ mr: 1 }}>
                    <Icon>filter_alt_off</Icon>
                </IconButton>
                <IconButton
                    onClick={() => setMoreFields((mf) => !mf)}
                    title={t('page.avisos.filter.more')}>
                    <Icon>filter_list</Icon>
                </IconButton>
            </Box>
        </MuiFilter>
        <MuiFilter
            apiRef={moreFilterApiRef}
            formApiRef={moreFormApiRef}
            resourceName="avis"
            code="FILTER"
            initialData={{ finalitzada: finishedOnly }}
            springFilterBuilder={data => springFilterBuilder.and(
                springFilterBuilder.eq('appId', data?.appId?.id),
                springFilterBuilder.eq('entornId', data?.entornId?.id),
                springFilterBuilder.like('nom', data?.nom),
                springFilterBuilder.like('descripcio', data?.descripcio),
                springFilterBuilder.eq('tipus', data?.tipus),
                data?.dataInici1 && springFilterBuilder.gte('dataInici', `'${formatStartOfDay(data?.dataInici1)}'`),
                data?.dataInici2 && springFilterBuilder.lte('dataInici', `'${formatEndOfDay(data?.dataInici2)}'`),
                data?.dataFi1 && springFilterBuilder.gte('dataFi', `'${formatStartOfDay(data?.dataFi1)}'`),
                data?.dataFi2 && springFilterBuilder.lte('dataFi', `'${formatEndOfDay(data?.dataFi2)}'`),
                data?.finalitzada && springFilterBuilder.eq('dataFi', null),
            )}
            onSpringFilterChange={onSpringFilterChange}
            commonFieldComponentProps={{ size: 'small' }}>
            <Grid container spacing={1} sx={{ display: moreFields ? undefined : 'none', mt: 1 }}>
                <Grid size={{ xs: 12, sm:4}}><FormField name="nom" /></Grid>
                <Grid size={{ xs: 6, sm:4}}><FormField name="descripcio" /></Grid>
                <Grid size={{ xs: 6, sm:4}}><FormField name="tipus" /></Grid>
                <Grid size={{ xs: 6, sm:3}}><FormField name="dataInici1" /></Grid>
                <Grid size={{ xs: 6, sm:3}}><FormField name="dataInici2" /></Grid>
                <Grid size={{ xs: 6, sm:3}}><FormField name="dataFi1" /></Grid>
                <Grid size={{ xs: 6, sm:3}}><FormField name="dataFi2" /></Grid>
            </Grid>
        </MuiFilter>
    </>;
}

const dataGridCommonColumns = [{
    field: 'descripcio',
    flex: 1,
}, {
    field: 'tipus',
    flex: 0.5,
    renderCell: (param:any) => {
        let style: any = {};
        switch (param?.row?.tipus) {
            case 'NOTICIA':
                style = { backgroundColor: 'success.main', color: 'white' }
                break;
            case 'INFO':
                style = { backgroundColor: 'info.main', color: 'white' }
                break;
            case 'ALERTA':
                style = { backgroundColor: 'warning.main', color: 'white' }
                break;
            case 'ERROR':
                style = { backgroundColor: 'error.main', color: 'white' }
                break;
            case 'CRITIC':
                style = { backgroundColor: '#6b0707', color: 'white' }
                break;
        }
        return <Typography variant={'inherit'} sx={{ p: 1, borderRadius: '4px', ...style }}>
            {param?.formattedValue}
        </Typography>;
    }
}, {
    field: 'dataInici',
    flex: 0.5,
}, {
    field: 'dataFi',
    flex: 0.5,
}];
const dataGridPerspectives = ['PATH'];

const Avis = () => {
    const { t } = useTranslation();
    const [filter, setFilter] = React.useState<string>();
    const gridApiRef = useGridApiRef();
    const {
        treeView,
        treeViewSwitch,
        dataGridProps: treeDataGridProps,
    } = useTreeData(
        (row) => row?.treePath,
        gridApiRef,
        t('page.avisos.grid.groupHeader'),
        1.5,
        true,
        true,
        { valueFormatter: (value: any, row: any) => row?.id ? row?.nom : value });
    const columns = [
        ...(!treeView ? [{ field: 'nom', flex: 1 }] : []),
        ...(filter?.includes('dataFi is null') ? dataGridCommonColumns.slice(0, -1) : dataGridCommonColumns),
    ];
    const filterElement = <AvisFilter onSpringFilterChange={setFilter}/>;
    return <GridPage>
        <MuiDataGrid
            title={t('menu.avis')}
            resourceName="avis"
            columns={columns}
            perspectives={dataGridPerspectives}
            datagridApiRef={gridApiRef}
            findDisabled={filter == null}
            filter={filter}
            readOnly
            toolbarType="upper"
            toolbarHideQuickFilter
            toolbarElementsWithPositions={[{ position: 1, element: treeViewSwitch }]}
            toolbarAdditionalRow={filterElement}
            {...treeDataGridProps}
        />
    </GridPage>;
}

export default Avis;
