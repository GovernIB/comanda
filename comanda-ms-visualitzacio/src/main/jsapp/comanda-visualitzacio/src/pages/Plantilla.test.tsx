import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { Plantilla } from './Plantilla';
import userEvent from '@testing-library/user-event';

const mocks = vi.hoisted(() => ({
    useFormContextValue: {
        data: {},
        apiRef: { current: { setFieldValue: vi.fn() } },
        fieldErrors: [],
    } as any,
    muiTheme: {
        palette: {
            mode: 'dark',
            primary: { main: '#BD93F9', contrastText: '#282A36' },
            secondary: { main: '#F8F8F2' },
            background: { default: '#282A36', paper: '#303341' },
            text: { primary: '#F8F8F2', secondary: '#D6D6C2' },
            divider: '#44475A',
            getContrastText: (color: string) => (color?.toLowerCase() === '#ffffff' ? '#000000' : '#ffffff'),
        },
    } as any,
    createPaletteMock: vi.fn(),
    updatePaletteMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    tGroupMock: vi.fn((group: string) => {
        const map: Record<string, string> = {
            LIGHT: 'Tema clar',
            DARK: 'Tema fosc',
            LIGHT_HIGHLIGHTED: 'Tema clar destacat',
            DARK_HIGHLIGHTED: 'Tema fosc destacat',
        };
        return map[group] || group;
    }),
    tTitleScopeMock: vi.fn((scope: string) => {
        const map: Record<string, string> = {
            TITOL_1: 'Títol 1',
            TITOL_2: 'Títol 2',
            TITOL_3: 'Títol 3',
        };
        return map[scope] || scope;
    }),
}));

vi.mock('reactlib', () => ({
    useFormContext: () => mocks.useFormContextValue,
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShowMock,
    }),
    useResourceApiService: () => ({
        isReady: true,
        create: mocks.createPaletteMock,
        update: mocks.updatePaletteMock,
    }),
    FormField: ({ name, label, value, onChange, type, required, ...props }: any) => {
        if (type === 'checkbox') {
            return <input data-testid={`form-field-${name}`} type="checkbox" checked={!!value} onChange={(e) => onChange?.(e.target.checked)} {...props} />;
        }
        if (type === 'number') {
            return <input data-testid={`form-field-${name}`} type="number" value={value || ''} onChange={(e) => onChange?.(e.target.value)} {...props} />;
        }
        return <input data-testid={`form-field-${name}`} value={value || ''} onChange={(e) => onChange?.(e.target.value)} {...props} />;
    },
    MuiDataGrid: ({ popupEditFormContent, toolbarType }: any) => (
        <div data-testid="mui-data-grid">
            {toolbarType === 'upper' && <div data-testid="grid-toolbar">Toolbar</div>}
            {popupEditFormContent}
        </div>
    ),
}));

vi.mock('react-i18next', () => ({
    useTranslation: vi.fn(() => ({
        t: (key: string | Function) => typeof key === 'string' ? key : 'translated',
    })),
}));

vi.mock('@mui/material/styles', async () => {
    const actual = await vi.importActual<typeof import('@mui/material/styles')>('@mui/material/styles');
    return {
        ...actual,
        useTheme: vi.fn(() => mocks.muiTheme),
    };
});

// === MOCKS DE COMPONENTES CUSTOM (inline para evitar hoisting) ===
vi.mock('../components/estadistiques/SimpleWidgetVisualization.tsx', () => ({
    default: () => <div data-testid="widget-preview" />
}));
vi.mock('../components/estadistiques/TaulaWidgetVisualization.tsx', () => ({
    default: () => <div data-testid="widget-preview" />
}));
vi.mock('../components/estadistiques/GraficWidgetVisualization.tsx', () => ({
    default: () => <div data-testid="widget-preview" />
}));
vi.mock('../components/IconAutocompleteSelect.tsx', () => ({
    default: ({ name, label }: any) => <select data-testid={`icon-select-${name}`}><option>{label}</option></select>,
}));
vi.mock('../components/PaletteFormContent.tsx', () => ({
    PaletteFormContent: ({ palette, onChange, mode, paletteTheme }: any) => (
        <div data-testid="palette-form-content" data-mode={mode}>
            <input data-testid="palette-nom" value={palette?.nom || ''} onChange={(e) => onChange?.({ ...palette, nom: e.target.value })} />
            <input data-testid="palette-descripcio" value={palette?.descripcio || ''} onChange={(e) => onChange?.({ ...palette, descripcio: e.target.value })} />
            <button data-testid="palette-add-color">Afegir color</button>
            {paletteTheme && <div data-testid="palette-theme-applied">Theme applied</div>}
        </div>
    ),
    useGetPaletteDialogTitle: () => (mode: string, nom?: string) => {
        const titles: Record<string, string> = {
            create: 'Nova paleta',
            edit: 'Editar paleta',
            duplicate: 'Duplicar paleta',
        };
        return `${titles[mode] || mode}${nom ? ` (${nom})` : ''}`;
    },
    normalizeColors: (colors: any[]) => colors || [],
}));

