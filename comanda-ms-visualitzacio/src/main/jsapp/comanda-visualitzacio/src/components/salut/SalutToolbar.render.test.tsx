import { act, fireEvent, render, screen } from '@testing-library/react';
import React from 'react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { GroupingEnum, SalutToolbar } from './SalutToolbar';

const mocks = vi.hoisted(() => ({
    goBackMock: vi.fn(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) =>
            selector({
                page: {
                    salut: {
                        filtrar: 'Filtrar',
                        senseFiltres: 'Sense filtres',
                        goBack: 'Tornar',
                        refrescar: 'Refrescar',
                        refresh: { last: 'Darrer', next: 'Següent' },
                        refreshperiod: {
                            title: 'Període de refresc',
                            PT1M: '1m',
                            PT5M: '5m',
                            PT10M: '10m',
                            PT30M: '30m',
                            PT1H: '1h',
                        },
                        timerange: {
                            title: 'Rang temporal',
                            PT15M: '15m',
                            PT1H: '1h',
                            P1D: '1d',
                            P7D: '7d',
                            P1M: '1m',
                        },
                        groupingSelect: {
                            label: 'Agrupació',
                            BY_APPLICATION: 'Per aplicació',
                            BY_ENVIRONMENT: 'Per entorn',
                            NONE: 'Cap',
                        },
                    },
                },
                components: { clear: 'Netejar', search: 'Cercar' },
            }),
    }),
}));

vi.mock('reactlib', () => ({
    Toolbar: ({
        title,
        elementsWithPositions,
    }: {
        title: string;
        elementsWithPositions: { element: React.ReactNode }[];
    }) => (
        <div>
            <h1>{title}</h1>
            {elementsWithPositions.map((item, index) => (
                <div key={index}>{item.element}</div>
            ))}
        </div>
    ),
    MuiDialog: ({ open, title, children }: { open: boolean; title: string; children?: React.ReactNode }) =>
        open ? (
            <div>
                <div>{title}</div>
                {children}
            </div>
        ) : null,
    MuiFilter: ({ children }: { children?: React.ReactNode }) => <div>{children}</div>,
    FormField: ({ name }: { name: string }) => <div>{name}</div>,
    springFilterBuilder: {
        and: (...parts: string[]) => parts.filter(Boolean).join(' and '),
        eq: (field: string, value: unknown) => `${field}:${String(value)}`,
        exists: (value: string) => `exists(${value})`,
    },
    useBaseAppContext: () => ({ goBack: mocks.goBackMock }),
    useFilterApiRef: () => ({ current: { filter: vi.fn(), clear: vi.fn() } }),
    useFormContext: () => ({ data: {} }),
}));

