import * as React from 'react';
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import Rendiment from './Rendiment';
import translationCa from '../../i18n/translationCa';
import { PerformanceMetricsResponse } from './types';
import { MUI_AXIS_WORKAROUND_HEIGHT } from '../../util/muiWorkarounds';

const clickTreeCheckbox = (nodeTestId: string): void => {
    const node = screen.getByTestId(nodeTestId);
    const checkbox = node.querySelector('input[type="checkbox"]');
    if (!checkbox) {
        throw new Error(`No checkbox found inside ${nodeTestId}`);
    }
    fireEvent.click(checkbox);
};

const mocks = vi.hoisted(() => ({
    requestHrefMock: vi.fn(),
    lastChartSeries: [] as Array<{ id?: string | number; label?: unknown }>,
    lastXAxis: [] as Array<{ id?: string | number; height?: number }>,
    lastYAxis: [] as Array<{ id?: string | number; position?: string }>,
    lastMargin: undefined as { top?: number; right?: number; bottom?: number; left?: number } | undefined,
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: unknown) =>
            typeof selector === 'function'
                ? (selector as (tr: typeof translationCa) => string)(translationCa)
                : String(selector),
    }),
}));

vi.mock('reactlib', () => ({
    useResourceApiContext: () => ({
        requestHref: mocks.requestHrefMock,
    }),
}));

vi.mock('../../util/requestUtils', () => ({
    buildHref: (path: string) => `/api/${path}`,
}));

