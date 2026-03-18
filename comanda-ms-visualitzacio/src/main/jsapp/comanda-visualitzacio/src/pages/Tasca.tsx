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
import { formatEndOfDay, formatStartOfDay } from '../util/dateUtils';
import { GridSortModel, useGridApiRef } from '@mui/x-data-grid-pro';
import dayjs from 'dayjs';
import { useUserContext } from '../components/UserContext';
import PageTitle from '../components/PageTitle.tsx';
import SalutChip from '../components/salut/SalutChip.tsx';
import {
    TascaEstatEnum,
    TascaPrioritatEnum,
    useGetColorByTascaEstat,
    useGetColorByTascaPrioritat
} from '../types/salut.model.tsx';
import { DataCommonAdditionalAction } from '../../lib/components/mui/datacommon/MuiDataCommon.tsx';
import { ROLE_ADMIN } from '../components/UserProvider.tsx';

const TascaPrioritatChip = (props: { prioritat?: TascaPrioritatEnum }) => {
    const { prioritat } = props;
    const { t } = useTranslation();
    const getColorByTascaPrioritat = useGetColorByTascaPrioritat();

    const getIcon = (prioritat: TascaPrioritatEnum) => {
        switch (prioritat) {
            case TascaPrioritatEnum.MAXIMA:
                return 'keyboard_double_arrow_up';
            case TascaPrioritatEnum.ALTA:
                return 'expand_less_outlined';
            case TascaPrioritatEnum.NORMAL:
                return 'drag_handle';
            case TascaPrioritatEnum.BAIXA:
                return 'expand_more_outlined';
            case TascaPrioritatEnum.NONE:
            default:
                return 'keyboard_double_arrow_down';
        }
    };

    const getLabel = (prioritat: TascaPrioritatEnum) => {
        const text = prioritat.toLowerCase();
        return text.charAt(0).toUpperCase() + text.slice(1);
    };

    const getTooltip = (prioritat: TascaPrioritatEnum) => {
        switch (prioritat) {
            case TascaPrioritatEnum.MAXIMA:
                return t($ => $.page.tasques.grid.column.prioritat.tooltip.MAXIMA);
            case TascaPrioritatEnum.ALTA:
                return t($ => $.page.tasques.grid.column.prioritat.tooltip.ALTA);
            case TascaPrioritatEnum.NORMAL:
                return t($ => $.page.tasques.grid.column.prioritat.tooltip.NORMAL);
            case TascaPrioritatEnum.BAIXA:
                return t($ => $.page.tasques.grid.column.prioritat.tooltip.BAIXA);
            case TascaPrioritatEnum.NONE:
                return t($ => $.page.tasques.grid.column.prioritat.tooltip.NONE);
            default:
                return '';
        }
    };

    const getIconColor = (prioritat: TascaPrioritatEnum) => {
        return prioritat === TascaPrioritatEnum.NORMAL ? 'inherit' : 'white !important';
    };

    if (!prioritat) return null;

    return (
        <SalutChip
            label={getLabel(prioritat)}
            backgroundColor={prioritat === TascaPrioritatEnum.NORMAL ? undefined : getColorByTascaPrioritat(prioritat)}
            textColor={prioritat === TascaPrioritatEnum.NORMAL ? 'inherit' : undefined}
            icon={<Icon sx={{ color: getIconColor(prioritat) }}>{getIcon(prioritat)}</Icon>}
            tooltip={getTooltip(prioritat)}
        />
    );
};

