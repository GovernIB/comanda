import * as React from 'react';
import { useTranslation } from 'react-i18next';
import {
    MuiDataGrid,
} from 'reactlib';
import { Box, Tab, Tabs } from '@mui/material';
import EstadisticaSimpleWidgetForm  from "../components/estadistiques/EstadisticaSimpleWidgetForm";
import EstadisticaGraficWidgetForm  from "../components/estadistiques/EstadisticaGraficWidgetForm";
import EstadisticaTaulaWidgetForm  from "../components/estadistiques/EstadisticaTaulaWidgetForm";
import PageTitle from '../components/PageTitle.tsx';
import { ScreenFrame } from '../lot2';
import useScreenGridProps from '../hooks/useScreenGridProps.tsx';
import { screenIcons } from '../util/screenIcons.ts';



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
    const screenGridProps = useScreenGridProps(screenIcons.widget, t($ => $.page.widget.simple.title));
    return (
        <ScreenFrame>
            <MuiDataGrid
                {...screenGridProps}
                resourceName="estadisticaSimpleWidget"
                columns={columns}
                paginationActive
                popupEditActive
                popupEditFormContent={<EstadisticaSimpleWidgetForm />}
                popupEditFormDialogResourceTitle={t($ => $.page.widget.simple.resourceTitle)}
                popupEditFormDialogComponentProps={defaultPopupEditFormDialogComponentProps}
            />
        </ScreenFrame>
    );
};

const EstadisticaGraficWidgetGrid: React.FC = () => {
    const { t } = useTranslation();
    const screenGridProps = useScreenGridProps(screenIcons.widget, t($ => $.page.widget.grafic.title));
    return (
        <ScreenFrame>
            <MuiDataGrid
                {...screenGridProps}
                resourceName="estadisticaGraficWidget"
                columns={columns}
                paginationActive
                popupEditActive
                popupEditFormContent={<EstadisticaGraficWidgetForm/>}
                popupEditFormDialogResourceTitle={t($ => $.page.widget.grafic.resourceTitle)}
                    popupEditFormDialogComponentProps={defaultPopupEditFormDialogComponentProps}
            />
        </ScreenFrame>
    );
}

const EstadisticaTaulaWidgetGrid: React.FC = () => {
    const { t } = useTranslation();
    const screenGridProps = useScreenGridProps(screenIcons.widget, t($ => $.page.widget.taula.title));
    return (
        <ScreenFrame>
            <MuiDataGrid
                {...screenGridProps}
                resourceName="estadisticaTaulaWidget"
                columns={columns}
                paginationActive
                popupEditActive
                popupEditFormContent={<EstadisticaTaulaWidgetForm />}
                popupEditFormDialogResourceTitle={t($ => $.page.widget.taula.resourceTitle)}
                popupEditFormDialogComponentProps={defaultPopupEditFormDialogComponentProps}
            />
        </ScreenFrame>
    );
};

const EstadisticaWidget: React.FC = () => {
    const { t } = useTranslation();
    const [tab, setTab] = React.useState(0);
    const handleChange = (_event: React.SyntheticEvent, newValue: number) => {
        setTab(newValue);
    };
    return (
        <>
            <PageTitle title={t($ => $.page.widget.title)} />
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
        </>
    );
};

export default EstadisticaWidget;
