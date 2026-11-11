import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { Plantilla } from './Plantilla';

const mocks = vi.hoisted(() => ({
    isUserAdmin: true,
    renderPopupEditForm: true,
    refreshGridMock: vi.fn(),
    artifactReportMock: vi.fn(),
    iniciaDescargaJSONMock: vi.fn(),
    useFormContextValue: {
        data: {},
        apiRef: { current: { setFieldValue: vi.fn() } },
        fieldErrors: [],
        fields: [{ name: 'file' }, { name: 'conflicts' }],
        dataDispatchAction: vi.fn(),
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
        artifactReport: mocks.artifactReportMock,
    }),
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: mocks.refreshGridMock,
        },
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
    MuiDataGrid: ({ popupEditFormContent, toolbarType, toolbarElementsWithPositions, rowAdditionalActions }: any) => {
        const mockRow = { id: 1, nom: 'Plantilla 1' };
        return (
            <div data-testid="mui-data-grid">
                {toolbarType === 'upper' && <div data-testid="grid-toolbar">Toolbar</div>}
                {toolbarElementsWithPositions?.map((item: any) => (
                    <div key={item.position} data-testid={`toolbar-${item.position}`}>
                        {item.element}
                    </div>
                ))}
                {mocks.renderPopupEditForm && popupEditFormContent}
                {rowAdditionalActions?.map((action: any) => (
                    <button
                        key={action.label}
                        data-testid={`action-${action.report || action.label}`}
                        onClick={() => action.onClick?.(mockRow.id, mockRow)}
                        type="button"
                    >
                        {action.label}
                    </button>
                ))}
            </div>
        );
    },
}));

vi.mock('../components/UserContext.ts', () => ({
    useIsUserAdmin: () => mocks.isUserAdmin,
}));

vi.mock('../components/FormActionDialog.tsx', () => ({
    default: ({ title, children, apiRef }: any) => {
        if (apiRef) {
            apiRef.current = { show: vi.fn() };
        }
        return (
            <div data-testid="form-action-dialog">
                <h3>{title}</h3>
                {children}
            </div>
        );
    },
}));

vi.mock('../util/commonsActions.ts', () => ({
    iniciaDescargaJSON: (...args: any[]) => mocks.iniciaDescargaJSONMock(...args),
}));

