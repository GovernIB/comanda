import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import RecursosEntorns from './RecursosEntorns';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

vi.mock('../../components/UserContext', () => ({
    useUserContext: () => ({
        user: {
            numElementsPagina: '_20',
        },
    }),
}));

vi.mock('../../util/recursosUtils', () => ({
    calcularPercentatgeUs: vi.fn(() => 75),
    getColorForPercentage: vi.fn(() => 'warning'),
}));

const translations = {
    page: {
        salut: {
            recursos: {
                title: 'Recursos del sistema',
                subtitle: 'Vista de consum de recursos',
                empty: 'No hi ha dades de recursos disponibles',
                noDisponible: 'N/D',
                ariaTable: 'Taula de recursos del sistema',
                columns: {
                    aplicacio: 'Aplicació',
                    entorn: 'Entorn',
                    memoriaPercent: 'Memòria (Ús %)',
                    memoriaValors: 'Memòria (Disp / Total)',
                    discPercent: 'Disc (Ús %)',
                    discValors: 'Disc (Disp / Total)',
                    detalls: 'Detalls',
                    detallsTitle: 'Veure els detalls',
                },
            },
        },
    },
};

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) => (typeof selector === 'function' ? selector(translations) : selector),
    }),
}));

const mockSalutGroups = [
    {
        entornApps: [
            { id: 1, app: { id: 10, description: 'App Test Desc' }, entorn: { id: 20, description: 'Entorn Test Desc' } }
        ],
        salutLastItems: [
            {
                entornAppId: 1,
                detalls: [
                    { codi: 'MET', valor: '1024 MB' },
                    { codi: 'MED', valor: '256 MB' },
                    { codi: 'EDT', valor: '500 GB' },
                    { codi: 'EDL', valor: '100 GB' },
                ]
            }
        ]
    }
];

describe('RecursosEntorns Component', () => {
    beforeEach(() => {
        mockNavigate.mockClear();
    });

    it('hauria de mostrar l\'estat de càrrega si loading és true i no hi ha files', () => {
        render(
            <BrowserRouter>
                <RecursosEntorns salutGroups={[]} loading={true} />
            </BrowserRouter>
        );
        expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('hauria de mostrar el missatge de buit si no hi ha dades', () => {
        render(
            <BrowserRouter>
                <RecursosEntorns salutGroups={[]} loading={false} />
            </BrowserRouter>
        );
        expect(screen.getByText('No hi ha dades de recursos disponibles')).toBeInTheDocument();
    });

    it('hauria de gestionar salutGroups undefined sense llançar error', () => {
        render(
            <BrowserRouter>
                <RecursosEntorns salutGroups={undefined} loading={false} />
            </BrowserRouter>
        );
        expect(screen.getByText('No hi ha dades de recursos disponibles')).toBeInTheDocument();
    });

    it('hauria de renderitzar les dades correctament', () => {
        render(
            <BrowserRouter>
                <RecursosEntorns salutGroups={mockSalutGroups} loading={false} />
            </BrowserRouter>
        );

        expect(screen.getByText('App Test Desc')).toBeInTheDocument();
        expect(screen.getByText('Entorn Test Desc')).toBeInTheDocument();

        const percentatges = screen.getAllByText('75%');
        expect(percentatges.length).toBe(2);

        expect(screen.getByText('256 MB / 1024 MB')).toBeInTheDocument();
        expect(screen.getByText('100 GB / 500 GB')).toBeInTheDocument();
    });

    it('hauria de permetre ordenar per columna de Memòria', () => {
        render(
            <BrowserRouter>
                <RecursosEntorns salutGroups={mockSalutGroups} loading={false} />
            </BrowserRouter>
        );

        const memoriaHeaderButton = screen.getByRole('button', { name: /Memòria \(Ús %\)/i });

        expect(memoriaHeaderButton.parentElement).toHaveAttribute('aria-sort', 'descending');

        fireEvent.click(memoriaHeaderButton);

        expect(memoriaHeaderButton.parentElement).toHaveAttribute('aria-sort', 'ascending');
    });

    it('hauria d\'ordenar alfabèticament tenint en compte caràcters accentuats (localeCompare)', () => {
        const groupsWithAccents = [
            {
                entornApps: [
                    { id: 1, app: { id: 1, description: 'Biel' }, entorn: { id: 10, description: 'Pre' } },
                    { id: 2, app: { id: 2, description: 'Àbac' }, entorn: { id: 10, description: 'Pre' } },
                    { id: 3, app: { id: 3, description: 'Cisterna' }, entorn: { id: 10, description: 'Pre' } },
                ],
                salutLastItems: [
                    { entornAppId: 1, detalls: [] },
                    { entornAppId: 2, detalls: [] },
                    { entornAppId: 3, detalls: [] },
                ],
            },
        ];

        render(
            <BrowserRouter>
                <RecursosEntorns salutGroups={groupsWithAccents as any} loading={false} />
            </BrowserRouter>
        );

        const appHeaderButton = screen.getByRole('button', { name: /Aplicació/i });
        // Primer clic: asc
        fireEvent.click(appHeaderButton);

        const cells = screen.getAllByRole('row').slice(1).map(row => row.querySelector('th')?.textContent);
        // Àbac ha d'anar abans de Biel en ordre ascendent
        expect(cells).toEqual(['Àbac', 'Biel', 'Cisterna']);
    });

    it('hauria de mostrar la paginació quan hi ha dades', () => {
        render(
            <BrowserRouter>
                <RecursosEntorns salutGroups={mockSalutGroups} loading={false} />
            </BrowserRouter>
        );

        expect(screen.getByText('1–1 of 1')).toBeInTheDocument();
    });

    it('hauria de navegar al detall en fer clic al botó', () => {
        render(
            <BrowserRouter>
                <RecursosEntorns salutGroups={mockSalutGroups} loading={false} />
            </BrowserRouter>
        );

        const detailButton = screen.getByRole('button', { name: /Veure els detalls/i });
        fireEvent.click(detailButton);

        expect(mockNavigate).toHaveBeenCalledWith('appinfo/1');
    });
});