describe('SalutToolbar render', () => {
    beforeEach(() => {
        vi.useFakeTimers();
        vi.setSystemTime(new Date('2026-03-13T10:00:00'));
    });

    afterEach(() => {
        vi.useRealTimers();
        vi.clearAllMocks();
    });

    it('SalutToolbar_quanEsRenderitza_mostraElTitolElSubtitolIActionsPrincipals', () => {
        // Comprova que la toolbar mostra el títol, el subtítol calculat i les accions bàsiques visibles.
        render(
            <SalutToolbar
                title="Salut"
                subtitle="Subtítol manual"
                ready={true}
                onRefreshClick={() => undefined}
                dataRangeDuration="PT15M"
                setDataRangeDuration={() => undefined}
                refreshDuration="PT5M"
                setRefreshDuration={() => undefined}
                filterData={{}}
                setFilterData={() => undefined}
                grouping={GroupingEnum.APPLICATION}
                setGrouping={() => undefined}
            />
        );

        expect(screen.getByText('Salut')).toBeInTheDocument();
        expect(screen.getByText('Subtítol manual')).toBeInTheDocument();
        expect(screen.getByTitle('Filtrar')).toBeInTheDocument();
        expect(screen.getByTitle('Refrescar')).toBeInTheDocument();
    });

    it('SalutToolbar_quanTeGoBackActive_permetTornarEnrere', () => {
        // Verifica que la toolbar mostra l'acció de tornar enrere i delega al context base.
        render(
            <SalutToolbar
                title="Salut"
                ready={true}
                onRefreshClick={() => undefined}
                goBackActive={true}
                dataRangeDuration="PT15M"
                setDataRangeDuration={() => undefined}
                refreshDuration="PT5M"
                setRefreshDuration={() => undefined}
                filterData={{}}
                setFilterData={() => undefined}
                grouping={GroupingEnum.APPLICATION}
                setGrouping={() => undefined}
            />
        );

        fireEvent.click(screen.getByTitle('Tornar'));

        expect(mocks.goBackMock).toHaveBeenCalledWith('/');
    });

    it('SalutToolbar_quanElFiltreEstaAmagat_noMostraLAccioDeFiltrar', () => {
        // Verifica que la toolbar pot ocultar explícitament l'acció de filtre en la vista de detall.
        render(
            <SalutToolbar
                title="Salut"
                ready={true}
                hideFilter={true}
                onRefreshClick={() => undefined}
                dataRangeDuration="PT15M"
                setDataRangeDuration={() => undefined}
                refreshDuration="PT5M"
                setRefreshDuration={() => undefined}
                filterData={{}}
                setFilterData={() => undefined}
                grouping={GroupingEnum.APPLICATION}
                setGrouping={() => undefined}
            />
        );

        expect(screen.queryByTitle('Filtrar')).not.toBeInTheDocument();
    });

    it('SalutToolbar_quanNoHiHaSubtitol_manualComputaElTextDesDelFiltre', () => {
        // Comprova que el subtítol es calcula a partir de l’app i l’entorn filtrats quan no es passa manualment.
        render(
            <SalutToolbar
                title="Salut"
                ready={true}
                onRefreshClick={() => undefined}
                dataRangeDuration="PT15M"
                setDataRangeDuration={() => undefined}
                refreshDuration="PT5M"
                setRefreshDuration={() => undefined}
                filterData={{
                    app: { id: '1', description: 'App Demo' },
                    entorn: { id: '2', description: 'PRO' },
                }}
                setFilterData={() => undefined}
                grouping={GroupingEnum.APPLICATION}
                setGrouping={() => undefined}
            />
        );

        expect(screen.getByText('App Demo - PRO')).toBeInTheDocument();
    });

    it('SalutToolbar_quanHiHaFiltreActiu_mostraLaVariantPlenaDeLaIcona', () => {
        // Verifica que la toolbar reflecteix visualment que hi ha un filtre aplicat.
        render(
            <SalutToolbar
                title="Salut"
                ready={true}
                onRefreshClick={() => undefined}
                dataRangeDuration="PT15M"
                setDataRangeDuration={() => undefined}
                refreshDuration="PT5M"
                setRefreshDuration={() => undefined}
                filterData={{ app: { id: '1', description: 'App Demo' } }}
                setFilterData={() => undefined}
                grouping={GroupingEnum.APPLICATION}
                setGrouping={() => undefined}
            />
        );

        expect(screen.getByTestId('FilterAltIcon')).toBeInTheDocument();
    });

    it('SalutToolbar_quanHiHaGroupingActiu_mostraElSelectorDAgrupacio', () => {
        // Comprova que el selector d’agrupació només apareix quan la vista el té habilitat.
        render(
            <SalutToolbar
                title="Salut"
                ready={true}
                groupingActive={true}
                onRefreshClick={() => undefined}
                dataRangeDuration="PT15M"
                setDataRangeDuration={() => undefined}
                refreshDuration="PT5M"
                setRefreshDuration={() => undefined}
                filterData={{}}
                setFilterData={() => undefined}
                grouping={GroupingEnum.APPLICATION}
                setGrouping={() => undefined}
            />
        );

        expect(screen.getByLabelText('grouping selection')).toBeInTheDocument();
        expect(screen.getByTitle('Per aplicació')).toBeInTheDocument();
        expect(screen.getByTitle('Per entorn')).toBeInTheDocument();
        expect(screen.getByTitle('Cap')).toBeInTheDocument();
    });

    it('SalutToolbar_quanNoEstaReady_deshabilitaLesAccionsTemporals', () => {
        // Verifica que els selectors i el botó de refresc queden deshabilitats mentre la pantalla no està preparada.
        render(
            <SalutToolbar
                title="Salut"
                ready={false}
                groupingActive={true}
                onRefreshClick={() => undefined}
                dataRangeDuration="PT15M"
                setDataRangeDuration={() => undefined}
                refreshDuration="PT5M"
                setRefreshDuration={() => undefined}
                filterData={{}}
                setFilterData={() => undefined}
                grouping={GroupingEnum.APPLICATION}
                setGrouping={() => undefined}
            />
        );

        expect(screen.getByTitle('Refrescar')).toBeDisabled();
        expect(screen.getByLabelText('Període de refresc')).toHaveAttribute('aria-disabled', 'true');
        expect(screen.getByLabelText('Rang temporal')).toHaveAttribute('aria-disabled', 'true');
        expect(screen.getByTitle('Per aplicació')).toBeDisabled();
        expect(screen.getByTitle('Per entorn')).toBeDisabled();
        expect(screen.getByTitle('Cap')).toBeDisabled();
    });

    it('SalutToolbar_quanHiHaDatesDeRefresh_mostraElResumTemporal', () => {
        // Comprova que la toolbar mostra el darrer refresh i el temps que falta fins al següent.
        vi.setSystemTime(new Date('2026-03-13T10:00:00'));

        render(
            <SalutToolbar
                title="Salut"
                ready={true}
                onRefreshClick={() => undefined}
                lastRefresh={new Date('2026-03-13T09:59:00')}
                nextRefresh={new Date('2026-03-13T10:00:30')}
                dataRangeDuration="PT15M"
                setDataRangeDuration={() => undefined}
                refreshDuration="PT5M"
                setRefreshDuration={() => undefined}
                filterData={{}}
                setFilterData={() => undefined}
                grouping={GroupingEnum.APPLICATION}
                setGrouping={() => undefined}
            />
        );

        act(() => {
            vi.advanceTimersByTime(1000);
        });

        expect(screen.getByText(/Darrer:/)).toBeInTheDocument();
        expect(screen.getByText(/Següent:/)).toBeInTheDocument();
    });
});
