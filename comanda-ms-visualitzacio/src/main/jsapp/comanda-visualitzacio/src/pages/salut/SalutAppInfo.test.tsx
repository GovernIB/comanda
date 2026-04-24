import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import SalutAppInfo from './SalutAppInfo';

const translations = {
    page: {
        salut: {
            appInfoTitle: 'Informació de salut',
            nd: 'N/D',
            tabs: {
                entorn: 'Entorn',
                estatActual: 'Estat actual',
                integracions: 'Integracions',
                historic: 'Històric',
                historicEstat: "Històric d'estat",
                logs: 'Logs',
            },
            info: {
                title: 'Informació d entorn',
                versio: 'Versió',
                revisio: 'Revisió',
                data: 'Data',
                noInfo: 'No hi ha informació de salut',
                downAlert: 'La petició de salut ha fallat',
                bdEstat: 'Estat BD',
                appLatencia: 'Latència app',
                jdk: {
                    versio: 'Versió JDK',
                },
            },
            contexts: {
                title: 'Contexts',
                noInfo: 'Sense contexts',
                column: {
                    nom: 'Nom',
                    path: 'Path',
                    api: 'API',
                },
            },
            missatges: {
                title: 'Missatges',
                noInfo: 'Sense missatges',
                column: {
                    data: 'Data',
                    nivell: 'Nivell',
                    missatge: 'Missatge',
                },
            },
            manuals: {
                title: 'Manuals',
            },
            detalls: {
                title: 'Detalls',
                noInfo: 'Sense detalls',
            },
            subsistemes: {
                title: 'Subsistemes',
                noInfo: 'Sense subsistemes',
                subsistemaDownCount: 'Subsistemes avall',
                column: {
                    codi: 'Codi',
                    nom: 'Nom',
                    estat: 'Estat',
                    peticionsTotals: 'Totals',
                    tempsMigTotal: 'Temps mig total',
                    peticionsPeriode: 'Període',
                    tempsMigPeriode: 'Temps mig període',
                },
                filter: {
                    estat: 'Filtrar per estat',
                },
                noResults: 'Cap subsistema coincideix amb els filtres',
            },
            integracions: {
                title: 'Integracions',
                noInfo: 'Sense integracions',
                integracioDownCount: 'Integracions avall',
                column: {
                    nom: 'Nom',
                    estat: 'Estat',
                    peticionsTotals: 'Totals',
                    tempsMigTotal: 'Temps mig total',
                    peticionsPeriode: 'Període',
                    tempsMigPeriode: 'Temps mig període',
                },
                filter: {
                    estat: 'Filtrar per estat',
                },
                noResults: 'Cap integració coincideix amb els filtres',
            },
            estats: {
                title: 'Estats',
            },
            estatLatencia: {
                noInfo: 'Sense històric',
            },
            latencia: {
                title: 'Latència',
            },
            historicEstat: {
                title: "Històric d'estat",
                noInfo: 'Sense canvis',
                peticioOk: 'Correcta',
                peticioError: 'Amb error',
                column: {
                    data: 'Data',
                    appEstat: 'Estat app',
                    peticio: 'Petició',
                },
            },
            memoria: {
                title: 'Memòria',
                espaiMeoria: 'Memòria emprada: {{disp}} / {{total}}',
                espaiDisc: 'Disc emprat: {{disp}} / {{total}}',
            },
        },
        enum: {
            appEstat: {
                UP: { title: 'UP', tooltip: 'UP' },
                WARN: { title: 'WARN', tooltip: 'WARN' },
                DEGRADED: { title: 'DEGRADED', tooltip: 'DEGRADED' },
                DOWN: { title: 'DOWN', tooltip: 'DOWN' },
                MAINTENANCE: { title: 'MAINTENANCE', tooltip: 'MAINTENANCE' },
                UNKNOWN: { title: 'UNKNOWN', tooltip: 'UNKNOWN' },
                ERROR: { title: 'ERROR', tooltip: 'ERROR' },
            },
        },
    },
};

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) => (typeof selector === 'function' ? selector(translations) : selector),
    }),
    Trans: ({ i18nKey, values }: { i18nKey: any; values?: any }) => {
        let text = typeof i18nKey === 'function' ? i18nKey(translations) : i18nKey;
        if (values) {
            Object.keys(values).forEach((key) => {
                text = text.replace(`{{${key}}}`, values[key]);
            });
        }
        return <>{text}</>;
    },
}));

