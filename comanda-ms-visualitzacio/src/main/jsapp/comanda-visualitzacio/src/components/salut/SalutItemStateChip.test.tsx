import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { ItemStateChip } from './SalutItemStateChip';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((key: string) => `tr:${key}`),
    getColorByStatEnumMock: vi.fn((state: string) => `color-${state}`),
    getMaterialIconByStateMock: vi.fn((state: string) => <span>{`icon-${state}`}</span>),
}));

vi.mock('../../hooks/useTranslationStringKey', () => ({
    default: () => ({ t: mocks.tMock }),
}));

vi.mock('../../types/salut.model.tsx', () => ({
    ENUM_APP_ESTAT_PREFIX: 'enum.appEstat.',
    TITLE: '.title',
    SalutEstatEnum: {
        UP: 'UP',
        UNKNOWN: 'UNKNOWN',
    },
    useGetColorByStatEnum: () => mocks.getColorByStatEnumMock,
    getMaterialIconByState: (state: string) => mocks.getMaterialIconByStateMock(state),
}));

vi.mock('../../util/colorUtil.ts', () => ({
    ChipColor: {
        WHITE: '#fff',
    },
}));

vi.mock('./SalutChipTooltip.tsx', () => ({
    SalutChipTooltip: ({
        children,
        stateEnum,
        salutField,
    }: {
        children: React.ReactNode;
        stateEnum: string;
        salutField: string;
    }) => <div data-testid="tooltip-wrapper">{`${stateEnum}:${salutField}`}{children}</div>,
    SalutField: {
        APP_ESTAT: 'appEstat',
    },
}));

describe('ItemStateChip', () => {
    it('ItemStateChip_quanRepUnEstat_renderitzaElChipAmbElTextILaData', () => {
        // Comprova que el component mostra l'estat traduït i la data auxiliar quan es proporcionen.
        render(
            <ItemStateChip
                salutField={'appEstat' as any}
                salutStatEnum={'UP' as any}
                date="2026-03-13 10:00"
            />
        );

        expect(screen.getByTestId('tooltip-wrapper')).toHaveTextContent('UP:appEstat');
        expect(screen.getByText('tr:enum.appEstat.UP.title')).toBeInTheDocument();
        expect(screen.getByText('2026-03-13 10:00')).toBeInTheDocument();
        expect(screen.getByText('icon-UP')).toBeInTheDocument();
    });

    it('ItemStateChip_quanNoRepEstat_usaUnknownPerDefecte', () => {
        // Verifica que el component fa servir l'estat UNKNOWN quan no es passa cap valor.
        render(<ItemStateChip salutField={'appEstat' as any} salutStatEnum={undefined} />);

        expect(screen.getByTestId('tooltip-wrapper')).toHaveTextContent('UNKNOWN:appEstat');
        expect(screen.getByText('tr:enum.appEstat.UNKNOWN.title')).toBeInTheDocument();
    });
});
