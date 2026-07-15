import React from 'react';
import { render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { WidgetPreview } from './WidgetPreview';

const mocks = vi.hoisted(() => ({
    useDashboardPlantillaMock: vi.fn(),
    resolveWidgetStylesMock: vi.fn(),
}));

vi.mock('./dashboardPlantillaHook', () => ({
    useDashboardPlantilla: (id: number) => mocks.useDashboardPlantillaMock(id),
}));

vi.mock('./dashboardStyleResolver', () => ({
    resolveWidgetStyles: (widgetData: any, widgetType: any, plantilla: any, temaFosc: boolean) => 
        mocks.resolveWidgetStylesMock(widgetData, widgetType, plantilla, temaFosc),
}));

vi.mock('./SimpleWidgetVisualization', () => ({
    default: ({ titol }: { titol?: string }) => <div data-testid="simple-widget">Simple: {titol}</div>,
}));

vi.mock('./GraficWidgetVisualization', () => ({
    default: ({ titol }: { titol?: string }) => <div data-testid="grafic-widget">Grafic: {titol}</div>,
}));

vi.mock('./TaulaWidgetVisualization', () => ({
    default: ({ titol }: { titol?: string }) => <div data-testid="taula-widget">Taula: {titol}</div>,
}));

vi.mock('./TitolWidgetVisualization', () => ({
    default: ({ titol }: { titol?: string }) => <div data-testid="titol-widget">Titol: {titol}</div>,
}));

const renderComponent = (ui: React.ReactElement) =>
    render(<ThemeProvider theme={createTheme()}>{ui}</ThemeProvider>);

describe('WidgetPreview', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mocks.useDashboardPlantillaMock.mockReturnValue({
            plantilla: null,
            loading: false,
        });
        mocks.resolveWidgetStylesMock.mockReturnValue({
            colorFons: '#FF0000',
            colorText: '#000000',
        });
    });

    it('WidgetPreview_quanEsRenderitzaAmbTipusSIMPLE_mostraSimpleWidgetVisualization', () => {
        renderComponent(
            <WidgetPreview
                widgetType="SIMPLE"
                widgetData={{ titol: 'Widget Simple', valor: 42 }}
            />
        );

        expect(screen.getByTestId('simple-widget')).toBeInTheDocument();
        expect(screen.getByText('Simple: Widget Simple')).toBeInTheDocument();
    });

    it('WidgetPreview_quanEsRenderitzaAmbTipusGRAFIC_mostraGraficWidgetVisualization', () => {
        renderComponent(
            <WidgetPreview
                widgetType="GRAFIC"
                widgetData={{ titol: 'Widget Grafic', tipusGrafic: 'BAR_CHART' }}
            />
        );

        expect(screen.getByTestId('grafic-widget')).toBeInTheDocument();
        expect(screen.getByText('Grafic: Widget Grafic')).toBeInTheDocument();
    });

    it('WidgetPreview_quanEsRenderitzaAmbTipusTAULA_mostraTaulaWidgetVisualization', () => {
        renderComponent(
            <WidgetPreview
                widgetType="TAULA"
                widgetData={{ titol: 'Widget Taula', files: [] }}
            />
        );

        expect(screen.getByTestId('taula-widget')).toBeInTheDocument();
        expect(screen.getByText('Taula: Widget Taula')).toBeInTheDocument();
    });

    it('WidgetPreview_quanEsRenderitzaAmbTipusTITOL_mostraTitolWidgetVisualization', () => {
        renderComponent(
            <WidgetPreview
                widgetType="TITOL"
                widgetData={{ titol: 'Widget Titol' }}
            />
        );

        expect(screen.getByTestId('titol-widget')).toBeInTheDocument();
        expect(screen.getByText('Titol: Widget Titol')).toBeInTheDocument();
    });

    it('WidgetPreview_quanPlantillaEstaCarregant_mostraCircularProgress', () => {
        mocks.useDashboardPlantillaMock.mockReturnValue({
            plantilla: null,
            loading: true,
        });

        renderComponent(
            <WidgetPreview
                widgetType="SIMPLE"
                widgetData={{ titol: 'Carregant', plantilla: { id: 1 } }}
            />
        );

        expect(screen.queryByTestId('simple-widget')).not.toBeInTheDocument();
    });

    it('WidgetPreview_quanWidgetTePlantilla_proporcionaLaPlantillaDelWidget', () => {
        const widgetPlantilla = { id: 10, nom: 'Plantilla Widget' };
        mocks.useDashboardPlantillaMock.mockReturnValue({
            plantilla: widgetPlantilla,
            loading: false,
        });

        renderComponent(
            <WidgetPreview
                widgetType="SIMPLE"
                widgetData={{ titol: 'Amb Plantilla', plantilla: { id: 10 } }}
                dashboardPlantilla={{ id: 5, nom: 'Plantilla Dashboard' }}
            />
        );

        expect(mocks.resolveWidgetStylesMock).toHaveBeenCalledWith(
            expect.objectContaining({ titol: 'Amb Plantilla' }),
            'SIMPLE',
            widgetPlantilla,
            expect.any(Boolean)
        );
    });

    it('WidgetPreview_quanWidgetNoTePlantilla_usaDashboardPlantilla', () => {
        const dashboardPlantilla = { id: 5, nom: 'Plantilla Dashboard' };
        mocks.useDashboardPlantillaMock.mockReturnValue({
            plantilla: null,
            loading: false,
        });

        renderComponent(
            <WidgetPreview
                widgetType="SIMPLE"
                widgetData={{ titol: 'Sense Plantilla' }}
                dashboardPlantilla={dashboardPlantilla}
            />
        );

        expect(mocks.resolveWidgetStylesMock).toHaveBeenCalledWith(
            expect.objectContaining({ titol: 'Sense Plantilla' }),
            'SIMPLE',
            dashboardPlantilla,
            expect.any(Boolean)
        );
    });

    it('WidgetPreview_quanNoHiHaCapPlantilla_passaUndefinedAResolveWidgetStyles', () => {
        mocks.useDashboardPlantillaMock.mockReturnValue({
            plantilla: null,
            loading: false,
        });

        renderComponent(
            <WidgetPreview
                widgetType="SIMPLE"
                widgetData={{ titol: 'Sense Cap Plantilla' }}
            />
        );

        expect(mocks.resolveWidgetStylesMock).toHaveBeenCalledWith(
            expect.objectContaining({ titol: 'Sense Cap Plantilla' }),
            'SIMPLE',
            undefined,
            false
        );
    });

    it('WidgetPreview_passaPreviewTrueAlWidgetFinal', () => {
        renderComponent(
            <WidgetPreview
                widgetType="SIMPLE"
                widgetData={{ titol: 'Preview Test' }}
            />
        );

        expect(screen.getByTestId('simple-widget')).toBeInTheDocument();
    });

    it('WidgetPreview_combinaEstilsResoltsAmbWidgetData', () => {
        mocks.resolveWidgetStylesMock.mockReturnValue({
            colorFons: '#FF0000',
            midaFontTitol: 24,
        });

        renderComponent(
            <WidgetPreview
                widgetType="SIMPLE"
                widgetData={{ titol: 'Combinat', valor: 100 }}
            />
        );

        expect(mocks.resolveWidgetStylesMock).toHaveBeenCalled();
        expect(screen.getByText('Simple: Combinat')).toBeInTheDocument();
    });
});