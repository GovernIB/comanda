import React from 'react';
import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import EstadisticaWidgetFormFields from './EstadisticaWidgetFormFields';

const mocks = vi.hoisted(() => ({
    useFormContextMock: vi.fn(),
    useResourceApiServiceMock: vi.fn(),
    findOptionsMock: vi.fn(),
}));

vi.mock('reactlib', () => ({
    FormField: ({
        name,
        disabled,
    }: {
        name: string;
        disabled?: boolean;
    }) => (
        <div data-testid={`form-field-${name}`} data-disabled={disabled ? 'true' : 'false'}>
            {name}
        </div>
    ),
    useFormContext: () => mocks.useFormContextMock(),
    useResourceApiService: () => mocks.useResourceApiServiceMock(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (_selector: unknown) => 'translated-divider',
    }),
}));

vi.mock('../FormFieldAdvancedSearchFilters.tsx', () => ({
    default: ({ name }: { name: string }) => (
        <div data-testid={`advanced-search-${name}`}>{name}</div>
    ),
}));

vi.mock('../../util/requestUtils.ts', () => ({
    findOptions: (...args: unknown[]) => mocks.findOptionsMock(...args),
}));

const renderComponent = (data: unknown, children: React.ReactNode = <div>Contingut extra</div>) => {
    mocks.useFormContextMock.mockReturnValue({ data });
    return render(<EstadisticaWidgetFormFields>{children}</EstadisticaWidgetFormFields>);
};

describe('EstadisticaWidgetFormFields', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('EstadisticaWidgetFormFields_quanLaplicacioNoEstaLlesta_noRenderitzaRes', () => {
        // Comprova que el formulari espera que el recurs d'aplicacions estigui inicialitzat.
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: false,
            find: vi.fn(),
        });

        const { container } = renderComponent({});

        expect(container).toBeEmptyDOMElement();
    });

    it('EstadisticaWidgetFormFields_quanNoHiHaAplicacio_mostraNomesElsCampsGenerals', () => {
        // Verifica que sense aplicació seleccionada només es mostren els camps base del formulari.
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: true,
            find: vi.fn(),
        });

        renderComponent({});

        expect(screen.getByTestId('form-field-aplicacio')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-titol')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-descripcio')).toBeInTheDocument();
        expect(screen.queryByTestId('advanced-search-dimensionsValor')).not.toBeInTheDocument();
        expect(screen.queryByText('Contingut extra')).not.toBeInTheDocument();
    });

    it('EstadisticaWidgetFormFields_quanEsModePresetAmbComptador_mostraElsCampsDependents', () => {
        // Comprova que el mode PRESET mostra el període, el comptador i els fills addicionals.
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: true,
            find: vi.fn(),
        });

        renderComponent({
            aplicacio: { id: 7 },
            periodeMode: 'PRESET',
            presetPeriode: 'DARRERS_N_DIES',
        });

        expect(screen.getByTestId('advanced-search-dimensionsValor')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-periodeMode')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-presetPeriode')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-presetCount')).toBeInTheDocument();
        expect(screen.getByText('Contingut extra')).toBeInTheDocument();
    });

    it('EstadisticaWidgetFormFields_quanEsModeRelatiu_mostraElsCampsRelatius', () => {
        // Verifica que el mode RELATIU activa els controls específics de referència i unitat.
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: true,
            find: vi.fn(),
        });

        renderComponent({
            aplicacio: { id: 7 },
            periodeMode: 'RELATIU',
        });

        expect(screen.getByTestId('form-field-relatiuPuntReferencia')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-relatiuCount')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-relatiueUnitat')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-relatiuAlineacio')).toBeInTheDocument();
    });

    it('EstadisticaWidgetFormFields_quanEsModeAbsolutDateRange_mostraLesDatesDIniciIFi', () => {
        // Comprova que el mode ABSOLUT amb rang de dates exposa els dos camps de data.
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: true,
            find: vi.fn(),
        });

        renderComponent({
            aplicacio: { id: 7 },
            periodeMode: 'ABSOLUT',
            absolutTipus: 'DATE_RANGE',
        });

        expect(screen.getByTestId('form-field-absolutTipus')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-absolutDataInici')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-absolutDataFi')).toBeInTheDocument();
    });

    it('EstadisticaWidgetFormFields_quanEsPeriodeEspecificAmbAnyFix_habilitaElCampDAnyValor', () => {
        // Verifica que el camp de l'any específic queda habilitat quan el tipus de referència ho permet.
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: true,
            find: vi.fn(),
        });

        renderComponent({
            aplicacio: { id: 7 },
            periodeMode: 'ABSOLUT',
            absolutTipus: 'SPECIFIC_PERIOD_OF_YEAR',
            absolutAnyReferencia: 'SPECIFIC_YEAR',
        });

        expect(screen.getByTestId('form-field-absolutAnyReferencia')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-absolutAnyValor')).toHaveAttribute('data-disabled', 'false');
        expect(screen.getByTestId('form-field-absolutPeriodeUnitat')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-absolutPeriodeInici')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-absolutPeriodeFi')).toBeInTheDocument();
    });
});
