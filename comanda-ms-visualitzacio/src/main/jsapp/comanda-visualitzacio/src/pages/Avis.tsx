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
    useBaseAppContext,
} from 'reactlib';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Icon from '@mui/material/Icon';
import IconButton from '@mui/material/IconButton';
import Button from '@mui/material/Button';
import { useTreeData } from '../hooks/treeData';
import { formatEndOfDay, formatStartOfDay } from '../util/dateUtils.ts';
import { GridSortModel, useGridApiRef} from '@mui/x-data-grid-pro';
import { useUserContext } from '../components/UserContext';
import PageTitle from '../components/PageTitle.tsx';
import SalutChip from '../components/salut/SalutChip.tsx';
import { useGetColorByAvisTipus, AvisTipusEnum } from '../types/salut.model.tsx';
import { ROLE_ADMIN } from '../components/UserProvider.tsx';
import { DataCommonAdditionalAction } from '../../lib/components/mui/datacommon/MuiDataCommon.tsx';

const AvisTipusChip = (props: { tipus?: AvisTipusEnum }) => {
    const { tipus } = props;
    const { t } = useTranslation();
    const getColorByAvisTipus = useGetColorByAvisTipus();

    const getIcon = (tipus: AvisTipusEnum) => {
        switch (tipus) {
            case AvisTipusEnum.NOTICIA:
                return 'add_circle_outlined';
            case AvisTipusEnum.INFO:
                return 'info_outlined';
            case AvisTipusEnum.ALERTA:
                return 'error_outline';
            case AvisTipusEnum.ERROR:
                return 'highlight_off_outline';
            case AvisTipusEnum.CRITIC:
                return 'crisis_alert_outlined';
            default:
                return 'help_outline';
        }
    };

    const getLabel = (tipus: AvisTipusEnum) => {
        const text = tipus.toLowerCase();
        return text.charAt(0).toUpperCase() + text.slice(1);
    };

    const getTooltip = (tipus: AvisTipusEnum) => {
        switch (tipus) {
            case AvisTipusEnum.NOTICIA:
                return t($ => $.page.avisos.grid.column.tipus.tooltip.NOTICIA);
            case AvisTipusEnum.INFO:
                return t($ => $.page.avisos.grid.column.tipus.tooltip.INFO);
            case AvisTipusEnum.ALERTA:
                return t($ => $.page.avisos.grid.column.tipus.tooltip.ALERTA);
            case AvisTipusEnum.ERROR:
                return t($ => $.page.avisos.grid.column.tipus.tooltip.ERROR);
            case AvisTipusEnum.CRITIC:
                return t($ => $.page.avisos.grid.column.tipus.tooltip.CRITIC);
            default:
                return '';
        }
    };

    if (!tipus) return null;

    return (
        <SalutChip
            label={getLabel(tipus)}
            backgroundColor={getColorByAvisTipus(tipus)}
            icon={<Icon sx={{ color: 'white !important' }}>{getIcon(tipus)}</Icon>}
            tooltip={getTooltip(tipus)}
        />
    );
};

const AvisGlobalChip = () => {
    const { t } = useTranslation();
    const getColorByAvisTipus = useGetColorByAvisTipus();

    return (
        <SalutChip
            label={t($ => $.page.avisos.grid.column.global)}
            backgroundColor={getColorByAvisTipus('ORANGE')}
            icon={<Icon sx={{ color: 'white !important' }}>public</Icon>}
            tooltip={t($ => $.page.avisos.grid.column.globalTooltip)}
        />
    );
};

