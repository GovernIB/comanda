import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import {
    GridPage,
    MuiGrid,
    MuiFilter,
    FormField,
    dateFormatLocale,
    useMuiContentDialog,
    useCloseDialogButtons,
    useFilterApiRef,
    springFilterBuilder as builder,
} from 'reactlib';
import { ContentDetail } from '../components/ContentDetail';
import { StacktraceBlock } from '../components/RickTextDetail';
import { Tabs, Tab, Chip, Box, Button, Icon } from '@mui/material';
import PageTitle from '../components/PageTitle.tsx';

const moduleOptions = [
    { value: 'SALUT', labelKey: 'page.monitors.modulEnum.salut' },
    { value: 'ESTADISTICA', labelKey: 'page.monitors.modulEnum.estadistica' },
    { value: 'CONFIGURACIO', labelKey: 'page.monitors.modulEnum.configuracio' },
];

type TabMonitorProps = {
    selectedModule: string,
    handleTabChange: (event: React.SyntheticEvent, value: any) => void
}

const TabMonitor: React.FC<TabMonitorProps> = (props) => {
    const { t } = useTranslation();
    const { selectedModule, handleTabChange } = props;
    return <Tabs
        value={selectedModule}
        onChange={handleTabChange}
        textColor="primary"
        indicatorColor="primary"
        sx={{ mb: 2 }} >
        {moduleOptions.map((option) => (
            <Tab key={option.value} label={t(option.labelKey)} value={option.value} />
        ))}
    </Tabs>;
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
  const { t } = useTranslation();
  const colorMap: Record<string, 'success' | 'error' | 'warning' | 'default'> = {
    OK: 'success',
    ERROR: 'error',
    WARN: 'warning',
  };
  const color = colorMap[value] ?? 'default';
  const label = children ?? translateEnumValue(value, estatTranslationMap, t);
  return <Chip label={label} color={color} size="small" />;
};

const columns = [
    { field: 'data', flex: 1, },
    { field: 'operacio', flex: 2, },
    { field: 'tipus', flex: 1, },
    { field: 'url', flex: 2, },
    { field: 'modul', flex: 1, },
    { field: 'tempsResposta', flex: 1, },
    {
        field: 'estat',
        flex: 0.5,
        renderCell: (params: any) => <EstatBadge value={params.value}>{params.formattedValue}</EstatBadge>
    },
];

const MonitorDetails: React.FC<any> = (props) => {
    const { data } = props;
    const { t } = useTranslation();
    const elementsDetail = [
        { label: t('page.monitors.detail.data'), value: dateFormatLocale(data?.data, true) },
        { label: t('page.monitors.detail.operacio'), value: data?.operacio },
        { label: t('page.monitors.detail.tipus'), value: translateEnumValue(data?.tipus, tipusTranslationMap, t) },
        { label: t('page.monitors.detail.estat'), contentValue: <EstatBadge value={data?.estat} /> },
        { label: t('page.monitors.detail.codiUsuari'), value: data?.codiUsuari },
        { label: t('page.monitors.detail.errorDescripcio'), value: data?.errorDescripcio },
        { label: t('page.monitors.detail.excepcioMessage'), value: data?.excepcioMessage },
        {
            contentValue: (
                <StacktraceBlock
                    title={t('page.monitors.detail.excepcioStacktrace')}
                    value={data?.excepcioStacktrace}
                />
            )
        },
    ];
    return <ContentDetail title={""} elements={elementsDetail} />;
}

const MonitorFilter: React.FC<any> = () => {
    const { t } = useTranslation();
    const filterRef = useFilterApiRef();
    const clear = () => filterRef.current.clear();
    const filter = () => filterRef.current.filter();
    const springFilterBuilder = (data: any): string => {
        return builder.and(
            builder.like("codiUsuari", data?.codi),
            builder.between("data", `'${data?.dataDesde}'`, `'${data?.dataFins}'`),
            builder.eq("tipus", `'${data?.tipus}'`),
            builder.eq("estat", `'${data?.estat}'`),
            builder.like("operacio", data?.descripcio),
        );
    }

    return <><MuiFilter
        resourceName="monitor"
        code="FILTER"
        springFilterBuilder={springFilterBuilder}
        apiRef={filterRef}
        buttonControlled
        commonFieldComponentProps={{ size: 'small' }}
        componentProps={{ sx: { my: 2 } }}
        >
        <Grid container columnSpacing={1} rowSpacing={1}>
            <Grid size={{xs: 12, sm: 4, lg:2}}><FormField name="codi" /></Grid>
            <Grid size={{xs: 12, sm: 4, lg:2}}><FormField name="dataDesde" /></Grid>
            <Grid size={{xs: 12, sm: 4, lg:2}}><FormField name="dataFins" /></Grid>
            <Grid size={{xs: 12, sm: 4, lg:2}}><FormField name="descripcio" /></Grid>
            <Grid size={{xs: 12, sm: 4, lg:2}}><FormField name="tipus" /></Grid>
            <Grid size={{xs: 12, sm: 4, lg:2}}><FormField name="estat" /></Grid>
            <Grid size={12} sx={{ display: 'flex', justifyContent: 'end' }}>
                <Box sx={{ width: { xs: '100%', sm: 'auto' }, display: 'flex', flexWrap: 'wrap', gap: 1, justifyContent: 'flex-start' }} >
                    <Button variant="outlined" onClick={clear} sx={{ flexBasis: { xs: 'calc(50% - 0.5rem)', sm: 'auto' }, flexGrow: 0, flex: '1 1 auto', minWidth: 'fit-content', borderRadius: 1 }} >
                        <Icon sx={{ mr: 1 }}>clear</Icon>
                        {t('components.clear')}
                    </Button>
                    <Button variant="contained" onClick={filter} sx={{ flexBasis: { xs: 'calc(50% - 0.5rem)', sm: 'auto' }, flexGrow: 0, flex: '1 1 auto', minWidth: 'fit-content', borderRadius: 1 }} >
                        <Icon sx={{ mr: 1 }}>filter_alt</Icon>
                        {t('components.search')}
                    </Button>
                </Box>
            </Grid>
        </Grid>
    </MuiFilter></>
};

const Monitors: React.FC = () => {
    const { t } = useTranslation();
    const closeDialogButton = useCloseDialogButtons();
    const [detailDialogShow, detailDialogComponent] = useMuiContentDialog(closeDialogButton);
    const showDetail = (data: any) => {
        detailDialogShow(
            t('page.monitors.detail.title'),
            <MonitorDetails data={data} />,
            closeDialogButton,
            { maxWidth: 'lg', fullWidth: true, }
        );
    }
    const [selectedModule, setSelectedModule] = React.useState<string>('SALUT');
    const handleTabChange = (_event: React.SyntheticEvent, newValue: string) => {
        setSelectedModule(newValue);
    };
    return <GridPage>
        <PageTitle title={t('page.monitors.title')} />
        <MuiGrid
            title={t('page.monitors.title')}
            toolbarAdditionalRow={
                <> <MonitorFilter></MonitorFilter>
                <TabMonitor selectedModule={selectedModule} handleTabChange={handleTabChange} /> </>
            }
            resourceName="monitor"
            columns={columns}
            toolbarType="upper"
            paginationActive
            readOnly
            onRowClick={(params: any) => showDetail(params.row)}
            staticFilter={`modul:'${selectedModule}'`}
        />
        {detailDialogComponent}
    </GridPage>;
}

export default Monitors;
