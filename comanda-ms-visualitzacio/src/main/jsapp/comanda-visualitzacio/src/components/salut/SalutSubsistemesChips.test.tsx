import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import SalutSubsistemesChips from './SalutSubsistemesChips';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                salut: {
                    subsistemes: {
                        subsistemaUpCount: 'Subsistemes OK',
                        subsistemaWarnCount: 'Subsistemes WARN',
                        subsistemaDownCount: 'Subsistemes DOWN',
                        subsistemaDesconegutCount: 'Subsistemes UNKNOWN',
                    },
                },
            },
        })
    ),
    getColorBySubsistemaMock: vi.fn((field: string) => `color-${field}`),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({ t: mocks.tMock }),
}));

vi.mock('../../types/salut.model', () => ({
    SalutModel: {
        SUBSISTEMA_UP_COUNT: 'subsistemaUpCount',
        SUBSISTEMA_WARN_COUNT: 'subsistemaWarnCount',
        SUBSISTEMA_DOWN_COUNT: 'subsistemaDownCount',
        SUBSISTEMA_DESCONEGUT_COUNT: 'subsistemaDesconegutCount',
    },
    useGetColorBySubsistema: () => mocks.getColorBySubsistemaMock,
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

describe('SalutSubsistemesChips', () => {
    it('SalutSubsistemesChips_quanEsRenderitza_mostraElsComptadorsDeSubsistemes', () => {
        // Comprova que el component renderitza els quatre comptadors de subsistemes amb els textos derivats.
        render(
            <SalutSubsistemesChips
                salutItem={{
                    subsistemaUpCount: 8,
                    subsistemaWarnCount: 7,
                    subsistemaDownCount: 6,
                    subsistemaDesconegutCount: 5,
                } as any}
            />
        );

        expect(screen.getByText('Subsistemes OK:8:color-subsistemaUpCount')).toBeInTheDocument();
        expect(screen.getByText('Subsistemes WARN:7:color-subsistemaWarnCount')).toBeInTheDocument();
        expect(screen.getByText('Subsistemes DOWN:6:color-subsistemaDownCount')).toBeInTheDocument();
        expect(screen.getByText('Subsistemes UNKNOWN:5:color-subsistemaDesconegutCount')).toBeInTheDocument();
    });
});
