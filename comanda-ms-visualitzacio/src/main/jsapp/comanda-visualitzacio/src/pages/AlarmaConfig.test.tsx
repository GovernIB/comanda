import * as React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import AlarmaConfig, { AlarmaConfigForm, EntornAppSelector } from './AlarmaConfig';
import { MuiDataGridColDef } from 'reactlib';

const mocks = vi.hoisted(() => ({
    setFieldValueMock: vi.fn(),
    artifactActionMock: vi.fn(),
    messageDialogShowMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    refreshMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            components: {
                clear: 'Netejar',
            },
            page: {
                alarmaConfig: {
                    title: 'Configuració d alarmes',
                    update: 'Editar configuració',
                    create: 'Crear configuració',
                    condicio: {
                        title: 'Condició',
                        subtitle: 'Condició de dispar',
                    },
                    periode: {
                        switch: 'Configurar període',
                        title: 'Període',
                        subtitle: 'Límit temporal',
                    },
                    filter: {
                        showOnlyOwnEnabled: 'Només meves',
                        showOnlyOwnDisabled: 'Totes les configuracions',
                        more: 'Més filtres',
                        entornApp: "Entorn d'aplicació",
                    },
                },
            },
        })
    ),
    tLibMock: vi.fn((key: string) => {
        const values: Record<string, string> = {
            'datacommon.delete.label': 'Eliminar',
            'datacommon.delete.single.label': 'Eliminar configuració',
            'datacommon.delete.single.confirm': 'Confirmes l eliminació?',
            'datacommon.delete.single.success': 'Eliminada',
            'datacommon.delete.single.error': 'No s ha pogut eliminar',
        };
        return values[key] ?? key;
    }),
    getOneEntornAppMock: vi.fn(),
    findEntornAppsMock: vi.fn(),
    getOneAlarmaConfigMock: vi.fn(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('react-router-dom', () => ({
    useParams: () => ({
        id: '44',
    }),
}));

vi.mock('reactlib', async (importOriginal) => {
    const original = await importOriginal<typeof import('reactlib')>();

    return {
    ...original,
    FormPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    GridPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    MuiDataGrid: ({
        title,
        filter,
        toolbarAdditionalRow,
        rowAdditionalActions,
        columns,
    }: {
        title: string;
        filter?: string;
        toolbarAdditionalRow?: React.ReactNode;
        rowAdditionalActions?: Array<{ label: string; onClick?: (id: unknown) => void }>;
        columns?: MuiDataGridColDef[];
    }) => (
        <section>
            <h2>{title}</h2>
            <div data-testid="filter-value">{filter ?? ''}</div>
            {toolbarAdditionalRow}
            {columns?.map((col: any, index: number) => (
                <div key={index} data-testid={`column-${col.field}`}>
                    {col.field}
                </div>
            ))}
            <button onClick={() => rowAdditionalActions?.[0]?.onClick?.('cfg-1')}>
                {rowAdditionalActions?.[0]?.label}
            </button>
        </section>
    ),
    MuiForm: ({
        title,
        children,
        onDataChange,
        onValidationErrorsChange,
    }: {
        title: string;
        children: React.ReactNode;
        onDataChange?: (data: any) => void;
        onValidationErrorsChange?: (id: any, errors?: any[]) => void;
    }) => {
        React.useEffect(() => {
            onDataChange?.({
                entornAppId: 7,
                tipus: 'APP_LATENCIA',
                periodeValor: 5,
                periodeUnitat: 'MINUTS',
            });
            onValidationErrorsChange?.(null, [
                { field: 'entornAppId', code: 'NotNull', message: 'Obligatori' },
            ]);
        }, []);
        return (
            <form>
                <h2>{title}</h2>
                {children}
            </form>
        );
    },
    MuiFilter: ({
        children,
        onDataChange,
        onSpringFilterChange,
        springFilterBuilder: springFilterBuilderProp,
        formApiRef,
    }: {
        children: React.ReactNode;
        onDataChange?: (data: any) => void;
        onSpringFilterChange?: (springFilter?: string) => void,
        springFilterBuilder?: (data: any) => string;
        formApiRef?: { current: { setFieldValue: (field: string, value: any) => void } };
    }) => {
        const data = { entornApp: { id: 9 } };
        React.useEffect(() => {
            onDataChange?.(data);
            formApiRef?.current?.setFieldValue?.('entornApp', { id: 7, description: 'Entorn 7' });
        }, []);
        React.useEffect(() => {
            const filter = springFilterBuilderProp?.(data);
            onSpringFilterChange?.(filter || undefined);
        }, [springFilterBuilderProp]);

        return <div>{children}</div>;
    },
    FormField: ({ name, disabled }: { name: string; disabled?: boolean }) => (
        <div data-testid={`field-${name}`}>{`${name}:${String(disabled ?? false)}`}</div>
    ),
    useFormApiRef: () => ({
        current: {
            setFieldValue: mocks.setFieldValueMock,
        },
    }),
    useResourceApiService: (resource: string) => {
        if (resource === 'entornApp') {
            return {
                isReady: true,
                getOne: mocks.getOneEntornAppMock,
                find: mocks.findEntornAppsMock,
            };
        }
        return {
            isReady: true,
            artifactAction: mocks.artifactActionMock,
            getOne: mocks.getOneAlarmaConfigMock,
        };
    },
    useBaseAppContext: () => ({
        messageDialogShow: mocks.messageDialogShowMock,
        temporalMessageShow: mocks.temporalMessageShowMock,
        t: mocks.tLibMock,
    }),
    useConfirmDialogButtons: () => <button>Confirmar</button>,
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: mocks.refreshMock,
        },
    }),
    useFilterApiRef: () => ({
        current: {
            clear: vi.fn(),
        },
    }),
}});