vi.mock('../components/estadistiques/ImportConflictsForm.tsx', () => ({
    default: ({ isAnalyzing, onReanalyze, conflictsDescription }: any) => (
        <div data-testid="import-conflicts-form">
            {isAnalyzing && <div>Analyzing...</div>}
            {conflictsDescription && <div>{conflictsDescription}</div>}
            {onReanalyze && <button onClick={onReanalyze}>Tornar a analitzar</button>}
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
vi.mock('../components/estadistiques/TitolWidgetVisualization.tsx', () => ({
    default: () => <div data-testid="titol-widget-preview" />
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
        mocks.renderPopupEditForm = true;
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
        mocks.renderPopupEditForm = false;
        render(<Plantilla />);
        expect(screen.getByTestId('mui-data-grid')).toBeInTheDocument();
        expect(screen.getByTestId('grid-toolbar')).toBeInTheDocument();
    });

    it('renderiza los campos principales del formulario (nom y paletas)', () => {
        render(<Plantilla />);

        expect(screen.getByTestId('form-field-nom')).toBeInTheDocument();
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

    it('aplica el tema clar o el tema fosc actual i canvia el tema del formulari en seleccionar-lo', () => {
        render(<Plantilla />);

        expect(screen.getByTestId('palette-group-LIGHT')).toHaveStyle({ '--plantilla-bg': '#fff' });
        expect(screen.getByTestId('palette-group-DARK')).toHaveStyle({ '--plantilla-bg': '#282A36' });

        fireEvent.click(screen.getByTestId('palette-group-DARK'));

        expect(screen.getByTestId('plantilla-form-theme')).toHaveAttribute('data-theme-group', 'DARK');
        expect(screen.getByTestId('plantilla-form-theme')).toHaveStyle({ '--plantilla-bg': '#282A36' });
    });

    const getTabs = () => Array.from(document.querySelectorAll('[role="tab"]')) as HTMLElement[];

    it('permet filtrar les paletes pel nom i mostra els colors al desplegable', () => {
        render(<Plantilla />);

        const lightGroup = screen.getByTestId('palette-group-LIGHT');
        const widgetSelector = lightGroup.querySelector('input[role="combobox"]') as HTMLInputElement;
        fireEvent.focus(widgetSelector);
        fireEvent.change(widgetSelector, { target: { value: 'fosc destacat' } });

        const listbox = document.querySelector('[role="listbox"]') as HTMLElement;
        const option = within(listbox).getByText('Tema fosc destacat - widget').closest('li') as HTMLElement;
        expect(option.querySelector('[data-palette-color="#111827"]')).toBeInTheDocument();
        expect(within(listbox).queryByText('Tema clar - widget')).not.toBeInTheDocument();
    });

    it('cambia de pestaña al hacer clic en los tabs', () => {
        render(<Plantilla />);

        expect(screen.getByTestId('form-field-nom')).toBeInTheDocument();

        const tabs = getTabs();
        expect(tabs.length).toBeGreaterThanOrEqual(3);

        fireEvent.click(tabs[1]);

        expect(screen.getByTestId('widget-preview')).toBeInTheDocument();
    });

    it('la pestanya de gràfics mostra pestanyes de tipus de gràfic (no un desplegable) i BAR_CHART per defecte només té les seves propietats', () => {
        render(<Plantilla />);

        expect(screen.getByTestId('form-field-nom')).toBeInTheDocument();

        const mainTabs = getTabs();
        fireEvent.click(mainTabs[2]);

        // El desplegable de tipus de gràfic ha estat substituït per pestanyes.
        expect(screen.queryByTestId('form-field-tipusGrafic')).not.toBeInTheDocument();

        // 5 pestanyes principals + 7 pestanyes de tipus de gràfic (BAR_CHART, LINE_CHART, PIE_CHART,
        // SCATTER_CHART, SPARK_LINE_CHART, GAUGE_CHART, HEATMAP_CHART).
        const allTabs = getTabs();
        expect(allTabs).toHaveLength(12);

        // BAR_CHART (pestanya per defecte) només té 3 propietats booleanes configurables: mostrarReticula,
        // barStacked i barHorizontal. Si el filtre no s'aplica, se'n mostren 8.
        expect(document.querySelectorAll('input[type="checkbox"]')).toHaveLength(3);
    });

    it('canviar de pestanya de tipus de gràfic mostra només les propietats d’aquell tipus', () => {
        // El mock de FormField/setFieldValue no simula el re-render reactiu del formulari real: cal fer-ho
        // explícit aquí, aplicant el canvi sobre les dades i tornant a renderitzar.
        mocks.useFormContextValue.apiRef.current.setFieldValue = vi.fn((field: string, value: unknown) => {
            mocks.useFormContextValue.data = { ...mocks.useFormContextValue.data, [field]: value };
        });
        const { rerender } = render(<Plantilla />);

        expect(screen.getByTestId('form-field-nom')).toBeInTheDocument();

        const mainTabs = getTabs();
        fireEvent.click(mainTabs[2]);

        const chartTypeTabs = getTabs().slice(5);
        // chartTypesList: ["BAR_CHART", "PIE_CHART", ...] -> índex 1 és PIE_CHART.
        // PIE_CHART: outerRadius, pieDonut, innerRadius, pieShowLabels, labelSize -> 2 booleans, 3 numèrics.
        fireEvent.click(chartTypeTabs[1]);
        rerender(<Plantilla />);

        expect(document.querySelectorAll('input[type="checkbox"]')).toHaveLength(2);
    });

    it('un títol amb una fila antiga de vora única (mostrarVora) no la mostra a la graella, ja que ara es configura gràficament', () => {
        mocks.useFormContextValue.data = {
            ...mocks.useFormContextValue.data,
            styleProperties: [
                { scope: 'TITOL_1', propertyName: 'mostrarVora', valueType: 'BOOLEAN', scalarValue: 'true' },
                { scope: 'TITOL_1', propertyName: 'colorVora', valueType: 'COLOR', paletteRole: 'WIDGET', paletteIndex: 2 },
                { scope: 'TITOL_1', propertyName: 'ampleVora', valueType: 'NUMBER', scalarValue: '2' },
            ],
        };
        render(<Plantilla />);

        expect(screen.getByTestId('form-field-nom')).toBeInTheDocument();

        const tabs = getTabs();
        fireEvent.click(tabs[4]);

        expect(screen.getByTestId('property-select-TITOL_1-posicioSubtitol')).toBeInTheDocument();
        expect(document.querySelector('input[type="checkbox"]')).not.toBeInTheDocument();
    });

    it('la pestanya de Títols conté 3 subpestanyes, una per cada títol', () => {
        render(<Plantilla />);

        expect(screen.getByTestId('form-field-nom')).toBeInTheDocument();

        // Només 5 pestanyes principals (Comuns, Simple, Gràfic, Taula, Títols); les subpestanyes de
        // títol encara no existeixen perquè la pestanya de Títols no està activa.
        const mainTabs = getTabs();
        expect(mainTabs).toHaveLength(5);

        fireEvent.click(mainTabs[4]);
        expect(screen.getByTestId('property-select-TITOL_1-posicioSubtitol')).toBeInTheDocument();
        expect(screen.queryByTestId('property-select-TITOL_2-posicioSubtitol')).not.toBeInTheDocument();
        expect(screen.queryByTestId('property-select-TITOL_3-posicioSubtitol')).not.toBeInTheDocument();

        // Ara que la pestanya de Títols és activa, apareixen les 3 subpestanyes (Títol 1/2/3) a més
        // de les 5 principals.
        const allTabsWithSubtabs = getTabs();
        expect(allTabsWithSubtabs).toHaveLength(8);
        const titleSubTabs = allTabsWithSubtabs.slice(5);

        fireEvent.click(titleSubTabs[1]);
        expect(screen.getByTestId('property-select-TITOL_2-posicioSubtitol')).toBeInTheDocument();
        expect(screen.queryByTestId('property-select-TITOL_1-posicioSubtitol')).not.toBeInTheDocument();

        fireEvent.click(getTabs().slice(5)[2]);
        expect(screen.getByTestId('property-select-TITOL_3-posicioSubtitol')).toBeInTheDocument();
        expect(screen.queryByTestId('property-select-TITOL_2-posicioSubtitol')).not.toBeInTheDocument();
    });

    it('inicializa los colores por defecto al crear una nueva plantilla', () => {
        mocks.renderPopupEditForm = false;
        render(<Plantilla />);
        expect(mockSetFieldValue).toBeDefined();
    });

    it('el camp posicioSubtitol de la pestanya de títols es mostra com un desplegable amb SOTA i COSTAT', () => {
        render(<Plantilla />);

        expect(screen.getByTestId('form-field-nom')).toBeInTheDocument();

        const tabs = getTabs();
        fireEvent.click(tabs[4]);

        const select = screen.getByTestId('property-select-TITOL_1-posicioSubtitol');
        const combobox = select.querySelector('[role="combobox"]') as HTMLElement;
        fireEvent.mouseDown(combobox);

        const listbox = document.querySelector('[role="listbox"]') as HTMLElement;
        const options = Array.from(listbox.querySelectorAll('[role="option"]'));
        const optionValues = options.map((option) => option.getAttribute('data-value'));
        expect(optionValues).toEqual(['SOTA', 'COSTAT']);

        fireEvent.click(options[1]);

        expect(mockSetFieldValue).toHaveBeenCalledWith(
            'styleProperties',
            expect.arrayContaining([
                expect.objectContaining({ scope: 'TITOL_1', propertyName: 'posicioSubtitol', scalarValue: 'COSTAT' }),
            ])
        );
    });

    it('els colors de fons es poden buidar (opció de buidat al desplegable), els altres colors no', () => {
        render(<Plantilla />);

        expect(screen.getByTestId('form-field-nom')).toBeInTheDocument();

        // Pestanya "Comú": conté colorFons (color de fons) i colorText (no és color de fons).
        const openOptions = (testId: string) => {
            const select = screen.getByTestId(testId);
            fireEvent.mouseDown(select.querySelector('[role="combobox"]') as HTMLElement);
            const listboxes = document.querySelectorAll('[role="listbox"]');
            const listbox = listboxes[listboxes.length - 1] as HTMLElement;
            const options = Array.from(listbox.querySelectorAll('[role="option"]'));
            const values = options.map((option) => option.getAttribute('data-value'));
            fireEvent.keyDown(listbox, { key: 'Escape', code: 'Escape' });
            return values;
        };

        const colorFonsValues = openOptions('property-select-COMMON-colorFons');
        expect(colorFonsValues).toContain('');

        const colorTextValues = openOptions('property-select-COMMON-colorText');
        expect(colorTextValues).not.toContain('');
    });

    it('en seleccionar l\'opció de buidat d\'un color de fons, la propietat queda sense posició de paleta', () => {
        render(<Plantilla />);

        expect(screen.getByTestId('form-field-nom')).toBeInTheDocument();

        const select = screen.getByTestId('property-select-COMMON-colorFons');
        fireEvent.mouseDown(select.querySelector('[role="combobox"]') as HTMLElement);

        const listbox = document.querySelector('[role="listbox"]') as HTMLElement;
        const emptyOption = listbox.querySelector('[role="option"][data-value=""]') as HTMLElement;
        expect(emptyOption).toBeDefined();
        fireEvent.click(emptyOption);

        expect(mockSetFieldValue).toHaveBeenCalledWith(
            'styleProperties',
            expect.arrayContaining([
                expect.objectContaining({ scope: 'COMMON', propertyName: 'colorFons', paletteIndex: undefined }),
            ])
        );
    });

    it('permet configurar gràficament les vores del títol clicant cada costat del rectangle', () => {
        render(<Plantilla />);

        expect(screen.getByTestId('form-field-nom')).toBeInTheDocument();

        const tabs = getTabs();
        fireEvent.click(tabs[4]);

        // Els camps plans (checkbox/color/número) de cada costat NO s'han de mostrar directament a la graella.
        expect(document.querySelector('input[type="checkbox"]')).not.toBeInTheDocument();

        const topZone = screen.getByTestId('vora-zone-TITOL_1-Top');
        fireEvent.click(topZone);

        const dialog = document.querySelector('[role="dialog"]') as HTMLElement;
        const checkbox = dialog.querySelector('input[type="checkbox"]') as HTMLElement;
        fireEvent.click(checkbox);

        expect(mockSetFieldValue).toHaveBeenCalledWith(
            'styleProperties',
            expect.arrayContaining([
                expect.objectContaining({ scope: 'TITOL_1', propertyName: 'mostrarVoraTop', scalarValue: 'true' }),
            ])
        );
    });

    it('Plantilla_quanUsuariEsAdmin_mostraBotoImportarAlToolbar', () => {
        mocks.renderPopupEditForm = false;
        mocks.isUserAdmin = true;
        render(<Plantilla />);
        expect(screen.getByTestId('toolbar-2')).toBeInTheDocument();
        expect(screen.getByText('upload')).toBeInTheDocument();
    });

    it('Plantilla_quanUsuariNoEsAdmin_noMostraBotoImportar', () => {
        mocks.renderPopupEditForm = false;
        mocks.isUserAdmin = false;
        render(<Plantilla />);
        expect(screen.queryByTestId('toolbar-2')).not.toBeInTheDocument();
        expect(screen.queryByText('upload')).not.toBeInTheDocument();
        mocks.isUserAdmin = true;
    });

    it('Plantilla_quanEsPremExportar_cridaArtifactReportIDescarregaJSON', async () => {
        mocks.renderPopupEditForm = false;
        mocks.artifactReportMock.mockResolvedValueOnce({ fitxer: 'plantilla.json' });
        render(<Plantilla />);

        const exportButton = screen.getByTestId('action-plantilla_export');
        fireEvent.click(exportButton);

        expect(mocks.artifactReportMock).toHaveBeenCalledWith(1, {
            code: 'plantilla_export',
            fileType: 'JSON',
        });

        await waitFor(() => {
            expect(mocks.iniciaDescargaJSONMock).toHaveBeenCalledWith({ fitxer: 'plantilla.json' });
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'translated', 'success');
        });
    });

    it('Plantilla_quanEsRenderitza_renderitzaElDialegDImportacioIFormulari', () => {
        mocks.renderPopupEditForm = false;
        render(<Plantilla />);
        expect(screen.getByTestId('form-action-dialog')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-file')).toBeInTheDocument();
        expect(screen.getByTestId('import-conflicts-form')).toBeInTheDocument();
    });
});
