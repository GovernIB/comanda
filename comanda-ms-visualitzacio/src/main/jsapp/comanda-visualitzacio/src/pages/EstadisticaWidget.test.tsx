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
            <div>{title}</div>
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
        const { container } = render(<EstadisticaWidget />);

        // El títol de la graella és un ScreenTitle dins el ScreenFrame de la pàgina.
        const frame = container.querySelector('.screen-frame');
        expect(frame).not.toBeNull();
        const screenTitle = (frame as HTMLElement).querySelector('.screen-title');
        expect(screenTitle).not.toBeNull();
        expect(screenTitle).toHaveTextContent('Widgets simples');
        // Les pestanyes són dins el marc: el ScreenFrame és l'arrel de la pàgina.
        expect(frame).toContainElement(screen.getByRole('tab', { name: 'Simple' }));

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
