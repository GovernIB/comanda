import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { describe, expect, it, vi } from 'vitest';
import { WidgetHeader, WidgetFooter, WidgetErrorDisplay, WidgetContainer } from './WidgetLayout';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any) =>
        selector({
            common: {
                error: 'Error',
            },
            page: {
                widget: {
                    noErrorTrace: "No hi ha traça de l'error disponible",
                },
            },
        })
    ),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('./WidgetEstils', () => ({
    default: {
        titleContainer: {},
        titleText: { fontSize: '16px' },
        entornCodi: {},
        iconContainer: {},
        footerContainer: {},
        descText: (color: string) => ({ color, fontSize: '14px' }),
        percText: (color: string) => ({ color, fontSize: '14px' }),
        errorAccordion: {},
        errorSummary: () => ({}),
        errorDetails: () => ({}),
        errorIcon: () => ({}),
        paperContainer: () => ({}),
    },
}));

vi.mock('../../util/colorUtil', () => ({
    createTransparentColor: vi.fn((color: string, opacity: number) => `${color}_${opacity}`),
}));

const renderComponent = (ui: React.ReactElement) =>
    render(<ThemeProvider theme={createTheme()}>{ui}</ThemeProvider>);

describe('WidgetHeader', () => {
    const defaultProps = {
        isWhiteBackground: true,
        backgroundColor: '#FFFFFF',
        voraColor: '#CCCCCC',
        contrastTextColor: '#000000',
    };

    it('WidgetHeader_quanNoEstaCarregant_mostraElTitol', () => {
        renderComponent(
            <WidgetHeader
                titol="Títol del widget"
                loading={false}
                {...defaultProps}
            />
        );

        expect(screen.getByText('Títol del widget')).toBeInTheDocument();
    });

    it('WidgetHeader_quanEstaCarregant_mostraSkeletons', () => {
        renderComponent(
            <WidgetHeader
                titol="Títol del widget"
                loading={true}
                {...defaultProps}
            />
        );

        expect(screen.queryByText('Títol del widget')).not.toBeInTheDocument();
    });

    it('WidgetHeader_quanEntornCodiEsDiferentDeDashboardEntornCodi_mostraElChip', () => {
        renderComponent(
            <WidgetHeader
                titol="Títol"
                entornCodi="PRO"
                dashboardEntornCodi="DEV"
                loading={false}
                {...defaultProps}
            />
        );

        expect(screen.getByText('PRO')).toBeInTheDocument();
    });

    it('WidgetHeader_quanEntornCodiEsIgualADashboardEntornCodi_noMostraElChip', () => {
        renderComponent(
            <WidgetHeader
                titol="Títol"
                entornCodi="PRO"
                dashboardEntornCodi="PRO"
                loading={false}
                {...defaultProps}
            />
        );

        expect(screen.queryByText('PRO')).not.toBeInTheDocument();
    });

    it('WidgetHeader_quanNoHiHaEntornCodi_noMostraElChip', () => {
        renderComponent(
            <WidgetHeader
                titol="Títol"
                loading={false}
                {...defaultProps}
            />
        );

        expect(screen.queryByText('PRO')).not.toBeInTheDocument();
    });

    it('WidgetHeader_quanTeMidaFontTitolPersonalitzada_aplicaLaMida', () => {
        renderComponent(
            <WidgetHeader
                titol="Títol gran"
                midaFontTitol={24}
                loading={false}
                {...defaultProps}
            />
        );

        const titulo = screen.getByText('Títol gran');
        expect(titulo).toHaveStyle({ fontSize: '24px' });
    });
});

