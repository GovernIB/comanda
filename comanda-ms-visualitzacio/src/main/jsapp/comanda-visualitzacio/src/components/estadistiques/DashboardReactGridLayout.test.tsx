import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import {
    DASHBOARD_DESIGN_WIDTH,
    dashboardRowHeight,
    DashboardReactGridLayout,
    horizontalSubdivisions,
    useMapDashboardItems,
} from './DashboardReactGridLayout';

const mocks = vi.hoisted(() => ({
    responsiveProps: null as any,
    isEqualMock: vi.fn(),
    resizeObserverCallbacksByNode: new Map<Element, ResizeObserverCallback>(),
}));

/** Simula que el ResizeObserver del node amb el testid indicat notifica una nova amplada/alçada. */
const triggerResize = (testId: string, width: number, height = 0) => {
    const node = screen.getByTestId(testId);
    const callback = mocks.resizeObserverCallbacksByNode.get(node);
    callback?.(
        [{contentRect: {width, height}} as ResizeObserverEntry],
        {} as ResizeObserver
    );
};

vi.mock('react-grid-layout', () => ({
    WidthProvider: (component: React.ComponentType<any>) => component,
    Responsive: (props: any) => {
        mocks.responsiveProps = props;
        return <div data-testid="responsive-grid">{props.children}</div>;
    },
}));

vi.mock('lodash', () => ({
    isEqual: (...args: unknown[]) => mocks.isEqualMock(...args),
    throttle: (fn: (...args: unknown[]) => void) => {
        const throttled = (...args: unknown[]) => fn(...args);
        throttled.cancel = () => undefined;
        return throttled;
    },
}));

vi.mock('./SimpleWidgetVisualization.tsx', () => ({
    default: ({ titol }: { titol?: string }) => <div>Simple:{titol}</div>,
}));

vi.mock('./GraficWidgetVisualization.tsx', () => ({
    default: ({ titol }: { titol?: string }) => <div>Grafic:{titol}</div>,
}));

vi.mock('./TaulaWidgetVisualization.tsx', () => ({
    default: ({ titol }: { titol?: string }) => <div>Taula:{titol}</div>,
}));

vi.mock('./TitolWidgetVisualization.tsx', () => ({
    default: ({ titol }: { titol?: string }) => <div>Titol:{titol}</div>,
}));

vi.mock('react-error-boundary', () => ({
    ErrorBoundary: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

vi.mock('reactlib', () => ({
    MuiFormDialog: ({ children }: { children?: React.ReactNode }) => <div>{children}</div>,
    useBaseAppContext: () => ({
        messageDialogShow: vi.fn(() => Promise.resolve(false)),
        temporalMessageShow: vi.fn(),
    }),
    useConfirmDialogButtons: () => [],
    useResourceApiService: () => ({
        delete: vi.fn(() => Promise.resolve()),
    }),
    envVar: vi.fn(),
    ContainerAuthProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
    KeycloakAuthProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) =>
            selector({
                common: { modify: 'Modificar', duplicate: 'Duplicar', delete: 'Eliminar' },
                page: { dashboards: { action: { llistarTitle: { label: 'Editar títol' } } } },
            }),
    }),
}));

vi.mock('../../pages/EstadisticaDashboardEdit.tsx', () => ({
    AfegirTitolFormContent: () => <div>Formulari títol</div>,
    useSimpleWidgetFormDialog: () => ({ handleOpen: vi.fn(), dialog: null }),
    useGraficWidgetFormDialog: () => ({ handleOpen: vi.fn(), dialog: null }),
    useTaulaWidgetFormDialog: () => ({ handleOpen: vi.fn(), dialog: null }),
}));

vi.mock('../salut/SalutErrorBoundaryFallback', () => ({
    SalutErrorBoundaryFallback: () => <div>Error fallback</div>,
}));

