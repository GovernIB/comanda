import * as React from 'react';
import { useTranslation } from 'react-i18next';
import {
    MuiDataGrid,
    MuiFilter,
    FormField,
    springFilterBuilder,
    useFormApiRef,
    useFilterApiRef,
    MuiDataGridColDef,
    BasePage,
} from 'reactlib';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import Icon from '@mui/material/Icon';
import IconButton from '@mui/material/IconButton';
import Button from '@mui/material/Button';
import { useTreeData } from '../hooks/treeData';
import { formatEndOfDay, formatStartOfDay } from '../util/dateUtils';
import { GridSortModel, useGridApiRef } from '@mui/x-data-grid-pro';
import dayjs from 'dayjs';
import { SxProps } from '@mui/material';
import { useUserContext } from '../components/UserContext';

export const StyledPrioritat = (props: {
    entity: any;
    children?: React.ReactNode;
}) => {
    const {entity, children} = props;
    let style: SxProps = {};
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

const TascaFilter = (props: { onSpringFilterChange: (springFilter: string | undefined) => void }) => {
    const { onSpringFilterChange } = props;
    const { t } = useTranslation();
    const { user } = useUserContext();
    const [unfinishedOnly, setUnfinishedOnly] = React.useState<boolean>(true);
    const [ownTasksOnly, setOwnTasksOnly] = React.useState<boolean>(true);
    const [moreFields, setMoreFields] = React.useState<boolean>(false);
    const appEntornFilterApiRef = useFilterApiRef();
    const moreFilterApiRef = useFilterApiRef();
    const moreFormApiRef = useFormApiRef();
    const netejar = () => {
        appEntornFilterApiRef?.current?.clear();
        moreFilterApiRef?.current?.clear({ finalitzada: unfinishedOnly, tascaPropia: ownTasksOnly });
    }
    React.useEffect(() => {
        moreFormApiRef.current?.setFieldValue('finalitzada', unfinishedOnly);
    }, [unfinishedOnly]);
    React.useEffect(() => {
        moreFormApiRef.current?.setFieldValue('tascaPropia', ownTasksOnly);
    }, [ownTasksOnly]);

    const currentUserCodi = user?.codi;

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
                    <Grid size={6}><FormField name={'app'} /></Grid>
                    <Grid size={6}><FormField name={'entorn'} /></Grid>
                </Grid>
                <Button
                    onClick={() => setUnfinishedOnly(fo => !fo)}
                    variant={unfinishedOnly ? 'contained' : 'outlined'}
                    title={unfinishedOnly ? t($ => $.page.tasques.filter.unfinishedOnlyEnabled) : t($ => $.page.tasques.filter.unfinishedOnlyDisabled)}
                    sx={{ mr: 2 }}>
                    <Icon>pending_actions</Icon>
                </Button>
                <Button
                    onClick={() => setOwnTasksOnly(value => !value)}
                    disabled={!currentUserCodi}
                    variant={ownTasksOnly ? 'contained' : 'outlined'}
                    title={ownTasksOnly ? t($ => $.page.tasques.filter.ownTasksOnlyEnabled) : t($ => $.page.tasques.filter.ownTasksOnlyDisabled)}
                    sx={{ mr: 2 }}>
                    <Icon>person</Icon>
                </Button>
                <IconButton
                    onClick={netejar}
                    title={t($ => $.components.clear)}
                    sx={{ mr: 1 }}>
                    <Icon>filter_alt_off</Icon>
                </IconButton>
                <IconButton
                    onClick={() => setMoreFields((mf) => !mf)}
                    title={t($ => $.page.tasques.filter.more)}>
                    <Icon>filter_list</Icon>
                </IconButton>
            </Box>
        </MuiFilter>
        <MuiFilter
            apiRef={moreFilterApiRef}
            formApiRef={moreFormApiRef}
            resourceName="tasca"
            code="FILTER"
            initialData={{ finalitzada: unfinishedOnly, tascaPropia: ownTasksOnly }}
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
                data?.tascaPropia && currentUserCodi && springFilterBuilder.eq('responsable', `'${currentUserCodi}'`),
            )}
            onSpringFilterChange={onSpringFilterChange}
            commonFieldComponentProps={{ size: 'small' }}>
            <Grid container spacing={1} sx={{ display: moreFields ? undefined : 'none', mt: 1 }}>
                <Grid size={{ xs: 6, sm:3}}><FormField name="nom" /></Grid>
                <Grid size={{ xs: 6, sm:3}}><FormField name="descripcio" /></Grid>
                <Grid size={{ xs: 6, sm:3}}><FormField name="tipus" /></Grid>
                <Grid size={{ xs: 6, sm:3}}><FormField name="prioritat" /></Grid>
                <Grid size={{ xs: 6, sm:3, md: 2 }}><FormField name="dataInici1" /></Grid>
                <Grid size={{ xs: 6, sm:3, md: 2 }}><FormField name="dataInici2" /></Grid>
                <Grid size={{ xs: 6, sm:3, md: 2 }}><FormField name="dataFi1" /></Grid>
                <Grid size={{ xs: 6, sm:3, md: 2 }}><FormField name="dataFi2" /></Grid>
                <Grid size={{ xs: 6, sm:3, md: 2 }}><FormField name="dataCaducitat1" /></Grid>
                <Grid size={{ xs: 6, sm:3, md: 2 }}><FormField name="dataCaducitat2" /></Grid>
            </Grid>
        </MuiFilter>
    </>;
}

