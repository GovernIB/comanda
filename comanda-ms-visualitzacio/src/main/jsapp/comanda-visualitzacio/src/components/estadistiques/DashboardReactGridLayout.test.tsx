import React from 'react';
import { render, screen } from '@testing-library/react';
import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
    DashboardReactGridLayout,
    useMapDashboardItems,
} from './DashboardReactGridLayout';

const mocks = vi.hoisted(() => ({
    responsiveProps: null as any,
    isEqualMock: vi.fn(),
}));

vi.mock('react-grid-layout', () => ({
    WidthProvider: (component: React.ComponentType<any>) => component,
    Responsive: (props: any) => {
        mocks.responsiveProps = props;
        return <div data-testid="responsive-grid">{props.children}</div>;
    },
}));

vi.mock('lodash', () => ({
    isEqual: (...args: unknown[]) => mocks.isEqualMock(...args),
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
            selector({ page: { dashboards: { action: { llistarTitle: { label: 'Editar títol' } } } } }),
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
});
