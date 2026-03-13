import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import EstadisticaWidget from './EstadisticaWidget';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                widget: {
                    title: 'Widgets',
                    simple: {
                        title: 'Widgets simples',
                        resourceTitle: 'Widget simple',
                        tab: {
                            title: 'Simple',
                        },
                    },
                    grafic: {
                        title: 'Widgets gràfics',
                        resourceTitle: 'Widget gràfic',
                        tab: {
                            title: 'Gràfic',
                        },
                    },
                    taula: {
                        title: 'Widgets taula',
                        resourceTitle: 'Widget taula',
                        tab: {
                            title: 'Taula',
                        },
                    },
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

vi.mock('reactlib', () => ({
    GridPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    MuiDataGrid: ({
        title,
        resourceName,
        popupEditFormDialogResourceTitle,
    }: {
        title: string;
        resourceName: string;
        popupEditFormDialogResourceTitle: string;
    }) => (
        <section>
            <h2>{title}</h2>
            <span>{resourceName}</span>
            <span>{popupEditFormDialogResourceTitle}</span>
        </section>
    ),
}));

vi.mock('../components/estadistiques/EstadisticaSimpleWidgetForm', () => ({
    default: () => <div>Form simple</div>,
}));

vi.mock('../components/estadistiques/EstadisticaGraficWidgetForm', () => ({
    default: () => <div>Form gràfic</div>,
}));

vi.mock('../components/estadistiques/EstadisticaTaulaWidgetForm', () => ({
    default: () => <div>Form taula</div>,
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <h1>{title}</h1>,
}));

describe('EstadisticaWidget', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('EstadisticaWidget_quanCanviaDeTab_mostraLaGraellaDelTipusSeleccionat', () => {
        // Comprova que la pàgina alterna entre widgets simples, gràfics i de taula segons la pestanya activa.
        render(<EstadisticaWidget />);

        expect(screen.getByRole('heading', { name: 'Widgets' })).toBeInTheDocument();
        expect(screen.getByRole('heading', { name: 'Widgets simples' })).toBeInTheDocument();
        expect(screen.getByText('estadisticaSimpleWidget')).toBeInTheDocument();

        fireEvent.click(screen.getByRole('tab', { name: 'Gràfic' }));

        expect(screen.getByRole('heading', { name: 'Widgets gràfics' })).toBeInTheDocument();
        expect(screen.getByText('estadisticaGraficWidget')).toBeInTheDocument();

        fireEvent.click(screen.getByRole('tab', { name: 'Taula' }));

        expect(screen.getByRole('heading', { name: 'Widgets taula' })).toBeInTheDocument();
        expect(screen.getByText('estadisticaTaulaWidget')).toBeInTheDocument();
    });
});