const dataGridCommonColumns: MuiDataGridColDef[] = [
    {
        field: 'descripcio',
        flex: 2,
    },
    {
        field: 'estat',
        flex: 0.1,
        minWidth: 100,
    },
    {
        field: 'tipus',
        flex: 0.5,
        minWidth: 100,
    },
    {
        field: 'responsable',
        flex: 1,
    },
    {
        field: 'prioritat',
        flex: 0.5,
        minWidth: 100,
        renderCell: (param) => (
            <StyledPrioritat entity={param.row}>{param?.formattedValue}</StyledPrioritat>
        ),
    },
    {
        field: 'dataInici',
        flex: 0.7,
        minWidth: 100,
        valueFormatter: (value) => value ? dayjs(value).format('DD/MM/YYYY') : value,
    },
    {
        field: 'dataCaducitat',
        flex: 0.7,
        minWidth: 100,
        valueFormatter: (value) => (value ? dayjs(value).format('DD/MM/YYYY') : value),
        renderCell: (param) => {
            const style =
                param?.row?.diesPerCaducar == null
                    ? {}
                    : param?.row?.diesPerCaducar <= 0
                      ? { color: 'white', backgroundColor: '#6b0707' }
                      : param?.row?.diesPerCaducar <= 3
                        ? { color: 'white', backgroundColor: 'error.main' }
                        : param?.row?.diesPerCaducar <= 5
                          ? { color: 'white', backgroundColor: 'warning.main' }
                          : {};
            return (
                <Typography variant={'inherit'} sx={{ p: 1, borderRadius: '4px', ...style }}>
                    {param?.formattedValue}
                </Typography>
            );
        },
    },
    {
        field: 'dataFi',
        flex: 0.1,
        minWidth: 100,
        valueFormatter: (value) => (value ? dayjs(value).format('DD/MM/YYYY') : value),
    },
];
const dataGridPerspectives = ['PATH', 'EXPIRATION'];
const dataGridSortModel: GridSortModel = [{ field: 'dataInici', sort: 'asc' }];

const Tasca = () => {
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
        t($ => $.page.tasques.grid.groupHeader),
        1.5,
        false,
        false,
        { valueFormatter: (value: any, row: any) => row?.id ? row?.nom : value });
    const columns = [
        ...(!treeView
            ? [
                  { field: 'nom', flex: 1 },
                  {
                      field: 'treePath',
                      flex: 1.2,
                      headerName: t($ => $.page.tasques.grid.column.appEntorn),
                      valueFormatter: (value: any) =>
                          `${value?.[0]} - ${value?.[1]}`,
                  },
              ]
            : []),
        ...(filter?.includes('dataFi is null')
            ? dataGridCommonColumns.slice(0, -1)
            : dataGridCommonColumns),
    ];
    const actions = [{
        icon: 'open_in_new',
        label: t($ => $.page.tasques.grid.action.obrir),
        showInMenu: false,
        linkTo: (row: any) => row?.url,
        linkTarget: '_blank',
        disabled: (row: any) => !row?.url,
        hidden: (row: any) => !row?.id,
    }];
    const filterElement = <TascaFilter onSpringFilterChange={setFilter}/>;

    // Se usa el componente BasePage para evitar posibles conflictos entre la suscripción de eventos y el estado "proceed" de GridPage
    return (
        <BasePage expandHeight style={{ height: '100%' }}>
            <MuiDataGrid
                title={t($ => $.menu.tasca)}
                datagridApiRef={gridApiRef}
                resourceName="tasca"
                columns={columns}
                perspectives={dataGridPerspectives}
                sortModel={dataGridSortModel}
                findDisabled={filter == null}
                filter={filter}
                readOnly
                toolbarType="upper"
                toolbarHideQuickFilter
                toolbarElementsWithPositions={[{ position: 1, element: treeViewSwitch }]}
                toolbarAdditionalRow={filterElement}
                rowAdditionalActions={actions}
                {...treeDataGridProps}
            />
        </BasePage>
    );
}

export default Tasca;