describe('useMapDashboardItems', () => {
    it('useMapDashboardItems_quanRepWidgets_elsTransformaAlFormatDelGrid', () => {
        // Comprova que el hook transforma els widgets rebuts al model que espera el grid.
        const { result } = renderHook(() =>
            useMapDashboardItems([
                {
                    dashboardItemId: 12,
                    posX: 1,
                    posY: 2,
                    width: 3,
                    height: 4,
                    tipus: 'SIMPLE',
                },
            ])
        );

        expect(result.current).toEqual([
            {
                id: '12',
                x: 1,
                y: 2,
                w: 3,
                h: 4,
                type: 'SIMPLE',
            },
        ]);
    });
});

describe('DashboardReactGridLayout', () => {
    beforeEach(() => {
        HTMLCanvasElement.prototype.getContext = vi.fn(() => ({
            clearRect: vi.fn(),
            beginPath: vi.fn(),
            moveTo: vi.fn(),
            lineTo: vi.fn(),
            stroke: vi.fn(),
            strokeStyle: '',
            lineWidth: 1,
        })) as any;

        // Comportament per defecte fidel a isEqual real (comparació estructural): useSizeTracker (usat per
        // DashboardScaledCanvas) també depèn d'isEqual per detectar canvis de mida, i si es deixés sempre a
        // `true` (com fan alguns tests per a la comparació de layouts) mai detectaria cap resize simulat.
        // Els tests que necessiten forçar un resultat concret ho poden sobreescriure amb mockReturnValue.
        mocks.isEqualMock.mockImplementation((a: unknown, b: unknown) => JSON.stringify(a) === JSON.stringify(b));

        // DashboardScaledCanvas usa useSizeTracker (ResizeObserver) per escalar el canvas. Es guarda el
        // callback associat a cada node observat perquè els tests puguin simular una mida concreta.
        mocks.resizeObserverCallbacksByNode = new Map();
        class ResizeObserverMock {
            callback: ResizeObserverCallback;
            constructor(callback: ResizeObserverCallback) {
                this.callback = callback;
            }
            observe(node: Element) {
                mocks.resizeObserverCallbacksByNode.set(node, this.callback);
            }
            disconnect() {}
        }
        vi.stubGlobal('ResizeObserver', ResizeObserverMock);
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('DashboardReactGridLayout_lagraellaTe60ColumnesIFilesQuadrades', () => {
        // Regressió: la graella ha de tenir 60 columnes d'amplada i files el més quadrades possible a
        // l'amplada de disseny (1920px / 60 columnes = 32px de costat, tant en ample com en alt).
        mocks.isEqualMock.mockReturnValue(true);

        render(
            <DashboardReactGridLayout
                dashboardId={1}
                editable={false}
                dashboardWidgets={[]}
                gridLayoutItems={[]}
            />
        );

        expect(horizontalSubdivisions).toBe(60);
        expect(DASHBOARD_DESIGN_WIDTH).toBe(1920);
        expect(dashboardRowHeight).toBe(DASHBOARD_DESIGN_WIDTH / horizontalSubdivisions);
        expect(mocks.responsiveProps.cols).toEqual({ md: horizontalSubdivisions });
        expect(mocks.responsiveProps.rowHeight).toBe(dashboardRowHeight);
    });

    it('DashboardReactGridLayout_quanElModeEsCenteredIPantallaMesEstreta_noEncongeixIPermetScrollHoritzontal', () => {
        // Regressió: en mode 'centered', a pantalles més estretes que 1920px NO s'ha d'escalar cap avall
        // (a diferència del mode 'fit'); s'ha de mantenir la mida real i mostrar scroll horitzontal.

        render(
            <DashboardReactGridLayout
                dashboardId={1}
                editable={false}
                dashboardWidgets={[]}
                gridLayoutItems={[]}
                largeScreenMode="centered"
            />
        );

        act(() => triggerResize('dashboard-scale-container', 800));

        expect(screen.getByTestId('dashboard-scaled-box')).toHaveStyle({ width: '1920px' });
        expect(screen.getByTestId('dashboard-scale-design')).toHaveStyle({ transform: 'scale(1)' });
        expect(screen.getByTestId('dashboard-scale-container')).toHaveStyle({ overflowX: 'auto' });
    });

    it('DashboardReactGridLayout_quanElModeEsFitIPantallaMesEstreta_escalaCapAvall', () => {
        // En mode 'fit', a diferència de 'centered', sí que s'ha d'escalar cap avall per ajustar-se.

        render(
            <DashboardReactGridLayout
                dashboardId={1}
                editable={false}
                dashboardWidgets={[]}
                gridLayoutItems={[]}
                largeScreenMode="fit"
            />
        );

        act(() => triggerResize('dashboard-scale-container', 960));

        expect(screen.getByTestId('dashboard-scaled-box')).toHaveStyle({ width: '960px' });
        expect(screen.getByTestId('dashboard-scale-design')).toHaveStyle({ transform: 'scale(0.5)' });
    });

    it('DashboardReactGridLayout_quanEsRenderitza_mostraElsWidgetsSegonsElTipus', () => {
        // Verifica que el component escull el renderitzador correcte per a cada tipus de widget.
        mocks.isEqualMock.mockReturnValue(true);

        render(
            <DashboardReactGridLayout
                dashboardId={1}
                editable={false}
                dashboardWidgets={[
                    { dashboardItemId: 1, titol: 'Widget simple', tipus: 'SIMPLE' },
                    { dashboardTitolId: 2, titol: 'Títol principal', tipus: 'TITOL' },
                ]}
                gridLayoutItems={[
                    { id: '1', type: 'SIMPLE', x: 0, y: 0, w: 2, h: 2 },
                    { id: '2', type: 'TITOL', x: 2, y: 0, w: 2, h: 1 },
                ]}
            />
        );

        expect(screen.getByText('Simple:Widget simple')).toBeInTheDocument();
        expect(screen.getByText('Titol:Títol principal')).toBeInTheDocument();
    });

    it('DashboardReactGridLayout_quanCanviaElLayout_notificaElsNousItemsMapejats', () => {
        // Comprova que el callback rep el layout actualitzat preservant el tipus conegut de cada ítem.
        mocks.isEqualMock.mockReturnValue(false);
        const onGridLayoutItemsChange = vi.fn();

        render(
            <DashboardReactGridLayout
                dashboardId={1}
                editable={true}
                dashboardWidgets={[{ dashboardItemId: 1, titol: 'Widget simple', tipus: 'SIMPLE' }]}
                gridLayoutItems={[{ id: '1', type: 'SIMPLE', x: 0, y: 0, w: 2, h: 2 }]}
                onGridLayoutItemsChange={onGridLayoutItemsChange}
            />
        );

        act(() => {
            mocks.responsiveProps.onLayoutChange([], {
                md: [{ i: '1', x: 4, y: 5, w: 6, h: 7 }],
            });
        });

        expect(onGridLayoutItemsChange).toHaveBeenCalledWith([
            { id: '1', type: 'SIMPLE', x: 4, y: 5, w: 6, h: 7 },
        ]);
    });

    it('DashboardReactGridLayout_quanEsFaUnClicSenseMoviment_seleccionaLItem', async () => {
        // Reprodueix el bug reportat: un simple clic (sense arrossegament) ha de seleccionar l'element.
        // react-grid-layout no distingeix clic d'arrossegament pels seus propis mitjans: la detecció es
        // fa comparant la posició del ratolí entre onDragStart i onDragStop (veure DashboardReactGridLayout.tsx).
        // La selecció s'ajorna un tick (setTimeout) perquè no interfereixi amb la pròpia transició
        // d'estat de ReactGridLayout, per això el test espera amb waitFor.
        mocks.isEqualMock.mockReturnValue(true);
        const onSelectItem = vi.fn();

        render(
            <DashboardReactGridLayout
                dashboardId={1}
                editable={true}
                dashboardWidgets={[{ dashboardItemId: 1, titol: 'Widget simple', tipus: 'SIMPLE' }]}
                gridLayoutItems={[{ id: '1', type: 'SIMPLE', x: 0, y: 0, w: 2, h: 2 }]}
                onSelectItem={onSelectItem}
            />
        );

        const oldItem = { i: '1', x: 0, y: 0, w: 2, h: 2 };
        act(() => {
            mocks.responsiveProps.onDragStart([], oldItem, oldItem, oldItem, { clientX: 100, clientY: 100 });
            mocks.responsiveProps.onDragStop([], oldItem, oldItem, oldItem, { clientX: 100, clientY: 100 });
        });

        await waitFor(() => {
            expect(onSelectItem).toHaveBeenCalledWith(
                expect.objectContaining({ dashboardItemId: 1 })
            );
        });
    });

    it('DashboardReactGridLayout_quanHiHaMovimentEntreOnDragStartIOnDragStop_noSelecciona', () => {
        // Comprova que un arrossegament (moviment per sobre del llindar) no dispara la selecció.
        mocks.isEqualMock.mockReturnValue(true);
        const onSelectItem = vi.fn();

        render(
            <DashboardReactGridLayout
                dashboardId={1}
                editable={true}
                dashboardWidgets={[{ dashboardItemId: 1, titol: 'Widget simple', tipus: 'SIMPLE' }]}
                gridLayoutItems={[{ id: '1', type: 'SIMPLE', x: 0, y: 0, w: 2, h: 2 }]}
                onSelectItem={onSelectItem}
            />
        );

        const oldItem = { i: '1', x: 0, y: 0, w: 2, h: 2 };
        const newItem = { i: '1', x: 3, y: 0, w: 2, h: 2 };
        act(() => {
            mocks.responsiveProps.onDragStart([], oldItem, oldItem, oldItem, { clientX: 100, clientY: 100 });
            mocks.responsiveProps.onDragStop([], oldItem, newItem, newItem, { clientX: 160, clientY: 100 });
        });

        expect(onSelectItem).not.toHaveBeenCalled();
    });

    it('DashboardReactGridLayout_quanEsFaClicDretSobreUnComponent_mostraElMenuContextualAmbLes3Opcions', () => {
        // Verifica que el clic dret obre el menú contextual amb Modificar/Duplicar/Eliminar.
        mocks.isEqualMock.mockReturnValue(true);

        render(
            <DashboardReactGridLayout
                dashboardId={1}
                editable={true}
                dashboardWidgets={[{ dashboardItemId: 1, titol: 'Widget simple', tipus: 'SIMPLE' }]}
                gridLayoutItems={[{ id: '1', type: 'SIMPLE', x: 0, y: 0, w: 2, h: 2 }]}
            />
        );

        fireEvent.contextMenu(screen.getByTestId('grid-item'), { clientX: 50, clientY: 60 });

        expect(screen.getByText('Modificar')).toBeInTheDocument();
        expect(screen.getByText('Duplicar')).toBeInTheDocument();
        expect(screen.getByText('Eliminar')).toBeInTheDocument();
    });

    it('DashboardReactGridLayout_quanEsPremModificarAlMenuContextual_seleccionaLElement', () => {
        mocks.isEqualMock.mockReturnValue(true);
        const onSelectItem = vi.fn();

        render(
            <DashboardReactGridLayout
                dashboardId={1}
                editable={true}
                dashboardWidgets={[{ dashboardItemId: 1, titol: 'Widget simple', tipus: 'SIMPLE' }]}
                gridLayoutItems={[{ id: '1', type: 'SIMPLE', x: 0, y: 0, w: 2, h: 2 }]}
                onSelectItem={onSelectItem}
            />
        );

        fireEvent.contextMenu(screen.getByTestId('grid-item'), { clientX: 50, clientY: 60 });
        fireEvent.click(screen.getByText('Modificar'));

        expect(onSelectItem).toHaveBeenCalledWith(expect.objectContaining({ dashboardItemId: 1 }));
    });

    it('DashboardReactGridLayout_quanEsPremDuplicarAlMenuContextual_cridaOnDuplicateItem', () => {
        mocks.isEqualMock.mockReturnValue(true);
        const onDuplicateItem = vi.fn();

        render(
            <DashboardReactGridLayout
                dashboardId={1}
                editable={true}
                dashboardWidgets={[{ dashboardItemId: 1, titol: 'Widget simple', tipus: 'SIMPLE' }]}
                gridLayoutItems={[{ id: '1', type: 'SIMPLE', x: 0, y: 0, w: 2, h: 2 }]}
                onDuplicateItem={onDuplicateItem}
            />
        );

        fireEvent.contextMenu(screen.getByTestId('grid-item'), { clientX: 50, clientY: 60 });
        fireEvent.click(screen.getByText('Duplicar'));

        expect(onDuplicateItem).toHaveBeenCalledWith(expect.objectContaining({ dashboardItemId: 1 }));
    });

    it('DashboardReactGridLayout_quanEsPremEliminarAlMenuContextual_cridaOnDeleteItem', () => {
        mocks.isEqualMock.mockReturnValue(true);
        const onDeleteItem = vi.fn();

        render(
            <DashboardReactGridLayout
                dashboardId={1}
                editable={true}
                dashboardWidgets={[{ dashboardItemId: 1, titol: 'Widget simple', tipus: 'SIMPLE' }]}
                gridLayoutItems={[{ id: '1', type: 'SIMPLE', x: 0, y: 0, w: 2, h: 2 }]}
                onDeleteItem={onDeleteItem}
            />
        );

        fireEvent.contextMenu(screen.getByTestId('grid-item'), { clientX: 50, clientY: 60 });
        fireEvent.click(screen.getByText('Eliminar'));

        expect(onDeleteItem).toHaveBeenCalledWith(expect.objectContaining({ dashboardItemId: 1 }));
    });

    it('DashboardReactGridLayout_quanNoEsEditable_noObreElMenuContextual', () => {
        mocks.isEqualMock.mockReturnValue(true);

        render(
            <DashboardReactGridLayout
                dashboardId={1}
                editable={false}
                dashboardWidgets={[{ dashboardItemId: 1, titol: 'Widget simple', tipus: 'SIMPLE' }]}
                gridLayoutItems={[{ id: '1', type: 'SIMPLE', x: 0, y: 0, w: 2, h: 2 }]}
            />
        );

        fireEvent.contextMenu(screen.getByTestId('grid-item'), { clientX: 50, clientY: 60 });

        expect(screen.queryByText('Modificar')).not.toBeInTheDocument();
    });

    it('DashboardReactGridLayout_quanEsPassaBackgroundColor_lAplicaAlContenidorDelCanvas', () => {
        // El color de fons del dashboard s'ha d'aplicar al contenidor del canvas, no només als widgets.
        mocks.isEqualMock.mockReturnValue(true);

        render(
            <DashboardReactGridLayout
                dashboardId={1}
                editable={false}
                dashboardWidgets={[]}
                gridLayoutItems={[]}
                backgroundColor="#ff0000"
            />
        );

        expect(screen.getByTestId('dashboard-canvas')).toHaveStyle({ backgroundColor: '#ff0000' });
    });

    it('DashboardReactGridLayout_quanNoHiHaBackgroundColorConfigurat_usaElFonsPerDefecteDelTemaActual', () => {
        // Sense color de fons configurat al dashboard, el canvas ha de reflectir igualment el tema
        // (clar/fosc) actiu, en lloc de quedar transparent i mostrar el fons real de l'aplicació.
        mocks.isEqualMock.mockReturnValue(true);

        const { rerender } = render(
            <ThemeProvider theme={createTheme({ palette: { mode: 'light' } })}>
                <DashboardReactGridLayout dashboardId={1} editable={false} dashboardWidgets={[]} gridLayoutItems={[]} />
            </ThemeProvider>
        );
        const lightBackground = getComputedStyle(screen.getByTestId('dashboard-canvas')).backgroundColor;

        rerender(
            <ThemeProvider theme={createTheme({ palette: { mode: 'dark' } })}>
                <DashboardReactGridLayout dashboardId={1} editable={false} dashboardWidgets={[]} gridLayoutItems={[]} />
            </ThemeProvider>
        );
        const darkBackground = getComputedStyle(screen.getByTestId('dashboard-canvas')).backgroundColor;

        expect(lightBackground).not.toBe(darkBackground);
    });
});