vi.mock('reactlib', () => ({
    dateFormatLocale: (value: string) => `format:${value}`,
}));

vi.mock('react-error-boundary', () => ({
    ErrorBoundary: ({
        children,
        fallback,
    }: {
        children: React.ReactNode;
        fallback: React.ReactNode;
    }) => <>{children ?? fallback}</>,
}));

vi.mock('@mui/x-charts', () => ({
    ChartContainer: ({ children }: { children: React.ReactNode }) => (
        <div data-testid="chart-container">{children}</div>
    ),
    ChartsTooltip: () => <div data-testid="charts-tooltip">Tooltip</div>,
    ChartsXAxis: () => <div data-testid="charts-x-axis">XAxis</div>,
    ChartsYAxis: () => <div data-testid="charts-y-axis">YAxis</div>,
    LinePlot: () => <div data-testid="line-plot">LinePlot</div>,
}));

vi.mock('@mui/x-charts/LineChart', () => ({
    MarkPlot: () => <div data-testid="mark-plot">MarkPlot</div>,
}));

vi.mock('../../components/salut/UpdownBarChart', () => ({
    default: () => <div data-testid="updown-bar-chart">UpdownBarChart</div>,
}));

vi.mock('../../components/salut/ResponsiveCardTable', () => ({
    default: ({
        title,
        tableSections,
    }: {
        title: string;
        tableSections: Array<{ id: string; headerName: string; cellContent: React.ReactNode }>;
    }) => (
        <section>
            <h2>{title}</h2>
            {tableSections.map((section) => (
                <div key={section.id}>
                    <span>{section.headerName}</span>
                    <div>{section.cellContent}</div>
                </div>
            ))}
        </section>
    ),
}));

vi.mock('../../components/salut/SalutItemStateChip.tsx', () => ({
    ItemStateChip: ({ salutStatEnum }: { salutStatEnum?: string }) => (
        <span>{salutStatEnum ?? 'UNKNOWN'}</span>
    ),
}));

vi.mock('../../components/salut/SalutChip', () => ({
    default: ({ label }: { label: React.ReactNode }) => <span>{label}</span>,
}));

vi.mock('../../components/salut/SalutErrorBoundaryFallback', () => ({
    SalutErrorBoundaryFallback: () => <div>Fallback salut</div>,
}));

vi.mock('./LogsViewer', () => ({
    default: ({ entornAppId }: { entornAppId: number }) => <div>Logs viewer {entornAppId}</div>,
}));

vi.mock('../../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <h1>{title}</h1>,
}));

vi.mock('../../components/UserContext.ts', () => ({
    useIsUserAdmin: () => true,
}));

vi.mock('../../types/salut.model.tsx', () => ({
    SalutEstatEnum: {
        UP: 'UP',
        WARN: 'WARN',
        DEGRADED: 'DEGRADED',
        DOWN: 'DOWN',
        MAINTENANCE: 'MAINTENANCE',
        UNKNOWN: 'UNKNOWN',
    },
    NivellEnum: {
        INFO: 'INFO',
        WARN: 'WARN',
        ERROR: 'ERROR',
    },
    SalutModel: {
        SUBSISTEMA_DOWN_COUNT: 'subsistemaDownCount',
        INTEGRACIO_DOWN_COUNT: 'integracioDownCount',
    },
    useGetColorByIntegracio: () => () => '#ff8800',
    useGetColorByNivellEnum: () => () => '#0088ff',
    useGetColorBySubsistema: () => () => '#00aa00',
    useSalutEstatTranslation: () => {
        const { t } = (vi.mocked(require('react-i18next')) as any).useTranslation();
        return {
            tTitle: (estat: string) => t(($: any) => $.enum.appEstat[estat]?.title ?? estat),
            tTooltip: (estat: string) => t(($: any) => $.enum.appEstat[estat]?.tooltip ?? estat),
        };
    },
    useSalutDetallCodeTranslation: () => {
        return {
            tDetallTitle: (codi: String, nom : String) => nom ? nom : codi,
        };
    }
}));