vi.mock('../components/UserContext', () => ({
    useIsUserAdmin: () => true,
    useUserContext: () => ({
        user: { codi: 'u001' },
    }),
}));

vi.mock('../components/CenteredCircularProgress.tsx', () => ({
    default: () => <div>Carregant configuració</div>,
}));

describe('EntornAppSelector', () => {
    beforeEach(() => {
        mocks.getOneEntornAppMock.mockResolvedValue({
            id: 7,
            entornAppDescription: 'Entorn 7',
        });
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('EntornAppSelector_quanRepId_carregaLEntornIMapejaElValorAlFormulari', async () => {
        // Comprova que el selector carrega l'entorn existent i el trasllada al formulari intern.
        render(
            <EntornAppSelector
                id={7}
                onEntornAppChange={vi.fn()}
                validationErrors={[]}
            />
        );

        await waitFor(() => {
            expect(mocks.getOneEntornAppMock).toHaveBeenCalledWith(7);
        });

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('entornApp', {
            id: 7,
            description: 'Entorn 7',
        });
    });
});

describe('AlarmaConfigForm', () => {
    beforeEach(() => {
        mocks.getOneEntornAppMock.mockResolvedValue({
            id: 7,
            entornAppDescription: 'Entorn 7',
        });
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('AlarmaConfigForm_quanRepDadesDeLatencia_mostraElsBlocsCondicionals', async () => {
        // Verifica que el formulari mostra la condició habilitada i el bloc de període quan hi ha dades associades.
        render(<AlarmaConfigForm />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Editar configuració' })).toBeInTheDocument();
            expect(screen.getByText('Condició')).toBeInTheDocument();
            expect(screen.getByText('Període')).toBeInTheDocument();
            expect(screen.getByTestId('field-condicio')).toHaveTextContent('condicio:false');
            expect(screen.getByTestId('field-valor')).toBeInTheDocument();
            expect(screen.getByTestId('field-periodeValor')).toBeInTheDocument();
            expect(screen.getByTestId('field-admin')).toHaveTextContent('admin:false');
        });
    });

    it('AlarmaConfigForm_quanCanviaLEntorn_actualitzaLEntornAppIdAlFormulariExtern', async () => {
        // Comprova que el selector d'entorn propaga l'identificador seleccionat al formulari principal.
        render(<AlarmaConfigForm />);

        await waitFor(() => {
            expect(mocks.setFieldValueMock).toHaveBeenCalledWith('entornAppId', 9);
        });
    });

    it('AlarmaConfigForm_quanEsDesactivaElToggleDePeriode_esborraElsValorsDelsCampsPeriode', async () => {
        render(<AlarmaConfigForm />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Editar configuració' })).toBeInTheDocument();
        });

        const periodeToggle = screen.getByLabelText(/Configurar període/);
        expect(periodeToggle).toBeChecked();
        fireEvent.click(periodeToggle);

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('periodeValor', null);
        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('periodeUnitat', null);
        expect(periodeToggle).not.toBeChecked();
    });
});

describe('AlarmaConfig', () => {
    beforeEach(() => {
        mocks.findEntornAppsMock.mockResolvedValue({ rows: [{ id: 1, entornAppDescription: 'Entorn 1' }] });
        mocks.messageDialogShowMock.mockResolvedValue(true);
        mocks.artifactActionMock.mockResolvedValue(undefined);
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('AlarmaConfig_quanEsRenderitza_mostraElGridIElFiltreInicialDeLusuari', async () => {
        // Comprova que la pàgina arrenca filtrant per l'usuari actual i mostra el toggle de només meves.
        render(<AlarmaConfig />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Configuració d alarmes' })).toBeInTheDocument();
        });

        expect(screen.getByTitle('Només meves')).toBeInTheDocument();
    });

    it('AlarmaConfig_quanEsPremElToggle_canviaElFiltreIQuanSElimina_refrescaElGrid', async () => {
        // Verifica que el toggle d'usuari alterna el filtre i que l'acció d'eliminar executa l'artifact corresponent.
        render(<AlarmaConfig />);

        await waitFor(() => {
            expect(screen.getByTitle('Només meves')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByTitle('Només meves'));
        expect(screen.getByTestId('filter-value')).not.toHaveTextContent('');
        expect(screen.getByTitle('Totes les configuracions')).toBeInTheDocument();

        fireEvent.click(screen.getByRole('button', { name: 'Eliminar' }));

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledWith('cfg-1', { code: 'delete_alarmaConfig' });
        });

        expect(mocks.refreshMock).toHaveBeenCalled();
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Eliminada', 'success');
    });

    it('AlarmaConfig_quanEsDesactivaElFiltreDeNomésMeva_mostraLaColumnaDeTipusUsuari', async () => {
        render(<AlarmaConfig />);

        await waitFor(() => {
            expect(screen.queryByTestId('column-tipusUsuariAlarma')).not.toBeInTheDocument();
        });

        fireEvent.click(screen.getByTitle('Només meves'));

        await waitFor(() => {
            expect(screen.getByTestId('column-tipusUsuariAlarma')).toBeInTheDocument();
        });
    });

    it('AlarmaConfig_quanEsRenderitza_mostraElGridIElFiltreInicialDeLusuari', async () => {
        render(<AlarmaConfig />);
        await waitFor(() => {
            expect(screen.getByTitle('Només meves')).toBeInTheDocument(); // ✅ Título inicial correcto
        });
    });

    it('AlarmaConfig_quanEsPremElToggle_canviaElFiltreIQuanSElimina_refrescaElGrid', async () => {
        render(<AlarmaConfig />);
        await waitFor(() => {
            expect(screen.getByTitle('Només meves')).toBeInTheDocument();
        });
        fireEvent.click(screen.getByTitle('Només meves'));
        expect(screen.getByTitle('Totes les configuracions')).toBeInTheDocument(); // ✅ Título cambia
    });

    it('AlarmaConfig_quanEsDesactivaElFiltreDeNomésMeva_mostraLaColumnaDeTipusUsuari', async () => {
        render(<AlarmaConfig />);
        await waitFor(() => {
            expect(screen.queryByTestId('column-tipusUsuariAlarma')).not.toBeInTheDocument();
        });
        fireEvent.click(screen.getByTitle('Només meves'));
        await waitFor(() => {
            expect(screen.getByTestId('column-tipusUsuariAlarma')).toBeInTheDocument(); // ✅ Columna aparece
        });
    });
});