const AvisFilter = (props: { onSpringFilterChange: (springFilter: string | undefined) => void }) => {
    const { onSpringFilterChange } = props;
    const { t } = useTranslation();
    const { user } = useUserContext();
    const [unfinishedOnly, setUnfinishedOnly] = React.useState<boolean>(true);
    const [ownAvisOnly, setOwnAvisOnly] = React.useState<boolean>(true);
    const [moreFields, setMoreFields] = React.useState<boolean>(false);
    const appEntornFilterApiRef = useFilterApiRef();
    const moreFilterApiRef = useFilterApiRef();
    const moreFormApiRef = useFormApiRef();
    const netejar = () => {
        appEntornFilterApiRef?.current?.clear();
        moreFilterApiRef?.current?.clear({ finalitzada: unfinishedOnly });
    }
    React.useEffect(() => {
        moreFormApiRef.current?.setFieldValue('finalitzada', unfinishedOnly);
    }, [unfinishedOnly]);
    React.useEffect(() => {
        moreFormApiRef.current?.setFieldValue('avisPropi', ownAvisOnly);
    }, [ownAvisOnly]);

    const currentUsername = user?.codi;
    const ownAvisOnlyFilterAvailable = currentUsername != null;

    return (
        <>
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
                        onClick={() => setUnfinishedOnly(fo => !fo)}
                        variant={unfinishedOnly ? 'contained' : 'outlined'}
                        title={unfinishedOnly ? t( $ => $.page.avisos.filter.unfinishedOnlyEnabled) : t($ => $.page.avisos.filter.unfinishedOnlyDisabled)}
                        sx={{ mr: 2 }}>
                        <Icon>pending_actions</Icon>
                    </Button>
                    <Button
                        onClick={() => setOwnAvisOnly(value => !value)}
                        disabled={!ownAvisOnlyFilterAvailable}
                        variant={ownAvisOnly ? 'contained' : 'outlined'}
                        title={ownAvisOnly ? t($ => $.page.avisos.filter.ownAvisOnlyEnabled) : t($ => $.page.avisos.filter.ownAvisOnlyDisabled)}
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
                        title={t($ => $.page.avisos.filter.more)}>
                        <Icon>filter_list</Icon>
                    </IconButton>
                </Box>
            </MuiFilter>
            <MuiFilter
                apiRef={moreFilterApiRef}
                formApiRef={moreFormApiRef}
                resourceName="avis"
                code="FILTER"
                initialData={{ finalitzada: unfinishedOnly, avisPropi: ownAvisOnly  }}
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
                    data?.avisPropi && ownAvisOnlyFilterAvailable && springFilterBuilder.eq('responsable', `'${currentUsername}'`),
                )}
                onSpringFilterChange={onSpringFilterChange}
                commonFieldComponentProps={{ size: 'small' }}>
                <Grid container spacing={1} sx={{ display: moreFields ? undefined : 'none', mt: 1 }}>
                    <Grid size={{ xs: 12, sm:4}}><FormField name="nom" /></Grid>
                    <Grid size={{ xs: 6, sm:4}}><FormField name="descripcio" /></Grid>
                    <Grid size={{ xs: 6, sm:4}}><FormField name="tipus" autocomplete /></Grid>
                    <Grid size={{ xs: 6, sm:3}}><FormField name="dataInici1" /></Grid>
                    <Grid size={{ xs: 6, sm:3}}><FormField name="dataInici2" /></Grid>
                    <Grid size={{ xs: 6, sm:3}}><FormField name="dataFi1" /></Grid>
                    <Grid size={{ xs: 6, sm:3}}><FormField name="dataFi2" /></Grid>
                </Grid>
            </MuiFilter>
        </>
    );
}

const dataGridCommonColumns: MuiDataGridColDef[] = [{
    field: 'descripcio',
    flex: 1,
}, {
    field: 'tipus',
    flex: 0.8,
    renderCell: (param) => {
        return (
            <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', height: '100%' }}>
                <AvisTipusChip tipus={param?.row?.tipus} />
                {param?.row?.global && <AvisGlobalChip />}
            </Box>
        );
    }
}, {
    field: 'responsable',
    flex: 1,
}, {
    field: 'dataInici',
    flex: 0.5,
}, {
    field: 'dataFi',
    flex: 0.5,
}];
const dataGridPerspectives = ['PATH'];
const dataGridSortModel: GridSortModel = [{ field: 'dataInici', sort: 'asc' }];

const Avis = () => {
    const { t } = useTranslation();
    const { t: tLib } = useBaseAppContext();
    const { currentRole } = useUserContext();
    const [filter, setFilter] = React.useState<string>();
    const gridApiRef = useGridApiRef();
    const isAdmin = currentRole === ROLE_ADMIN;
    const {
        treeView,
        treeViewSwitch,
        dataGridProps: treeDataGridProps,
    } = useTreeData(
        (row) => row?.treePath,
        gridApiRef,
        t($ => $.page.avisos.grid.groupHeader),
        1.5,
        false,
        false,
        { valueFormatter: (value: any, row: any) => row?.id ? row?.nom : value });
    const columns: MuiDataGridColDef[] = [
        ...(!treeView
            ? [
                  { field: 'nom', flex: 1 },
                  {
                      field: 'treePath',
                      flex: 0.7,
                      headerName: t($ => $.page.avisos.grid.column.appEntorn),
                      valueFormatter: (value: any) => `${value?.[0]} - ${value?.[1]}`,
                  },
              ]
            : []),
        ...(filter?.includes('dataFi is null')
            ? dataGridCommonColumns.slice(0, -1)
            : dataGridCommonColumns),
    ];
    const actions = React.useMemo<DataCommonAdditionalAction[]>(() => {
        const baseActions: DataCommonAdditionalAction[] = [{
                icon: 'open_in_new',
                label: t($ => $.page.avisos.grid.action.obrir),
                showInMenu: false,
                linkTo: (row: any) => row?.url,
                linkTarget: '_blank',
                disabled: (row: any) => !row?.url,
                hidden: (row: any) => !row?.url,
            }];
        if (isAdmin) {
            baseActions.push({
                label: tLib('datacommon.delete.label'),
                icon: 'delete',
                clickTriggerDelete: true,
                hidden(row) {return !row?.id;},
            });
        }
        return baseActions;
    }, [t, tLib, isAdmin,]);
    const filterElement = <AvisFilter onSpringFilterChange={setFilter}/>;

    return (
        <Box sx={{ height: '100%' }}>
            <PageTitle title={t($ => $.menu.avis)} />
            <MuiDataGrid
                title={t($ => $.menu.avis)}
                resourceName="avis"
                columns={columns}
                perspectives={dataGridPerspectives}
                datagridApiRef={gridApiRef}
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
        </Box>
    );
}

export default Avis;
