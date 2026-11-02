import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Avis, { avisFilterBuilder } from './Avis';

let avisFormData = {
    avisPropi: true,
};

const mocks = vi.hoisted(() => ({
    filterOnDataChange: {} as Record<string, ((data: any) => void) | undefined>,
    useUserContextMock: vi.fn(() => ({
        user: { codi: 'u001' },
        currentRole: 'ADMIN',
    })),
    setFieldValueMock: vi.fn((field, value) => {
        avisFormData = { ...avisFormData, [field]: value };
    }),
    clearAppEntornMock: vi.fn(),
    clearMoreMock: vi.fn(),
    filterApiRefCallMock: vi.fn(),
    getColorByAvisTipusMock: vi.fn((tipus: string) => `color-${tipus}`),
    tMock: vi.fn((selector: any) =>
        selector({
            menu: { avis: 'Avisos' },
            page: {
                message: { grid: { timestamp: 'Data' } },
                avisos: {
                    grid: {
                        groupHeader: 'Grup',
                        column: {
                            appEntorn: 'App/Entorn',
                            global: 'Global',
                            globalTooltip: 'Afecta tots els entorns',
                            tipus: {
                                tooltip: {
                                    NOTICIA: 'Notícia',
                                    INFO: 'Informació',
                                    ALERTA: 'Alerta',
                                    ERROR: 'Error',
                                    CRITIC: 'Crític',
                                },
                            },
                        },
                    },
                    action: {
                        obrir: 'Obrir',
                        llegit: { label: 'Llegit' },
                        nollegit: { label: 'No llegit' },
                    },
                    filter: {
                        ownAvisOnlyEnabled: 'Només meus',
                        ownAvisOnlyDisabled: 'Tots els avisos',
                        more: 'Més filtres',
                    },
                },
            },
            components: { clear: 'Netejar' },
        })
    ),
    tLibMock: vi.fn((key: string) => {
        const values: Record<string, string> = { 'datacommon.delete.label': 'Eliminar' };
        return values[key] ?? key;
    }),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({ t: (selector: any) => typeof selector === 'function' ? mocks.tMock(selector) : selector }),
    Trans: ({ i18nKey }: { i18nKey: any }) => <>{typeof i18nKey === 'function' ? mocks.tMock(i18nKey) : i18nKey}</>,
}));

vi.mock('reactlib', () => ({
    MuiDataGrid: ({ title, columns, toolbarAdditionalRow, toolbarElementsWithPositions, rowAdditionalActions, filter }: any) => (
        <section>
            <h2>{title}</h2>
            <div data-testid="filter-value">{filter ?? ''}</div>
            <div data-testid="columns">{columns.map((column: any) => column.field).join(',')}</div>
            <div data-testid="tipus-cell">
                {columns.find((column: any) => column.field === 'tipus')?.renderCell?.({ row: { tipus: 'INFO', global: true } })}
            </div>
            {toolbarElementsWithPositions?.map((entry: any, index: number) => <div key={index}>{entry.element}</div>)}
            <div>{toolbarAdditionalRow}</div>
            <div data-testid="row-action-count">{rowAdditionalActions?.length ?? 0}</div>
            <div data-testid="row-action-hidden">{String(rowAdditionalActions?.[0]?.hidden?.({ url: null }))}</div>
        </section>
    ),
    MuiFilter: ({ children, resourceName, onDataChange }: any) => {
        mocks.filterOnDataChange[resourceName] = onDataChange;
        return <div>{children}</div>;
    },
    FormField: ({ name }: { name: string }) => <div data-testid={`field-${name}`}>{name}</div>,
    springFilterBuilder: {
        and: (...parts: Array<string | undefined | false | null>) => parts.filter(Boolean).join(' && '),
        eq: (field: string, value: unknown) => `${field}=${String(value)}`,
        like: (field: string, value: unknown) => `${field}~${String(value)}`,
        gte: (field: string, value: unknown) => `${field}>=${String(value)}`,
        lte: (field: string, value: unknown) => `${field}<=${String(value)}`,
    },
    useFormApiRef: () => ({ current: { setFieldValue: mocks.setFieldValueMock } }),
    useFormContext: () => ({ apiRef: { current: { setFieldValue: mocks.setFieldValueMock } }, data: avisFormData }),
    useFilterApiRef: () => {
        const call = mocks.filterApiRefCallMock.mock.calls.length;
        mocks.filterApiRefCallMock();
        return {
            current: {
                clear: call === 0 ? mocks.clearAppEntornMock : mocks.clearMoreMock,
                setFieldValue: mocks.setFieldValueMock,
            },
        };
    },
    useResourceApiContext: () => ({ indexState: { links: { has: (rel: string) => rel === 'avis' } } }),
    useResourceApiService: () => ({ isReady: true, find: vi.fn().mockResolvedValue({ rows: [] }) }),
    useBaseAppContext: () => ({ t: mocks.tLibMock }),
    useMuiDataGridApiRef: () => ({ current: {} }),
}));