const MOCK_SALUT_ESTAT_ENUM = {
    UP: 'UP',
    WARN: 'WARN',
    DEGRADED: 'DEGRADED',
    DOWN: 'DOWN',
    MAINTENANCE: 'MAINTENANCE',
    UNKNOWN: 'UNKNOWN',
};

const expectedEstats = Object.values(MOCK_SALUT_ESTAT_ENUM);

const createAppInfoData = (overrides: Record<string, unknown> = {}) => ({
    loading: false,
    entornApp: {
        id: 7,
        versio: '1.0.0',
        revisioSimplificat: 'abc123',
        jdkVersion: '17',
        logsUrl: 'http://logs',
    },
    estats: {
        7: [{ data: '2026-03-13T10:00:00', totalUp: 3 }],
    },
    latencies: [
        {
            data: '2026-03-13T10:00:00',
            latenciaMitja: 25,
        },
    ],
    agrupacio: 'HORA',
    grupsDates: ['13/03 10:00', '13/03 11:00'],
    salutCurrentApp: {
        data: '2026-03-13T10:00:00',
        peticioError: false,
        bdEstat: 'UP',
        appLatencia: 15,
        subsistemaDownCount: 2,
        integracioDownCount: 1,
        detalls: [
            {
                id: 'detall-1',
                nom: 'Build',
                valor: '2026.03',
            },
        ],
        contexts: [
            {
                nom: 'API',
                path: 'http://app/api',
                api: 'http://app/swagger',
                manuals: [
                    {
                        nom: 'Manual',
                        path: 'http://docs/manual',
                    },
                ],
            },
        ],
        missatges: [
            {
                data: '2026-03-13T10:00:00',
                nivell: 'INFO',
                missatge: 'Tot correcte',
            },
        ],
        historics: [
            {
                id: 1,
                data: '2026-03-13T09:00:00',
                appEstat: 'DOWN',
                peticioError: true,
            },
        ],
        subsistemes: [
            {
                codi: 'SUB-1',
                nom: 'Subsistema principal',
                estat: 'UP',
                totalOk: 12,
                totalError: 1,
                totalTempsMig: 20,
                peticionsOkUltimPeriode: 4,
                peticionsErrorUltimPeriode: 0,
                tempsMigUltimPeriode: 18,
            },
        ],
        integracions: [
            {
                id: 10,
                codi: 'PARENT',
                nom: 'Integració pare',
                estat: 'UP',
                totalOk: 9,
                totalError: 1,
                totalTempsMig: 33,
                peticionsOkUltimPeriode: 2,
                peticionsErrorUltimPeriode: 0,
                tempsMigUltimPeriode: 30,
            },
            {
                id: 11,
                codi: 'CHILD',
                nom: 'Integració filla',
                estat: 'UP',
                totalOk: 3,
                totalError: 0,
                totalTempsMig: 12,
                peticionsOkUltimPeriode: 1,
                peticionsErrorUltimPeriode: 0,
                tempsMigUltimPeriode: 10,
                pare: {
                    id: 10,
                },
            },
        ],
    },
    ...overrides,
});

