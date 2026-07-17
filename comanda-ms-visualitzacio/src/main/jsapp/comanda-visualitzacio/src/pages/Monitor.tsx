import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import {
    MuiDataGrid,
    MuiFilter,
    FormField,
    dateFormatLocale,
    useMuiContentDialog,
    useCloseDialogButtons,
    useFilterApiRef,
    springFilterBuilder as builder,
    MuiDataGridColDef,
    useAuthContext,
    useBaseAppContext,
} from 'reactlib';
import { GridRenderCellParams } from '@mui/x-data-grid';
import { ContentDetail } from '../components/ContentDetail';
import { StacktraceBlock } from '../components/RickTextDetail';
import { Tabs, Tab, Chip, Box, Icon, IconButton, Button, CircularProgress } from '@mui/material';
import useTranslationStringKey from '../hooks/useTranslationStringKey';
import PageTitle from '../components/PageTitle.tsx';

const moduleOptions = [
    { value: 'SALUT', labelKey: 'page.monitors.modulEnum.salut' },
    { value: 'ESTADISTICA', labelKey: 'page.monitors.modulEnum.estadistica' },
    { value: 'CONFIGURACIO', labelKey: 'page.monitors.modulEnum.configuracio' },
    { value: 'ALARMES', labelKey: 'page.monitors.tab.alarmes', fixedFilter: `modul:'ALARMES' and tipus:'INTERNA'` },
    { value: 'EMAIL', labelKey: 'page.monitors.tab.email', fixedFilter: `modul:'ALARMES' and tipus:'SORTIDA'` },
    { value: 'TASCA', labelKey: 'page.monitors.modulEnum.tasca' },
    { value: 'AVIS', labelKey: 'page.monitors.modulEnum.avis' },
    { value: 'USUARIS', labelKey: 'page.monitors.modulEnum.usuaris' },
];

type TabMonitorProps = {
    selectedModule: string,
    handleTabChange: (event: React.SyntheticEvent, value: any) => void
}

const TabMonitor: React.FC<TabMonitorProps> = (props) => {
    const { t } = useTranslationStringKey();
    const { selectedModule, handleTabChange } = props;
    return (
        <Tabs
            value={selectedModule}
            onChange={handleTabChange}
            textColor="primary"
            indicatorColor="primary"
            sx={{ mb: 2 }} >
            {moduleOptions.map((option) => (
                <Tab key={option.value} label={t(option.labelKey)} value={option.value} />
            ))}
        </Tabs>
    );
}

export const translateEnumValue = (
  value: string | undefined,
  translationMap?: Record<string, string>,
  t?: (key: string) => string
): string => {
  if (!value) return '';
  if (translationMap && t && translationMap[value]) {
    return t(translationMap[value]);
  }
  return value;
};
const estatTranslationMap: Record<string, string> = {
  OK: 'page.monitors.detail.estatEnum.ok',
  ERROR: 'page.monitors.detail.estatEnum.error',
  WARN: 'page.monitors.detail.estatEnum.warn',
};
const tipusTranslationMap: Record<string, string> = {
  SORTIDA: 'page.monitors.detail.tipusEnum.sortida',
  ENTRADA: 'page.monitors.detail.tipusEnum.entrada',
  INTERNA: 'page.monitors.detail.tipusEnum.interna',
};

const EstatBadge: React.FC<{ value: string, children?: string, }> = ({ value, children }) => {
  const { t } = useTranslationStringKey();
  const colorMap: Record<string, 'success' | 'error' | 'warning' | 'default'> = {
    OK: 'success',
    ERROR: 'error',
    WARN: 'warning',
  };
  const color = colorMap[value] ?? 'default';
  const label = children ?? translateEnumValue(value, estatTranslationMap, t);
  return <Chip label={label} color={color} title={label} size="small" />;
};

type MonitorDetailsProps = {
    data: any;
    selectedModule: string;
};