vi.mock('../../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

vi.mock('@mui/x-charts', () => ({
    ChartsDataProvider: ({
        children,
        series,
        xAxis,
        yAxis,
        margin,
    }: {
        children: React.ReactNode;
        series: Array<{ id?: string | number; label?: unknown }>;
        xAxis: Array<{ id?: string | number; height?: number }>;
        yAxis: Array<{ id?: string | number; position?: string }>;
        margin?: { top?: number; right?: number; bottom?: number; left?: number };
    }) => {
        const isMain = series.some(s => !String(s.id ?? '').startsWith('preview-'));
        if (isMain) {
            mocks.lastChartSeries = series;
            mocks.lastXAxis = xAxis;
            mocks.lastYAxis = yAxis;
            mocks.lastMargin = margin;
            return (
                <div data-testid="charts-data-provider" data-series-count={series.length}>
                    {series.map(s => (
                        <span key={String(s.id)} data-testid={`series-${s.id}`}>
                            {String(s.label ?? '')}
                        </span>
                    ))}
                    {children}
                </div>
            );
        }
        return <div data-testid="preview-charts-provider">{children}</div>;
    },
    ChartsSurface: ({ children }: { children: React.ReactNode }) => (
        <div data-testid="charts-surface">{children}</div>
    ),
    ChartsGrid: () => <div data-testid="charts-grid" />,
    AreaPlot: () => <div data-testid="area-plot" />,
    BarPlot: ({ renderer }: { renderer?: string }) => (
        <div data-testid="bar-plot" data-renderer={renderer} />
    ),
    LinePlot: () => <div data-testid="line-plot" />,
    MarkPlot: () => <div data-testid="mark-plot" />,
    ChartsAxisHighlight: () => <div data-testid="axis-highlight" />,
    ChartsBrushOverlay: () => <div data-testid="brush-overlay" />,
    ChartsXAxis: () => <div data-testid="charts-x-axis" />,
    ChartsYAxis: ({ axisId }: { axisId?: string | number }) => (
        <div data-testid={`charts-y-axis-${axisId}`} />
    ),
    ChartsTooltip: () => <div data-testid="charts-tooltip" />,
    useBrush: () => null,
    useDrawingArea: () => ({ left: 0, top: 0, width: 500, height: 300, bottom: 0, right: 0 }),
}));

const mockFetchSuccess = (payload: PerformanceMetricsResponse) => {
    mocks.requestHrefMock.mockResolvedValue({
        data: payload,
    });
    return mocks.requestHrefMock;
};

describe('Rendiment', () => {
    beforeEach(() => {
        mocks.requestHrefMock.mockReset();
        mocks.lastChartSeries = [];
        mocks.lastXAxis = [];
        mocks.lastYAxis = [];
        mocks.lastMargin = undefined;
    });

    afterEach(() => {
        vi.restoreAllMocks();
        vi.unstubAllGlobals();
        vi.clearAllMocks();
    });

    it('Rendiment_quanCapDatasourceTeEstadistiquesActivesISenseWorkers_mostraAvisDesactivat', async () => {
        mockFetchSuccess({
            collectedAt: '2026-11-19T12:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 1,
            datasources: [
                {
                    poolName: 'comandaDB',
                    jndiName: 'java:jboss/datasources/comandaDS',
                    statisticsEnabled: false,
                    waitCountType: 'INTERVAL_DELTA',
                    minSize: 5,
                    maxSize: 50,
                    history: [],
                },
                {
                    poolName: 'ExampleDS',
                    jndiName: 'java:jboss/datasources/ExampleDS',
                    statisticsEnabled: false,
                    waitCountType: 'INTERVAL_DELTA',
                    minSize: 1,
                    maxSize: 20,
                    history: [],
                },
            ],
            workerPools: [],
            jmsQueues: [],
        });

        render(<Rendiment />);

        expect(
            await screen.findByText('Estadístiques dels datasources desactivades')
        ).toBeInTheDocument();
        expect(screen.getByText(/comandaDB \(5–50\)/)).toBeInTheDocument();
        expect(screen.getByText(/ExampleDS \(1–20\)/)).toBeInTheDocument();
        expect(screen.queryByTestId('charts-data-provider')).not.toBeInTheDocument();

        const alert = screen.getByTestId('ds-stats-disabled-alert');
        fireEvent.click(within(alert).getByRole('button'));
        expect(screen.queryByTestId('ds-stats-disabled-alert')).not.toBeInTheDocument();
    });

    it('Rendiment_quanDatasourcesDesactivatsIWorkersJmsActius_mostraAvisBdIGraficWorkersJms', async () => {
        mockFetchSuccess({
            collectedAt: '2026-11-20T09:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 1,
            datasources: [
                {
                    poolName: 'comandaDB',
                    statisticsEnabled: false,
                    minSize: 5,
                    maxSize: 50,
                    history: [],
                },
            ],
            workerPools: [
                {
                    poolName: 'salutWorkerExecutor',
                    threadNamePrefix: 'salut-worker-',
                    poolType: 'EXECUTOR',
                    coreSize: 5,
                    maxSize: 5,
                    poolSize: 5,
                    activeCount: 2,
                    queueSize: 3,
                    queueCapacity: 1000,
                    completedTasks: 4,
                    history: [
                        {
                            timestamp: '2026-11-20T08:59:58',
                            poolSize: 5,
                            activeCount: 1,
                            queueSize: 0,
                            completedTasks: 2,
                        },
                        {
                            timestamp: '2026-11-20T09:00:00',
                            poolSize: 5,
                            activeCount: 2,
                            queueSize: 3,
                            completedTasks: 4,
                        },
                    ],
                },
            ],
            jmsQueues: [
                {
                    queueName: 'tasquesQueue',
                    listenerCount: 1,
                    minConsumers: 5,
                    maxConsumers: 50,
                    consumerCount: 5,
                    deliveringCount: 2,
                    pendingCount: 6,
                    completedMessages: 10,
                    addedMessages: 12,
                    history: [
                        {
                            timestamp: '2026-11-20T08:59:58',
                            consumerCount: 5,
                            deliveringCount: 1,
                            pendingCount: 0,
                            completedMessages: 5,
                            addedMessages: 5,
                        },
                        {
                            timestamp: '2026-11-20T09:00:00',
                            consumerCount: 5,
                            deliveringCount: 2,
                            pendingCount: 6,
                            completedMessages: 10,
                            addedMessages: 12,
                        },
                    ],
                },
            ],
        });

        render(<Rendiment />);

        expect(await screen.findByText('Estadístiques dels datasources desactivades')).toBeInTheDocument();
        expect(screen.getByTestId('charts-data-provider')).toBeInTheDocument();
        expect(screen.getByTestId('metrics-tree-sidebar')).toBeInTheDocument();
        expect(screen.getByTestId('disabled-datasources-note')).toHaveTextContent('comandaDB (5–50)');
        expect(screen.getByTestId('zoom-preview-slider')).toBeInTheDocument();
        // 2 worker default layers + 2 JMS default layers = 4 series (DB layers omeses perquè stats=false)
        expect(screen.getByTestId('charts-data-provider')).toHaveAttribute('data-series-count', '4');
        expect(screen.getByText('Pools de workers · salutWorkerExecutor')).toBeInTheDocument();
        expect(screen.getByText('Cues JMS · tasquesQueue')).toBeInTheDocument();
        expect(screen.queryByTestId('tree-layer-inUseCount')).not.toBeInTheDocument();
    });

    it('Rendiment_quanNoEsDetectaCapDatasourcePeroHiHaWorkers_mostraAvisSenseDatasources', async () => {
        mockFetchSuccess({
            collectedAt: '2026-11-20T09:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 1,
            datasources: [],
            workerPools: [
                {
                    poolName: 'salutWorkerExecutor',
                    poolType: 'EXECUTOR',
                    coreSize: 5,
                    maxSize: 5,
                    poolSize: 5,
                    activeCount: 2,
                    queueSize: 3,
                    queueCapacity: 1000,
                    completedTasks: 4,
                    history: [
                        {
                            timestamp: '2026-11-20T09:00:00',
                            poolSize: 5,
                            activeCount: 2,
                            queueSize: 3,
                            completedTasks: 4,
                        },
                    ],
                },
            ],
            jmsQueues: [],
        });

        render(<Rendiment />);

        expect(await screen.findByTestId('no-datasources-alert')).toHaveTextContent(
            "No s'ha detectat cap datasource actiu al servidor."
        );
        expect(screen.queryByText('Estadístiques dels datasources desactivades')).not.toBeInTheDocument();
        expect(screen.getByTestId('charts-data-provider')).toBeInTheDocument();
        expect(screen.getByText('Tasques en cua (workers)')).toBeInTheDocument();
    });

    it('Rendiment_quanJbossAmbEstadistiquesActives_mostraCapesCuradesFinestresIEixDuracio', async () => {
        const fetchMock = mockFetchSuccess({
            collectedAt: '2026-11-19T12:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 2,
            datasources: [
                {
                    poolName: 'comandaDB',
                    jndiName: 'java:jboss/datasources/comandaDS',
                    statisticsEnabled: true,
                    waitCountType: 'INTERVAL_DELTA',
                    minSize: 5,
                    maxSize: 50,
                    inUseCount: 4,
                    activeCount: 12,
                    waitCount: 3,
                    maxWaitTimeMs: 42,
                    history: [
                        {
                            timestamp: '2026-11-19T11:55:00',
                            inUseCount: 2,
                            activeCount: 10,
                            waitCount: 0,
                            maxWaitTimeMs: 18,
                        },
                        {
                            timestamp: '2026-11-19T12:00:00',
                            inUseCount: 4,
                            activeCount: 12,
                            waitCount: 3,
                            maxWaitTimeMs: 42,
                        },
                    ],
                },
                {
                    poolName: 'ExampleDS',
                    statisticsEnabled: false,
                    minSize: 1,
                    maxSize: 20,
                    history: [],
                },
            ],
        });

        render(<Rendiment />);

        expect(await screen.findByTestId('charts-data-provider')).toBeInTheDocument();
        expect(screen.getByText('Resolució: 2 s')).toBeInTheDocument();
        expect(screen.getByTestId('zoom-preview-slider')).toBeInTheDocument();
        expect(screen.getByTestId('disabled-datasources-note')).toHaveTextContent('ExampleDS (1–20)');
        // KPI simplificats
        expect(screen.getAllByText('Connexions en ús').length).toBeGreaterThanOrEqual(2);
        expect(screen.getByTestId('tree-layer-inUseCount')).toHaveTextContent('Connexions en ús');
        expect(screen.getByText("Temps màx. d'espera")).toBeInTheDocument();
        expect(screen.getAllByText('Connexions físiques')).toHaveLength(1);
        // Curated defaults per BD: inUseCount i waitCount = 2 capes (maxWaitTimeMs OFF per defecte)
        expect(screen.getByTestId('charts-data-provider')).toHaveAttribute('data-series-count', '2');
        expect(mocks.lastXAxis[0]?.height).toBe(MUI_AXIS_WORKAROUND_HEIGHT);
        expect(mocks.lastMargin).toEqual({ top: 12, right: 12, bottom: 8, left: 4 });
        expect(screen.getByTestId('bar-plot')).toHaveAttribute('data-renderer', 'svg-batch');
        expect(screen.queryByTestId('charts-y-axis-durationMs')).not.toBeInTheDocument();

        // Canviar finestra temporal a 1h envia windowSeconds=3600
        fireEvent.click(screen.getByTestId('window-option-1h'));
        await waitFor(() => {
            expect(fetchMock).toHaveBeenLastCalledWith(
                expect.stringContaining('windowSeconds=3600')
            );
        });

        // Activar maxWaitTimeMs al TreeView -> 3 capes i mostra eix dret durationMs; desactivar-la l'amaga
        clickTreeCheckbox('tree-layer-maxWaitTimeMs');
        expect(screen.getByTestId('charts-data-provider')).toHaveAttribute('data-series-count', '3');
        expect(screen.getByTestId('charts-y-axis-durationMs')).toBeInTheDocument();

        clickTreeCheckbox('tree-layer-maxWaitTimeMs');
        expect(screen.getByTestId('charts-data-provider')).toHaveAttribute('data-series-count', '2');
        expect(screen.queryByTestId('charts-y-axis-durationMs')).not.toBeInTheDocument();
    });

    it('Rendiment_quanEsCommutenPresetsIPanellLateral_actualitzaGraficILlegendaPlegada', async () => {
        mockFetchSuccess({
            collectedAt: '2026-11-19T12:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 1,
            datasources: [
                {
                    poolName: 'comandaDB',
                    statisticsEnabled: true,
                    waitCountType: 'INTERVAL_DELTA',
                    minSize: 5,
                    maxSize: 50,
                    inUseCount: 4,
                    activeCount: 12,
                    waitCount: 3,
                    maxWaitTimeMs: 42,
                    history: [
                        {
                            timestamp: '2026-11-19T12:00:00',
                            inUseCount: 4,
                            activeCount: 12,
                            waitCount: 3,
                            maxWaitTimeMs: 42,
                        },
                    ],
                },
            ],
        });

        render(<Rendiment />);
        expect(await screen.findByTestId('charts-data-provider')).toHaveAttribute('data-series-count', '2');

        fireEvent.click(screen.getByTestId('layers-select-all-button'));
        expect(screen.getByTestId('charts-data-provider')).toHaveAttribute('data-series-count', '6');

        fireEvent.click(screen.getByTestId('layers-clear-all-button'));
        expect(
            screen.getByText('Selecciona almenys una capa per visualitzar el gràfic.')
        ).toBeInTheDocument();

        fireEvent.click(screen.getByTestId('layers-reset-default-button'));
        expect(screen.getByTestId('charts-data-provider')).toHaveAttribute('data-series-count', '2');
        expect(screen.queryByTestId('collapsed-chart-legend')).not.toBeInTheDocument();

        fireEvent.click(screen.getByTestId('toggle-sidebar-button'));
        expect(screen.queryByTestId('metrics-tree-sidebar')).not.toBeInTheDocument();
        const collapsedLegend = screen.getByTestId('collapsed-chart-legend');
        expect(collapsedLegend).toHaveTextContent('BD · Connexions en ús');
        expect(collapsedLegend).toHaveTextContent('BD · Esperes (interval)');

        fireEvent.click(screen.getByTestId('toggle-sidebar-button'));
        expect(screen.getByTestId('metrics-tree-sidebar')).toBeInTheDocument();
        expect(screen.queryByTestId('collapsed-chart-legend')).not.toBeInTheDocument();
    });

    it('Rendiment_quanBdWorkersIJmsActius_mostra4KpisICapesCuradesAmbDesglossamentIExclusio', async () => {
        mockFetchSuccess({
            collectedAt: '2026-11-20T09:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 1,
            datasources: [
                {
                    poolName: 'comandaDB',
                    statisticsEnabled: true,
                    waitCountType: 'INTERVAL_DELTA',
                    minSize: 5,
                    maxSize: 50,
                    inUseCount: 3,
                    activeCount: 10,
                    waitCount: 0,
                    maxWaitTimeMs: 12,
                    history: [
                        {
                            timestamp: '2026-11-20T09:00:00',
                            inUseCount: 3,
                            activeCount: 10,
                            waitCount: 0,
                            maxWaitTimeMs: 12,
                        },
                    ],
                },
            ],
            workerPools: [
                {
                    poolName: 'salutWorkerExecutor',
                    poolType: 'EXECUTOR',
                    coreSize: 5,
                    maxSize: 5,
                    poolSize: 5,
                    activeCount: 2,
                    queueSize: 1,
                    queueCapacity: 1000,
                    completedTasks: 9,
                    history: [
                        {
                            timestamp: '2026-11-20T09:00:00',
                            poolSize: 5,
                            activeCount: 2,
                            queueSize: 1,
                            completedTasks: 9,
                        },
                    ],
                },
                {
                    poolName: 'monitorTaskScheduler',
                    poolType: 'SCHEDULER',
                    coreSize: 10,
                    maxSize: 10,
                    poolSize: 4,
                    activeCount: 1,
                    queueSize: 0,
                    queueCapacity: null,
                    completedTasks: 2,
                    history: [
                        {
                            timestamp: '2026-11-20T09:00:00',
                            poolSize: 4,
                            activeCount: 1,
                            queueSize: 0,
                            completedTasks: 2,
                        },
                    ],
                },
            ],
            jmsQueues: [
                {
                    queueName: 'tasquesQueue',
                    listenerCount: 1,
                    minConsumers: 5,
                    maxConsumers: 50,
                    consumerCount: 5,
                    deliveringCount: 2,
                    pendingCount: 4,
                    completedMessages: 7,
                    addedMessages: 11,
                    history: [
                        {
                            timestamp: '2026-11-20T09:00:00',
                            consumerCount: 5,
                            deliveringCount: 2,
                            pendingCount: 4,
                            completedMessages: 7,
                            addedMessages: 11,
                        },
                    ],
                },
            ],
        });

        render(<Rendiment />);

        // Curated defaults amb workerQueueSize desglossat: 2 DB + 1 workerActiveCount + 2 stacked workerQueueSize + 2 JMS = 7 sèries
        expect(await screen.findByTestId('charts-data-provider')).toHaveAttribute('data-series-count', '7');
        expect(screen.getByText('salutWorkerExecutor · Tasques en cua')).toBeInTheDocument();
        expect(screen.getByText('monitorTaskScheduler · Tasques en cua')).toBeInTheDocument();

        // Només les 4 targetes KPI importants
        expect(screen.getAllByText('Connexions en ús').length).toBeGreaterThanOrEqual(2);
        expect(screen.getAllByText('Esperes (interval)').length).toBeGreaterThanOrEqual(2);
        expect(screen.getByText("Temps màx. d'espera")).toBeInTheDocument();
        expect(screen.getByText('Tasques en cua (workers)')).toBeInTheDocument();
        expect(screen.getAllByText('Connexions físiques')).toHaveLength(1);
        expect(screen.queryByText('Fils en execució')).not.toBeInTheDocument();
        expect(screen.queryByText('Missatges en procés (JMS)')).not.toBeInTheDocument();

        // Integració d'exclusió individual sobre les sèries del gràfic
        clickTreeCheckbox('tree-series-workerQueueSize-monitorTaskScheduler');
        expect(screen.getByTestId('charts-data-provider')).toHaveAttribute('data-series-count', '6');
        expect(screen.queryByText('monitorTaskScheduler · Tasques en cua')).not.toBeInTheDocument();
        expect(screen.getByText('salutWorkerExecutor · Tasques en cua')).toBeInTheDocument();
    });

    it('Rendiment_quanZoomAmbSlider_demanaFromIToIPermetRestablirZoom', async () => {
        const fetchMock = mockFetchSuccess({
            collectedAt: '2026-11-19T12:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 3600,
            bucketSeconds: 20,
            datasources: [
                {
                    poolName: 'comandaDB',
                    statisticsEnabled: true,
                    waitCountType: 'INTERVAL_DELTA',
                    minSize: 5,
                    maxSize: 50,
                    inUseCount: 3,
                    activeCount: 10,
                    waitCount: 1,
                    maxWaitTimeMs: 25,
                    history: [
                        { timestamp: '2026-11-19T11:00:00', inUseCount: 2, waitCount: 0 },
                        { timestamp: '2026-11-19T11:20:00', inUseCount: 18, waitCount: 4 },
                        { timestamp: '2026-11-19T11:40:00', inUseCount: 5, waitCount: 0 },
                        { timestamp: '2026-11-19T12:00:00', inUseCount: 3, waitCount: 1 },
                    ],
                },
            ],
        });

        render(<Rendiment />);
        await screen.findByTestId('zoom-preview-slider');

        const sliders = screen.getAllByRole('slider', { name: 'Selector de rang temporal' });
        expect(sliders).toHaveLength(2);

        // Simulem canvi i confirmació al slider d'inici fins a 33% en temps continu (11:00..12:00 -> 11:19:48)
        fireEvent.change(sliders[0], { target: { value: 33 } });
        fireEvent.mouseUp(sliders[0]);

        await waitFor(() => {
            expect(fetchMock).toHaveBeenLastCalledWith(
                expect.stringContaining('from=2026-11-19T11%3A19%3A48')
            );
        });

        expect(await screen.findByText('Zoom actiu (autorefresc en pausa)')).toBeInTheDocument();

        const resetButtons = screen.getAllByRole('button', { name: 'Restablir zoom' });
        fireEvent.click(resetButtons[0]);

        await waitFor(() => {
            const lastUrl = String(fetchMock.mock.calls[fetchMock.mock.calls.length - 1][0]);
            expect(lastUrl).not.toContain('from=');
        });
    });

    it('Rendiment_quanEsArrossegaSobreLaBarraPreview_desplacaLaFinestraDeZoomIPan', async () => {
        const previewPoints = [
            { timestamp: '2026-11-19T11:00:00', inUseCount: 2, waitCount: 0 },
            { timestamp: '2026-11-19T11:15:00', inUseCount: 10, waitCount: 1 },
            { timestamp: '2026-11-19T11:30:00', inUseCount: 25, waitCount: 4 },
            { timestamp: '2026-11-19T11:45:00', inUseCount: 12, waitCount: 2 },
            { timestamp: '2026-11-19T12:00:00', inUseCount: 4, waitCount: 0 },
        ];
        const fetchMock = mockFetchSuccess({
            collectedAt: '2026-11-19T12:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 3600,
            bucketSeconds: 20,
            datasources: [
                {
                    poolName: 'comandaDB',
                    statisticsEnabled: true,
                    waitCountType: 'INTERVAL_DELTA',
                    minSize: 5,
                    maxSize: 50,
                    inUseCount: 4,
                    activeCount: 10,
                    waitCount: 0,
                    maxWaitTimeMs: 25,
                    history: previewPoints,
                    preview: previewPoints,
                },
            ],
        });

        render(<Rendiment />);
        const previewBar = await screen.findByTestId('zoom-preview-bar');
        expect(screen.getByTestId('zoom-preview-svg')).toBeInTheDocument();
        expect(previewBar).toContainElement(screen.getByTestId('zoom-preview-svg'));
        vi.spyOn(previewBar, 'getBoundingClientRect').mockReturnValue({
            left: 0,
            right: 400,
            top: 0,
            bottom: 56,
            width: 400,
            height: 56,
            x: 0,
            y: 0,
            toJSON: () => ({}),
        });

        // 1) Estant sense zoom (0..100%), arrossegar de 0% a 50% (clientX 0 -> 200) selecciona [11:00..11:30]
        fireEvent.pointerDown(previewBar, { pointerId: 1, clientX: 0, button: 0, pointerType: 'mouse' });
        fireEvent.pointerMove(previewBar, { pointerId: 1, clientX: 200 });
        fireEvent.pointerUp(previewBar, { pointerId: 1, clientX: 200 });

        await waitFor(() => {
            const lastUrl = String(fetchMock.mock.calls[fetchMock.mock.calls.length - 1][0]);
            expect(lastUrl).toContain('from=2026-11-19T11%3A00%3A00');
            expect(lastUrl).toContain('to=2026-11-19T11%3A30%3A00');
        });

        // 2) Ja amb zoom [0%..50%], arrossegar dins la barra +25% (clientX 100 -> 200) desplaça la finestra a [25%..75%] = [11:15..11:45]
        fireEvent.pointerDown(previewBar, { pointerId: 2, clientX: 100, button: 0, pointerType: 'mouse' });
        fireEvent.pointerMove(previewBar, { pointerId: 2, clientX: 200 });
        fireEvent.pointerUp(previewBar, { pointerId: 2, clientX: 200 });

        await waitFor(() => {
            const lastUrl = String(fetchMock.mock.calls[fetchMock.mock.calls.length - 1][0]);
            expect(lastUrl).toContain('from=2026-11-19T11%3A15%3A00');
            expect(lastUrl).toContain('to=2026-11-19T11%3A45%3A00');
        });

        // 3) Commutar autorefresc manté l'estat i doble clic al gràfic principal restableix el zoom
        fireEvent.click(screen.getByRole('switch', { name: /Autorefresc/ }));
        expect(screen.getByText('Zoom actiu (autorefresc en pausa)')).toBeInTheDocument();
        fireEvent.click(screen.getByRole('switch', { name: /Autorefresc/ }));
        expect(screen.getByText('Zoom actiu (autorefresc en pausa)')).toBeInTheDocument();

        fireEvent.doubleClick(screen.getByTestId('rendiment-chart-box'));

        await waitFor(() => {
            const lastUrl = String(fetchMock.mock.calls[fetchMock.mock.calls.length - 1][0]);
            expect(lastUrl).not.toContain('from=');
            expect(lastUrl).not.toContain('to=');
        });
        expect(screen.queryByText('Zoom actiu (autorefresc en pausa)')).not.toBeInTheDocument();
    });

    it('Rendiment_quanZoomActiuEnDirecte_congelaFinestraPreviewFinsRestablirZoom', async () => {
        const initialPayload: PerformanceMetricsResponse = {
            collectedAt: '2026-11-19T12:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 2,
            previewBucketSeconds: 2,
            windowStart: '2026-11-19T11:55:00',
            windowEnd: '2026-11-19T12:00:00',
            datasources: [
                {
                    poolName: 'comandaDB',
                    statisticsEnabled: true,
                    waitCountType: 'INTERVAL_DELTA',
                    minSize: 5,
                    maxSize: 50,
                    inUseCount: 4,
                    activeCount: 10,
                    waitCount: 0,
                    history: [
                        { timestamp: '2026-11-19T11:55:00', inUseCount: 2, waitCount: 0 },
                        { timestamp: '2026-11-19T11:57:30', inUseCount: 25, waitCount: 3 },
                        { timestamp: '2026-11-19T12:00:00', inUseCount: 4, waitCount: 0 },
                    ],
                },
            ],
        };

        // En fer zoom, el servidor respon amb una finestra avançada (+30 s)
        const zoomedServerPayload: PerformanceMetricsResponse = {
            ...initialPayload,
            collectedAt: '2026-11-19T12:00:30',
            bucketSeconds: 1,
            previewBucketSeconds: 2,
            windowStart: '2026-11-19T11:55:30',
            windowEnd: '2026-11-19T12:00:30',
            rangeStart: '2026-11-19T11:55:00',
            rangeEnd: '2026-11-19T11:57:30',
            datasources: [
                {
                    ...initialPayload.datasources[0],
                    history: [
                        { timestamp: '2026-11-19T11:55:00', inUseCount: 2, waitCount: 0 },
                        { timestamp: '2026-11-19T11:57:30', inUseCount: 25, waitCount: 3 },
                    ],
                    preview: [
                        { timestamp: '2026-11-19T11:55:30', inUseCount: 3, waitCount: 0 },
                        { timestamp: '2026-11-19T12:00:30', inUseCount: 5, waitCount: 0 },
                    ],
                },
            ],
        };

        const unzoomedServerPayload: PerformanceMetricsResponse = {
            ...initialPayload,
            collectedAt: '2026-11-19T12:00:45',
            windowStart: '2026-11-19T11:55:45',
            windowEnd: '2026-11-19T12:00:45',
            datasources: [
                {
                    ...initialPayload.datasources[0],
                    history: [
                        { timestamp: '2026-11-19T11:55:45', inUseCount: 3, waitCount: 0 },
                        { timestamp: '2026-11-19T12:00:45', inUseCount: 6, waitCount: 0 },
                    ],
                },
            ],
        };

        const fetchMock = mocks.requestHrefMock
            .mockResolvedValueOnce({ data: initialPayload })
            .mockResolvedValueOnce({ data: zoomedServerPayload })
            .mockResolvedValueOnce({ data: unzoomedServerPayload });

        render(<Rendiment />);
        const previewBar = await screen.findByTestId('zoom-preview-bar');
        vi.spyOn(previewBar, 'getBoundingClientRect').mockReturnValue({
            left: 0,
            right: 400,
            top: 0,
            bottom: 56,
            width: 400,
            height: 56,
            x: 0,
            y: 0,
            toJSON: () => ({}),
        });

        // Seleccionem 0%..50% de [11:55:00..12:00:00] -> [11:55:00..11:57:30]
        fireEvent.pointerDown(previewBar, { pointerId: 1, clientX: 0, button: 0, pointerType: 'mouse' });
        fireEvent.pointerMove(previewBar, { pointerId: 1, clientX: 200 });
        fireEvent.pointerUp(previewBar, { pointerId: 1, clientX: 200 });

        await waitFor(() => {
            expect(fetchMock).toHaveBeenCalledTimes(2);
        });

        // Tot i que el servidor ha avançat a 11:55:30..12:00:30, la preview congelada manté 11:55:00..12:00:00
        const sliderContainer = screen.getByTestId('zoom-preview-slider');
        expect(sliderContainer).toHaveTextContent('11:55:00');
        expect(sliderContainer).toHaveTextContent('12:00:00');
        expect(sliderContainer).not.toHaveTextContent('12:00:30');

        const sliders = screen.getAllByRole('slider', { name: 'Selector de rang temporal' });
        expect(sliders[0]).toHaveAttribute('aria-valuenow', '0');
        expect(sliders[1]).toHaveAttribute('aria-valuenow', '50');

        // En restablir zoom, es descongela i mostra la finestra actualitzada del servidor
        fireEvent.click(screen.getAllByRole('button', { name: 'Restablir zoom' })[0]);
        await waitFor(() => {
            expect(sliderContainer).toHaveTextContent('11:55:45');
            expect(sliderContainer).toHaveTextContent('12:00:45');
        });
    });

    it('Rendiment_quanPestanyaOcultaIReapareix_pausaIReprenAutorefresc', async () => {
        const fetchMock = mockFetchSuccess({
            collectedAt: '2026-11-19T12:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 1,
            datasources: [],
            workerPools: [],
            jmsQueues: [],
        });

        let hiddenState = true;
        Object.defineProperty(document, 'hidden', {
            configurable: true,
            get: () => hiddenState,
        });

        try {
            render(<Rendiment />);
            await screen.findByText("No s'ha detectat cap datasource, worker ni cua JMS actiu al servidor.");
            expect(fetchMock).toHaveBeenCalledTimes(1);

            hiddenState = false;
            fireEvent(document, new Event('visibilitychange'));

            await waitFor(() => {
                expect(fetchMock).toHaveBeenCalledTimes(2);
            });
        } finally {
            Object.defineProperty(document, 'hidden', {
                configurable: true,
                value: false,
            });
        }
    });

    it('Rendiment_quanHikariInstantaneousIMultiplesPoolsActius_adaptaEtiquetaIModePerPool', async () => {
        mockFetchSuccess({
            collectedAt: '2026-11-19T12:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 1,
            datasources: [
                {
                    poolName: 'HikariMain',
                    statisticsEnabled: true,
                    waitCountType: 'INSTANTANEOUS',
                    minSize: 2,
                    maxSize: 10,
                    inUseCount: 1,
                    activeCount: 3,
                    waitCount: 0,
                    maxWaitTimeMs: 11,
                    history: [
                        {
                            timestamp: '2026-11-19T12:00:00',
                            inUseCount: 1,
                            activeCount: 3,
                            waitCount: 0,
                            maxWaitTimeMs: 11,
                        },
                    ],
                },
                {
                    poolName: 'HikariAudit',
                    statisticsEnabled: true,
                    waitCountType: 'INSTANTANEOUS',
                    minSize: 1,
                    maxSize: 5,
                    inUseCount: 2,
                    activeCount: 2,
                    waitCount: 1,
                    maxWaitTimeMs: 25,
                    history: [
                        {
                            timestamp: '2026-11-19T12:00:00',
                            inUseCount: 2,
                            activeCount: 2,
                            waitCount: 1,
                            maxWaitTimeMs: 25,
                        },
                    ],
                },
            ],
        });

        render(<Rendiment />);

        expect(await screen.findByTestId('charts-data-provider')).toBeInTheDocument();
        expect(screen.getAllByText('Esperes (actual)').length).toBeGreaterThanOrEqual(2);
        // Curated defaults: 2 series (inUseCount, waitCount)
        expect(screen.getByTestId('charts-data-provider')).toHaveAttribute('data-series-count', '2');

        // Desglossar "Connexions en ús" per pool a l'arbre
        fireEvent.click(screen.getByTestId('split-toggle-inUseCount'));
        expect(screen.getByTestId('charts-data-provider')).toHaveAttribute('data-series-count', '3');
        expect(screen.getByText('HikariMain · Connexions en ús')).toBeInTheDocument();
        expect(screen.getByText('HikariAudit · Connexions en ús')).toBeInTheDocument();
    });

    it('Rendiment_quanLaPeticióFalla_mostraErrorIReintentaAmbRefrescar', async () => {
        mocks.requestHrefMock
            .mockRejectedValueOnce(new Error('HTTP 503'))
            .mockResolvedValueOnce({
                data: {
                    collectedAt: '2026-11-19T12:00:00',
                    samplingIntervalSeconds: 1,
                    maxHistoryPoints: 3600,
                    windowSeconds: 300,
                    bucketSeconds: 1,
                    datasources: [],
                    workerPools: [],
                    jmsQueues: [],
                },
            });

        render(<Rendiment />);

        expect(await screen.findByText(/HTTP 503/)).toBeInTheDocument();

        fireEvent.click(screen.getByRole('button', { name: 'Refrescar' }));

        await waitFor(() => {
            expect(
                screen.getByText("No s'ha detectat cap datasource, worker ni cua JMS actiu al servidor.")
            ).toBeInTheDocument();
        });
    });

    it('Rendiment_quanEsPremExportarJson_descarregaFitxerAmbCapcaleraAttachment', async () => {
        const livePayload: PerformanceMetricsResponse = {
            collectedAt: '2026-11-19T12:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 1,
            datasources: [],
            workerPools: [],
            jmsQueues: [],
        };
        const fetchMock = mocks.requestHrefMock
            .mockResolvedValueOnce({
                data: livePayload,
            })
            .mockResolvedValueOnce({
                headers: new Headers({
                    'content-disposition': 'attachment; filename="performance-metrics-20261119-120000.json"',
                }),
                data: livePayload,
            });

        const createObjectUrlMock = vi.fn(() => 'blob:mock-export');
        const revokeObjectUrlMock = vi.fn();
        Object.defineProperty(URL, 'createObjectURL', {
            configurable: true,
            writable: true,
            value: createObjectUrlMock,
        });
        Object.defineProperty(URL, 'revokeObjectURL', {
            configurable: true,
            writable: true,
            value: revokeObjectUrlMock,
        });

        let downloadedFileName = '';
        const anchorClickSpy = vi
            .spyOn(HTMLAnchorElement.prototype, 'click')
            .mockImplementation(function (this: HTMLAnchorElement) {
                downloadedFileName = this.download;
            });

        render(<Rendiment />);
        await screen.findByText("No s'ha detectat cap datasource, worker ni cua JMS actiu al servidor.");

        fireEvent.click(screen.getByTestId('export-json-button'));

        await waitFor(() => {
            expect(fetchMock).toHaveBeenCalledWith('/api/performance-metrics/export');
            expect(createObjectUrlMock).toHaveBeenCalledWith(expect.any(Blob));
            expect(anchorClickSpy).toHaveBeenCalledTimes(1);
            expect(downloadedFileName).toBe('performance-metrics-20261119-120000.json');
            expect(revokeObjectUrlMock).toHaveBeenCalledWith('blob:mock-export');
        });

        const createdBlob = (createObjectUrlMock.mock.calls as unknown[][])[0][0] as Blob;
        expect(createdBlob.type).toBe('application/json');
        expect(await createdBlob.text()).toBe(JSON.stringify(livePayload));
    });

    it('Rendiment_quanEsCarregaJsonValid_visualitzaEnModeForaDeLiniaISensePeticionsXarxa', async () => {
        const fetchMock = mockFetchSuccess({
            collectedAt: '2026-11-24T09:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 1,
            datasources: [],
            workerPools: [],
            jmsQueues: [],
        });

        const pad2 = (n: number): string => String(n).padStart(2, '0');
        const t0 = new Date(2026, 10, 24, 10, 0, 0).getTime();
        const dsHistory = Array.from({ length: 600 }, (_, i) => {
            const d = new Date(t0 + i * 1000);
            const timestamp =
                `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}` +
                `T${pad2(d.getHours())}:${pad2(d.getMinutes())}:${pad2(d.getSeconds())}`;
            return {
                timestamp,
                inUseCount: i === 550 ? 45 : 3,
                activeCount: 10,
                waitCount: i === 550 ? 4 : 0,
                maxWaitTimeMs: 12,
            };
        });

        const snapshotPayload: PerformanceMetricsResponse = {
            collectedAt: dsHistory[dsHistory.length - 1].timestamp,
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 599,
            bucketSeconds: 1,
            datasources: [
                {
                    poolName: 'comandaDB',
                    statisticsEnabled: true,
                    waitCountType: 'INTERVAL_DELTA',
                    minSize: 5,
                    maxSize: 50,
                    inUseCount: 3,
                    activeCount: 10,
                    waitCount: 0,
                    maxWaitTimeMs: 12,
                    history: dsHistory,
                },
            ],
            workerPools: [],
            jmsQueues: [],
        };

        render(<Rendiment />);
        await screen.findByText("No s'ha detectat cap datasource, worker ni cua JMS actiu al servidor.");
        const callsBeforeImport = fetchMock.mock.calls.length;

        const file = new File([JSON.stringify(snapshotPayload)], 'captura-incidencia.json', {
            type: 'application/json',
        });
        fireEvent.change(screen.getByTestId('import-json-input'), {
            target: { files: [file] },
        });

        const banner = await screen.findByTestId('imported-snapshot-banner');
        expect(banner).toHaveTextContent('Visualitzant històric importat: captura-incidencia.json');
        expect(banner).toHaveTextContent('Mode fora de línia');
        expect(screen.getByTestId('charts-data-provider')).toBeInTheDocument();
        expect(screen.getAllByText('Connexions en ús').length).toBeGreaterThanOrEqual(2);
        expect(screen.getByText('Resolució: 5 s')).toBeInTheDocument();
        expect(screen.getByTestId('export-json-button')).toBeDisabled();
        expect(screen.getByRole('switch', { name: /Autorefresc/ })).toBeDisabled();
        expect(screen.getByRole('button', { name: 'Refrescar' })).toBeDisabled();

        // Canviar finestra a 2m recalcula localment a resolució 1 s sense fer peticions HTTP
        fireEvent.click(screen.getByRole('button', { name: '2m' }));
        expect(screen.getByText('Resolució: 1 s')).toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledTimes(callsBeforeImport);

        // Fer zoom amb el slider funciona fora de línia sense peticions HTTP
        const sliders = screen.getAllByRole('slider', { name: 'Selector de rang temporal' });
        fireEvent.change(sliders[0], { target: { value: 25 } });
        fireEvent.mouseUp(sliders[0]);
        expect(await screen.findByText('Zoom actiu (autorefresc en pausa)')).toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledTimes(callsBeforeImport);

        // Tornar al directe neteja el banner i torna a consultar l'API en viu
        fireEvent.click(screen.getByTestId('return-to-live-button'));
        await waitFor(() => {
            expect(screen.queryByTestId('imported-snapshot-banner')).not.toBeInTheDocument();
            expect(fetchMock.mock.calls.length).toBeGreaterThan(callsBeforeImport);
        });
    });

    it('Rendiment_quanEsCarregaJsonInvalidOFormatIncorrecte_mostraErrorIManteVistaActual', async () => {
        mockFetchSuccess({
            collectedAt: '2026-11-19T12:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 1,
            datasources: [
                {
                    poolName: 'comandaDB',
                    statisticsEnabled: true,
                    waitCountType: 'INTERVAL_DELTA',
                    minSize: 5,
                    maxSize: 50,
                    inUseCount: 2,
                    activeCount: 10,
                    waitCount: 0,
                    maxWaitTimeMs: 10,
                    history: [
                        {
                            timestamp: '2026-11-19T12:00:00',
                            inUseCount: 2,
                            activeCount: 10,
                            waitCount: 0,
                            maxWaitTimeMs: 10,
                        },
                    ],
                },
            ],
        });

        render(<Rendiment />);
        await screen.findByTestId('charts-data-provider');

        const invalidJsonFile = new File(['{bad-json'], 'trencat.json', { type: 'application/json' });
        fireEvent.change(screen.getByTestId('import-json-input'), {
            target: { files: [invalidJsonFile] },
        });
        expect(
            await screen.findByText('El fitxer seleccionat no conté un JSON vàlid.')
        ).toBeInTheDocument();
        expect(screen.queryByTestId('imported-snapshot-banner')).not.toBeInTheDocument();
        expect(screen.getByTestId('charts-data-provider')).toBeInTheDocument();

        const wrongShapeFile = new File([JSON.stringify({ hello: 'world' })], 'altre.json', {
            type: 'application/json',
        });
        fireEvent.change(screen.getByTestId('import-json-input'), {
            target: { files: [wrongShapeFile] },
        });
        expect(
            await screen.findByText(
                "El fitxer JSON no té el format esperat d'un històric de mètriques de rendiment."
            )
        ).toBeInTheDocument();
        expect(screen.queryByTestId('imported-snapshot-banner')).not.toBeInTheDocument();
        expect(screen.getByTestId('charts-data-provider')).toBeInTheDocument();
    });

    it('Rendiment_barraEines_situaBotonsJsonALEsquerraDeUltimaActualitzacioIMostraMemoria', async () => {
        mockFetchSuccess({
            collectedAt: '2026-11-24T10:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 1,
            collectionEnabled: true,
            estimatedMemoryBytes: 2_621_440,
            datasources: [],
            workerPools: [],
            jmsQueues: [],
        });

        render(<Rendiment />);
        const lastUpdateEl = await screen.findByTestId('last-update-label');
        const exportBtn = screen.getByTestId('export-json-button');
        const importBtn = screen.getByTestId('import-json-button');
        const memoryChip = screen.getByTestId('estimated-memory-chip');

        expect(
            Boolean(memoryChip.compareDocumentPosition(exportBtn) & Node.DOCUMENT_POSITION_FOLLOWING)
        ).toBe(true);
        expect(
            Boolean(exportBtn.compareDocumentPosition(lastUpdateEl) & Node.DOCUMENT_POSITION_FOLLOWING)
        ).toBe(true);
        expect(
            Boolean(importBtn.compareDocumentPosition(lastUpdateEl) & Node.DOCUMENT_POSITION_FOLLOWING)
        ).toBe(true);
        expect(memoryChip).toHaveTextContent('Memòria: ≤ 2.50 MB');

        fireEvent.mouseOver(memoryChip);
        const tooltip = await screen.findByRole('tooltip');
        expect(tooltip).toHaveTextContent('es.caib.comanda.monitor.rendiment.actiu');
    });

    it('Rendiment_quanRecolleccioDesactivadaPerParametre_mostraAvisIDesactivaExportacio', async () => {
        mockFetchSuccess({
            collectedAt: null,
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 300,
            bucketSeconds: 1,
            collectionEnabled: false,
            estimatedMemoryBytes: 0,
            datasources: [],
            workerPools: [],
            jmsQueues: [],
        });

        render(<Rendiment />);
        const alert = await screen.findByTestId('collection-disabled-alert');
        expect(alert).toHaveTextContent('Recol·lecció de mètriques desactivada');
        expect(alert).toHaveTextContent('es.caib.comanda.monitor.rendiment.actiu');
        expect(screen.getByTestId('estimated-memory-chip')).toHaveTextContent('Memòria: 0 B');
        expect(screen.getByTestId('export-json-button')).toBeDisabled();
        expect(screen.getByTestId('import-json-button')).toBeEnabled();
        expect(
            screen.queryByText("No s'ha detectat cap datasource, worker ni cua JMS actiu al servidor.")
        ).not.toBeInTheDocument();
    });
});