vi.mock('../hooks/treeData', () => ({
    useTreeData: () => ({ treeView: false, treeViewSwitch: <button>Canvia arbre</button>, dataGridProps: { treeData: false } }),
}));

vi.mock('../util/dateUtils.ts', () => ({
    formatStartOfDay: (value: string) => `START(${value})`,
    formatEndOfDay: (value: string) => `END(${value})`,
}));

vi.mock('@mui/x-data-grid-pro', () => ({ useGridApiRef: () => ({ current: {} }) }));
vi.mock('../components/UserContext', () => ({ useUserContext: () => mocks.useUserContextMock() }));
vi.mock('../components/PageTitle.tsx', () => ({ default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div> }));
vi.mock('../components/salut/SalutChip.tsx', () => ({ default: ({ label, tooltip }: { label: string; tooltip?: string }) => <div><span>{label}</span><span>{tooltip}</span></div> }));
vi.mock('../types/salut.model.tsx', () => ({
    AvisTipusEnum: { NOTICIA: 'NOTICIA', INFO: 'INFO', ALERTA: 'ALERTA', ERROR: 'ERROR', CRITIC: 'CRITIC' },
    useGetColorByAvisTipus: () => mocks.getColorByAvisTipusMock,
}));
vi.mock('../components/UserProvider.tsx', () => ({ ROLE_ADMIN: 'ADMIN' }));
vi.mock('../../lib/components/mui/datacommon/MuiDataCommon.tsx', () => ({}));

