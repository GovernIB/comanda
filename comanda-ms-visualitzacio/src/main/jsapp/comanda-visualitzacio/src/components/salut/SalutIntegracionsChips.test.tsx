import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import SalutIntegracionsChips from './SalutIntegracionsChips';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                salut: {
                    integracions: {
                        integracioUpCount: 'Integracions OK',
                        integracioWarnCount: 'Integracions WARN',
                        integracioDownCount: 'Integracions DOWN',
                        integracioDesconegutCount: 'Integracions UNKNOWN',
                    },
                },
            },
        })
    ),
    getColorByIntegracioMock: vi.fn((field: string) => `color-${field}`),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({ t: mocks.tMock }),
}));

vi.mock('../../types/salut.model', () => ({
    SalutModel: {
        INTEGRACIO_UP_COUNT: 'integracioUpCount',
        INTEGRACIO_WARN_COUNT: 'integracioWarnCount',
        INTEGRACIO_DOWN_COUNT: 'integracioDownCount',
        INTEGRACIO_DESCONEGUT_COUNT: 'integracioDesconegutCount',
    },
    useGetColorByIntegracio: () => mocks.getColorByIntegracioMock,
}));

vi.mock('./SalutChip', () => ({
    default: ({
        label,
        tooltip,
        backgroundColor,
    }: {
        label: React.ReactNode;
        tooltip?: string;
        backgroundColor?: string;
    }) => <div>{`${tooltip}:${label}:${backgroundColor}`}</div>,
}));

describe('SalutIntegracionsChips', () => {
    it('SalutIntegracionsChips_quanEsRenderitza_mostraElsQuatreComptadorsAmbElSeuTooltip', () => {
        // Comprova que el component renderitza tots els comptadors d'integracions amb el color derivat.
        render(
            <SalutIntegracionsChips
                salutItem={{
                    integracioUpCount: 4,
                    integracioWarnCount: 3,
                    integracioDownCount: 2,
                    integracioDesconegutCount: 1,
                } as any}
            />
        );

        expect(screen.getByText('Integracions OK:4:color-integracioUpCount')).toBeInTheDocument();
        expect(screen.getByText('Integracions WARN:3:color-integracioWarnCount')).toBeInTheDocument();
        expect(screen.getByText('Integracions DOWN:2:color-integracioDownCount')).toBeInTheDocument();
        expect(screen.getByText('Integracions UNKNOWN:1:color-integracioDesconegutCount')).toBeInTheDocument();
    });
});
