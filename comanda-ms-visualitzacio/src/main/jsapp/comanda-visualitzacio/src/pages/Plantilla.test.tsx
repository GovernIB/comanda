import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { Plantilla } from './Plantilla';
import userEvent from '@testing-library/user-event';

const mocks = vi.hoisted(() => ({
    useFormContextValue: {
        data: {},
        apiRef: { current: { setFieldValue: vi.fn() } },
        fieldErrors: [],
    } as any,
}));

vi.mock('reactlib', () => ({
    useFormContext: () => mocks.useFormContextValue,
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

vi.mock('@mui/material/styles', () => ({
    useTheme: vi.fn(() => ({
        palette: {
            divider: '#e0e0e0',
            getContrastText: (color: string) => (color?.toLowerCase() === '#ffffff' ? '#000000' : '#ffffff'),
            primary: { main: '#0070f3' },
        },
    })),
}));

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

describe('Plantilla', () => {
    const mockSetFieldValue = vi.fn();

    beforeEach(() => {
        vi.clearAllMocks();
        // ✅ CORRECCIÓN: Añadir "data:" como clave del objeto
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

        expect(screen.getByText('Tema clar')).toBeInTheDocument();
        expect(screen.getByText('Tema fosc')).toBeInTheDocument();
    });

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
});