describe('WidgetFooter', () => {
    it('WidgetFooter_quanNoEstaCarregant_mostraDescripcioICanviPercentual', () => {
        renderComponent(
            <WidgetFooter
                descripcio="Descripció del widget"
                canviPercentual="12.5"
                textColor="#000000"
                loading={false}
            />
        );

        expect(screen.getByText('Descripció del widget')).toBeInTheDocument();
        expect(screen.getByText('12.5%')).toBeInTheDocument();
    });

    it('WidgetFooter_quanEstaCarregant_mostraSkeletons', () => {
        renderComponent(
            <WidgetFooter
                descripcio="Descripció"
                canviPercentual="10"
                textColor="#000000"
                loading={true}
            />
        );

        expect(screen.queryByText('Descripció')).not.toBeInTheDocument();
        expect(screen.queryByText('10%')).not.toBeInTheDocument();
    });

    it('WidgetFooter_quanNoHiHaDescripcio_noLaRenderitza', () => {
        renderComponent(
            <WidgetFooter
                canviPercentual="5"
                textColor="#000000"
                loading={false}
            />
        );

        expect(screen.getByText('5%')).toBeInTheDocument();
    });

    it('WidgetFooter_quanNoHiHaCanviPercentual_noElRenderitza', () => {
        renderComponent(
            <WidgetFooter
                descripcio="Només descripció"
                textColor="#000000"
                loading={false}
            />
        );

        expect(screen.getByText('Només descripció')).toBeInTheDocument();
    });

    it('WidgetFooter_quanTeMidaFontPersonalitzada_aplicaLaMida', () => {
        renderComponent(
            <WidgetFooter
                descripcio="Descripció"
                canviPercentual="10"
                textColor="#000000"
                midaFontDescripcio={16}
                midaFontCanviPercentual={18}
                loading={false}
            />
        );

        const descripcio = screen.getByText('Descripció');
        const canvi = screen.getByText('10%');
        expect(descripcio).toHaveStyle({ fontSize: '16px' });
        expect(canvi).toHaveStyle({ fontSize: '18px' });
    });
});

describe('WidgetErrorDisplay', () => {
    it('WidgetErrorDisplay_quanHiHaError_mostraElMissatgeIExpandible', () => {
        renderComponent(
            <WidgetErrorDisplay
                errorMsg="Error de connexió"
                errorTrace="Stack trace detallat"
            />
        );

        expect(screen.getByText('Error de connexió')).toBeInTheDocument();
    });

    it('WidgetErrorDisplay_quanNoHiHaErrorMsg_mostraTextPerDefecte', () => {
        renderComponent(<WidgetErrorDisplay />);

        expect(screen.getByText('Error')).toBeInTheDocument();
    });

    it('WidgetErrorDisplay_quanNoHiHaErrorTrace_mostraMissatgePerDefecte', () => {
        renderComponent(
            <WidgetErrorDisplay errorMsg="Error sense trace" />
        );

        expect(screen.getByText('Error sense trace')).toBeInTheDocument();
    });
});

describe('WidgetContainer', () => {
    const defaultProps = {
        bgColor: '#FFFFFF',
        bg: 'none',
        textColor: '#000000',
        voraAmple: 1,
        voraColor: '#CCCCCC',
    };

    it('WidgetContainer_renderitzaElsChildren', () => {
        renderComponent(
            <WidgetContainer {...defaultProps}>
                <div>Contingut del widget</div>
            </WidgetContainer>
        );

        expect(screen.getByText('Contingut del widget')).toBeInTheDocument();
    });

    it('WidgetContainer_quanRepOnClick_invocaElCallbackEnClicar', () => {
        const onClick = vi.fn();
        renderComponent(
            <WidgetContainer {...defaultProps} onClick={onClick}>
                <div>Contingut clicable</div>
            </WidgetContainer>
        );

        fireEvent.click(screen.getByText('Contingut clicable'));

        expect(onClick).toHaveBeenCalledTimes(1);
    });

    it('WidgetContainer_quanNoRepOnClick_noTeComportamentClicable', () => {
        renderComponent(
            <WidgetContainer {...defaultProps}>
                <div>Contingut no clicable</div>
            </WidgetContainer>
        );

        expect(screen.getByText('Contingut no clicable')).toBeInTheDocument();
    });

    it('WidgetContainer_quanMostrarVoraEsTrue_aplicaEstilsDeVora', () => {
        renderComponent(
            <WidgetContainer {...defaultProps} mostrarVora={true}>
                <div>Amb vora</div>
            </WidgetContainer>
        );

        expect(screen.getByText('Amb vora')).toBeInTheDocument();
    });
});
