import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import SalutMissatgesChips from './SalutMissatgesChips';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                salut: {
                    msgs: {
                        missatgeErrorCount: 'Errors',
                        missatgeWarnCount: 'Warnings',
                        missatgeInfoCount: 'Infos',
                    },
                },
            },
        })
    ),
    getColorByMissatgeMock: vi.fn((field: string) => `color-${field}`),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({ t: mocks.tMock }),
}));

vi.mock('../../types/salut.model', () => ({
    SalutModel: {
        MISSATGE_ERROR_COUNT: 'missatgeErrorCount',
        MISSATGE_WARN_COUNT: 'missatgeWarnCount',
        MISSATGE_INFO_COUNT: 'missatgeInfoCount',
    },
    useGetColorByMissatge: () => mocks.getColorByMissatgeMock,
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

describe('SalutMissatgesChips', () => {
    it('SalutMissatgesChips_quanEsRenderitza_mostraElsComptadorsDeMissatges', () => {
        // Verifica que el component mostra els tres comptadors de missatges amb el color corresponent.
        render(
            <SalutMissatgesChips
                salutItem={{
                    missatgeErrorCount: 6,
                    missatgeWarnCount: 5,
                    missatgeInfoCount: 4,
                } as any}
            />
        );

        expect(screen.getByText('Errors:6:color-missatgeErrorCount')).toBeInTheDocument();
        expect(screen.getByText('Warnings:5:color-missatgeWarnCount')).toBeInTheDocument();
        expect(screen.getByText('Infos:4:color-missatgeInfoCount')).toBeInTheDocument();
    });
});