describe('Avis', () => {
    afterEach(() => {
        vi.clearAllMocks();
        mocks.useUserContextMock.mockReturnValue({
            user: { codi: 'u001' },
            currentRole: 'ADMIN',
        });
        avisFormData = { avisPropi: true };
    });

    it('Avis_quanEsRenderitza_mostraLaGraellaElsXipsIElFiltreInicial', () => {
        render(<Avis />);
        expect(screen.getByTestId('page-title')).toHaveTextContent('Avisos');
        expect(screen.getByRole('heading', { name: 'Avisos' })).toBeInTheDocument();
        expect(screen.getByTestId('columns')).toHaveTextContent('logo,app,entorn,nom,descripcio,tipus,responsable,dataInici');
        expect(screen.getByTestId('tipus-cell')).toHaveTextContent('Info');
        expect(screen.getByTestId('tipus-cell')).toHaveTextContent('Global');
        expect(screen.getByText('Canvia arbre')).toBeInTheDocument();
    });

    it('Avis_quanEsPremenElsTogglesIDeNeteja_actualitzaLEstatDelFiltreVisual', () => {
        render(<Avis />);

        const btnMeus = screen.getByTitle('Només meus');
        const btnNetejar = screen.getByTitle('Netejar');

        expect(btnMeus).toBeInTheDocument();
        expect(btnNetejar).toBeInTheDocument();
        expect(() => fireEvent.click(btnMeus)).not.toThrow();
        expect(() => fireEvent.click(btnNetejar)).not.toThrow();
    });

    it('Avis_quanLusuariEsAdmin_afegeixLaccioDEliminar', () => {
        render(<Avis />);
        expect(screen.getByTestId('row-action-count')).toHaveTextContent('4');
        expect(screen.getByTestId('row-action-hidden')).toHaveTextContent('true');
    });

    it('Avis_quanEsPremNetejar_sinvocaElsClearsDelFiltre', () => {
        render(<Avis />);
        fireEvent.click(screen.getByTitle('Netejar'));
        expect(mocks.clearAppEntornMock).toHaveBeenCalled();
        expect(mocks.clearMoreMock).toHaveBeenCalled();
    });

    it('Avis_quanLaFilaEsGlobal_mostraElChipGlobal', () => {
        render(<Avis />);
        expect(screen.getByTestId('tipus-cell')).toHaveTextContent('Global');
    });

    it('Avis_quanLusuariEsAdmin_mostraCampFiltreResponsable', () => {
        render(<Avis />);
        expect(screen.getByTestId('field-responsable')).toBeInTheDocument();
    });

    it('Avis_quanLusuariNoEsAdmin_tambeMostraCampFiltreResponsable', () => {
        mocks.useUserContextMock.mockReturnValue({
            user: { codi: 'u002' },
            currentRole: 'USER',
        });
        render(<Avis />);
        expect(screen.getByTestId('field-responsable')).toBeInTheDocument();
    });

    it('avisFilterBuilder_quanHiHaResponsable_aplicaFiltreResponsable', () => {
        const filter = avisFilterBuilder({ responsable: 'usuari_test', avisPropi: true }, 'admin01');
        expect(filter).toContain('responsable~usuari_test');
        expect(filter).not.toContain("responsable='admin01'");
    });

    it('avisFilterBuilder_quanResponsableTeEspais_faTrim', () => {
        const filter = avisFilterBuilder({ responsable: '  usuari_test  ' }, 'admin01');
        expect(filter).toContain('responsable~usuari_test');
    });

    it('avisFilterBuilder_quanNoHiHaResponsable_aplicaFallbackUsuariActual', () => {
        const filter = avisFilterBuilder({ responsable: '', avisPropi: true }, 'user01');
        expect(filter).toContain("responsable='user01'");
    });

    it('avisFilterBuilder_quanNoHiHaResponsableNiUsuariActual_noAfegeixFiltre', () => {
        const filter = avisFilterBuilder({ responsable: '', avisPropi: false }, null);
        expect(filter).not.toContain('responsable');
    });

    it('avisFilterBuilder_quanAvisPropiEsFalseISenseResponsable_noAfegeixFiltreResponsableEncaraQueHiHagiUsuariActual', () => {
        const filter = avisFilterBuilder({ responsable: '', avisPropi: false }, 'user01');
        expect(filter).not.toContain('responsable');
    });

    it('avisFilterBuilder_quanHiHaResponsable_aplicaFiltreResponsableIndependentmentdAvisPropi', () => {
        const filterAmbPropi = avisFilterBuilder({ responsable: 'altre_usuari', avisPropi: true }, 'user01');
        expect(filterAmbPropi).toContain('responsable~altre_usuari');
        expect(filterAmbPropi).not.toContain("responsable='user01'");

        const filterSensePropi = avisFilterBuilder({ responsable: 'altre_usuari', avisPropi: false }, 'user01');
        expect(filterSensePropi).toContain('responsable~altre_usuari');
    });

    it('Avis_quanEsFiltraPerResponsable_esDesactivaElBotoNomesMeusISincronitzaElFiltre', () => {
        const { rerender } = render(<Avis />);

        expect(screen.getByTitle('Només meus')).toBeInTheDocument();

        // L'usuari introdueix un responsable
        mocks.filterOnDataChange['avis']?.({ responsable: 'usuari_altre' });
        rerender(<Avis />);

        // El botó s'ha de desactivar (mostrar el títol 'Tots els avisos')
        expect(screen.getByTitle('Tots els avisos')).toBeInTheDocument();
        expect(screen.getByTestId('filter-value')).toHaveTextContent('responsable~usuari_altre');
        expect(screen.getByTestId('filter-value')).not.toHaveTextContent("responsable='u001'");

        // Ara l'usuari reactiva el botó "Només meus"
        fireEvent.click(screen.getByTitle('Tots els avisos'));
        rerender(<Avis />);

        // El botó torna a estar actiu i s'ha buidat el camp responsable
        expect(screen.getByTitle('Només meus')).toBeInTheDocument();
        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('responsable', '');
        expect(screen.getByTestId('filter-value')).toHaveTextContent("responsable='u001'");
        expect(screen.getByTestId('filter-value')).not.toHaveTextContent('responsable~usuari_altre');

        // L'usuari desactiva el botó manualment
        fireEvent.click(screen.getByTitle('Només meus'));
        rerender(<Avis />);

        // El filtre no ha d'incloure cap responsable
        expect(screen.getByTitle('Tots els avisos')).toBeInTheDocument();
        expect(screen.getByTestId('filter-value')).not.toHaveTextContent('responsable');
    });

    it('Avis_quanLusuariNoEsAdmin_potFiltrarPerResponsableISincronitzaElBoto', () => {
        mocks.useUserContextMock.mockReturnValue({
            user: { codi: 'u002' },
            currentRole: 'USER',
        });
        const { rerender } = render(<Avis />);

        expect(screen.getByTitle('Només meus')).toBeInTheDocument();

        // L'usuari normal introdueix un responsable
        mocks.filterOnDataChange['avis']?.({ responsable: 'usuari_altre' });
        rerender(<Avis />);

        // El botó s'ha de desactivar (mostrar el títol 'Tots els avisos')
        expect(screen.getByTitle('Tots els avisos')).toBeInTheDocument();
        expect(screen.getByTestId('filter-value')).toHaveTextContent('responsable~usuari_altre');
        expect(screen.getByTestId('filter-value')).not.toHaveTextContent("responsable='u002'");

        // Ara l'usuari reactiva el botó "Només meus"
        fireEvent.click(screen.getByTitle('Tots els avisos'));
        rerender(<Avis />);

        // El botó torna a estar actiu i s'ha buidat el camp responsable
        expect(screen.getByTitle('Només meus')).toBeInTheDocument();
        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('responsable', '');
        expect(screen.getByTestId('filter-value')).toHaveTextContent("responsable='u002'");
        expect(screen.getByTestId('filter-value')).not.toHaveTextContent('responsable~usuari_altre');
    });

    it('Avis_quanEsCanviaAppOEntorn_esConservaAvisPropi', () => {
        const { rerender } = render(<Avis />);

        // Simula la inicialització dels filtres
        mocks.filterOnDataChange['entornApp']?.({ avisPropi: true });
        mocks.filterOnDataChange['avis']?.({ noLlegit: true });
        rerender(<Avis />);

        expect(screen.getByTitle('Només meus')).toBeInTheDocument();
        expect(screen.getByTestId('filter-value')).toHaveTextContent("responsable='u001'");

        // El formulari entornApp emet un canvi sense el camp no registrat avisPropi
        mocks.filterOnDataChange['entornApp']?.({ app: { id: 123 } });
        rerender(<Avis />);

        expect(screen.getByTitle('Només meus')).toBeInTheDocument();
        expect(screen.getByTestId('filter-value')).toHaveTextContent("responsable='u001'");
        expect(screen.getByTestId('filter-value')).toHaveTextContent('appId=123');
    });

    it('Avis_quanEsPremNetejarDespresDeFiltrarResponsable_restableixBotoIFiltreUsuari', () => {
        const { rerender } = render(<Avis />);

        mocks.filterOnDataChange['avis']?.({ responsable: 'usuari_altre' });
        rerender(<Avis />);
        expect(screen.getByTitle('Tots els avisos')).toBeInTheDocument();
        expect(screen.getByTestId('filter-value')).toHaveTextContent('responsable~usuari_altre');

        fireEvent.click(screen.getByTitle('Netejar'));
        rerender(<Avis />);

        expect(screen.getByTitle('Només meus')).toBeInTheDocument();
        expect(screen.getByTestId('filter-value')).toHaveTextContent("responsable='u001'");
        expect(screen.getByTestId('filter-value')).not.toHaveTextContent('responsable~usuari_altre');
    });
});

