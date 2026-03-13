import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { SalutChipTooltip, SalutField, SalutGenericTooltip } from './SalutChipTooltip';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((key: string) => key),
    getColorByStatEnumMock: vi.fn((state: string) => `color-${state}`),
    getMaterialIconByStateMock: vi.fn((state: string) => <span>{`icon-${state}`}</span>),
    transMock: vi.fn(({ i18nKey }: { i18nKey: string }) => <span>{`trans:${i18nKey}`}</span>),
}));

vi.mock('../../hooks/useTranslationStringKey', () => ({
    default: () => ({ t: mocks.tMock }),
}));

vi.mock('react-i18next', () => ({
    Trans: (props: { i18nKey: string }) => mocks.transMock(props),
}));

vi.mock('../../types/salut.model.tsx', () => ({
    ENUM_APP_ESTAT_PREFIX: 'enum.appEstat.',
    ENUM_BD_ESTAT_PREFIX: 'enum.bdEstat.',
    ENUM_INTEGRACIO_ESTAT_PREFIX: 'enum.integracioEstat.',
    TITLE: '.title',
    TOOLTIP: '.tooltip',
    SalutEstatEnum: {
        UP: 'UP',
        WARN: 'WARN',
    },
    useGetColorByStatEnum: () => mocks.getColorByStatEnumMock,
    getMaterialIconByState: (state: string) => mocks.getMaterialIconByStateMock(state),
}));

describe('SalutChipTooltip', () => {
    it('SalutChipTooltip_quanHiHaTraduccioDeTooltip_mostraElTitolIElContingutEnObrirElTooltip', async () => {
        // Comprova que el tooltip mostra tant el títol traduït com el cos enriquit quan la clau existeix.
        mocks.tMock.mockImplementation((key: string) => {
            if (key.endsWith('.tooltip')) {
                return `text:${key}`;
            }
            return `title:${key}`;
        });

        render(
            <SalutChipTooltip stateEnum={'UP' as any} salutField={SalutField.APP_ESTAT}>
                <button type="button">Mostra</button>
            </SalutChipTooltip>
        );

        fireEvent.mouseOver(screen.getByRole('button', { name: 'Mostra' }));

        expect(await screen.findByText('title:enum.appEstat.UP.title')).toBeInTheDocument();
        expect(screen.getByText('trans:enum.appEstat.UP.tooltip')).toBeInTheDocument();
        expect(screen.getByText('icon-UP')).toBeInTheDocument();
    });

    it('SalutChipTooltip_quanNoHiHaTraduccioDeTooltip_noRenderitzaElTextAddicional', async () => {
        // Verifica que si la traducció no existeix només es mostra el títol de l'estat.
        mocks.tMock.mockImplementation((key: string) => key);

        render(
            <SalutChipTooltip stateEnum={'WARN' as any} salutField={SalutField.BD_ESTAT}>
                <button type="button">Mostra</button>
            </SalutChipTooltip>
        );

        fireEvent.mouseOver(screen.getByRole('button', { name: 'Mostra' }));

        expect(await screen.findByText('enum.appEstat.WARN.title')).toBeInTheDocument();
        expect(screen.queryByText('trans:enum.bdEstat.WARN.tooltip')).not.toBeInTheDocument();
    });
});

describe('SalutGenericTooltip', () => {
    it('SalutGenericTooltip_quanSObri_exposaElContingutGenericDelTooltip', async () => {
        // Comprova que el tooltip genèric continua mostrant el títol passat per props.
        render(
            <SalutGenericTooltip title="Descripcio resumida">
                <button type="button">Ajuda</button>
            </SalutGenericTooltip>
        );

        fireEvent.mouseOver(screen.getByRole('button', { name: 'Descripcio resumida' }));

        expect(await screen.findByText('Descripcio resumida')).toBeInTheDocument();
    });
});