const TascaEstatChip = (props: { estat?: TascaEstatEnum }) => {
    const { estat } = props;
    const { t } = useTranslation();
    const getColorByTascaEstat = useGetColorByTascaEstat();

    const getIcon = (estat: TascaEstatEnum) => {
        switch (estat) {
            case TascaEstatEnum.PENDENT:
                return 'schedule';
            case TascaEstatEnum.INICIADA:
                return 'play_circle_outline';
            case TascaEstatEnum.FINALITZADA:
                return 'check_circle_outline';
            case TascaEstatEnum.CANCELADA:
                return 'cancel_outlined';
            case TascaEstatEnum.ERROR:
                return 'error_outline';
            default:
                return 'help_outline';
        }
    };

    const getLabel = (estat: TascaEstatEnum) => {
        const text = estat.toLowerCase();
        return text.charAt(0).toUpperCase() + text.slice(1);
    };

    const getTooltip = (estat: TascaEstatEnum) => {
        switch (estat) {
            case TascaEstatEnum.PENDENT:
                return t($ => $.page.tasques.grid.column.estat.tooltip.PENDENT);
            case TascaEstatEnum.INICIADA:
                return t($ => $.page.tasques.grid.column.estat.tooltip.INICIADA);
            case TascaEstatEnum.FINALITZADA:
                return t($ => $.page.tasques.grid.column.estat.tooltip.FINALITZADA);
            case TascaEstatEnum.CANCELADA:
                return t($ => $.page.tasques.grid.column.estat.tooltip.CANCELADA);
            case TascaEstatEnum.ERROR:
                return t($ => $.page.tasques.grid.column.estat.tooltip.ERROR);
            default:
                return '';
        }
    };

    if (!estat) return null;

    return (
        <SalutChip
            label={getLabel(estat)}
            backgroundColor={getColorByTascaEstat(estat)}
            icon={<Icon sx={{ color: 'white !important' }}>{getIcon(estat)}</Icon>}
            tooltip={getTooltip(estat)}
        />
    );
};


