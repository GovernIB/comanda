import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import Grid from '@mui/material/Grid2';
import {
    GridPage,
    FormPage,
    MuiGrid,
    MuiForm,
    FormField,
    dateFormatLocale,
    useContentDialog,
} from 'reactlib';
import { ContentDetail } from '../components/ContentDetail';
import { StacktraceBlock } from '../components/RickTextDetail';
import { Tabs, Tab, Chip } from '@mui/material';

const moduleOptions = ['SALUT', 'ESTADISTICA'];

type TabMonitorProps = {
    selectedModule: string,
    handleTabChange: (event: React.SyntheticEvent, value: any) => void
}

const TabMonitor: React.FC<TabMonitorProps> = (props) => {
    const { selectedModule, handleTabChange } = props;
    return <Tabs
        value={selectedModule}
        onChange={handleTabChange}
        textColor="primary"
        indicatorColor="primary"
        sx={{ mb: 2 }} >
        {moduleOptions.map((option) => (
            <Tab key={option} label={option} value={option} />
        ))}
    </Tabs>;
}

type EstatBadgeProps = {
  value: string;
};

const EstatBadge: React.FC<EstatBadgeProps> = ({ value }) => {
  let color: 'success' | 'error' | 'warning' | 'default' = 'default';
  let label = value;

  switch (value) {
    case 'OK':
      color = 'success';
      break;
    case 'ERROR':
      color = 'error';
      break;
    case 'WARN':
      color = 'warning';
      break;
    default:
      color = 'default';
  }

  return <Chip label={label} color={color} size="small" />;
};

const columns = [{
    field: 'data',
    flex: 1,
}, {
    field: 'operacio',
    flex: 2,
}, {
    field: 'tipus',
    flex: 1,
}, {
    field: 'url',
    flex: 2,
}, {
    field: 'modul',
    flex: 1,
}, {
    field: 'tempsResposta',
    flex: 1,
}, {
    field: 'estat',
    flex: 0.5,
    renderCell: (params: any) => <EstatBadge value={params.value} />
},];

const MonitorDetails: React.FC<any> = (props) => {
    const { data } = props;
    const { t } = useTranslation();
    const elementsDetail = [{
        label: t('page.monitors.detail.data'),
        value: dateFormatLocale(data?.data, true)
    }, {
        label: t('page.monitors.detail.operacio'),
        value: data?.operacio
    }, {
        label: t('page.monitors.detail.tipus'),
        value: data?.tipus
    },
    {
        label: t('page.monitors.detail.estat'),
        contentValue: <EstatBadge value={data?.estat} />
    },
    {
        label: t('page.monitors.detail.codiUsuari'),
        value: data?.codiUsuari
    },
    {
        label: t("page.monitors.detail.errorDescripcio"),
        value: data?.errorDescripcio
    },
    {
        label: t("page.monitors.detail.excepcioMessage"),
        value: data?.excepcioMessage
    },
    {
        contentValue: <StacktraceBlock
            title={t("page.monitors.detail.excepcioStacktrace")}
            value={data?.excepcioStacktrace}
        />
    },]
    return <ContentDetail title={""} elements={elementsDetail} />;
}

export const MonitorForm: React.FC = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    return <FormPage>
        <MuiForm
            id={id}
            title={id ? t('page.monitors.update') : t('page.monitors.create')}
            resourceName="monitor"
            goBackLink="/monitor"
            createLink="form/{{id}}">
            <Grid container spacing={2}>
                <Grid size={4}><FormField name="data" /></Grid>
                <Grid size={4}><FormField name="tipus" /></Grid>
                <Grid size={4}><FormField name="modul" /></Grid>
                <Grid size={12}><FormField name="operacio" type="textarea" /></Grid>
                <Grid size={12}><FormField name="url" /></Grid>
                <Grid size={4}><FormField name="tempsResposta" /></Grid>
                <Grid size={4}><FormField name="estat" /></Grid>
                <Grid size={4}><FormField name="codiUsuari" /></Grid>
                <Grid size={6}><FormField name="errorDescripcio" type="textarea" /></Grid>
                <Grid size={6}><FormField name="excepcioMessage" type="textarea" /></Grid>
                <Grid size={6}><FormField name="excepcioStacktrace" type="textarea" /></Grid>
            </Grid>
        </MuiForm>
    </FormPage>;
}

const Monitors: React.FC = () => {
    const { t } = useTranslation();
    const [detailDialogShow, detailDialogComponent] = useContentDialog();
    const showDetail = (data: any) => {
        detailDialogShow(
            t('page.monitors.detail.title'),
            <MonitorDetails data={data} />,
            undefined,
            { maxWidth: 'lg', fullWidth: true, }
        );
    }
    const [selectedModule, setSelectedModule] = React.useState<string>('SALUT');
    const handleTabChange = (event: React.SyntheticEvent, newValue: string) => {
        setSelectedModule(newValue);
    };
    return <GridPage>
        <MuiGrid
            title={t('page.monitors.title')}
            toolbarAdditionalRow={
                <TabMonitor selectedModule={selectedModule} handleTabChange={handleTabChange} />
            }
            resourceName="monitor"
            columns={columns}
            toolbarType="upper"
            paginationActive
            readOnly
            onRowClick={(params: any) => showDetail(params.row)}
            staticFilter={`modul:'${selectedModule}'`}
        // rowDetailLink="/monitor/{{id}}"
        // toolbarCreateLink="form"
        // rowUpdateLink="form/{{id}}"
        />
        {detailDialogComponent}
    </GridPage>;
}

export default Monitors;
