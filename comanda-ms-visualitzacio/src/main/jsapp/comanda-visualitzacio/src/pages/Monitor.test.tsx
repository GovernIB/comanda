import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Monitors, { translateEnumValue } from './Monitor';
import React from 'react';

const mocks = vi.hoisted(() => ({
    clearMock: vi.fn(),
    filterMock: vi.fn(),
    showDialogMock: vi.fn(),
    setFieldValueMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                monitors: {
                    title: 'Monitor',
                    detail: {
                        title: 'Detall monitor',
                        data: 'Data',
                        operacio: 'Operació',
                        tipus: 'Tipus',
                        estat: 'Estat',
                        codiUsuari: 'Codi usuari',
                        errorDescripcio: 'Descripció error',
                        excepcioMessage: 'Missatge excepció',
                        excepcioStacktrace: 'Stacktrace',
                    },
                    modulEnum: {
                        salut: 'Salut',
                        estadistica: 'Estadística',
                        configuracio: 'Configuració',
                        alarmes: 'Alarmes',
                    },
                    column: {
                        mailAddress: 'Adreça de correu',
                    },
                    tab: {
                        email: 'EMAIL',
                    },
                    filter: {
                        more: "Més camps",
                    },
                    detailTipus: 'Tipus',
                },
            },
            components: {
                clear: 'Netejar',
                search: 'Cercar',
            },
        })
    ),
    tStringKeyMock: vi.fn((key: string) => {
        const translations: Record<string, string> = {
            'page.monitors.modulEnum.salut': 'Salut',
            'page.monitors.modulEnum.estadistica': 'Estadística',
            'page.monitors.modulEnum.configuracio': 'Configuració',
            'page.monitors.modulEnum.alarmes': 'Alarmes',
            'page.monitors.column.mailAddress': 'Adreça de correu',
            'page.monitors.tab.email': 'EMAIL',
            'page.monitors.detail.estatEnum.ok': 'Correcte',
            'page.monitors.detail.estatEnum.error': 'Error',
            'page.monitors.detail.estatEnum.warn': 'Avís',
            'page.monitors.detail.tipusEnum.sortida': 'Sortida',
            'page.monitors.detail.tipusEnum.entrada': 'Entrada',
            'page.monitors.detail.tipusEnum.interna': 'Interna',
        };
        return translations[key] ?? key;
    }),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('reactlib', () => ({
    GridPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    MuiDataGrid: ({
        title,
        toolbarAdditionalRow,
        onRowClick,
        fixedFilter,
        columns,
    }: {
        title: string;
        toolbarAdditionalRow?: React.ReactNode;
        onRowClick?: (params: { row: Record<string, unknown> }) => void;
        fixedFilter?: string;
        columns: Array<{ field: string; headerName?: string; renderCell?: (params: any) => React.ReactNode }>;
    }) => (
        <section>
            <h2>{title}</h2>
            <div data-testid="fixed-filter">{fixedFilter}</div>
            <div>{toolbarAdditionalRow}</div>
            <div data-testid="url-header">{columns[3]?.headerName ?? columns[3]?.field}</div>
            <button onClick={() => onRowClick?.({ row: { estat: 'ERROR', tipus: 'SORTIDA', excepcioStacktrace: 'stack' } })}>
                Obre detall
            </button>
            <div data-testid="estat-cell">
                {columns[6]?.renderCell?.({ value: 'WARN', formattedValue: 'Avís' })}
            </div>
        </section>
    ),
    MuiFilter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    FormField: ({ name }: { name: string }) => <div data-testid={`field-${name}`}>{name}</div>,
    dateFormatLocale: () => '13/03/2026 10:00',
    useMuiContentDialog: () => [mocks.showDialogMock, <div key="dialog">Diàleg monitor</div>],
    useCloseDialogButtons: () => <button>Tancar</button>,
    useFilterApiRef: () => ({
        current: {
            clear: mocks.clearMock,
            filter: mocks.filterMock,
        },
    }),
    springFilterBuilder: {
        and: (...parts: Array<string | undefined | false>) => parts.filter(Boolean).join(' && '),
        like: (field: string, value: unknown) => `${field}~${String(value)}`,
        between: (field: string, from: unknown, to: unknown) => `${field}[${String(from)},${String(to)}]`,
        eq: (field: string, value: unknown) => `${field}=${String(value)}`,
    },
    useFormApiRef: () => ({ current: { setFieldValue: mocks.setFieldValueMock, }, }),
}));

