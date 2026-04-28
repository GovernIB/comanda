import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Entorns from './Entorns';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                entorns: {
                    title: 'Entorns',
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
    FormField: ({ name }: { name: string }) => <div data-testid={`field-${name}`}>{name}</div>,
    MuiDataGrid: ({
        title,
        columns,
        toolbarElementsWithPositions,
        popupEditFormContent,
    }: {
        title: string;
        columns: Array<{ field: string }>;
        toolbarElementsWithPositions?: Array<{ element: React.ReactNode }>;
        popupEditFormContent?: React.ReactNode;
    }) => (
        <section>
            <h2>{title}</h2>
            <div data-testid="columns">{columns.map((column) => column.field).join(',')}</div>
            {toolbarElementsWithPositions?.map((entry, index) => (
                <div key={index}>{entry.element}</div>
            ))}
            <div>{popupEditFormContent}</div>
        </section>
    ),
}));

vi.mock('../hooks/reordering.tsx', () => ({
    default: () => ({
        dataGridProps: { disableRowSelectionOnClick: true },
        loadingElement: <div>Reordenant entorns</div>,
    }),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

vi.mock('../hooks/useReadOnlyGestor.ts', () => ({
  default: () => false,
}));

describe('Entorns', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('Entorns_quanEsRenderitza_mostraLaGraellaIElFormulariBasic', () => {
        // Comprova que la pàgina d'entorns exposa les columnes bàsiques i el formulari emergent corresponent.
        render(<Entorns />);

        expect(screen.getByTestId('page-title')).toHaveTextContent('Entorns');
        expect(screen.getByRole('heading', { name: 'Entorns' })).toBeInTheDocument();
        expect(screen.getByTestId('columns')).toHaveTextContent('codi,nom');
        expect(screen.getByText('Reordenant entorns')).toBeInTheDocument();
        expect(screen.getByTestId('field-codi')).toBeInTheDocument();
        expect(screen.getByTestId('field-nom')).toBeInTheDocument();
    });
});