describe('SalutAppInfo', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('SalutAppInfo_quanEncaraCarrega_mostraLIndicadorDeCarrega', () => {
        // Comprova que el tab principal mostra un spinner mentre les dades encara no estan disponibles.
        render(
            <SalutAppInfo
                ready
                appInfoData={createAppInfoData({
                    loading: true,
                    salutCurrentApp: null,
                    entornApp: null,
                }) as any}
            />
        );

        expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('SalutAppInfo_quanNoHiHaDadesDespresDeCarregar_mostraLAvís', () => {
        // Verifica que el component informa clarament quan la càrrega ha acabat però no hi ha dades de salut.
        render(
            <SalutAppInfo
                ready
                appInfoData={createAppInfoData({
                    loading: false,
                    salutCurrentApp: null,
                    entornApp: null,
                }) as any}
            />
        );

        expect(screen.getByText('No hi ha informació de salut')).toBeInTheDocument();
    });

    it('SalutAppInfo_quanNoHiHaDadesINoShaAcabatLaCarrega_mostraSpinnerAlTabActiu', () => {
        // Comprova que el wrapper dels tabs mostra un spinner mentre encara no es poden validar les dades carregades.
        render(
            <SalutAppInfo
                ready={false}
                appInfoData={createAppInfoData({
                    loading: undefined,
                    salutCurrentApp: null,
                    entornApp: null,
                }) as any}
            />
        );

        expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('SalutAppInfo_quanHiHaDadesDEntorn_mostraContextsIMissatges', () => {
        // Comprova que el tab d'entorn renderitza la informació principal, els contexts i els missatges rebuts.
        render(<SalutAppInfo ready appInfoData={createAppInfoData() as any} />);

        expect(screen.getByRole('heading', { name: 'Informació de salut' })).toBeInTheDocument();
        expect(screen.getByRole('heading', { name: 'Informació d entorn' })).toBeInTheDocument();
        expect(screen.getByText('Contexts')).toBeInTheDocument();
        expect(screen.getByText('Missatges')).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'http://app/api' })).toBeInTheDocument();
        expect(screen.getByText('Tot correcte')).toBeInTheDocument();
        expect(screen.getByText('Manual')).toBeInTheDocument();
    });

    it('SalutAppInfo_quanLaPeticioFalla_mostraLAlertaDeCaigudaAlsTabsFuncionals', () => {
        // Verifica que els tabs dependents de la resposta de salut mostren l'alerta d'error quan la petició falla.
        render(
            <SalutAppInfo
                ready
                appInfoData={createAppInfoData({
                    salutCurrentApp: {
                        ...createAppInfoData().salutCurrentApp,
                        peticioError: true,
                    },
                }) as any}
            />
        );

        expect(screen.getByText('La petició de salut ha fallat')).toBeInTheDocument();

        fireEvent.click(screen.getByRole('tab', { name: /Estat actual/i }));

        expect(screen.getByText('La petició de salut ha fallat')).toBeInTheDocument();
    });

    it('SalutAppInfo_quanSexpandeixUnaIntegracio_mostraElsFills', () => {
        // Comprova que el tab d'integracions permet expandir una fila pare i veure'n les integracions filles.
        render(<SalutAppInfo ready appInfoData={createAppInfoData() as any} />);

        fireEvent.click(screen.getByRole('tab', { name: /Integracions/i }));

        expect(screen.getByText('Integració pare')).toBeInTheDocument();
        expect(screen.queryByText('Integració filla')).not.toBeInTheDocument();

        fireEvent.click(screen.getByRole('button', { name: '' }));

        expect(screen.getByText('Integració filla')).toBeInTheDocument();
    });

    it('SalutAppInfo_quanNoHiHaIntegracionsNiSubsistemes_mostraElsMissatgesBuitsIValorsPerDefecte', () => {
        // Verifica que els tabs funcionals mostren els estats buits i els valors N/D quan falta informació opcional.
        render(
            <SalutAppInfo
                ready
                appInfoData={createAppInfoData({
                    salutCurrentApp: {
                        ...createAppInfoData().salutCurrentApp,
                        appLatencia: null,
                        contexts: [],
                        missatges: [],
                        subsistemes: [],
                        integracions: [],
                        detalls: [],
                    },
                }) as any}
            />
        );

        fireEvent.click(screen.getByRole('tab', { name: /Estat actual/i }));
        expect(screen.getByText('N/D')).toBeInTheDocument();
        expect(screen.getByText('Sense subsistemes')).toBeInTheDocument();

        fireEvent.click(screen.getByRole('tab', { name: /Integracions/i }));
        expect(screen.getByText('Sense integracions')).toBeInTheDocument();

        fireEvent.click(screen.getByRole('tab', { name: /Entorn/i }));
        expect(screen.getByText('Sense contexts')).toBeInTheDocument();
        expect(screen.getByText('Sense missatges')).toBeInTheDocument();
    });

    it('SalutAppInfo_quanHiHaComponentsSensePeticions_mostraNDEnllocDeZeroZero', () => {
        render(
            <SalutAppInfo
                ready
                appInfoData={createAppInfoData({
                    salutCurrentApp: {
                        ...createAppInfoData().salutCurrentApp,
                        subsistemes: [
                            {
                                codi: 'SUB-1',
                                nom: 'Subsistema principal',
                                estat: 'UP',
                                totalOk: null,
                                totalError: null,
                                totalTempsMig: null,
                                peticionsOkUltimPeriode: null,
                                peticionsErrorUltimPeriode: null,
                                tempsMigUltimPeriode: null,
                            },
                        ],
                        integracions: [
                            {
                                id: 10,
                                codi: 'PARENT',
                                nom: 'Integració pare',
                                estat: 'UP',
                                totalOk: null,
                                totalError: null,
                                totalTempsMig: null,
                                peticionsOkUltimPeriode: null,
                                peticionsErrorUltimPeriode: null,
                                tempsMigUltimPeriode: null,
                            },
                        ],
                    },
                }) as any}
            />
        );

        fireEvent.click(screen.getByRole('tab', { name: /Estat actual/i }));
        expect(screen.getAllByText('N/D').length).toBeGreaterThanOrEqual(3);

        fireEvent.click(screen.getByRole('tab', { name: /Integracions/i }));
        expect(screen.getAllByText('N/D').length).toBeGreaterThanOrEqual(3);
    });

    it('SalutAppInfo_quanEsConsultaLHistoric_mostraElsGraficsPreparats', () => {
        // Verifica que el tab històric renderitza les visualitzacions quan hi ha agrupació, estats i latències.
        render(
            <SalutAppInfo
                ready
                appInfoData={createAppInfoData() as any}
                grupsDates={['13/03 10:00', '13/03 11:00']}
            />
        );

        fireEvent.click(screen.getByRole('tab', { name: /^Històric$/i }));

        expect(screen.getByText('Estats')).toBeInTheDocument();
        expect(screen.getByText('Latència')).toBeInTheDocument();
        expect(screen.getByTestId('updown-bar-chart')).toBeInTheDocument();
        expect(screen.getByTestId('chart-container')).toBeInTheDocument();
    });

    it("SalutAppInfo_quanLusuariEsAdmin_iObreHistoricEstat_mostraLhistorial", () => {
        render(<SalutAppInfo ready appInfoData={createAppInfoData() as any} />);

        fireEvent.click(screen.getByRole('tab', { name: /Històric d'estat/i }));

        expect(screen.getByRole('tab', { name: /Històric d'estat/i })).toHaveAttribute('aria-selected', 'true');
        expect(screen.getByText('format:2026-03-13T09:00:00')).toBeInTheDocument();
        expect(screen.getByText('Amb error')).toBeInTheDocument();
    });

    it('SalutAppInfo_quanHiHaLogsDisponibles_permetObrirElTabDeLogs', () => {
        // Comprova que el tab de logs s'habilita quan l'entorn té URL de logs i renderitza el visor corresponent.
        render(<SalutAppInfo ready appInfoData={createAppInfoData() as any} />);

        fireEvent.click(screen.getByRole('tab', { name: /Logs/i }));

        expect(screen.getByText('Logs viewer 7')).toBeInTheDocument();
    });

    it('SalutAppInfo_quanNoHiHaLogsDeshabilitaElTabDeLogs', () => {
        // Verifica que el tab de logs queda deshabilitat si l'entorn no publica l'enllaç de logs.
        render(
            <SalutAppInfo
                ready
                appInfoData={createAppInfoData({
                    entornApp: {
                        id: 7,
                        versio: '1.0.0',
                        revisioSimplificat: 'abc123',
                        jdkVersion: '17',
                        logsUrl: null,
                    },
                }) as any}
            />
        );

        expect(screen.getByRole('tab', { name: /Logs/i })).toBeDisabled();
    });

    it('SalutAppInfo_quanSactivaElTabDeLogsSenseEntornMostraSpinner', () => {
        // Comprova que el tab de logs presenta l’indicador de càrrega mentre encara no existeix l’entorn.
        render(
            <SalutAppInfo
                ready
                appInfoData={createAppInfoData({
                    entornApp: null,
                }) as any}
            />
        );

        fireEvent.click(screen.getByRole('tab', { name: /Logs/i }));

        expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('SalutAppInfo_quanNoHiHaHistoric_mostraElsMissatgesDeSenseInformacio', () => {
        // Comprova que el tab històric mostra l'estat buit quan no hi ha estats ni latències disponibles.
        render(
            <SalutAppInfo
                ready
                appInfoData={createAppInfoData({
                    estats: {},
                    latencies: [],
                    agrupacio: 'HORA',
                }) as any}
                grupsDates={['13/03 10:00']}
            />
        );

        fireEvent.click(screen.getByRole('tab', { name: /^Històric$/i }));

        expect(screen.getAllByText('Sense històric').length).toBeGreaterThan(0);
    });

    it('AlertUltimaDataActiva_quanTeDadesCompletes_mostraLAlertAmbLaDataIEstat', () => {
        const appData = createAppInfoData({
            salutCurrentApp: {
            ...createAppInfoData().salutCurrentApp,
            ultimEstatInfo: { data: '2026-03-15T10:30:00', estat: 'UP' },
            },
        });

        render(<SalutAppInfo ready appInfoData={appData as any} />);

        expect(screen.getByRole('alert')).toBeInTheDocument();
        expect(screen.getByText('format:2026-03-15T10:30:00')).toBeInTheDocument();
    });

    it('Integracions_elFiltreMostraTotsElsValorsDeSalutEstatEnum', () => {
        render(<SalutAppInfo ready appInfoData={createAppInfoData() as any} />);
        fireEvent.click(screen.getByRole('tab', { name: /Integracions/i }));
        const filterLabel = screen.getByLabelText('Filtrar per estat');
        fireEvent.mouseDown(filterLabel);
        expectedEstats.forEach((estat) => {
            expect(screen.getByRole('option', { name: estat })).toBeInTheDocument();
        });
        fireEvent.keyDown(filterLabel, { key: 'Escape' });
    });

    it('Subsistemes_elFiltreMostraTotsElsValorsDeSalutEstatEnum', () => {
        render(<SalutAppInfo ready appInfoData={createAppInfoData() as any} />);
        fireEvent.click(screen.getByRole('tab', { name: /Estat actual/i }));
        const filterLabel = screen.getByLabelText('Filtrar per estat');
        fireEvent.mouseDown(filterLabel);
        expectedEstats.forEach((estat) => {
            expect(screen.getByRole('option', { name: estat })).toBeInTheDocument();
        });
        fireEvent.keyDown(filterLabel, { key: 'Escape' });
    });

    it('Integracions_seleccionarEstatsFiltraCorrectamentLesFiles', () => {
        const appData = createAppInfoData({
            salutCurrentApp: {
                ...createAppInfoData().salutCurrentApp,
                integracions: [
                    {
                        id: 1,
                        codi: 'INT-UP',
                        nom: 'Integració UP',
                        estat: 'UP',
                        totalOk: 10,
                        totalError: 0,
                        totalTempsMig: 20,
                        peticionsOkUltimPeriode: 5,
                        peticionsErrorUltimPeriode: 0,
                        tempsMigUltimPeriode: 15,
                    },
                    {
                        id: 2,
                        codi: 'INT-DOWN',
                        nom: 'Integració DOWN',
                        estat: 'DOWN',
                        totalOk: 0,
                        totalError: 5,
                        totalTempsMig: 0,
                        peticionsOkUltimPeriode: 0,
                        peticionsErrorUltimPeriode: 2,
                        tempsMigUltimPeriode: 0,
                    },
                ],
            },
        });
        render(<SalutAppInfo ready appInfoData={appData as any} />);
        fireEvent.click(screen.getByRole('tab', { name: /Integracions/i }));
        expect(screen.getByText('Integració UP')).toBeInTheDocument();
        expect(screen.getByText('Integració DOWN')).toBeInTheDocument();
        const filterLabel = screen.getByLabelText('Filtrar per estat');
        fireEvent.mouseDown(filterLabel);
        fireEvent.click(screen.getByRole('option', { name: 'UP' }));
        fireEvent.keyDown(filterLabel, { key: 'Escape' });
        expect(screen.getByText('Integració UP')).toBeInTheDocument();
        expect(screen.queryByText('Integració DOWN')).not.toBeInTheDocument();
    });

    it('Subsistemes_seleccionarEstatsFiltraCorrectamentLesFiles', () => {
        const appData = createAppInfoData({
            salutCurrentApp: {
                ...createAppInfoData().salutCurrentApp,
                subsistemes: [
                    {
                        codi: 'SUB-UP',
                        nom: 'Subsistema UP',
                        estat: 'UP',
                        totalOk: 10,
                        totalError: 0,
                        totalTempsMig: 20,
                        peticionsOkUltimPeriode: 5,
                        peticionsErrorUltimPeriode: 0,
                        tempsMigUltimPeriode: 15,
                    },
                    {
                        codi: 'SUB-WARN',
                        nom: 'Subsistema WARN',
                        estat: 'WARN',
                        totalOk: 8,
                        totalError: 2,
                        totalTempsMig: 25,
                        peticionsOkUltimPeriode: 4,
                        peticionsErrorUltimPeriode: 1,
                        tempsMigUltimPeriode: 22,
                    },
                ],
            },
        });
        render(<SalutAppInfo ready appInfoData={appData as any} />);
        fireEvent.click(screen.getByRole('tab', { name: /Estat actual/i }));
        expect(screen.getByText('Subsistema UP')).toBeInTheDocument();
        expect(screen.getByText('Subsistema WARN')).toBeInTheDocument();
        const filterLabel = screen.getByLabelText('Filtrar per estat');
        fireEvent.mouseDown(filterLabel);
        fireEvent.click(screen.getByRole('option', { name: 'WARN' }));
        fireEvent.keyDown(filterLabel, { key: 'Escape' });
        expect(screen.queryByText('Subsistema UP')).not.toBeInTheDocument();
        expect(screen.getByText('Subsistema WARN')).toBeInTheDocument();
    });

    it('Integracions_ALL_ESTATS_derivatCorrectamentDeSalutEstatEnum', () => {
        const { SalutEstatEnum } = require('../../types/salut.model.tsx');
        const expectedValues = Object.values(SalutEstatEnum) as string[];
        render(<SalutAppInfo ready appInfoData={createAppInfoData() as any} />);
        fireEvent.click(screen.getByRole('tab', { name: /Integracions/i }));
        const filterLabel = screen.getByLabelText('Filtrar per estat');
        fireEvent.mouseDown(filterLabel);
        const options = screen.getAllByRole('option');
        expect(options).toHaveLength(expectedValues.length);
        expectedValues.forEach((value: string) => {
            expect(screen.getByRole('option', { name: value })).toBeInTheDocument();
        });
        fireEvent.keyDown(filterLabel, { key: 'Escape' });
    });

});