const MonitorDetails: React.FC<MonitorDetailsProps> = (props) => {
    const { data, selectedModule } = props;
    const { t } = useTranslation();
    const { t: tStringKey } = useTranslationStringKey();
    const { getToken } = useAuthContext();
    const { temporalMessageShow } = useBaseAppContext();
    const [retrying, setRetrying] = React.useState(false);

    const isNetejaError = data?.operacio === 'netejaEntornApp' && data?.estat === 'ERROR';

    const handleReintent = async () => {
        setRetrying(true);
        try {
            const token = getToken?.();
            const response = await fetch(`/api/v1/monitors/${data.id}/reintentarNeteja`, {
                method: 'POST',
                headers: token ? { 'Authorization': 'Bearer ' + token } : undefined,
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            temporalMessageShow?.(null, t($ => $.page.monitors.detail.netejaEntornApp.reintentarSuccess), 'success');
        } catch (err) {
            temporalMessageShow?.(t($ => $.page.monitors.detail.netejaEntornApp.reintentarError), String(err), 'error');
        } finally {
            setRetrying(false);
        }
    };

    const elementsDetail = [
        { label: t($ => $.page.monitors.detail.app), value: data?.app?.description },
        { label: t($ => $.page.monitors.detail.entorn), value: data?.entorn?.description },
        { label: t($ => $.page.monitors.detail.data), value: dateFormatLocale(data?.data, true) },
        { label: t($ => $.page.monitors.detail.operacio), value: data?.operacio },
        {
            label: t($ => $.page.monitors.detail.tipus),
            value: translateEnumValue(data?.tipus, tipusTranslationMap, tStringKey),
        },
        {
            label: t($ => $.page.monitors.detail.estat),
            contentValue: <EstatBadge value={data?.estat} />,
        },
        { label: t($ => $.page.monitors.detail.codiUsuari), value: data?.codiUsuari },
        { label: t($ => $.page.monitors.column.destinatari), value: selectedModule === 'ALARMES' ? data?.url : undefined },
        { label: t($ => $.page.monitors.column.mailAddress), value: selectedModule === 'EMAIL' ? data?.url : undefined },
        { label: t($ => $.page.monitors.detail.errorDescripcio), value: data?.errorDescripcio },
        { label: t($ => $.page.monitors.detail.excepcioMessage), value: data?.excepcioMessage },
        {
            contentValue: (
                <StacktraceBlock
                    title={t($ => $.page.monitors.detail.excepcioStacktrace)}
                    value={data?.excepcioStacktrace}
                />
            ),
        },
    ];
    return (
        <>
            <ContentDetail title={""} elements={elementsDetail} />
            {isNetejaError && (
                <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
                    <Button
                        variant="contained"
                        color="warning"
                        startIcon={retrying ? <CircularProgress size={16} color="inherit" /> : <Icon>refresh</Icon>}
                        onClick={handleReintent}
                        disabled={retrying}
                    >
                        {t($ => $.page.monitors.detail.netejaEntornApp.reintentarButton)}
                    </Button>
                </Box>
            )}
        </>
    );
}

type MonitorFilterProps = {
    onAppChange?: (appId: number | undefined) => void;
    onEntornChange?: (entornId: number | undefined) => void;
}

const MonitorFilter: React.FC<MonitorFilterProps> = ({ onAppChange, onEntornChange }) => {
    const { t } = useTranslation();
    const [moreFields, setMoreFields] = React.useState<boolean>(false);
    const entornAppFilterApiRef = useFilterApiRef();
    const monitorFilterApiRef = useFilterApiRef();
    const clear = () => {
        entornAppFilterApiRef.current?.clear();
        monitorFilterApiRef.current?.clear();
        setTimeout(() => {
            onAppChange?.(undefined);
            onEntornChange?.(undefined);
        }, 0);
    };
    const entornAppSpringFilterBuilder = (data: any): string => {
        const appId = data?.app?.id;
        const entornId = data?.entorn?.id;
        monitorFilterApiRef.current?.setFieldValue('appId', appId);
        monitorFilterApiRef.current?.setFieldValue('entornId', entornId);
        onAppChange?.(appId);
        onEntornChange?.(entornId);
        return '';
    };
    const monitorSpringFilterBuilder = (data: any): string => {
        return builder.and(
            builder.like("codiUsuari", data?.codi),
            builder.between("data", `'${data?.dataDesde}'`, `'${data?.dataFins}'`),
            data?.tipus && builder.eq("tipus", `'${data?.tipus}'`),
            data?.estat && builder.eq("estat", `'${data?.estat}'`),
            builder.like("operacio", data?.descripcio),
        );
    };

    return (
        <><MuiFilter
            resourceName="entornApp"
            code="optional_entornApp_filter"
            springFilterBuilder={entornAppSpringFilterBuilder}
            apiRef={entornAppFilterApiRef}
            commonFieldComponentProps={{ size: 'small' }}
            componentProps={{ sx: { mb: moreFields ? 1 : 2 } }} >
            <Box sx={{
                display: 'flex',
                justifyContent: 'space-between',
                flexDirection: { xs: 'column', sm: 'row' },
                alignItems: { xs: 'stretch', sm: 'center' },
                gap: { xs: 1, sm: 0 },
            }}>
                <Grid container spacing={1} sx={{ flexGrow: 1, mr: 1 }}>
                    <Grid size={{ xs: 12, sm: 6 }}><FormField name={'app'}
                        advancedSearchColumns={[{ field: 'codi', flex: 1, }, { field: 'nom', flex: 2, },]}/></Grid>
                    <Grid size={{ xs: 12, sm: 6 }}><FormField name={'entorn'}
                        advancedSearchColumns={[{ field: 'codi', flex: 1, }, { field: 'nom', flex: 2, },]}/></Grid>
                </Grid>
                <Box sx={{
                    display: 'flex',
                    justifyContent: 'flex-end',
                    flexWrap: 'wrap',
                    width: { xs: '100%', sm: 'auto' },
                    mt: { xs: 1, sm: 0 },
                }}>
                    <IconButton
                        onClick={clear}
                        title={t($ => $.components.clear)}
                        sx={{ mr: 1 }}>
                        <Icon>filter_alt_off</Icon>
                    </IconButton>
                    <IconButton
                        onClick={() => setMoreFields((mf) => !mf)}
                        title={t($ => $.page.monitors.filter.more)}>
                        <Icon>filter_list</Icon>
                    </IconButton>
                </Box>
            </Box>
        </MuiFilter>
        <MuiFilter
            resourceName="monitor"
            code="FILTER"
            springFilterBuilder={monitorSpringFilterBuilder}
            apiRef={monitorFilterApiRef}
            commonFieldComponentProps={{ size: 'small' }}
            componentProps={{
                sx: { mb: 1, display: moreFields ? 'block' : 'none' }
            }} >
            <Grid container columnSpacing={1} rowSpacing={1}>
                <Grid size={{xs: 12, sm: 4, lg:2}}><FormField name="codi" /></Grid>
                <Grid size={{xs: 12, sm: 4, lg:2}}><FormField name="dataDesde" /></Grid>
                <Grid size={{xs: 12, sm: 4, lg:2}}><FormField name="dataFins" /></Grid>
                <Grid size={{xs: 12, sm: 4, lg:2}}><FormField name="descripcio" /></Grid>
                <Grid size={{xs: 12, sm: 4, lg:2}}><FormField name="tipus" /></Grid>
                <Grid size={{xs: 12, sm: 4, lg:2}}><FormField name="estat" /></Grid>
            </Grid>
        </MuiFilter></>
    );
};

const dataGridPerspectives = ['ENTORN_APP'];

const Monitors: React.FC = () => {
    const { t } = useTranslation();
    const closeDialogButton = useCloseDialogButtons();
    const [selectedModule, setSelectedModule] = React.useState<string>('SALUT');
    const [detailDialogShow, detailDialogComponent] = useMuiContentDialog(closeDialogButton);
    const showDetail = (data: any) => {
        detailDialogShow(
            t($ => $.page.monitors.detail.title),
            <MonitorDetails data={data} selectedModule={selectedModule} />,
            closeDialogButton,
            { maxWidth: 'lg', fullWidth: true, }
        );
    }
    const [filterAppId, setFilterAppId] = React.useState<number | undefined>();
    const [filterEntornId, setFilterEntornId] = React.useState<number | undefined>();
    const namedQueries = React.useMemo(() => {
        const queries: string[] = [];
        if (filterAppId) queries.push(`filterByApp:${filterAppId}`);
        if (filterEntornId) queries.push(`filterByEntorn:${filterEntornId}`);
        return queries.length > 0 ? queries : undefined;
    }, [filterAppId, filterEntornId]);
    const handleFilterAppChange = (appId: number | undefined) => setFilterAppId(appId);
    const handleFilterEntornChange = (entornId: number | undefined) => setFilterEntornId(entornId);
    const handleTabChange = (_event: React.SyntheticEvent, newValue: string) => {
        setSelectedModule(newValue);
    };
    const currentModuleOption = moduleOptions.find(o => o.value === selectedModule);
    const currentFixedFilter = currentModuleOption?.fixedFilter ?? `modul:'${selectedModule}'`;
    const baseColumns = React.useMemo<MuiDataGridColDef[]>(() => [
        { field: 'app', flex: 1, sortable: false, valueFormatter: (value?: any) => value?.description },
        { field: 'entorn', flex: 1.5, sortable: false, valueFormatter: (value?: any) => value?.description },
        { field: 'data', flex: 1, minWidth: 150 },
        { field: 'operacio', flex: 2, },
        { field: 'tipus', flex: 1, },
        {
            field: 'url',
            flex: 2,
            headerName: selectedModule === 'ALARMES'
                ? t($ => $.page.monitors.column.destinatari)
                : selectedModule === 'EMAIL'
                    ? t($ => $.page.monitors.column.mailAddress)
                    : selectedModule === 'USUARIS'
                        ? t($ => $.page.monitors.column.rolOUsuari)
                        : 'URL',
        },
        { field: 'modul', flex: 1 },
        { field: 'tempsResposta', flex: 1.4 },
        {
            field: 'estat',
            flex: 1,
            renderCell: (params: GridRenderCellParams) => (
                <EstatBadge value={params.value}>{params.formattedValue}</EstatBadge>
            ),
        },
    ], [selectedModule, t]);
    const columns = React.useMemo(() => {
        let filteredColumns = [...baseColumns];
        if (filterAppId) {
            filteredColumns = filteredColumns.filter(col => col.field !== 'app');
        }
        if (filterEntornId) {
            filteredColumns = filteredColumns.filter(col => col.field !== 'entorn');
        }
        if (selectedModule === 'TASCA' || selectedModule === 'AVIS') {
            filteredColumns = filteredColumns.filter(col => col.field !== 'url');
        }
        return filteredColumns;
    }, [selectedModule, filterAppId, filterEntornId, baseColumns]);
    return (
        <>
            <PageTitle title={t($ => $.page.monitors.title)} />
            <MuiDataGrid
                title={t($ => $.page.monitors.title)}
                toolbarAdditionalRow={
                    <> <MonitorFilter
                        onAppChange={handleFilterAppChange}
                        onEntornChange={handleFilterEntornChange} />
                    <TabMonitor selectedModule={selectedModule} handleTabChange={handleTabChange} /> </>
                }
                resourceName="monitor"
                columns={columns}
                perspectives={dataGridPerspectives}
                toolbarType="upper"
                paginationActive
                readOnly
                onRowClick={(params) => showDetail(params.row)}
                fixedFilter={currentFixedFilter}
                namedQueries={namedQueries}
            />
            {detailDialogComponent}
        </>
    );
}

export default Monitors;