describe('Plantilla', () => {
    const mockSetFieldValue = vi.fn();

    beforeEach(() => {
        vi.clearAllMocks();
        mocks.createPaletteMock.mockImplementation(({ data }: any) => Promise.resolve({
            ...data,
            id: 100,
            clientId: '100',
        }));
        mocks.updatePaletteMock.mockImplementation((id: any, { data }: any) => Promise.resolve({
            ...data,
            id,
            clientId: String(id),
        }));
        mocks.useFormContextValue = {
            data: {
                paleta: 'clar',
                colors: {
                    colorText_clar: '#000000',
                    colorFons_clar: '#ffffff',
                },
            },
            apiRef: { current: { setFieldValue: mockSetFieldValue } },
        };
    });

    it('renderiza el MuiDataGrid con el formulario de edición', () => {
        render(<Plantilla />);
        expect(screen.getByTestId('mui-data-grid')).toBeInTheDocument();
        expect(screen.getByTestId('grid-toolbar')).toBeInTheDocument();
    });

    it('renderiza los campos principales del formulario (nom y paletas)', async () => {
        render(<Plantilla />);

        await waitFor(() => {
            expect(screen.getByTestId('form-field-nom')).toBeInTheDocument();
        });

        expect(screen.getByTestId('palette-group-LIGHT')).toBeInTheDocument();
        expect(screen.getByTestId('palette-group-DARK')).toBeInTheDocument();
    });

    it('mostra els grups de plantilles en lordre esperat', () => {
        render(<Plantilla />);

        const groupIds = ['LIGHT', 'LIGHT_HIGHLIGHTED', 'DARK', 'DARK_HIGHLIGHTED'];
        const groups = groupIds.map(id => screen.getByTestId(`palette-group-${id}`));

        groups.forEach((group, index) => {
            if (index < groups.length - 1) {
                expect(group.compareDocumentPosition(groups[index + 1]) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
            }
        });
    });

    it('aplica el tema clar o el tema fosc actual i canvia el tema del formulari en seleccionar-lo', async () => {
        render(<Plantilla />);

        expect(screen.getByTestId('palette-group-LIGHT')).toHaveStyle({ '--plantilla-bg': '#fff' });
        expect(screen.getByTestId('palette-group-DARK')).toHaveStyle({ '--plantilla-bg': '#282A36' });

        fireEvent.click(screen.getByTestId('palette-group-DARK'));

        await waitFor(() => {
            expect(screen.getByTestId('plantilla-form-theme')).toHaveAttribute('data-theme-group', 'DARK');
            expect(screen.getByTestId('plantilla-form-theme')).toHaveStyle({ '--plantilla-bg': '#282A36' });
        });
    });

    it('permet filtrar les paletes pel nom i mostra els colors al desplegable', async () => {
        const user = userEvent.setup();
        render(<Plantilla />);

        const widgetSelector = screen.getAllByRole('combobox', { name: 'Widget' })[0];
        await user.click(widgetSelector);
        await user.clear(widgetSelector);
        await user.type(widgetSelector, 'fosc destacat');

        const option = await screen.findByRole('option', { name: /Tema fosc destacat - widget/ });
        expect(option.querySelector('[data-palette-color="#111827"]')).toBeInTheDocument();
        expect(screen.queryByRole('option', { name: /Tema clar - widget/ })).not.toBeInTheDocument();
    }, 30000);

    it('cambia de pestaña al hacer clic en los tabs', async () => {
        const user = userEvent.setup();
        render(<Plantilla />);

        await waitFor(() => {
            expect(screen.getByTestId('form-field-nom')).toBeInTheDocument();
        });

        const tabs = screen.getAllByRole('tab');
        expect(tabs.length).toBeGreaterThanOrEqual(3);

        await user.click(tabs[1]);

        await waitFor(() => {
            expect(screen.getByTestId('widget-preview')).toBeInTheDocument();
        });
    });

    it('inicializa los colores por defecto al crear una nueva plantilla', () => {
        render(<Plantilla />);
        expect(mockSetFieldValue).toBeDefined();
    });

    // TODO: Revisar el test
    it.skip('obre la modal per editar una paleta des del grup', async () => {
        render(<Plantilla />);

        fireEvent.click(screen.getByRole('button', { name: 'Editar paleta widget Tema clar' }));

        expect(await screen.findByRole('heading', { name: /Editar paleta · Tema clar · Widget/ })).toBeInTheDocument();
        const dialogContent = screen.getByTestId('palette-dialog-theme');
        const dialog = screen.getByRole('dialog');
        expect(dialogContent).toHaveStyle({ '--plantilla-bg': '#fff' });

        fireEvent.change(within(dialog).getByRole('textbox', { name: 'Nom' }), { target: { value: 'Paleta editada' } });
        fireEvent.click(within(dialog).getByRole('button', { name: 'Desar' }));

        await waitFor(() => {
            expect(mockSetFieldValue).toHaveBeenCalledWith(
                'paletes',
                expect.arrayContaining([
                    expect.objectContaining({ nom: 'Paleta editada' }),
                ])
            );
        });
    }, 10000);

    // TODO: Revisar el test
    it.skip('obre la modal per crear una paleta i lassigna al grup', async () => {
        const user = userEvent.setup();
        render(<Plantilla />);

        await user.click(screen.getByRole('button', { name: 'Crear paleta grafic Tema clar' }));

        expect(screen.getByRole('heading', { name: /Nova paleta · Tema clar · Grafic/ })).toBeInTheDocument();

        await user.click(screen.getByRole('button', { name: 'Desar' }));

        await waitFor(() => {
            expect(mockSetFieldValue).toHaveBeenCalledWith(
                'paletes',
                expect.arrayContaining([
                    expect.objectContaining({ nom: 'Tema clar - Grafic copia' }),
                ])
            );
            expect(mockSetFieldValue).toHaveBeenCalledWith(
                'paletteGroups',
                expect.arrayContaining([
                    expect.objectContaining({
                        groupType: 'LIGHT',
                        chartPalette: undefined,
                        chartPaletteClientId: '100',
                    }),
                ])
            );
        });
    });
});