const TascaDataCaducitatChip = (props: { row: any, formattedValue: any }) => {
    const { row, formattedValue } = props;
    const { t } = useTranslation();
    const backgroundColor =
        row?.diesPerCaducar == null
            ? 'gray'
            : row?.diesPerCaducar <= 0
                ? '#6b0707'
                : row?.diesPerCaducar <= 3
                    ? 'error.main'
                    : row?.diesPerCaducar <= 5
                        ? 'warning.main'
                        : 'success.main';

    if (!formattedValue) return null;

    return (
        <SalutChip
            label={formattedValue}
            backgroundColor={backgroundColor}
            // icon={<Icon sx={{ color: 'white !important' }}>schedule</Icon>}
            tooltip={t($ => $.page.tasques.grid.column.dataCaducitat.tooltip)}
        />
    );
};

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
                justifyContent: 'space-between',
                flexDirection: { xs: 'column', sm: 'row' },
                alignItems: { xs: 'stretch', sm: 'center' },
                gap: { xs: 1, sm: 0 },
            }}>
                <Grid container spacing={1} sx={{ flexGrow: 1, mr: 1 }}>
                    <Grid size={6}><FormField name={'app'} /></Grid>
                    <Grid size={6}><FormField name={'entorn'} /></Grid>
                </Grid>
                <Box sx={{
                        display: 'flex',
                        justifyContent: 'flex-end',
                        flexWrap: 'wrap',
                        width: { xs: '100%', sm: 'auto' },
                        mt: { xs: 1, sm: 0 },
                }}>
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
                springFilterBuilder.like('numeroExpedient', data?.numeroExpedient),
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
                <Grid size={{ xs: 12,  md:6, lg: 3}}><FormField name="nom" /></Grid>
                <Grid size={{ xs: 12, sm: 6, md:3}}><FormField name="descripcio" /></Grid>
                <Grid size={{ xs: 12, sm: 6, md:3, lg: 2}}><FormField name="numeroExpedient" /></Grid>
                <Grid size={{ xs: 12, sm: 6, md:3, lg: 2}}><FormField name="tipus" /></Grid>
                <Grid size={{ xs: 12, sm: 6, md:3, lg: 2}}><FormField name="prioritat" /></Grid>
                <Grid size={{ xs: 12, sm: 6, md:3, lg: 2 }}><FormField name="dataInici1" /></Grid>
                <Grid size={{ xs: 12, sm: 6, md:3, lg: 2 }}><FormField name="dataInici2" /></Grid>
                <Grid size={{ xs: 12, sm: 6, md:3, lg: 2 }}><FormField name="dataFi1" /></Grid>
                <Grid size={{ xs: 12, sm: 6, md:3, lg: 2 }}><FormField name="dataFi2" /></Grid>
                <Grid size={{ xs: 12, sm: 6, md:3, lg: 2 }}><FormField name="dataCaducitat1" /></Grid>
                <Grid size={{ xs: 12, sm: 6, md:3, lg: 2 }}><FormField name="dataCaducitat2" /></Grid>
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
        field: 'numeroExpedient',
        flex: 1,
    },
    {
        field: 'estat',
        flex: 0.8,
        minWidth: 100,
        renderCell: (param) => {
            return <TascaEstatChip estat={param?.row?.estat} />;
        }
    },
    {
        field: 'tipus',
        flex: 1,
        minWidth: 100,
    },
    {
        field: 'responsable',
        flex: 1,
    },
    {
        field: 'prioritat',
        flex: 0.8,
        minWidth: 100,
        renderCell: (param) => {
            return <TascaPrioritatChip prioritat={param?.row?.prioritat} />;
        }
    },
    {
        field: 'dataInici',
        flex: 0.7,
        minWidth: 100,
        valueFormatter: (value) => value ? dayjs(value).format('DD/MM/YYYY') : value,
    },
    {
        field: 'dataCaducitat',
        flex: 0.8,
        minWidth: 100,
        valueFormatter: (value) => (value ? dayjs(value).format('DD/MM/YYYY') : value),
        renderCell: (param) => {
            return <TascaDataCaducitatChip row={param.row} formattedValue={param.formattedValue} />;
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

const INVALID_ENTORNAPP = "INVALID_ENTORNAPP";

const Tasca = () => {
    const { t } = useTranslation();
    const { t: tLib } = useBaseAppContext();
    const { currentRole } = useUserContext();
    const [filter, setFilter] = React.useState<string>();
    const gridApiRef = useGridApiRef();
    const isAdmin = currentRole === ROLE_ADMIN;
    const treePathFormatInvalidEntornApp = (invalidPath: string) =>
        t($ => $.page.tasques.grid.entornAppInvalid) + ` [ID: ${invalidPath.split(' ')[1]}]`;
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
        { valueFormatter: (value: any, row: any) => row?.id ? row?.nom : value?.startsWith?.(INVALID_ENTORNAPP) ? treePathFormatInvalidEntornApp(value) : value });
    const columns = [
        ...(!treeView
            ? [
                  { field: 'nom', flex: 1 },
                  {
                      field: 'treePath',
                      flex: 1.2,
                      headerName: t($ => $.page.tasques.grid.column.appEntorn),
                      valueFormatter: (value: any) =>
                          value?.[0].startsWith(INVALID_ENTORNAPP)
                              ? treePathFormatInvalidEntornApp(value[0])
                              : `${value?.[0]} - ${value?.[1]}`,
                  },
              ]
            : []),
        ...(filter?.includes('dataFi is null')
            ? dataGridCommonColumns.slice(0, -1)
            : dataGridCommonColumns),
    ];
    const rowAdditionalActions = React.useMemo(() => {
        const additionalActions: DataCommonAdditionalAction[] = [{
            icon: 'open_in_new',
            label: t($ => $.page.tasques.grid.action.obrir),
            showInMenu: false,
            linkTo: (row: any) => row?.url,
            linkTarget: '_blank',
            disabled: (row: any) => !row?.url,
            hidden: (row: any) => !row?.id,
        }];
        if (isAdmin) {
            additionalActions.push({
                label: tLib('datacommon.delete.label'),
                icon: 'delete',
                clickTriggerDelete: true,
                hidden(row) {return !row?.id;},
            });
        }
        return additionalActions;
    }, [isAdmin, tLib])
    const filterElement = <TascaFilter onSpringFilterChange={setFilter}/>;

    return (
        <Box sx={{ height: '100%' }}>
            <PageTitle title={t($ => $.menu.tasca)} />
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
                rowAdditionalActions={rowAdditionalActions}
                {...treeDataGridProps}
            />
        </Box>
    );
}

export default Tasca;