vi.mock('../components/ContentDetail', () => ({
    ContentDetail: ({ elements }: { elements: Array<{ label?: string; value?: unknown; contentValue?: React.ReactNode }> }) => (
        <div>
            {elements.map((element, index) => (
                <div key={index}>
                    <span>{element.label}</span>
                    <span>{element.contentValue ?? String(element.value ?? '')}</span>
                </div>
            ))}
        </div>
    ),
}));

vi.mock('../components/RickTextDetail', () => ({
    StacktraceBlock: ({ title }: { title: string }) => <div>{title}</div>,
}));

vi.mock('../hooks/useTranslationStringKey', () => ({
    default: () => ({
        t: mocks.tStringKeyMock,
    }),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('translateEnumValue', () => {
    it('translateEnumValue_quanHiHaTraduccio_retornaElTextTraduït', () => {
        // Comprova que els enums coneguts es transformen amb el mapa i la funció de traducció.
        const result = translateEnumValue('OK', { OK: 'page.monitors.detail.estatEnum.ok' }, mocks.tStringKeyMock);
        expect(result).toBe('Correcte');
    });

    it('translateEnumValue_quanNoHiHaMapa_retornaElValorOriginal', () => {
        // Verifica que si no hi ha mapa de traducció es manté el valor original.
        expect(translateEnumValue('CUSTOM')).toBe('CUSTOM');
    });
});

describe('Monitors', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('Monitors_quanEsRenderitza_mostraElFiltreInicialITotsElsControlsPrincipals', () => {
        // Comprova que la pàgina arrenca al mòdul de salut i mostra el filtre, els camps i les pestanyes principals.
        render(<Monitors />);

        expect(screen.getByTestId('page-title')).toHaveTextContent('Monitor');
        expect(screen.getByRole('heading', { name: 'Monitor' })).toBeInTheDocument();
        expect(screen.getByTestId('fixed-filter')).toHaveTextContent("modul:'SALUT'");
        expect(screen.getByTestId('field-codi')).toBeInTheDocument();
        expect(screen.getByTestId('field-dataDesde')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Netejar' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Més camps' })).toBeInTheDocument();
        expect(screen.getByRole('tab', { name: 'Salut' })).toBeInTheDocument();
        expect(screen.getByRole('tab', { name: 'EMAIL' })).toBeInTheDocument();
        expect(screen.getByTestId('url-header')).toHaveTextContent('URL');
        expect(screen.getByTestId('estat-cell')).toHaveTextContent('Avís');
    });

    it('Monitors_quanEsPremenElsBotonsDelFiltre_executaClearIFilter', () => {
        // Verifica que els botons del filtre deleguen correctament a l'API del filtre.
        render(<Monitors />);

        fireEvent.click(screen.getByRole('button', { name: 'Netejar' }));

        expect(mocks.clearMock).toHaveBeenCalled();;
    });

    it('Monitors_quanCanviaLaPestanya_actualitzaElFiltreStaticIObreElDetall', () => {
        // Comprova que canviar de pestanya actualitza el mòdul actiu i que clicar una fila obre el detall.
        render(<Monitors />);

        fireEvent.click(screen.getByRole('tab', { name: 'Estadística' }));
        expect(screen.getByTestId('fixed-filter')).toHaveTextContent("modul:'ESTADISTICA'");

        fireEvent.click(screen.getByRole('button', { name: 'Obre detall' }));
        expect(mocks.showDialogMock).toHaveBeenCalledWith(
            'Detall monitor',
            expect.anything(),
            expect.anything(),
            expect.objectContaining({ maxWidth: 'lg', fullWidth: true })
        );
    });

    it('Monitors_quanSeleccionaEmail_mostraElModulAlarmesIElHeaderDeCorreu', () => {
        render(<Monitors />);

        fireEvent.click(screen.getByRole('tab', { name: 'EMAIL' }));

        expect(screen.getByTestId('fixed-filter')).toHaveTextContent("modul:'ALARMES'");
        expect(screen.getByTestId('url-header')).toHaveTextContent('Adreça de correu');
    });
});
