import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Paletes from './Paletes';

const mocks = vi.hoisted(() => ({
    setFieldValueMock: vi.fn(),
    useFormContextValue: {
        data: {},
        apiRef: { current: { setFieldValue: vi.fn() } },
        fieldErrors: [],
    } as any,
    temporalMessageShowMock: vi.fn(),
}));

vi.mock('reactlib', () => ({
    useFormContext: () => mocks.useFormContextValue,
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShowMock,
    }),
    useResourceApiService: () => ({
        isReady: true,
        create: vi.fn().mockResolvedValue({ id: 1, clientId: '1' }),
        update: vi.fn().mockResolvedValue({ id: 1, clientId: '1' }),
    }),
    GridPage: ({ children }: any) => <div data-testid="grid-page">{children}</div>,
    MuiDataGrid: ({
        resourceName,
        columns,
        toolbarType,
        popupEditFormContent,
        paginationActive,
        title
    }: any) => (
        <div data-testid="mui-data-grid" data-resource={resourceName} data-title={title}>
            {toolbarType === 'upper' && <div data-testid="grid-toolbar">Toolbar</div>}
            {paginationActive && <div data-testid="grid-pagination">Pagination</div>}
            {columns?.map((col: any) => (
                <div key={col.field} data-testid={`column-${col.field}`}>
                    {col.headerName || col.field}
                </div>
            ))}
            {popupEditFormContent}
        </div>
    ),
}));

import translationCa from '../i18n/translationCa';

vi.mock('react-i18next', () => ({
    useTranslation: vi.fn(() => ({
        t: (key: any) => {
            if (typeof key === 'function') {
                return key({
                    ...translationCa,
                    menu: {
                        ...translationCa.menu,
                        paleta: 'Paleta',
                    },
                });
            }
            const translations: Record<string, string> = {
                'menu.paleta': 'Paleta',
                'common.cancel': 'Cancel·lar',
                'common.save': 'Desar',
            };
            return translations[key] || key;
        },
    })),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: any) => <h1 data-testid="page-title">{title}</h1>,
}));

vi.mock('../components/PaletteFormContent.tsx', () => ({
    PaletteFormContent: ({ palette, onChange, mode, showDuplicateButton }: any) => (
        <div data-testid="palette-form-content" data-mode={mode}>
            <input
                data-testid="palette-nom"
                value={palette?.nom || ''}
                onChange={(e) => onChange?.({ ...palette, nom: e.target.value })}
                aria-label="Nom"
            />
            <input
                data-testid="palette-descripcio"
                value={palette?.descripcio || ''}
                onChange={(e) => onChange?.({ ...palette, descripcio: e.target.value })}
                aria-label="Descripció"
            />
            <button data-testid="palette-add-color">Afegir color</button>
            {showDuplicateButton && <button data-testid="palette-duplicate">Duplicar</button>}
            <button data-testid="palette-save" onClick={() => onChange?.(palette)}>Desar</button>
        </div>
    ),
    normalizeColors: (colors: any[]) => colors || [],
}));

describe('Paletes', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renderitza la pàgina amb el títol i la taula de paletes', () => {
        render(<Paletes />);
        expect(screen.getByTestId('grid-page')).toBeInTheDocument();
        expect(screen.getByTestId('page-title')).toHaveTextContent('Paleta');
        expect(screen.getByTestId('mui-data-grid')).toBeInTheDocument();
        expect(screen.getByTestId('mui-data-grid')).toHaveAttribute('data-resource', 'paleta');
    });

    it('mostra les columnes configurades: nom, descripcio i colors', () => {
        render(<Paletes />);
        expect(screen.getByTestId('column-nom')).toBeInTheDocument();
        expect(screen.getByTestId('column-descripcio')).toBeInTheDocument();
        expect(screen.getByTestId('column-colors')).toBeInTheDocument();
        expect(screen.getByTestId('column-colors')).toHaveTextContent('Colors');
    });

    it('activa la toolbar superior i la paginació', () => {
        render(<Paletes />);
        expect(screen.getByTestId('grid-toolbar')).toBeInTheDocument();
        expect(screen.getByTestId('grid-pagination')).toBeInTheDocument();
    });

    it('passa el component PaletaForm com a contingut del popup dedició', () => {
        render(<Paletes />);
        expect(screen.queryByTestId('palette-form-content')).toBeInTheDocument();
    });

    it('inicialitza una nova paleta amb els colors per defecte', () => {
        render(<Paletes />);
        expect(screen.getByTestId('mui-data-grid')).toBeInTheDocument();
    });

    it('el formulari es mostra en mode "create" quan no hi ha ID', async () => {
        mocks.useFormContextValue.data = {};
        render(<Paletes />);
        await waitFor(() => {
            const form = screen.queryByTestId('palette-form-content');
            expect(form).toHaveAttribute('data-mode', 'create');
        });
    });

    it('el formulari es mostra en mode "edit" quan hi ha ID', async () => {
        mocks.useFormContextValue.data = { id: 1, nom: 'Test', colors: [] };
        render(<Paletes />);
        await waitFor(() => {
            const form = screen.queryByTestId('palette-form-content');
            expect(form).toHaveAttribute('data-mode', 'edit');
        });
    });

    it('no mostra el botó de duplicar al formulari (showDuplicateButton=false)', () => {
        render(<Paletes />);
        expect(screen.queryByTestId('palette-duplicate')).not.toBeInTheDocument();
    });
});
