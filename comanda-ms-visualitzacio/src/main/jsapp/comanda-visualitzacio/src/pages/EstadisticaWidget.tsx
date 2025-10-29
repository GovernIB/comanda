import * as React from 'react';
import { useTranslation } from 'react-i18next';
import {
    GridPage,
    MuiGrid,
} from 'reactlib';
import { Box, Tab, Tabs } from '@mui/material';
import EstadisticaSimpleWidgetForm  from "../components/estadistiques/EstadisticaSimpleWidgetForm";
import EstadisticaGraficWidgetForm  from "../components/estadistiques/EstadisticaGraficWidgetForm";
import EstadisticaTaulaWidgetForm  from "../components/estadistiques/EstadisticaTaulaWidgetForm";

const columns = [
    {
        field: 'aplicacio',
        flex: 1,
    },
    {
        field: 'titol',
        flex: 1,
    },
    {
        field: 'descripcio',
        flex: 3,
    },
];

const defaultPopupEditFormDialogComponentProps = { fullWidth: true, maxWidth: 'xl' };

const EstadisticaSimpleWidgetGrid: React.FC = () => {
    const { t } = useTranslation();
    return (
        <MuiGrid
            title={t($ => $.page.widget.simple.title)}
            resourceName="estadisticaSimpleWidget"
            columns={columns}
            toolbarType="upper"
            paginationActive
            popupEditActive
            popupEditFormContent={<EstadisticaSimpleWidgetForm />}
            popupEditFormDialogResourceTitle={t($ => $.page.widget.simple.resourceTitle)}
            popupEditFormDialogComponentProps={defaultPopupEditFormDialogComponentProps}
        />
    );
};

const EstadisticaGraficWidgetGrid: React.FC = () => {
    const { t } = useTranslation();
    return (
        <MuiGrid
            title={t($ => $.page.widget.grafic.title)}
            resourceName="estadisticaGraficWidget"
            columns={columns}
            toolbarType="upper"
            paginationActive
            popupEditActive
            popupEditFormContent={<EstadisticaGraficWidgetForm/>}
            popupEditFormDialogResourceTitle={t($ => $.page.widget.grafic.resourceTitle)}
                popupEditFormDialogComponentProps={defaultPopupEditFormDialogComponentProps}
        />
    );
}

const EstadisticaTaulaWidgetGrid: React.FC = () => {
    const { t } = useTranslation();
    return (
        <MuiGrid
            title={t($ => $.page.widget.taula.title)}
            resourceName="estadisticaTaulaWidget"
            columns={columns}
            toolbarType="upper"
            paginationActive
            popupEditActive
            popupEditFormContent={<EstadisticaTaulaWidgetForm />}
            popupEditFormDialogResourceTitle={t($ => $.page.widget.taula.resourceTitle)}
            popupEditFormDialogComponentProps={defaultPopupEditFormDialogComponentProps}
        />
    );
};

const EstadisticaWidget: React.FC = () => {
    const { t } = useTranslation();
    const [tab, setTab] = React.useState(0);
    const handleChange = (_event: React.SyntheticEvent, newValue: number) => {
        setTab(newValue);
    };
    return (
        <GridPage>
            <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
                <Tabs value={tab} onChange={handleChange}>
                    <Tab label={t($ => $.page.widget.simple.tab.title)} />
                    <Tab label={t($ => $.page.widget.grafic.tab.title)} />
                    <Tab label={t($ => $.page.widget.taula.tab.title)} />
                </Tabs>
            </Box>
            {tab === 0 && (<EstadisticaSimpleWidgetGrid />)}
            {tab === 1 && (<EstadisticaGraficWidgetGrid />)}
            {tab === 2 && (<EstadisticaTaulaWidgetGrid />)}
        </GridPage>
    );
};

export default EstadisticaWidget;
